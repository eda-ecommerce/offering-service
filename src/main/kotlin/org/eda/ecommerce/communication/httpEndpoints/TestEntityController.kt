package org.eda.ecommerce.communication.httpEndpoints

import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter
import org.eda.ecommerce.data.models.TestEntity
import org.eda.ecommerce.services.TestEntityService
import java.net.URI

@Path("/entity")
class TestEntityController {

    @Inject
    private lateinit var testEntityService: TestEntityService


    @GET
    fun getAll(): List<TestEntity> {
        return testEntityService.getAll()
    }

    @GET
    @Path("/{id}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Operation(summary = "Returns a TestEntity by its ID.")
    fun getById(
        @QueryParam("id")
        @Parameter(
            name = "id",
            description = "The ID of the TestEntity to be returned.",
            schema = Schema(type = SchemaType.NUMBER, format = "long")
        )
        id: Long
    ): TestEntity {
        return testEntityService.findById(id)
    }

    @POST
    fun createNew(testEntity: TestEntity): Response {
        testEntityService.createNewEntity(testEntity)

        return Response.created(URI.create("/entity/" + testEntity.id)).build()
    }
}
