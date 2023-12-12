package org.eda.ecommerce.data.models.events

import org.eda.ecommerce.data.models.Product

class ProductEvent(type: String, var payload: Product) : GenericEvent(type)
