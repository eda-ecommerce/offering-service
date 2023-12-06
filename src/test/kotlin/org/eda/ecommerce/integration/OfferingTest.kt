package org.eda.ecommerce.integration

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.smallrye.common.annotation.Identifier
import io.vertx.core.json.JsonObject
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.eda.ecommerce.JsonSerdeFactory
import org.eda.ecommerce.data.models.Product
import org.eda.ecommerce.data.models.events.OfferingEvent
import org.eda.ecommerce.data.repositories.OfferingRepository
import org.eda.ecommerce.data.repositories.ProductRepository
import org.junit.jupiter.api.*
import java.time.Duration
import java.util.*


@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OfferingTest {

    @Inject
    @Identifier("default-kafka-broker")
    lateinit var kafkaConfig: Map<String, Any>

    lateinit var offeringConsumer: KafkaConsumer<String, OfferingEvent>

    @Inject
    lateinit var offeringRepository: OfferingRepository

    @Inject
    lateinit var productRepository: ProductRepository

    @BeforeAll
    @Transactional
    fun setup() {
        val product = Product().apply { id = 1 }
        productRepository.persist(product)
    }

    @BeforeEach
    @Transactional
    fun deleteRepositoryData() {
        offeringRepository.deleteAll()
    }

    @BeforeEach
    fun setupKafkaHelpers() {
        val offeringEventJsonSerdeFactory = JsonSerdeFactory<OfferingEvent>()
        offeringConsumer = KafkaConsumer(
            consumerConfig(),
            StringDeserializer(),
            offeringEventJsonSerdeFactory.createDeserializer(OfferingEvent::class.java)
        )
    }

    fun consumerConfig(): Properties {
        val properties = Properties()
        properties.putAll(kafkaConfig)
        properties[ConsumerConfig.GROUP_ID_CONFIG] = "test-group-id"
        properties[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = "true"
        properties[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        return properties
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
        offeringConsumer.subscribe(listOf("offering"))

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

        val records: ConsumerRecords<String, OfferingEvent> = offeringConsumer.poll(Duration.ofMillis(10000))

        val offeringResponse = records.records("offering").iterator().asSequence().toList().map { it.value() }.first()

        Assertions.assertEquals(jsonBody.getValue("quantity"), offeringResponse.payload.quantity)
        Assertions.assertEquals(jsonBody.getValue("price"), offeringResponse.payload.price)
        Assertions.assertEquals(jsonBody.getValue("productId"), offeringResponse.payload.product?.id)
    }

    @Test
    fun testDelete() {
        offeringConsumer.subscribe(listOf("offering"))

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

        val records: ConsumerRecords<String, OfferingEvent> = offeringConsumer.poll(Duration.ofMillis(10000))

        val event = records.records("offering").iterator().iterator().asSequence().toList().map { it.value() }.first()

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
        offeringConsumer.subscribe(listOf("offering"))

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

        val records: ConsumerRecords<String, OfferingEvent> = offeringConsumer.poll(Duration.ofMillis(10000))

        val event = records.records("offering").iterator().asSequence().toList().map { it.value() }.first()

        Assertions.assertEquals("offering-service", event.source)
        Assertions.assertEquals("updated", event.type)
        Assertions.assertEquals(createdId, event.payload.id)
        Assertions.assertEquals(jsonBodyUpdated.getValue("quantity"), event.payload.quantity)
        Assertions.assertEquals(jsonBodyUpdated.getValue("price"), event.payload.price)
        Assertions.assertEquals(jsonBodyUpdated.getValue("product"), event.payload.product)

        Assertions.assertEquals(1, offeringRepository.count())
    }

}
