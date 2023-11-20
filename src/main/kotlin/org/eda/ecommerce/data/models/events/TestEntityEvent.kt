package org.eda.ecommerce.data.models.events

import org.eda.ecommerce.data.models.TestEntity

class TestEntityEvent(source: String, type: String, var payload: TestEntity) : GenericEvent(source, type)
