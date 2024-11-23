package org.hellasdirect.assignment.util

import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.hellasdirect.assignment.model.CommunicationStatus
import org.hellasdirect.assignment.model.CustomerCommunicationEntity
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

/**
 * Creates a logger for the containing class.
 * Usage:
 *      companion object {
 *          private val log = logger()
 *      }
 */
fun Any.logger(): Logger {
    val clazz = if (this::class.isCompanion) this::class.java.enclosingClass else this::class.java
    return LoggerFactory.getLogger(clazz)
}

data class CustomerCommunication(
    val id: Long = 0,
    val policy_reference: String,
    @Email(message = "Invalid email format")
    val email: String,
    val phone_number: String,
    val policy_issued_date: LocalDateTime,
    @Enumerated(EnumType.STRING)
    val status: CommunicationStatus,
)

fun CustomerCommunication.toEntity(): CustomerCommunicationEntity =
    CustomerCommunicationEntity(
        id = id,
        policy_reference = policy_reference,
        email = email,
        phone_number = phone_number,
        policy_issued_date = policy_issued_date,
        status = status,
    )

fun CustomerCommunication.toDto(): CustomerCommunicationDto =
    CustomerCommunicationDto(
        policy_reference = policy_reference,
        email = email,
        phone_number = phone_number,
        policy_issued_date = policy_issued_date,
        status = status,
    )

data class AMPQExchangeMessage(
    val policy_reference: String,
    @field:Email(message = "Invalid email format")
    val email: String,
    val phone_number: String,
    val policy_issued_date: LocalDateTime,
)

fun AMPQExchangeMessage.toCustomerCommunication(): CustomerCommunication =
    CustomerCommunication(
        policy_reference = policy_reference,
        email = email,
        phone_number = phone_number,
        policy_issued_date = policy_issued_date,
        status = CommunicationStatus.PENDING,
    )

data class CustomerCommunicationDto(
    val policy_reference: String,
    val email: String,
    val phone_number: String,
    val policy_issued_date: LocalDateTime,
    @Enumerated(EnumType.STRING)
    val status: CommunicationStatus,
)

class CustomerCommunicationDoesNotExistException(error: String) : RuntimeException(error)