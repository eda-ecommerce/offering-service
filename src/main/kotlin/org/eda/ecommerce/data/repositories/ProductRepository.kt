package org.eda.ecommerce.data.repositories

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.context.control.ActivateRequestContext
import org.eda.ecommerce.data.models.Product
import java.util.UUID

@ApplicationScoped
class ProductRepository : PanacheRepositoryBase<Product, UUID> {

    @ActivateRequestContext
    fun countWithRequestContext() : Long {
        return count()
    }

    @ActivateRequestContext
    fun getFirstWithRequestContext() : Product {
        return listAll().first()
    }
}
