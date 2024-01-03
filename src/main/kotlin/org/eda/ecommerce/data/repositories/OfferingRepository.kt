package org.eda.ecommerce.data.repositories

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import org.eda.ecommerce.data.models.Offering
import java.util.*

@ApplicationScoped
class OfferingRepository : PanacheRepositoryBase<Offering, UUID>
