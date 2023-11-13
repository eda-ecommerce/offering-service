package org.eda.ecommerce.communication.httpEndpoints

import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.openapi.annotations.Operation


@Path("/hello")
@ApplicationScoped
class GreetingResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "Returns a Hello World String")
    fun hello() = "Hello from RESTEasy Reactive"

}
