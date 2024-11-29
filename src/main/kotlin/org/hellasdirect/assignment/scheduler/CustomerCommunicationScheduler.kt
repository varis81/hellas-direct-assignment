package org.hellasdirect.assignment.scheduler

import org.hellasdirect.assignment.model.CommunicationStatus
import org.hellasdirect.assignment.service.CustomerCommunicationService
import org.hellasdirect.assignment.util.CustomerCommunication
import org.hellasdirect.assignment.util.logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class CustomerCommunicationScheduler(
    private val customerCommunicationService: CustomerCommunicationService,
) {

    @Scheduled(cron = "0 0 22 * * ?", zone = "Europe/Athens")
    fun runWelcomeEmailScheduler() {
        log.info("Email sending scheduler executed at: ${LocalDateTime.now()}")
        // we need to use distributed locking here to make sure that we do not send the emails twice
        val customerCommunications = customerCommunicationService.listUnansweredDelayedCustomerCommunications()

        customerCommunications.forEach {
            sendEmail(it)
            customerCommunicationService.updateStatusOfCustomerCommunication(it.id, CommunicationStatus.EMAIL_SENT)
        }
    }

    // That should normally go to a service - keeping it here for simplicity
    private fun sendEmail(customerCommunication: CustomerCommunication) {
        log.info("Welcome email sent for customer ${customerCommunication.policy_reference} at: ${LocalDateTime.now()}")
    }

    companion object {
        private val log = logger()
    }
}