package org.hellasdirect.assignment.service

import jakarta.transaction.Transactional
import org.hellasdirect.assignment.model.CommunicationStatus
import org.hellasdirect.assignment.model.CustomerCommunicationEntity
import org.hellasdirect.assignment.repository.CustomerCommunicationRepository
import org.hellasdirect.assignment.util.AMPQExchangeMessage
import org.hellasdirect.assignment.util.CustomerCommunicationDoesNotExistException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertNotNull
import org.junit.jupiter.api.assertThrows

@SpringBootTest // Loads the full Spring context
@Transactional // Ensures each test runs in its own transaction, rolled back after test
class CustomerCommunicationServiceIT {

    @Autowired
    private lateinit var customerCommunicationService: CustomerCommunicationService

    @Autowired
    private lateinit var customerCommunicationRepository: CustomerCommunicationRepository

    // Helper method to create a test communication
    private fun createTestCommunication(status: CommunicationStatus, daysBefore: Long = 1): CustomerCommunicationEntity {
        val communication = CustomerCommunicationEntity(
            policy_reference = "AHSIUJ",
            email = "test@hellasdirect.org",
            phone_number = "1234567890",
            policy_issued_date = LocalDateTime.now().minusDays(daysBefore),
            status = status,
        )
        return customerCommunicationRepository.save(communication)
    }

    @Test
    fun `test create customer communication`() {
        // Arrange
        val amqpMessage = AMPQExchangeMessage(
            policy_reference = "AHSIUJ",
            email = "test@hellasdirect.org",
            phone_number = "1234567890",
            policy_issued_date = LocalDateTime.now().minusDays(1),
        )

        // Act: Create the customer communication
        val customerCommunication = customerCommunicationService.createCustomerCommunication(amqpMessage)

        // Assert: Verify the customer communication was created
        assertEquals(CommunicationStatus.PENDING, customerCommunication.status)

        val retrieved = customerCommunicationRepository.findById(customerCommunication.id)
        assertNotNull(retrieved)
        assertEquals(CommunicationStatus.PENDING, customerCommunication.status)
    }

    @Test
    fun `test list unprocessed customer communications`() {
        // Arrange: Save a few unprocessed communications
        createTestCommunication(CommunicationStatus.PENDING,1)
        createTestCommunication(CommunicationStatus.PENDING,2)
        createTestCommunication(CommunicationStatus.ANSWERED)
        createTestCommunication(CommunicationStatus.PENDING, 4)

        // Act: Retrieve unprocessed communications
        val unprocessedCommunications = customerCommunicationService.listUnprocessedCustomerCommunicationsInChronologicalAscendingOrder()

        // Assert: Verify the communications are returned
        assertEquals(unprocessedCommunications.size, 2)
        assertTrue(unprocessedCommunications.get(0).policy_issued_date < unprocessedCommunications.get(1).policy_issued_date)
    }

    @Test
    fun `test update status of customer communication`() {
        // Arrange: Create a customer communication with PENDING status
        val communication = createTestCommunication(CommunicationStatus.PENDING)

        // Act: Update the status
        customerCommunicationService.updateStatusOfCustomerCommunication(communication.id, CommunicationStatus.ANSWERED)

        // Assert: Verify the status has been updated
        val updatedCommunication = customerCommunicationRepository.findById(communication.id).orElseThrow()
        assertEquals(CommunicationStatus.ANSWERED, updatedCommunication.status)
    }

    @Test
    fun `test update status throws exception when communication not found`() {
        val exception = assertThrows<CustomerCommunicationDoesNotExistException> {
            customerCommunicationService.updateStatusOfCustomerCommunication(999L, CommunicationStatus.ANSWERED)
        }

        assertEquals("Customer communication with id 999 does not exist", exception.message)
    }

    @Test
    fun `test list unanswered delayed customer communications`() {
        // Arrange: Create some communications
        createTestCommunication(CommunicationStatus.NO_ANSWER, 3)
        createTestCommunication(CommunicationStatus.NO_ANSWER, 4)
        createTestCommunication(CommunicationStatus.ANSWERED, 4)
        createTestCommunication(CommunicationStatus.EMAIL_SENT, 3)
        createTestCommunication(CommunicationStatus.PENDING, 3)


        // Act: Retrieve unanswered delayed communications
        val unansweredDelayed = customerCommunicationService.listUnansweredDelayedCustomerCommunications()

        // Assert: Verify that only the unansered delayed ones are returned
        assertEquals(unansweredDelayed.size, 2)
        assertEquals(unansweredDelayed[0].status, CommunicationStatus.NO_ANSWER)
    }
}