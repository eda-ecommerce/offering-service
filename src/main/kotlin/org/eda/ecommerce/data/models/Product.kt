package org.eda.ecommerce.data.models

import io.quarkus.hibernate.orm.panache.PanacheEntityBase
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class Product : PanacheEntityBase() {

    @Id
    var id: Long? = null

    override fun toString(): String {
        return "Product(id=$id)"
    }
}
