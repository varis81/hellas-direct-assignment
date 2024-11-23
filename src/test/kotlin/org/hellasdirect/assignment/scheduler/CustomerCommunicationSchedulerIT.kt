package org.hellasdirect.assignment.scheduler

import org.hellasdirect.assignment.model.CommunicationStatus
import org.hellasdirect.assignment.model.CustomerCommunicationEntity
import org.hellasdirect.assignment.repository.CustomerCommunicationRepository
import org.junit.jupiter.api.Assertions.assertEquals

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import kotlin.test.Test

@SpringBootTest
class CustomerCommunicationSchedulerIT {

    @Autowired
    private lateinit var customerCommunicationScheduler: CustomerCommunicationScheduler

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
    fun `test email is sent only for unanswered delayed customer communications`() {
        // Arrange: Create some communications
        val communication1 = createTestCommunication(CommunicationStatus.NO_ANSWER, 3)
        val communication2 = createTestCommunication(CommunicationStatus.NO_ANSWER, 4)
        createTestCommunication(CommunicationStatus.ANSWERED, 4)
        createTestCommunication(CommunicationStatus.EMAIL_SENT, 3)
        createTestCommunication(CommunicationStatus.PENDING, 3)


        // Act: Send email to the unaswered delayed ones
        customerCommunicationScheduler.runWelcomeEmailScheduler()

        // Assert: Verify that the status has changed
        val emailSentCommunications = customerCommunicationRepository.findByStatus(CommunicationStatus.EMAIL_SENT)
        assertEquals(emailSentCommunications.size, 3)

        val retrieved1 = customerCommunicationRepository.findById(communication1.id).orElseThrow()
        assertEquals(retrieved1.status, CommunicationStatus.EMAIL_SENT)

        val retrieved2 = customerCommunicationRepository.findById(communication2.id).orElseThrow()
        assertEquals(retrieved2.status, CommunicationStatus.EMAIL_SENT)
    }
}