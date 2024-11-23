package org.hellasdirect.assignment.repository

import org.assertj.core.api.Assertions.assertThat
import org.hellasdirect.assignment.model.CommunicationStatus
import org.hellasdirect.assignment.model.CustomerCommunicationEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrNull
import kotlin.test.Test

@DataJpaTest
class CustomerCommunicationRepositoryIT {

    @Autowired
    private lateinit var customerCommunicationRepository: CustomerCommunicationRepository

    @Test
    fun `test save and retrieve customer communication`() {
        val customerCommunication = createCustomerCommunicationEntity()
        val savedEntity = customerCommunicationRepository.save(customerCommunication)

        val retrievedEntity = customerCommunicationRepository.findById(savedEntity.id).getOrNull()

        assertThat(retrievedEntity).isEqualTo(savedEntity)
    }

    @Test
    fun `test save and retrieve multiple communication entities by status`() {
        val customerCommunication = createCustomerCommunicationEntity(CommunicationStatus.NO_ANSWER)
        customerCommunicationRepository.save(customerCommunication)

        val customerCommunication1 = createCustomerCommunicationEntity(CommunicationStatus.NO_ANSWER)
        customerCommunicationRepository.save(customerCommunication1)

        val retrievedEntities = customerCommunicationRepository.findByStatus(CommunicationStatus.NO_ANSWER)

        assertThat(retrievedEntities.size).isEqualTo(2)
    }

    private fun createCustomerCommunicationEntity(status: CommunicationStatus = CommunicationStatus.PENDING) =
        CustomerCommunicationEntity(
            policy_reference = "IJASK2K",
            email = "aris@test.com",
            phone_number = "+49512145522",
            status = status,
            policy_issued_date = LocalDateTime.now()
        )
}