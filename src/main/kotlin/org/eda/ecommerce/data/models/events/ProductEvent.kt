package org.eda.ecommerce.data.models.events

import org.eda.ecommerce.data.models.Product

class ProductEvent(source: String, type: String, var payload: Product) : GenericEvent(source, type)
