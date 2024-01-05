package org.eda.ecommerce.integration

import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.kafka.InjectKafkaCompanion
import io.quarkus.test.kafka.KafkaCompanionResource
import io.restassured.RestAssured.given
import io.smallrye.common.annotation.Identifier
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion
import io.vertx.core.json.JsonObject
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.eda.ecommerce.JsonSerdeFactory
import org.eda.ecommerce.data.models.Offering
import org.eda.ecommerce.data.models.OfferingStatus
import org.eda.ecommerce.data.models.Product
import org.eda.ecommerce.data.repositories.OfferingRepository
import org.eda.ecommerce.data.repositories.ProductRepository
import org.eda.ecommerce.helpers.KafkaTestHelper
import org.junit.jupiter.api.*
import java.time.Duration
import java.util.*


@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@QuarkusTestResource(KafkaCompanionResource::class)
class OfferingTest {

    @InjectKafkaCompanion
    lateinit var companion: KafkaCompanion

    @Inject
    @Identifier("default-kafka-broker")
    lateinit var kafkaConfig: Map<String, Any>

    lateinit var consumer: KafkaConsumer<String, Offering>

    @Inject
    lateinit var offeringRepository: OfferingRepository

    @Inject
    lateinit var productRepository: ProductRepository

    var productId = UUID.randomUUID()

    @BeforeAll
    @Transactional
    fun setup() {
        val product = Product().apply { id = productId }
        productRepository.persist(product)

        val offeringEventJsonSerdeFactory = JsonSerdeFactory<Offering>()
        consumer = KafkaConsumer(
            consumerConfig(),
            StringDeserializer(),
            offeringEventJsonSerdeFactory.createDeserializer(Offering::class.java)
        )
    }

    @BeforeEach
    @Transactional
    fun cleanRepositoryAndKafkaTopics() {
        KafkaTestHelper.clearTopicIfNotEmpty(companion, "offering")

        offeringRepository.deleteAll()
    }

    @AfterEach
    fun unsubscribeConsumer() {
        consumer.unsubscribe()
    }

    fun consumerConfig(): Properties {
        val properties = Properties()
        properties.putAll(kafkaConfig)
        properties[ConsumerConfig.GROUP_ID_CONFIG] = "test-group-id"
        properties[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = "true"
        properties[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        return properties
    }

    @Transactional
    fun createOffering () {
        val offering = Offering().apply { product = Product().apply { id = productId }; quantity = 1; price = 1.99F }
        this.offeringRepository.persist(offering)
    }

    @Test
    fun testCreationAndPersistenceWhenCreatingWithPost() {
        val jsonBody: JsonObject = JsonObject()
            .put("quantity", 1)
            .put("price", 1.99F)
            .put("productId", productId)

        given()
            .contentType("application/json")
            .body(jsonBody.toString())
            .`when`().post("/offering")
            .then()
            .statusCode(201)

        Assertions.assertEquals(1, offeringRepository.count())

        val createdId = offeringRepository.listAll()[0].id

        Assertions.assertEquals(OfferingStatus.ACTIVE, offeringRepository.findById(createdId).status)
        Assertions.assertEquals(jsonBody.getValue("quantity"), offeringRepository.findById(createdId).quantity)
        Assertions.assertEquals(jsonBody.getValue("price"), offeringRepository.findById(createdId).price)
        Assertions.assertEquals(jsonBody.getValue("productId"), offeringRepository.findById(createdId).product?.id)
    }

    @Test
    fun testCreationAndPersistenceWithInactiveStatusWhenCreatingWithPost() {
        val jsonBody: JsonObject = JsonObject()
            .put("status", "inactive")
            .put("quantity", 1)
            .put("price", 1.99F)
            .put("productId", productId)

        given()
            .contentType("application/json")
            .body(jsonBody.toString())
            .`when`().post("/offering")
            .then()
            .statusCode(201)

        Assertions.assertEquals(1, offeringRepository.count())

        val createdId = offeringRepository.listAll()[0].id

        Assertions.assertEquals(OfferingStatus.INACTIVE, offeringRepository.findById(createdId).status)
        Assertions.assertEquals(jsonBody.getValue("quantity"), offeringRepository.findById(createdId).quantity)
        Assertions.assertEquals(jsonBody.getValue("price"), offeringRepository.findById(createdId).price)
        Assertions.assertEquals(jsonBody.getValue("productId"), offeringRepository.findById(createdId).product?.id)
    }

    @Test
    fun testCreationFailedForNonexistingProductWhenCreatingWithPost() {
        val jsonBody: JsonObject = JsonObject()
            .put("quantity", 1)
            .put("price", 1.99F)
            .put("productId", UUID.randomUUID())

        given()
            .contentType("application/json")
            .body(jsonBody.toString())
            .`when`().post("/offering")
            .then()
            .statusCode(404)

        Assertions.assertEquals(0, offeringRepository.count())
    }

    @Test
    fun testKafkaEmitWhenCreatingWithPost() {
        consumer.subscribe(listOf("offering"))

        val jsonBody: JsonObject = JsonObject()
            .put("quantity", 1)
            .put("price", 1.99F)
            .put("productId", productId)

        given()
            .contentType("application/json")
            .body(jsonBody.toString())
            .`when`().post("/offering")
            .then()
            .statusCode(201)

        val records: ConsumerRecords<String, Offering> = consumer.poll(Duration.ofMillis(10000))

        val event = records.records("offering").iterator().asSequence().toList().first()
        val eventPayload = event.value()
        val eventHeaders = event.headers().toList().associateBy({ it.key() }, { it.value().toString(Charsets.UTF_8) })

        Assertions.assertEquals("offering", eventHeaders["source"])
        Assertions.assertEquals("created", eventHeaders["operation"])
        Assertions.assertEquals(OfferingStatus.ACTIVE, eventPayload.status)
        Assertions.assertEquals(jsonBody.getValue("quantity"), eventPayload.quantity)
        Assertions.assertEquals(jsonBody.getValue("price"), eventPayload.price)
        Assertions.assertEquals(jsonBody.getValue("productId"), eventPayload.product?.id)
    }

    @Test
    fun testUpdate() {
        consumer.subscribe(listOf("offering"))

        createOffering()
        Assertions.assertEquals(1, offeringRepository.count())

        val createdId = offeringRepository.listAll()[0].id

        val jsonBodyUpdated: JsonObject = JsonObject()
            .put("id", createdId)
            .put("status", "retired")
            .put("quantity", 2)
            .put("price", 2.99F)
            .put("productId", productId)

        given()
            .contentType("application/json")
            .body(jsonBodyUpdated.toString())
            .`when`()
            .put("/offering")
            .then()
            .statusCode(204)

        val records: ConsumerRecords<String, Offering> = consumer.poll(Duration.ofMillis(10000))

        val event = records.records("offering").iterator().asSequence().toList().first()
        val eventPayload = event.value()
        val eventHeaders = event.headers().toList().associateBy({ it.key() }, { it.value().toString(Charsets.UTF_8) })

        Assertions.assertEquals("offering", eventHeaders["source"])
        Assertions.assertEquals("updated", eventHeaders["operation"])
        Assertions.assertEquals(createdId, eventPayload.id)
        Assertions.assertEquals(OfferingStatus.RETIRED, eventPayload.status)
        Assertions.assertEquals(jsonBodyUpdated.getValue("quantity"), eventPayload.quantity)
        Assertions.assertEquals(jsonBodyUpdated.getValue("price"), eventPayload.price)
        Assertions.assertEquals(productId, eventPayload.product?.id)

        Assertions.assertEquals(1, offeringRepository.count())
    }

    @Test
    fun testDelete() {
        consumer.subscribe(listOf("offering"))

        createOffering()
        Assertions.assertEquals(1, offeringRepository.count())

        val createdId = offeringRepository.listAll()[0].id

        given()
            .contentType("application/json")
            .`when`()
            .delete("/offering/$createdId")
            .then()
            .statusCode(204)

        val records: ConsumerRecords<String, Offering> = consumer.poll(Duration.ofMillis(10000))

        val event = records.records("offering").iterator().asSequence().toList().first()
        val eventPayload = event.value()
        val eventHeaders = event.headers().toList().associateBy({ it.key() }, { it.value().toString(Charsets.UTF_8) })

        Assertions.assertEquals("offering", eventHeaders["source"])
        Assertions.assertEquals("deleted", eventHeaders["operation"])
        Assertions.assertEquals(createdId, eventPayload.id)

        Assertions.assertEquals(0, offeringRepository.count())
    }

}
