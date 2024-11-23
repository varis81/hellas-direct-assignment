package org.hellasdirect.assignment.controller

import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.hellasdirect.assignment.service.CustomerCommunicationService
import org.hellasdirect.assignment.util.AMPQExchangeMessage
import org.hellasdirect.assignment.util.logger
import org.hellasdirect.assignment.util.toDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class CustomerCommunicationApiController(
    private val customerCommunicationService: CustomerCommunicationService
) {

    @PostMapping("/api/v1/customer-communication")
    @Operation(
        description = "Call this POST request to initiate a customer communication. " +
                "In the context of this project, this is to simulate how a consumer of an AMQP message would behave."
    )
    fun navigate(
        @Valid
        @RequestBody
        request: AMPQExchangeMessage
    ): ResponseEntity<*> {
        // We should make sure that we do not log any PII data
        log.info("AMQP message received: $request")
        val customerCommunication = customerCommunicationService.createCustomerCommunication(request)

        return ResponseEntity.ok().body(customerCommunication.toDto())
    }

    companion object {
        val log = logger()
    }
}