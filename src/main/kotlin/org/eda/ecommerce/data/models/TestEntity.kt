package org.eda.ecommerce.data.models

import io.quarkus.hibernate.orm.panache.PanacheEntity
import io.quarkus.hibernate.orm.panache.PanacheEntity_
import jakarta.persistence.Entity

@Entity
class TestEntity : PanacheEntity() {
    var value: String? = null

    override fun toString(): String {
        return "TestEntity(id=$id, value=$value)"
    }
}

class TestEntityDTO {
    var value: String? = null

    fun toTestEntity(): TestEntity {
        val testEntity = TestEntity()
        testEntity.value = value
        return testEntity
    }

    override fun toString(): String {
        return "TestEntity(id=${PanacheEntity_.id}, value=$value)"
    }
}
