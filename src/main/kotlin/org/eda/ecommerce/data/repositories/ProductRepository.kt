package org.eda.ecommerce.data.repositories

import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import org.eda.ecommerce.data.models.Product

@ApplicationScoped
class ProductRepository : PanacheRepository<Product> {

    @Transactional
    fun persistWithTransaction(product: Product) {
        super.persist(product)
    }
}
