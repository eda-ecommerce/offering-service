package org.eda.ecommerce.integration

import io.quarkus.test.junit.QuarkusTest
import io.smallrye.common.annotation.Identifier
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.awaitility.Awaitility.await
import org.eda.ecommerce.data.repositories.ProductRepository
import org.junit.jupiter.api.*
import java.util.concurrent.TimeUnit


@QuarkusTest
class ProductTest {

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
        productProducer = KafkaProducer(kafkaConfig, StringSerializer(), StringSerializer())
    }

    @AfterEach
    fun tearDown() {
        productProducer.close()
    }

    @Test
    fun testCreationOnProductCreatedEvent() {
        productProducer
            .send(ProducerRecord("product", "{\"source\": \"product-service\", \"timestamp\": 1699910206866, \"type\": \"created\", \"payload\": { \"id\": 1, \"color\": \"string\", \"description\": \"string\" }}"))
            .get()

        await().atMost(10, TimeUnit.SECONDS).untilAsserted {
            Assertions.assertEquals(1, productRepository.countWithRequestContext())
        }
        val product = productRepository.listAll().first()

        Assertions.assertEquals(1, product.id)
    }

}
