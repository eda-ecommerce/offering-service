package org.eda.ecommerce.data.models.events
open class GenericEvent(source: String, type: String) {
    var source: String? = source
    var timestamp: Long? = null
    var type: String? = type

    init {
        this.timestamp = System.currentTimeMillis()
    }
}
