package daggerok

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.error.ErrorAttributeOptions.Include.EXCEPTION
import org.springframework.boot.web.error.ErrorAttributeOptions.Include.MESSAGE
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.server.ServerRequest

@SpringBootApplication
class DurationReactiveWebfluxApplication

fun main(args: Array<String>) {
    runApplication<DurationReactiveWebfluxApplication>(*args)
}

data class GreetDTO(
    val name: String = "",
    val createdAt: Instant = Instant.now(),
    val greetDate: LocalDate = LocalDate.now(),
    val startTime: LocalTime = LocalTime.MIN,
    val duration: Duration = Duration.ofDays(1),
    val endTime: LocalTime =
        if (duration != Duration.ofDays(1)) startTime.plus(duration.toMillis(), ChronoUnit.MILLIS)
        else LocalTime.MAX,
)

data class GreetDocument(
    val greeting: String = "Hello!",
    val createdAt: Instant,
    val greetingDate: LocalDate,
    val startTime: LocalTime,
    val duration: Duration,
    val endTime: LocalTime,
)

@RestController
class ServletResource {

    @PostMapping
    fun greet(@RequestBody request: GreetDTO): GreetDocument =
        GreetDocument(
            greeting = request.name.ifBlank { "Hello, $this!" },
            createdAt = request.createdAt,
            greetingDate = request.greetDate,
            startTime = request.startTime,
            duration = request.duration,
            endTime = request.endTime,
        )
}

/**
 * This component customizes error response.
 *
 * Adds `api` map with supported endpoints
 */
@Component
class RestApiErrorAttributes : DefaultErrorAttributes() {

    override fun getErrorAttributes(request: ServerRequest?, options: ErrorAttributeOptions): MutableMap<String, Any> =
        super.getErrorAttributes(request, options.including(MESSAGE, EXCEPTION)).apply {
            val baseUrl = request?.uri()?.let { "${it.scheme}://${it.authority}" } ?: ""
            val api = mapOf(
                "Create greeting => POST" to "$baseUrl name={name}",
                "example" to "http post $baseUrl name=Maksimko",
            )
            put("api", api)
        }
}
