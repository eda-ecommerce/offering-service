package org.eda.ecommerce.data.repositories

import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.context.control.ActivateRequestContext
import org.eda.ecommerce.data.models.Product

@ApplicationScoped
class ProductRepository : PanacheRepository<Product> {

    @ActivateRequestContext
    fun countWithRequestContext() : Long {
        return count()
    }
}
