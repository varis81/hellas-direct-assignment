# Customer communication management service

### Overview

This is an interview assignment in the context of the hiring process of Hellas Direct. In the context of the project, we assume that we have an insurance system that issues and
manages policies. The business need is that we want to manage welcome calls, performed by a team of customer care agents for new customers. This is a service that listens for PolicyIssued events and
creates customer communication entries that are then used by the customer care agents to call the customers.
The customer communication entries can have one of the following statuses: PENDING, NO_ANSWER, ANSWERED, EMAIL_SENT

Use cases that we want to support:
* Create a new record when a PolicyIssued event is consumed. The events are received through a message bus - here an AMQP exchange. The initial status is PENDING.
* List all non processed welcome calls in chronological order, do not include the delayed ones. A delayed customer communication is one that remains PENDING i.e. not called within two days of policy issuance.
* Update the status of a customer communication record.
* Avoid calling the same customer twice. This is achieved by updating the status of a record to NO_ANSWER or ANSWERED when a call is made. Thus the list functionality above only fetches PENDING of the last two days.
* Every night, we should send welcome emails to the clients that did not answer the call. So only delayed customer communications with status NO_ANSWER.

How things are built:
* H2 was used as a DB for this project. Schema is in the schema.sql file
* Authentication and authorization is out of scope for this project
* The main service is called CustomerCommunicationService. The consumer of the PolicyIssued event, should call the method of this service in order to achieve the use cases listed below. There is a sample API controller on this project that simulates the receipt of a PolicyIssued event - it then creates a customer communication with status PENDING.
* The consumer of the CustomerCommunicationService - probably a UI - can use the method listUnprocessedCustomerCommunicationsInChronologicalAscendingOrder (should be exposed through an API). This method returns all the customers that should be called - starting from the oldest one.
* Once a call is made, the method updateStatusOfCustomerCommunication (again exposed through an API endpoint) should be used in order to update the status of the customer communication to NO_ANSWER or ANSWERED.
* We avoid calling the same customer twice since the listUnprocessedCustomerCommunicationsInChronologicalAscendingOrder only fetches PENDING customer communications.
* There is a scheduler that runs at 10pm local time (this is configurable). This scheduler uses the method listUnansweredDelayedCustomerCommunications that fetches all entities with status NO_ANSWER and sends an email - here simulated by a log. It then updates the status to EMAIL_SENT.

Open for discussion:
* How should a failure on the scheduler update method handled? The customer should not get a duplicate email on the retry. Maybe use an idempotency key for the email server?
* Error handling in general. What happens on failure to write on the DB? Should the AMQP message be retried?

### Tech stack

* Kotlin 1.9.25
* Spring boot 3.3.4
* Java 21

Gradle is used as the build tool.

### How to run

In order to run the application, import it on Intellij IDEA (or the IDE of choice), create a Run Configuration and run it.

Alternatively, from the command line, you can run it as follows:

`./gradlew bootRun`

Another way would be to create the jar and then run it directly with java 21:

`./gradlew bootJar` -- this will create the jar in build/libs

`java -jar build/libs/hellas-direct-assignment-1.0.0.jar`

All these ways will start the application locally. There is Swagger configured, so you can access it on port 8080:

[Swagger link](http://localhost:8080/swagger-ui/index.html)

Alternatively, a request to the POST /customer-communication endpoint can be done as follows using curl:

`curl -X POST http://localhost:8080/api/v1/customer-communication 
  -H 'accept: */*' 
  -H 'Content-Type: application/json' 
  -d '{
          "policy_reference": "IUQWUU",
          "email": "aris@test.com",
          "phone_number": "+3083777299",
          "policy_issued_date": "2024-11-23T13:46:53.497Z"
       }`

### Notes

* No fields should be unique on the db because we might send communications to the same customer multiple times. This service can be used for other kind of communication, not just welcome emails - we should handle statuses correctly though.
* Validations are done in order to ensure that the input is valid in general. Here I only validated the email in order to show how to do it.
* We should also validate the phone numbers - although they should be coming from another service and we should trust that validation happened there.
* Distributed locking should be used on the scheduler in order to make sure that we are not sending the emails more than once

#### Testing
There are integration tests for all main classes, the controller, service and repository.
