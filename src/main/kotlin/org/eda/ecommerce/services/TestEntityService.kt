package org.eda.ecommerce.services

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.reactive.messaging.Channel
import org.eclipse.microprofile.reactive.messaging.Emitter
import org.eda.ecommerce.data.models.TestEntity
import org.eda.ecommerce.data.models.events.TestEntityEvent
import org.eda.ecommerce.data.repositories.TestEntityRepository

@ApplicationScoped
class TestEntityService {

    @Inject
    private lateinit var testEntityRepository: TestEntityRepository

    @Inject
    @Channel("test-entity-out")
    private lateinit var testEntityEmitter: Emitter<TestEntityEvent>

    fun getAll(): List<TestEntity> {
        return testEntityRepository.listAll()
    }

    fun findById(id: Long): TestEntity {
        return testEntityRepository.findById(id)
    }

    fun deleteById(id: Long): Boolean {
        val productToDelete = testEntityRepository.findById(id) ?: return false

        testEntityRepository.delete(productToDelete)

        val productEvent = TestEntityEvent(
            source = "test-service",
            type = "deleted",
            payload = TestEntity().apply { this.id = id }
        )

        testEntityEmitter.send(productEvent).toCompletableFuture().get()

        return true
    }

    fun createNewEntity(testEntity: TestEntity) {
        testEntityRepository.persist(testEntity)

        val testEntityEvent = TestEntityEvent(
            source = "test-service",
            type = "deleted",
            payload = testEntity
        )

        testEntityEmitter.send(testEntityEvent).toCompletableFuture().get()
    }

    fun updateTestEntity(testEntity: TestEntity) : Boolean {
        val entity = testEntityRepository.findById(testEntity.id) ?: return false

        entity.apply {
            this.value = testEntity.value
        }

        testEntityRepository.persist(entity)

        val productEvent = TestEntityEvent(
            source = "test-service",
            type = "updated",
            payload = entity
        )

        testEntityEmitter.send(productEvent).toCompletableFuture().get()

        return true
    }

}
