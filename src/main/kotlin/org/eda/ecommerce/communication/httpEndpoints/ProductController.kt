package org.eda.ecommerce.communication.httpEndpoints

import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType
import org.eclipse.microprofile.openapi.annotations.media.Content
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses
import org.eda.ecommerce.data.models.Product
import org.eda.ecommerce.services.ProductService
import java.util.*

@Path("/products")
class ProductController {

    @Inject
    private lateinit var productService: ProductService


    @GET
    @Operation(summary = "Returns a list of all Products")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "A list of Products.",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = Array<Product>::class))]
        )
    )
    fun getAll(): List<Product> {
        return productService.getAll()
    }

    @GET
    @Path("{id}")
    @Operation(summary = "Returns a Product by its ID.")
    @APIResponses(
        APIResponse(
            responseCode = "200",
            description = "The Product with the given ID.",
            content = [Content(mediaType = MediaType.APPLICATION_JSON, schema = Schema(implementation = Product::class))]
        ),
        APIResponse(responseCode = "404", description = "Product not found")
    )
    @Consumes(MediaType.TEXT_PLAIN)
    fun getById(
        @PathParam("id")
        @Parameter(
            name = "id",
            description = "The ID of the Product to be returned.",
            schema = Schema(type = SchemaType.STRING, format = "UUID")
        )
        id: UUID
    ): Product {
        return productService.findById(id)
    }
}
