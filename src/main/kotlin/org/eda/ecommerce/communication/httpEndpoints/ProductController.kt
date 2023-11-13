package org.eda.ecommerce.communication.httpEndpoints

import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter
import org.eda.ecommerce.data.models.CreateProductDTO
import org.eda.ecommerce.data.models.Product
import org.eda.ecommerce.services.ProductService
import java.net.URI

@Path("/products")
class ProductController {

    @Inject
    private lateinit var productService: ProductService


    @GET
    fun getAll(): List<Product> {
        return productService.getAll()
    }

    @GET
    @Path("/{id}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Operation(summary = "Returns a Product by its ID.")
    fun getById(
        @QueryParam("id")
        @Parameter(
            name = "id",
            description = "The ID of the Product to be returned.",
            schema = Schema(type = SchemaType.NUMBER, format = "long")
        )
        id: Long
    ): Product {
        return productService.findById(id)
    }

    @POST
    fun createNewProduct(productDTO: CreateProductDTO): Response {
        val product = productDTO.toProduct()

        productService.createNewProduct(product)

        return Response.created(URI.create("/products/" + product.id)).build()
    }
}
