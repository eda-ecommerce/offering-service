package org.eda.ecommerce.data.models

import com.fasterxml.jackson.annotation.JsonValue
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
    lateinit var id: UUID

    var status: OfferingStatus = OfferingStatus.ACTIVE

    var quantity: Int? = null
    var price: Float? = null

    @ManyToOne(fetch = FetchType.LAZY)
    var product: Product? = null

    override fun toString(): String {
        return "Offering(id=$id, status=$status, squantity=$quantity, price=$price, product=$product)"
    }
}

class CreateOfferingDTO(var status: OfferingStatus = OfferingStatus.ACTIVE, var productId: UUID? = null, var quantity: Int? = null, var price: Float? = null) {
    override fun toString(): String {
        return "Offering(status=$status, productId=$productId, quantity=$quantity, price=$price)"
    }
}

class UpdateOfferingDTO(
    var id: UUID,
    var status: OfferingStatus = OfferingStatus.ACTIVE,
    var productId: UUID? = null,
    var quantity: Int? = null,
    var price: Float? = null
) {
    override fun toString(): String {
        return "OfferingDTO(id=$id, status=$status, productId=$productId, quantity=$quantity, price=$price)"
    }
}

enum class OfferingStatus(@JsonValue val value: String) {
    ACTIVE("active"),
    INACTIVE("inactive"),
    RETIRED("retired");
}
