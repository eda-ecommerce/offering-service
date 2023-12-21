package org.eda.ecommerce.data.models.events

import org.eda.ecommerce.data.models.Offering

class OfferingEvent(type: String, var content: Offering) : GenericEvent(type)
