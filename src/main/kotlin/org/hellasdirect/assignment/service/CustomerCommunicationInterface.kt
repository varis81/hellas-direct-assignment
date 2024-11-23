package org.hellasdirect.assignment.service

import org.hellasdirect.assignment.model.CommunicationStatus
import org.hellasdirect.assignment.util.AMPQExchangeMessage
import org.hellasdirect.assignment.util.CustomerCommunication

interface CustomerCommunicationInterface {
    fun createCustomerCommunication(amqpExchangeMessage: AMPQExchangeMessage): CustomerCommunication

    fun updateStatusOfCustomerCommunication(customerCommunicationId: Long, newStatus: CommunicationStatus)

    fun listUnprocessedCustomerCommunicationsInChronologicalAscendingOrder(): List<CustomerCommunication>

    fun listUnansweredDelayedCustomerCommunications(): List<CustomerCommunication>
}