package org.eda.ecommerce.unit

import jakarta.ws.rs.NotFoundException
import org.eda.ecommerce.data.models.CreateOfferingDTO
import org.eda.ecommerce.data.models.OfferingStatus
import org.eda.ecommerce.data.models.Product
import org.eda.ecommerce.data.repositories.OfferingRepository
import org.eda.ecommerce.data.repositories.ProductRepository
import org.eda.ecommerce.services.OfferingService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.lang.reflect.Field
import java.util.*


class OfferingServiceTest {

    private lateinit var offeringService: OfferingService
    private val productRepository = mock(ProductRepository::class.java)
    private val offeringRepository = mock(OfferingRepository::class.java)

    @BeforeEach
    fun setUp() {
        offeringService = OfferingService()
        setPrivateField(offeringService, "productRepository", productRepository)
        setPrivateField(offeringService, "offeringRepository", offeringRepository)
    }

    private fun setPrivateField(instance: Any, fieldName: String, value: Any) {
        val field: Field = instance::class.java.getDeclaredField(fieldName)
        field.isAccessible = true
        field.set(instance, value)
    }
    @Test
    fun testOfferingDTOToOffering_Success() {
        val productId = UUID.randomUUID()
        val product = Product()
        `when`(productRepository.findById(productId)).thenReturn(product)

        val createOfferingDTO = CreateOfferingDTO(OfferingStatus.ACTIVE, productId, 10, 100.0f)
        val offering = offeringService.offeringDTOToOffering(createOfferingDTO)

        assertNotNull(offering)
        assertEquals(OfferingStatus.ACTIVE, offering.status)
        assertEquals(10, offering.quantity)
        assertEquals(100.0f, offering.price)
        assertEquals(product, offering.product)
    }

    @Test
    fun testOfferingDTOToOffering_NotFoundException() {
        val productId = UUID.randomUUID()
        `when`(productRepository.findById(productId)).thenReturn(null)

        val createOfferingDTO = CreateOfferingDTO(OfferingStatus.ACTIVE, productId, 10, 100.0f)

        assertThrows(NotFoundException::class.java) {
            offeringService.offeringDTOToOffering(createOfferingDTO)
        }
    }

}