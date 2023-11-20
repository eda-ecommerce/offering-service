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
import org.eda.ecommerce.data.models.events.TestEntityEvent
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
        val testEntityJsonSerdeFactory = JsonSerdeFactory<TestEntityEvent>()
        companion.registerSerde(
            TestEntityEvent::class.java,
            testEntityJsonSerdeFactory.createSerializer(),
            testEntityJsonSerdeFactory.createDeserializer(TestEntityEvent::class.java)
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
    fun testKafkaEmitOnPost() {
        val jsonBody: JsonObject = JsonObject()
            .put("value", "a value")

        given()
            .contentType("application/json")
            .body(jsonBody.toString())
            .`when`().post("/entity")
            .then()
            .statusCode(201)

        val testEntityConsumer: ConsumerTask<String, TestEntityEvent> =
            companion.consume(TestEntityEvent::class.java).fromTopics("test-entity", 1)

        testEntityConsumer.awaitCompletion()

        val testEntityResponse = testEntityConsumer.firstRecord.value()

        Assertions.assertEquals(jsonBody.getValue("value"), testEntityResponse.payload.value)
    }

    @Test
    fun testDelete() {
        val jsonBody: JsonObject = JsonObject()
            .put("value", "Test")

        given()
            .contentType("application/json")
            .body(jsonBody.toString())
            .`when`().post("/entity")
            .then()
            .statusCode(201)

        Assertions.assertEquals(1, testEntityRepository.count())

        val createdId = testEntityRepository.listAll()[0].id

        given()
            .contentType("application/json")
            .`when`()
            .queryParam("id", createdId)
            .delete("/entity")
            .then()
            .statusCode(202)

        val productConsumer: ConsumerTask<String, TestEntityEvent> =
            companion.consume(TestEntityEvent::class.java).fromTopics("test-entity", 2)

        productConsumer.awaitCompletion()

        val event = productConsumer.records[1].value()
        Assertions.assertEquals("test-service", event.source)
        Assertions.assertEquals("deleted", event.type)
        Assertions.assertEquals(createdId, event.payload.id)
        Assertions.assertEquals(null, event.payload.value)

        Assertions.assertEquals(0, testEntityRepository.count())
    }

    @Test
    fun testUpdate() {
        val jsonBody: JsonObject = JsonObject()
            .put("value", "A thing")

        given()
            .contentType("application/json")
            .body(jsonBody.toString())
            .`when`().post("/entity")
            .then()
            .statusCode(201)

        Assertions.assertEquals(1, testEntityRepository.count())

        val createdId = testEntityRepository.listAll()[0].id

        val jsonBodyUpdated: JsonObject = JsonObject()
            .put("id", createdId)
            .put("color", "Another thing")

        given()
            .contentType("application/json")
            .body(jsonBodyUpdated.toString())
            .`when`()
            .put("/entity")
            .then()
            .statusCode(202)

        val productConsumer: ConsumerTask<String, TestEntityEvent> =
            companion.consume(TestEntityEvent::class.java).fromTopics("test-entity", 2)

        productConsumer.awaitCompletion()

        val event = productConsumer.records[1].value()
        Assertions.assertEquals("test-service", event.source)
        Assertions.assertEquals("updated", event.type)
        Assertions.assertEquals(createdId, event.payload.id)
        Assertions.assertEquals(jsonBodyUpdated.getValue("value"), event.payload.value)

        Assertions.assertEquals(1, testEntityRepository.count())
    }

}
