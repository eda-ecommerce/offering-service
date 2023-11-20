package org.eda.ecommerce.integration

import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.kafka.InjectKafkaCompanion
import io.quarkus.test.kafka.KafkaCompanionResource
import io.restassured.RestAssured.given
import io.smallrye.reactive.messaging.kafka.companion.ConsumerTask
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion
import io.vertx.core.json.JsonObject
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.eda.ecommerce.JsonSerdeFactory
import org.eda.ecommerce.data.models.Product
import org.eda.ecommerce.data.models.events.OfferingEvent
import org.eda.ecommerce.data.repositories.OfferingRepository
import org.eda.ecommerce.data.repositories.ProductRepository
import org.junit.jupiter.api.*


@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OfferingTest {

    @InjectKafkaCompanion
    lateinit var companion: KafkaCompanion

    @Inject
    lateinit var offeringRepository: OfferingRepository

    @Inject
    lateinit var productRepository: ProductRepository

    @BeforeAll
    @Transactional
    fun setup() {
        val offeringEventJsonSerdeFactory = JsonSerdeFactory<OfferingEvent>()
        companion.registerSerde(
            OfferingEvent::class.java,
            offeringEventJsonSerdeFactory.createSerializer(),
            offeringEventJsonSerdeFactory.createDeserializer(OfferingEvent::class.java)
        )

        val product = Product().apply { id = 1 }
        productRepository.persist(product)
    }

    @BeforeEach
    @Transactional
    fun recreateTestedTopics() {
        companion.topics().delete("offering")
        companion.topics().create("offering", 1)
        offeringRepository.deleteAll()
    }

    @Test
    fun testCreationAndPersistenceOnPost() {
        val jsonBody: JsonObject = JsonObject()
            .put("quantity", 1)
            .put("price", 1.99F)
            .put("productId", 1L)

        given()
            .contentType("application/json")
            .body(jsonBody.toString())
            .`when`().post("/offering")
            .then()
            .statusCode(201)

        Assertions.assertEquals(1, offeringRepository.count())
        Assertions.assertEquals(jsonBody.getValue("quantity"), offeringRepository.findById(1L).quantity)
        Assertions.assertEquals(jsonBody.getValue("price"), offeringRepository.findById(1L).price)
        Assertions.assertEquals(jsonBody.getValue("productId"), offeringRepository.findById(1L).product?.id)
    }

    @Test
    fun testCreationFailedForNonexistingProductOnPost() {
        val jsonBody: JsonObject = JsonObject()
            .put("quantity", 1)
            .put("price", 1.99F)
            .put("productId", 2L)

        given()
            .contentType("application/json")
            .body(jsonBody.toString())
            .`when`().post("/offering")
            .then()
            .statusCode(404)

        Assertions.assertEquals(0, offeringRepository.count())
    }

    @Test
    fun testKafkaEmitOnPost() {
        val jsonBody: JsonObject = JsonObject()
            .put("quantity", 1)
            .put("price", 1.99F)
            .put("productId", 1L)

        given()
            .contentType("application/json")
            .body(jsonBody.toString())
            .`when`().post("/offering")
            .then()
            .statusCode(201)

        val offeringConsumer: ConsumerTask<String, OfferingEvent> =
            companion.consume(OfferingEvent::class.java).fromTopics("offering", 1)

        offeringConsumer.awaitCompletion()

        val offeringResponse = offeringConsumer.firstRecord.value()

        Assertions.assertEquals(jsonBody.getValue("quantity"), offeringResponse.payload.quantity)
        Assertions.assertEquals(jsonBody.getValue("price"), offeringResponse.payload.price)
        Assertions.assertEquals(jsonBody.getValue("productId"), offeringResponse.payload.product?.id)
    }

    @Test
    fun testDelete() {
        val jsonBody: JsonObject = JsonObject()
            .put("quantity", 1)
            .put("price", 1.99F)
            .put("productId", 1L)

        given()
            .contentType("application/json")
            .body(jsonBody.toString())
            .`when`().post("/offering")
            .then()
            .statusCode(201)

        Assertions.assertEquals(1, offeringRepository.count())

        val createdId = offeringRepository.listAll()[0].id

        given()
            .contentType("application/json")
            .`when`()
            .queryParam("id", createdId)
            .delete("/offering")
            .then()
            .statusCode(202)

        val productConsumer: ConsumerTask<String, OfferingEvent> =
            companion.consume(OfferingEvent::class.java).fromTopics("offering", 2)

        productConsumer.awaitCompletion()

        val event = productConsumer.records[1].value()
        Assertions.assertEquals("offering-service", event.source)
        Assertions.assertEquals("deleted", event.type)
        Assertions.assertEquals(createdId, event.payload.id)
        Assertions.assertEquals(null, event.payload.quantity)
        Assertions.assertEquals(null, event.payload.price)
        Assertions.assertEquals(null, event.payload.product)

        Assertions.assertEquals(0, offeringRepository.count())
    }

    @Test
    fun testUpdate() {
        val jsonBody: JsonObject = JsonObject()
            .put("quantity", 1)
            .put("price", 1.99F)
            .put("productId", 1L)

        given()
            .contentType("application/json")
            .body(jsonBody.toString())
            .`when`().post("/offering")
            .then()
            .statusCode(201)

        Assertions.assertEquals(1, offeringRepository.count())

        val createdId = offeringRepository.listAll()[0].id

        val jsonBodyUpdated: JsonObject = JsonObject()
            .put("id", createdId)
            .put("quantity", 2)
            .put("price", 2.99F)
            .put("productId", 1L)

        given()
            .contentType("application/json")
            .body(jsonBodyUpdated.toString())
            .`when`()
            .put("/offering")
            .then()
            .statusCode(202)

        val productConsumer: ConsumerTask<String, OfferingEvent> =
            companion.consume(OfferingEvent::class.java).fromTopics("offering", 2)

        productConsumer.awaitCompletion()

        val event = productConsumer.records[1].value()
        Assertions.assertEquals("offering-service", event.source)
        Assertions.assertEquals("updated", event.type)
        Assertions.assertEquals(createdId, event.payload.id)
        Assertions.assertEquals(jsonBodyUpdated.getValue("quantity"), event.payload.quantity)
        Assertions.assertEquals(jsonBodyUpdated.getValue("price"), event.payload.price)
        Assertions.assertEquals(jsonBodyUpdated.getValue("product"), event.payload.product)

        Assertions.assertEquals(1, offeringRepository.count())
    }

}
