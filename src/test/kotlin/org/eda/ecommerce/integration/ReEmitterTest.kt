package org.eda.ecommerce.integration

import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.kafka.InjectKafkaCompanion
import io.quarkus.test.kafka.KafkaCompanionResource
import io.smallrye.reactive.messaging.kafka.companion.ConsumerTask
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion
import org.apache.kafka.clients.producer.ProducerRecord
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource::class)
class ReEmitterTest {

    @InjectKafkaCompanion
    lateinit var companion: KafkaCompanion


    @BeforeEach
    fun recreateTestedTopics() {
        companion.topics().delete("test")
        companion.topics().create("test", 1)
        companion.topics().delete("test-acknowledged")
        companion.topics().create("test-acknowledged", 1)
    }

    @Test
    fun testReEmitterOnce() {
        companion.produce(ByteArray::class.java).fromRecords(
            ProducerRecord("test", "A String Value".toByteArray())
        )

        val testAcknowledgementConsumer: ConsumerTask<String, String> = companion.consumeStrings().fromTopics("test", 1)

        testAcknowledgementConsumer.awaitCompletion()

        Assertions.assertEquals(1, testAcknowledgementConsumer.count())
        Assertions.assertEquals("A String Value", testAcknowledgementConsumer.firstRecord.value())
    }

    @Test
    fun testReEmitterMultiple() {
        companion.produce(ByteArray::class.java).fromRecords(
            ProducerRecord("test", "first".toByteArray()),
            ProducerRecord("test", "second".toByteArray()),
            ProducerRecord("test", "third".toByteArray())
        )

        val testAcknowledgementConsumer: ConsumerTask<String, String> = companion.consumeStrings().fromTopics("test", 3)

        testAcknowledgementConsumer.awaitCompletion()

        Assertions.assertEquals(3, testAcknowledgementConsumer.count())
        Assertions.assertEquals("first", testAcknowledgementConsumer.records[0].value())
        Assertions.assertEquals("second", testAcknowledgementConsumer.records[1].value())
        Assertions.assertEquals("third", testAcknowledgementConsumer.records[2].value())
    }
}
