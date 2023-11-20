package org.eda.ecommerce.communication.httpEndpoints

import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter
import org.eda.ecommerce.data.models.TestEntity
import org.eda.ecommerce.data.models.TestEntityDTO
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
    @Transactional
    fun createNew(testEntityDTO: TestEntityDTO): Response {
        val testEntity = testEntityDTO.toTestEntity()

        testEntityService.createNewEntity(testEntity)

        return Response.created(URI.create("/entity/" + testEntity.id)).build()
    }

    @PUT
    @Transactional
    fun updateProduct(testEntity: TestEntity): Response {
        val updated = testEntityService.updateTestEntity(testEntity)

        return if (updated)
            Response.status(Response.Status.ACCEPTED).build()
        else
            Response.status(Response.Status.NOT_FOUND).build()
    }

    @DELETE
    @Transactional
    fun deleteProductById(
        @QueryParam("id")
        @Parameter(
            name = "id",
            description = "The ID of the Product to be deleted.",
            schema = Schema(type = SchemaType.NUMBER, format = "long")
        )
        id: Long
    ): Response {
        val deleted = testEntityService.deleteById(id)

        return if (deleted)
            Response.status(Response.Status.ACCEPTED).build()
        else
            Response.status(Response.Status.NOT_FOUND).build()
    }
}
