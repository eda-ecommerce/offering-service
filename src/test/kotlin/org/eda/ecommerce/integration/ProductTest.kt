package org.eda.ecommerce.integration

import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.kafka.InjectKafkaCompanion
import io.quarkus.test.kafka.KafkaCompanionResource
import io.smallrye.common.annotation.Identifier
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.awaitility.Awaitility.await
import org.eda.ecommerce.data.repositories.ProductRepository
import org.eda.ecommerce.helpers.KafkaTestHelper
import org.junit.jupiter.api.*
import java.util.*
import java.util.concurrent.TimeUnit


@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource::class)
class ProductTest {

    @InjectKafkaCompanion
    lateinit var companion: KafkaCompanion

    @Inject
    @Identifier("default-kafka-broker")
    lateinit var kafkaConfig: Map<String, Any>

    lateinit var productProducer: KafkaProducer<String, String>

    @Inject
    lateinit var productRepository: ProductRepository


    @BeforeEach
    @Transactional
    fun deleteAllProducts() {
        productRepository.deleteAll()
    }

    @BeforeEach
    fun setupKafkaHelpers() {
       KafkaTestHelper.clearTopicIfNotEmpty(companion,"product")

        productProducer = KafkaProducer(kafkaConfig, StringSerializer(), StringSerializer())
    }

    @AfterEach
    fun tearDown() {
        productProducer.close()
    }

    @Test
    fun testCreationOnProductCreatedEvent() {
        val productUUID = UUID.randomUUID()

        productProducer
            .send(ProducerRecord("product", "{\"source\": \"product-service\", \"timestamp\": 1699910206866, \"type\": \"created\", \"content\": { \"id\": \"${productUUID}\", \"color\": \"string\", \"description\": \"string\" }}"))
            .get()

        await().atMost(10, TimeUnit.SECONDS).untilAsserted {
            Assertions.assertEquals(1, productRepository.countWithRequestContext())
        }
        val product = productRepository.listAll().first()

        Assertions.assertEquals(productUUID, product.id)
    }

}
