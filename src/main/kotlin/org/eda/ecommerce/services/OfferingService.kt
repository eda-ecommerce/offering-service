package org.eda.ecommerce.services

import io.smallrye.reactive.messaging.MutinyEmitter
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.NotFoundException
import org.eclipse.microprofile.reactive.messaging.Channel
import org.eclipse.microprofile.reactive.messaging.Emitter
import org.eda.ecommerce.data.models.Offering
import org.eda.ecommerce.data.models.CreateOfferingDTO
import org.eda.ecommerce.data.models.UpdateOfferingDTO
import org.eda.ecommerce.data.models.events.OfferingCreatedEvent
import org.eda.ecommerce.data.models.events.OfferingDeletedEvent
import org.eda.ecommerce.data.models.events.OfferingUpdatedEvent
import org.eda.ecommerce.data.repositories.OfferingRepository
import org.eda.ecommerce.data.repositories.ProductRepository
import java.util.UUID

@ApplicationScoped
class OfferingService {

    @Inject
    private lateinit var offeringRepository: OfferingRepository

    @Inject
    private lateinit var productRepository: ProductRepository

    @Inject
    @Channel("offering-out")
    private lateinit var offeringEventEmitter: MutinyEmitter<Offering>

    fun getAll(): List<Offering> {
        return offeringRepository.listAll()
    }

    fun findById(id: UUID): Offering? {
        return offeringRepository.findById(id)
    }

    fun offeringDTOToOffering(createOfferingDTO: CreateOfferingDTO): Offering {
        val existingProduct = productRepository.findById(createOfferingDTO.productId)
            ?: throw NotFoundException("Product with id ${createOfferingDTO.productId} not found")

        val offering = Offering()
        offering.status = createOfferingDTO.status
        offering.quantity = createOfferingDTO.quantity
        offering.price = createOfferingDTO.price
        offering.product = existingProduct
        return offering
    }

    fun deleteById(id: UUID): Boolean {
        val offeringToDelete = offeringRepository.findById(id) ?: return false

        offeringRepository.delete(offeringToDelete)

        offeringEventEmitter.sendMessageAndAwait(OfferingDeletedEvent(offeringToDelete))

        return true
    }

    // It is unfortunately necessary to split the transformation of th DTO to the real Offering and persisting / emitting
    // for some weird transaction related issue that locks the database on findById and does not release the lock to save.
    fun createNewEntity(createOfferingDTO: CreateOfferingDTO) : Offering{
        val offering = offeringDTOToOffering(createOfferingDTO)

        persistWithTransactionAndEmit(offering)
        return offering
    }

    @Transactional
    fun persistWithTransactionAndEmit (offering: Offering) {
        offeringRepository.persist(offering)

        offeringEventEmitter.sendMessageAndAwait(OfferingCreatedEvent(offering))
    }

    fun updateOffering(offeringDTO: UpdateOfferingDTO) : Boolean {
        val entity = offeringRepository.findById(offeringDTO.id) ?: return false

        val product = productRepository.findById(offeringDTO.productId)
            ?: throw NotFoundException("Product with id ${offeringDTO.productId} not found")

        entity.apply {
            this.quantity = offeringDTO.quantity
            this.price = offeringDTO.price
            this.quantity = offeringDTO.quantity
            this.status = offeringDTO.status
            this.product = product
        }

        offeringRepository.persist(entity)

        offeringEventEmitter.sendMessageAndAwait(OfferingUpdatedEvent(entity))

        return true
    }

}
