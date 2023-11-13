package org.eda.ecommerce.data.models

import io.quarkus.hibernate.orm.panache.PanacheEntity
import io.quarkus.hibernate.orm.panache.PanacheEntity_
import jakarta.persistence.Entity

@Entity
class Product : PanacheEntity() {
    var color: String? = null
    var description: String? = null

    override fun toString(): String {
        return "Product(id=$id, color=$color, description=$description)"
    }
}

class CreateProductDTO {
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

