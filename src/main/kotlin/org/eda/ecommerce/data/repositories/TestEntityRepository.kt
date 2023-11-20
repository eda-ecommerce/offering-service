package org.eda.ecommerce.data.repositories

import io.quarkus.hibernate.orm.panache.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import org.eda.ecommerce.data.models.TestEntity

@ApplicationScoped
class TestEntityRepository : PanacheRepository<TestEntity>
