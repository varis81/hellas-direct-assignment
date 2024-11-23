package org.hellasdirect.assignment

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class HellasDirectAssignmentApplication

fun main(args: Array<String>) {
    runApplication<HellasDirectAssignmentApplication>(*args)
}
