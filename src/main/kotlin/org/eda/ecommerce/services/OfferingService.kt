package org.eda.ecommerce.services

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.NotFoundException
import org.eclipse.microprofile.reactive.messaging.Channel
import org.eclipse.microprofile.reactive.messaging.Emitter
import org.eda.ecommerce.data.models.Offering
import org.eda.ecommerce.data.models.OfferingDTO
import org.eda.ecommerce.data.models.events.OfferingEvent
import org.eda.ecommerce.data.repositories.OfferingRepository
import org.eda.ecommerce.data.repositories.ProductRepository

@ApplicationScoped
class OfferingService {

    @Inject
    private lateinit var offeringRepository: OfferingRepository

    @Inject
    private lateinit var productRepository: ProductRepository

    @Inject
    @Channel("offering-out")
    private lateinit var offeringEventEmitter: Emitter<OfferingEvent>

    fun getAll(): List<Offering> {
        return offeringRepository.listAll()
    }

    fun findById(id: Long): Offering? {
        return offeringRepository.findById(id)
    }

    fun offeringDTOToOffering(offeringDTO: OfferingDTO): Offering {
        val existingProduct = productRepository.findById(offeringDTO.productId)
            ?: throw NotFoundException("Product with id ${offeringDTO.productId} not found")

        val offering = Offering()
        offering.quantity = offeringDTO.quantity
        offering.price = offeringDTO.price
        offering.product = existingProduct
        return offering
    }

    fun deleteById(id: Long): Boolean {
        val offeringToDelete = offeringRepository.findById(id) ?: return false

        offeringRepository.delete(offeringToDelete)

        val offeringEvent = OfferingEvent(
            type = "deleted",
            payload = Offering().apply { this.id = id }
        )

        offeringEventEmitter.send(offeringEvent).toCompletableFuture().get()

        return true
    }

    // It is unfortunately necessary to split the transformation of th DTO to the real Offering and persisting / emitting
    // for some weird transaction related issue that locks the database on findById and does not release the lock to save.
    fun createNewEntity(offeringDTO: OfferingDTO) : Offering{
        val offering = offeringDTOToOffering(offeringDTO)

        persistWithTransactionAndEmit(offering)
        return offering
    }

    @Transactional
    fun persistWithTransactionAndEmit (offering: Offering) {
        offeringRepository.persist(offering)

        val offeringEvent = OfferingEvent(
            type = "deleted",
            payload = offering
        )

        offeringEventEmitter.send(offeringEvent).toCompletableFuture().get()
    }

    fun updateOffering(offering: Offering) : Boolean {
        val entity = offeringRepository.findById(offering.id) ?: return false

        entity.apply {
            this.quantity = offering.quantity
            this.price = offering.price
            this.quantity = offering.quantity
            this.product = offering.product
        }

        offeringRepository.persist(entity)

        val offeringEvent = OfferingEvent(
            type = "updated",
            payload = entity
        )

        offeringEventEmitter.send(offeringEvent).toCompletableFuture().get()

        return true
    }

}
