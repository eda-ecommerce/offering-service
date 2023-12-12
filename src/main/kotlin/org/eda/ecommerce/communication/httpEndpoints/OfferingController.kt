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
import org.eda.ecommerce.data.models.OfferingDTO
import org.eda.ecommerce.services.OfferingService
import java.net.URI

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
        @QueryParam("id")
        @Parameter(
            name = "id",
            description = "The ID of the Offering to be returned.",
            schema = Schema(type = SchemaType.NUMBER, format = "long")
        )
        id: Long
    ): Offering {
        return offeringService.findById(id)
    }

    @POST
    fun createNew(offeringDTO: OfferingDTO): Response {
        val newOffering = offeringService.createNewEntity(offeringDTO)

        return Response.created(URI.create("/offering/" + newOffering.id)).build()
    }

    @PUT
    @Transactional
    fun updateOffering(offering: Offering): Response {
        val updated = offeringService.updateOffering(offering)

        return if (updated)
            Response.status(Response.Status.NO_CONTENT).build()
        else
            Response.status(Response.Status.NOT_FOUND).build()
    }

    @DELETE
    @Transactional
    fun deleteOfferingById(
        @QueryParam("id")
        @Parameter(
            name = "id",
            description = "The ID of the Offering to be deleted.",
            schema = Schema(type = SchemaType.NUMBER, format = "long")
        )
        id: Long
    ): Response {
        val deleted = offeringService.deleteById(id)

        return if (deleted)
            Response.status(Response.Status.NO_CONTENT).build()
        else
            Response.status(Response.Status.NOT_FOUND).build()
    }
}
