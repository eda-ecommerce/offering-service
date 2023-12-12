package org.eda.ecommerce.data.models.events

open class GenericEvent(type: String) {
    var source: String? = null
    var timestamp: Long? = null
    var type: String? = type

    init {
        this.source = "offering-service"
        this.timestamp = System.currentTimeMillis()
    }
}
