package org.eda.ecommerce

import com.fasterxml.jackson.databind.ObjectMapper
import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer
import io.quarkus.kafka.client.serialization.ObjectMapperSerializer

class JsonSerdeFactory<T> {
    fun createDeserializer(type : Class<T>) : ObjectMapperDeserializer<T>{
        return ObjectMapperDeserializer(type)
    }

    fun createSerializer() : ObjectMapperSerializer<T> {
        return ObjectMapperSerializer(ObjectMapper())
    }
}
