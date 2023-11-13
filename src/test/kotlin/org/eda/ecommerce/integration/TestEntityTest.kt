package org.eda.ecommerce.integration

import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.kafka.InjectKafkaCompanion
import io.quarkus.test.kafka.KafkaCompanionResource
import io.restassured.RestAssured.given
import io.smallrye.reactive.messaging.kafka.companion.ConsumerTask
import io.smallrye.reactive.messaging.kafka.companion.KafkaCompanion
import io.vertx.core.json.JsonObject
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.eda.ecommerce.JsonSerdeFactory
import org.eda.ecommerce.data.models.TestEntity
import org.eda.ecommerce.data.repositories.TestEntityRepository
import org.junit.jupiter.api.*


@QuarkusTest
@QuarkusTestResource(KafkaCompanionResource::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestEntityTest {
    @InjectKafkaCompanion
    lateinit var companion: KafkaCompanion

    @Inject
    lateinit var testEntityRepository: TestEntityRepository

    @BeforeAll
    fun setup() {
        val testEntityJsonSerdeFactory = JsonSerdeFactory<TestEntity>()
        companion.registerSerde(
            TestEntity::class.java,
            testEntityJsonSerdeFactory.createSerializer(),
            testEntityJsonSerdeFactory.createDeserializer(TestEntity::class.java)
        )
    }

    @BeforeEach
    @Transactional
    fun recreateTestedTopics() {
        companion.topics().delete("test-entity")
        companion.topics().create("test-entity", 1)
        testEntityRepository.deleteAll()
    }

    @Test
    @Transactional
    fun testCreationAndPersistenceOnPost() {
        val jsonBody: JsonObject = JsonObject()
            .put("value", "test")

        given()
            .contentType("application/json")
            .body(jsonBody.toString())
            .`when`().post("/entity")
            .then()
            .statusCode(201)

        Assertions.assertEquals(1, testEntityRepository.count())
        Assertions.assertEquals(jsonBody.getValue("value"), testEntityRepository.findById(1L).value)
    }

    @Test
    @Transactional
    fun testKafkaEmitOnPost() {
        val jsonBody: JsonObject = JsonObject()
            .put("value", "a value")

        given()
            .contentType("application/json")
            .body(jsonBody.toString())
            .`when`().post("/entity")
            .then()
            .statusCode(201)

        val testEntityConsumer: ConsumerTask<String, TestEntity> =
            companion.consume(TestEntity::class.java).fromTopics("test-entity", 1)

        testEntityConsumer.awaitCompletion()

        val testEntityResponse = testEntityConsumer.firstRecord.value()

        Assertions.assertEquals(jsonBody.getValue("value"), testEntityResponse.value)
    }

}
