package org.eda.ecommerce.integration

import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.kafka.InjectKafkaCompanion
import io.quarkus.test.kafka.KafkaCompanionResource
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.apache.kafka.clients.producer.ProducerRecord
import org.eda.ecommerce.JsonSerdeFactory
import org.eda.ecommerce.data.models.events.OfferingEvent
import org.eda.ecommerce.data.repositories.ProductRepository
import org.junit.jupiter.api.*


@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProductTest {

    @InjectKafkaCompanion
    lateinit var companion: KafkaCompanion

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
    }

    @BeforeEach
    @Transactional
    fun recreateTestedTopics() {
        companion.topics().delete("offering")
        companion.topics().create("offering", 1)
        companion.topics().delete("product")
        companion.topics().create("product", 1)
        productRepository.deleteAll()
    }

    @Test
    fun testCreationOnProductCreatedEvent() {
        companion.produce(ByteArray::class.java).fromRecords(
            ProducerRecord("product", "{\"source\": \"product-service\", \"timestamp\": 1699910206866, \"type\": \"created\", \"payload\": { \"id\": 1, \"color\": \"string\", \"description\": \"string\" }}".toByteArray())
        ).awaitCompletion()

        Assertions.assertEquals(1, productRepository.count())

        val product = productRepository.listAll().first()

        Assertions.assertEquals(1, product.id)
    }

}
