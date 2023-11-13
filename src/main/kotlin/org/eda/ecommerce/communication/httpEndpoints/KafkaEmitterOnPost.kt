package org.eda.ecommerce.communication.httpEndpoints

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eda.ecommerce.services.KafkaTestService

@Path("emit")
@ApplicationScoped
class KafkaEmitterOnPost {

    @Inject
    private lateinit var kafkaTestService: KafkaTestService

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Operation(summary = "Accepts a plain Body that is interpreted as a String and send as an Event into the 'test' topic.")
    fun emitOnPost(string: String) {
        kafkaTestService.emitTestMessage(string)
    }

}
