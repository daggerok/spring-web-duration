package daggerok

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.TimeZone
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.error.ErrorAttributeOptions.Include.EXCEPTION
import org.springframework.boot.web.error.ErrorAttributeOptions.Include.MESSAGE
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@SpringBootApplication
class DurationServletSpringWebApplication

fun main(args: Array<String>) {
    runApplication<DurationServletSpringWebApplication>(*args) {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.from(ZoneOffset.UTC)))
        Locale.setDefault(Locale.US)
    }
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

    override fun getErrorAttributes(webRequest: WebRequest, options: ErrorAttributeOptions): MutableMap<String, Any> =
        super.getErrorAttributes(webRequest, options.including(MESSAGE, EXCEPTION)).apply {
            val baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
            val api = mapOf(
                "Create greeting => POST" to "$baseUrl name={name}",
                "example" to "http post $baseUrl name=Maksimko",
            )
            put("api", api)
        }
}

// @ControllerAdvice
// class GlobalControllerExceptionHandler {
//
//     // @Throws(Exception::class)
//     // @ExceptionHandler(Exception::class)
//     // fun handleHtmlError(request: HttpServletRequest, ex: Exception): ModelAndView {
//     //     val baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
//     //     val error = ex.message ?: "Unknown error"
//     //     return ModelAndView().apply {
//     //         addObject("exception", ex)
//     //         addObject("error", error)
//     //         addObject("path", request.requestURL)
//     //         addObject("api", "Create greeting: POST $baseUrl")
//     //         viewName = "error"
//     //     }
//     // }
// }
