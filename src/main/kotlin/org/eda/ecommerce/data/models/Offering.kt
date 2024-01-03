package org.eda.ecommerce.data.models

import io.quarkus.hibernate.orm.panache.PanacheEntityBase
import jakarta.persistence.*
import org.hibernate.annotations.GenericGenerator
import java.util.*

@Entity
class Offering : PanacheEntityBase() {

    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    var id: UUID? = null

    var quantity: Int? = null
    var price: Float? = null

    @ManyToOne(fetch = FetchType.LAZY)
    var product: Product? = null

    override fun toString(): String {
        return "Offering(id=$id, quantity=$quantity, price=$price, product=$product)"
    }
}

class OfferingDTO(var productId: UUID? = null, var quantity: Int? = null, var price: Float? = null) {
    override fun toString(): String {
        return "OfferingDTO(productId=$productId, quantity=$quantity, price=$price)"
    }
}
