package org.hellasdirect.assignment.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import kotlin.test.Test

@SpringBootTest
@AutoConfigureMockMvc
class CustomerCommunicationApiControllerIT {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `validation should fail if email is not valid`() {
        // When I make a POST request to the endpoint
        val result = mockMvc.post("/api/v1/customer-communication") {
            contentType = MediaType.APPLICATION_JSON
            content = """
            {
              "policy_reference": "IUQWUU",
              "email": "invalid-email",
              "phone_number": "+306978378791",
              "policy_issued_date": "2024-11-23T13:46:53.497Z"
            }""".trimIndent()
        }

        // Then the response should be BAD REQUEST (400)
        result.andExpect {
            status { isBadRequest() }
            content { string("Invalid email format") }
        }
    }

    @Test
    fun `when all data is valid, a PENDING customer communications should be created`() {
        // When I make a POST request to the endpoint
        val result = mockMvc.post("/api/v1/customer-communication") {
            contentType = MediaType.APPLICATION_JSON
            content = """
            {
              "policy_reference": "IUQWUU",
              "email": "aris@test.com",
              "phone_number": "+30512111",
              "policy_issued_date": "2024-11-23T13:46:53.497Z"
            }""".trimIndent()
        }

        // Then the response should be BAD REQUEST (400)
        result.andExpect {
            status { isOk() }
            content { string("{\"policy_reference\":\"IUQWUU\",\"email\":\"aris@test.com\",\"phone_number\":\"+30512111\",\"policy_issued_date\":\"2024-11-23T13:46:53.497\",\"status\":\"PENDING\"}") }
        }
    }
}