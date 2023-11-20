package org.eda.ecommerce.data.models.events

import org.eda.ecommerce.data.models.Offering

class OfferingEvent(source: String, type: String, var payload: Offering) : GenericEvent(source, type)
