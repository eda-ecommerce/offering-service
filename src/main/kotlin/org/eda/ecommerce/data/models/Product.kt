package org.eda.ecommerce.data.models

import com.fasterxml.jackson.databind.ObjectMapper
import io.quarkus.hibernate.orm.panache.PanacheEntity
import io.quarkus.hibernate.orm.panache.PanacheEntity_
import io.quarkus.kafka.client.serialization.ObjectMapperSerializer
import jakarta.persistence.Entity

@Entity
class Product : PanacheEntity() {
    var color: String? = null
    var description: String? = null

    fun toJson(): String {
        val objectMapper = ObjectMapper()
        return objectMapper.writeValueAsString(this)
    }

    override fun toString(): String {
        return "Product(id=$id, color=$color, description=$description)"
    }
}

class CreateProductDTO{
    var color: String? = null
    var description: String? = null

    fun toProduct(): Product {
        val product = Product()
        product.color = color
        product.description = description
        return product
    }

    override fun toString(): String {
        return "Product(id=${PanacheEntity_.id}, color=$color, description=$description)"
    }
}

