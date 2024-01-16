package org.eda.ecommerce.communication.httpEndpoints

import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType
import org.eclipse.microprofile.openapi.annotations.media.Content
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses
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
    @Operation(summary = "Returns a list of all Offerings")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "A list of Offerings.",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = Array<Offering>::class))]
        )
    )
    fun getAll(): List<Offering> {
        return offeringService.getAll()
    }

    @GET
    @Path("/{id}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Operation(summary = "Returns a Offering by its ID.")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "The Offering with the given ID.",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = Offering::class))]
        ),
        APIResponse(responseCode = "404", description = "Offering not found")
    )
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
    @Operation(summary = "Creates a new Offering.")
    @APIResponses(
        APIResponse(
            responseCode = "201",
            description = "Product created",
        )
    )
    fun createNew(createOfferingDTO: CreateOfferingDTO): Response {
        val newOffering = offeringService.createNewOfferingIfAllowed(createOfferingDTO)

        return Response.created(URI.create("/offering/" + newOffering.id)).build()
    }

    @PUT
    @Operation(summary = "Updates an existing Offering.")
    @APIResponses(
        APIResponse(responseCode = "204", description = "Offering updated"),
        APIResponse(responseCode = "400", description = "Invalid Offering data supplied"),
        APIResponse(responseCode = "404", description = "Offering not found")
    )
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
    @Operation(summary = "Deletes an existing Offering.")
    @APIResponses(
        APIResponse(responseCode = "204", description = "Offering deleted"),
        APIResponse(responseCode = "404", description = "Offering not found")
    )
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
