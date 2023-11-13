package org.eda.ecommerce.services

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.reactive.messaging.Channel
import org.eclipse.microprofile.reactive.messaging.Emitter
import org.eda.ecommerce.data.models.TestEntity
import org.eda.ecommerce.data.repositories.TestEntityRepository

@ApplicationScoped
class TestEntityService {

    @Inject
    private lateinit var testEntityRepository: TestEntityRepository

    @Inject
    @Channel("test-entity-out")
    private lateinit var testEntityEmitter: Emitter<TestEntity>

    fun getAll(): List<TestEntity> {
        return testEntityRepository.listAll()
    }

    fun findById(id: Long): TestEntity {
        return testEntityRepository.findById(id)
    }

    fun createNewEntity(testEntity: TestEntity) {
        testEntityRepository.persistWithTransaction(testEntity)

        testEntityEmitter.send(testEntity)
    }

}
