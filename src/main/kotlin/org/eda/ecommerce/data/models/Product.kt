package org.eda.ecommerce.data.models

import com.fasterxml.jackson.annotation.JsonValue
import io.quarkus.hibernate.orm.panache.PanacheEntityBase
import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.util.*

@Entity
class Product : PanacheEntityBase() {

    @Id
    lateinit var id: UUID

    lateinit var status: ProductStatus

    override fun toString(): String {
        return "Product(id=${id}, status=$status)"
    }
}

enum class ProductStatus(@JsonValue val value: String) {
    ACTIVE("active"),
    RETIRED("retired");
}
