package org.eda.ecommerce.system

import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.kafka.InjectKafkaCompanion
import io.quarkus.test.kafka.KafkaCompanionResource
import io.restassured.RestAssured
import io.smallrye.common.annotation.Identifier
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion
import io.vertx.core.json.JsonObject
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.awaitility.Awaitility
import org.eda.ecommerce.JsonSerdeFactory
import org.eda.ecommerce.data.models.Offering
import org.eda.ecommerce.data.models.OfferingStatus
import org.eda.ecommerce.data.models.ProductStatus
import org.eda.ecommerce.data.repositories.OfferingRepository
import org.eda.ecommerce.data.repositories.ProductRepository
import org.eda.ecommerce.helpers.KafkaTestHelper
import org.junit.jupiter.api.*
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@QuarkusTestResource(KafkaCompanionResource::class)
class ProductOfferingTest {

    @InjectKafkaCompanion
    lateinit var companion: KafkaCompanion

    @Inject
    @Identifier("default-kafka-broker")
    lateinit var kafkaConfig: Map<String, Any>

    lateinit var consumer: KafkaConsumer<String, Offering>
    lateinit var productProducer: KafkaProducer<String, String>

    @Inject
    lateinit var offeringRepository: OfferingRepository

    @Inject
    lateinit var productRepository: ProductRepository

    @BeforeEach
    @Transactional
    fun cleanRepositoryAndKafkaTopics() {
        KafkaTestHelper.clearTopicIfNotEmpty(companion, "offering")
        KafkaTestHelper.clearTopicIfNotEmpty(companion, "product")

        offeringRepository.deleteAll()
        productRepository.deleteAll()
    }

    @BeforeEach
    fun setupKafkaHelpers() {
        productProducer = KafkaProducer(kafkaConfig, StringSerializer(), StringSerializer())

        consumer = KafkaTestHelper.setupConsumer<Offering>(kafkaConfig)
    }

    @AfterEach
    fun unsubscribeConsumer() {
        KafkaTestHelper.deleteConsumer(consumer)
    }

    @Test
    fun testCreateProductOnEventAndThenOfferingForIt() {
        val productUUID = UUID.randomUUID()

        // Create product via Event
        val productRecord = ProducerRecord<String, String>(
            "product",
            "{\"id\": \"${productUUID}\", \"color\": \"string\", \"description\": \"string\", \"status\": \"active\" }"
        )
        productRecord.headers()
            .add("operation", "created".toByteArray())
            .add("source", "product".toByteArray())
            .add("timestamp", System.currentTimeMillis().toString().toByteArray())

        productProducer
            .send(productRecord)
            .get()

        Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted {
            Assertions.assertEquals(1, productRepository.countWithRequestContext())

            val product = productRepository.getFirstWithRequestContext()

            Assertions.assertEquals(productUUID, product.id)
            Assertions.assertEquals(ProductStatus.ACTIVE, product.status)
        }

        // Create offering via REST API for that product
        val jsonBody: JsonObject = JsonObject()
            .put("quantity", 1)
            .put("price", 1.99F)
            .put("productId", productUUID)

        RestAssured.given()
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
    fun testCreateProductOnEventSetToRetiredAndThenFailToCreateOfferingForIt() {
        val productUUID = UUID.randomUUID()

        // Create product via Event
        val productRecord = ProducerRecord<String, String>(
            "product",
            "{\"id\": \"${productUUID}\", \"color\": \"string\", \"description\": \"string\", \"status\": \"active\" }"
        )
        productRecord.headers()
            .add("operation", "created".toByteArray())
            .add("source", "product".toByteArray())
            .add("timestamp", System.currentTimeMillis().toString().toByteArray())

        productProducer
            .send(productRecord)
            .get()

        Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted {
            Assertions.assertEquals(1, productRepository.countWithRequestContext())

            val product = productRepository.getFirstWithRequestContext()

            Assertions.assertEquals(productUUID, product.id)
            Assertions.assertEquals(ProductStatus.ACTIVE, product.status)
        }

        // Set product to retired via Event
        val productRetiredRecord = ProducerRecord<String, String>(
            "product",
            "{\"id\": \"${productUUID}\", \"color\": \"string\", \"description\": \"string\", \"status\": \"retired\" }"
        )
        productRetiredRecord.headers()
            .add("operation", "updated".toByteArray())
            .add("source", "product".toByteArray())
            .add("timestamp", System.currentTimeMillis().toString().toByteArray())

        productProducer
            .send(productRetiredRecord)
            .get()

        Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted {
            val product = productRepository.getFirstWithRequestContext()

            Assertions.assertEquals(productUUID, product.id)
            Assertions.assertEquals(ProductStatus.RETIRED, product.status)
        }


        // Create offering via REST API for that product
        val jsonBody: JsonObject = JsonObject()
            .put("quantity", 1)
            .put("price", 1.99F)
            .put("productId", productUUID)

        RestAssured.given()
            .contentType("application/json")
            .body(jsonBody.toString())
            .`when`().post("/offering")
            .then()
            .statusCode(400)

        Assertions.assertEquals(0, offeringRepository.count())
    }

    @Test
    fun testCreateProductOnEventCreateOfferingSetProductToRetiredAndThenExpectOfferingEvent() {
        consumer.subscribe(listOf("offering"))

        val productUUID = UUID.randomUUID()

        // Create product via Event
        val productRecord = ProducerRecord<String, String>(
            "product",
            "{\"id\": \"${productUUID}\", \"color\": \"string\", \"description\": \"string\", \"status\": \"active\" }"
        )
        productRecord.headers()
            .add("operation", "created".toByteArray())
            .add("source", "product".toByteArray())
            .add("timestamp", System.currentTimeMillis().toString().toByteArray())

        productProducer
            .send(productRecord)
            .get()

        Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted {
            Assertions.assertEquals(1, productRepository.countWithRequestContext())
        }

        // Create offering via REST API for that product
        val jsonBody: JsonObject = JsonObject()
            .put("quantity", 1)
            .put("price", 1.99F)
            .put("productId", productUUID)

        RestAssured.given()
            .contentType("application/json")
            .body(jsonBody.toString())
            .`when`().post("/offering")
            .then()
            .statusCode(201)

        // Set product to retired via Event
        val productRetiredRecord = ProducerRecord<String, String>(
            "product",
            "{\"id\": \"${productUUID}\", \"color\": \"string\", \"description\": \"string\", \"status\": \"retired\" }"
        )
        productRetiredRecord.headers()
            .add("operation", "updated".toByteArray())
            .add("source", "product".toByteArray())
            .add("timestamp", System.currentTimeMillis().toString().toByteArray())

        productProducer
            .send(productRetiredRecord)
            .get()

        Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted {
            val product = productRepository.getFirstWithRequestContext()

            Assertions.assertEquals(productUUID, product.id)
            Assertions.assertEquals(ProductStatus.RETIRED, product.status)
        }

        // And expect retired events for offering
        val records: ConsumerRecords<String, Offering> = consumer.poll(Duration.ofMillis(10000))

        val event = records.records("offering").iterator().asSequence().toList().first()
        val eventHeaders = event.headers().toList().associateBy({ it.key() }, { it.value().toString(Charsets.UTF_8) })

        Assertions.assertEquals("offering", eventHeaders["source"])
        Assertions.assertEquals("updated", eventHeaders["operation"])
    }
}
