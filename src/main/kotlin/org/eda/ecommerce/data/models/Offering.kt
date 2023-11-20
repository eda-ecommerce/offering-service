package org.eda.ecommerce.data.models

import io.quarkus.hibernate.orm.panache.PanacheEntity
import io.quarkus.hibernate.orm.panache.PanacheEntity_
import jakarta.persistence.*

@Entity
class Offering : PanacheEntity() {
    var quantity: Int? = null
    var price: Float? = null

    @ManyToOne(fetch = FetchType.LAZY)
    var product: Product? = null

    override fun toString(): String {
        return "Offering(id=$id, quantity=$quantity, price=$price, product=$product)"
    }
}

class OfferingDTO {
    var quantity: Int? = null
    var price: Float? = null
    var productId: Long? = null

    override fun toString(): String {
        return "Offering(id=${PanacheEntity_.id})"
    }
}
