package org.eda.ecommerce.data.repositories

import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional
import org.eda.ecommerce.data.models.TestEntity

@ApplicationScoped
class TestEntityRepository : PanacheRepository<TestEntity> {

    @Transactional
    fun persistWithTransaction(testEntity: TestEntity) {
        super.persist(testEntity)
    }
}
