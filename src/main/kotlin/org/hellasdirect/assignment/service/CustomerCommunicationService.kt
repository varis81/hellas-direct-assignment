package org.hellasdirect.assignment.service

import jakarta.transaction.Transactional
import org.hellasdirect.assignment.model.CommunicationStatus
import org.hellasdirect.assignment.model.toDomainObject
import org.hellasdirect.assignment.repository.CustomerCommunicationRepository
import org.hellasdirect.assignment.util.*
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime

@Service
class CustomerCommunicationService(
    private val customerCommunicationRepository: CustomerCommunicationRepository
): CustomerCommunicationInterface {

    @Transactional
    override fun createCustomerCommunication(amqpExchangeMessage: AMPQExchangeMessage): CustomerCommunication {
        // Initially the communication is created from the amqp message and has status PENDING
        val customerCommunication = amqpExchangeMessage.toCustomerCommunication()
        try {
            customerCommunicationRepository.save(customerCommunication.toEntity())
        } catch (e: DataIntegrityViolationException) {
            throw IllegalArgumentException("Failed to save customer communication due to data integrity violation: ${e.message}", e)
        } catch (e: Exception) {
            throw RuntimeException("Unexpected error occurred while saving customer communication", e)
        }

        // We should not log any PII data
        log.info("Customer communication for policy ${customerCommunication.policy_reference} saved")

        return customerCommunication
    }

    override fun listUnprocessedCustomerCommunicationsInChronologicalAscendingOrder(): List<CustomerCommunication> {
        // Unprocessed customer communications are listed. Exclude the delayed ones.
        // A delayed customer communication is one that is not processed with two days of policy issuance
        // We need to retrieve all PENDING customer communications that have policy_issued_date within the last two days
        val unprocessedCustomerCommunications =
            customerCommunicationRepository.findByStatus(CommunicationStatus.PENDING)
                .filter { !isCustomerCommunicationDelayed(it.toDomainObject().policy_issued_date) }
                .sortedBy { it.policy_issued_date } // Ascending

        log.info("Retrieved ${unprocessedCustomerCommunications.size} communications that are PENDING and not delayed")

        return unprocessedCustomerCommunications.map { it.toDomainObject() }
    }

    @Transactional
    override fun updateStatusOfCustomerCommunication(customerCommunicationId: Long, newStatus: CommunicationStatus) {
        val customerCommunication = customerCommunicationRepository.findById(customerCommunicationId)
            .orElseThrow {
                CustomerCommunicationDoesNotExistException("Customer communication with id $customerCommunicationId does not exist")
            }

        try {
            customerCommunicationRepository.save(customerCommunication.copy(status = newStatus))
        } catch (e: Exception) {
            throw RuntimeException("Unexpected error occurred while saving customer communication", e)
        }
    }

    override fun listUnansweredDelayedCustomerCommunications(): List<CustomerCommunication> {
        // We need the attempted communications (so, only those with NO_ANSWER) that are delayed
        val unansweredDelayedCommunications =
            customerCommunicationRepository.findByStatus(CommunicationStatus.NO_ANSWER)
                .filter { isCustomerCommunicationDelayed(it.toDomainObject().policy_issued_date) }

        log.info("Retrieved ${unansweredDelayedCommunications.size} communications that are NO_ANSWER and delayed")

        return unansweredDelayedCommunications.map { it.toDomainObject() }
    }

    private fun isCustomerCommunicationDelayed(policy_issued_date: LocalDateTime): Boolean {
        val duration = Duration.between(policy_issued_date, LocalDateTime.now())
        return duration.toDays() > 2
    }

    companion object {
        private val log = logger()
    }
}