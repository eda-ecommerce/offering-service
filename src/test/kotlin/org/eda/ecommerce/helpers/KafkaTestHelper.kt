package org.eda.ecommerce.helpers

import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion

class KafkaTestHelper {
    companion object    {
        fun clearTopicIfNotEmpty(companion: KafkaCompanion, topic: String) {
            companion.topics().delete(topic)
            companion.topics().createAndWait(topic, 1)
        }
    }


}
