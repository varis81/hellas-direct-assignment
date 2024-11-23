package org.hellasdirect.assignment.repository

import org.hellasdirect.assignment.model.CommunicationStatus
import org.hellasdirect.assignment.model.CustomerCommunicationEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CustomerCommunicationRepository : JpaRepository<CustomerCommunicationEntity, Long> {
    fun findByStatus(status: CommunicationStatus): List<CustomerCommunicationEntity>
}