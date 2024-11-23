package org.hellasdirect.assignment.model

import jakarta.persistence.*
import jakarta.validation.constraints.Email
import org.hellasdirect.assignment.util.CustomerCommunication
import java.time.LocalDateTime

@Entity
@Table(name = "customer_communication")
data class CustomerCommunicationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val policy_reference: String,

    @Email(message = "Invalid email format")
    val email: String,

    val phone_number: String,

    val policy_issued_date: LocalDateTime,

    @Enumerated(EnumType.STRING)
    val status: CommunicationStatus,
)

enum class CommunicationStatus {
    PENDING, NO_ANSWER, ANSWERED, EMAIL_SENT
}

fun CustomerCommunicationEntity.toDomainObject(): CustomerCommunication =
    CustomerCommunication(
        id = id,
        policy_reference = policy_reference,
        email = email,
        phone_number = phone_number,
        policy_issued_date = policy_issued_date,
        status = status,
    )