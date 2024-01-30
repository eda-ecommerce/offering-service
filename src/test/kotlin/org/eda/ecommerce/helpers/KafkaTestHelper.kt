package org.eda.ecommerce.helpers

import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.eda.ecommerce.JsonSerdeFactory
import java.util.*

class KafkaTestHelper {
    companion object    {

        fun clearTopicIfNotEmpty(companion: KafkaCompanion, topic: String) {
            companion.topics().delete(topic)
            companion.topics().createAndWait(topic, 1)
        }

        fun <K, V> deleteConsumer(consumer: KafkaConsumer<K, V>) {
            consumer.unsubscribe()
            consumer.close()
        }

        inline fun <reified T> setupConsumer (kafkaConfig: Map<String, Any>): KafkaConsumer<String, T> {
            val consumerProperties = Properties()
            consumerProperties.putAll(kafkaConfig)
            consumerProperties[ConsumerConfig.GROUP_ID_CONFIG] = "test-group-id"
            consumerProperties[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = "true"
            consumerProperties[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"



            val offeringEventJsonSerdeFactory = JsonSerdeFactory<T>()
            return KafkaConsumer(
                consumerProperties,
                StringDeserializer(),
                offeringEventJsonSerdeFactory.createDeserializer(T::class.java)
            )
        }
    }


}
