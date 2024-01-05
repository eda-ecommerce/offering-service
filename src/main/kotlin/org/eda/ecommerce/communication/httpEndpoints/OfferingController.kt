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
import org.eda.ecommerce.data.models.Offering
import org.eda.ecommerce.data.models.CreateOfferingDTO
import org.eda.ecommerce.data.models.UpdateOfferingDTO
import org.eda.ecommerce.services.OfferingService
import java.net.URI
import java.util.UUID

@Path("/offering")
class OfferingController {

    @Inject
    private lateinit var offeringService: OfferingService


    @GET
    fun getAll(): List<Offering> {
        return offeringService.getAll()
    }

    @GET
    @Path("/{id}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Operation(summary = "Returns a Offering by its ID.")
    fun getById(
        @PathParam("id")
        @Parameter(
            name = "id",
            description = "The ID of the Offering to be returned.",
            schema = Schema(type = SchemaType.STRING, format = "UUID")
        )
        id: UUID
    ): Response {
        val offering = offeringService.findById(id)

        return if (offering != null)
            Response.ok(offering).build()
        else
            Response.status(Response.Status.NOT_FOUND).build()
    }

    @POST
    fun createNew(createOfferingDTO: CreateOfferingDTO): Response {
        val newOffering = offeringService.createNewEntity(createOfferingDTO)

        return Response.created(URI.create("/offering/" + newOffering.id)).build()
    }

    @PUT
    @Transactional
    fun updateOffering(updateOfferingDTO: UpdateOfferingDTO): Response {
        val updated = offeringService.updateOffering(updateOfferingDTO)

        return if (updated)
            Response.status(Response.Status.NO_CONTENT).build()
        else
            Response.status(Response.Status.NOT_FOUND).build()
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    fun deleteOfferingById(
        @PathParam("id")
        @Parameter(
            name = "id",
            description = "The ID of the Offering to be returned.",
            schema = Schema(type = SchemaType.STRING, format = "UUID")
        )
        id: UUID
    ): Response {
        val deleted = offeringService.deleteById(id)

        return if (deleted)
            Response.status(Response.Status.NO_CONTENT).build()
        else
            Response.status(Response.Status.NOT_FOUND).build()
    }
}