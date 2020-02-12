package de.neonew.webssh

import com.beust.klaxon.KlaxonException
import freemarker.cache.ClassTemplateLoader
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.UserHashedTableAuth
import io.ktor.auth.authenticate
import io.ktor.auth.basic
import io.ktor.features.CallLogging
import io.ktor.freemarker.FreeMarker
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.locations.Locations
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.getDigestFunction
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.schmizz.sshj.common.SSHException
import org.slf4j.LoggerFactory.getLogger
import org.slf4j.event.Level.INFO
import java.util.*

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::main).start(wait = true)
}

fun Application.main() {
    val logger = getLogger(javaClass.name)

    install(CallLogging) {
        level = INFO
    }
    install(Locations)
    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(Application::class.java.classLoader, "webapp/templates")
    }
    install(WebSockets)

    val users = UserHashedTableAuth(
            getDigestFunction("SHA-256") { "ktor" },
            mapOf("ssh" to Base64.getDecoder().decode("0relocFXy/kW5nBaQi3Thtf9OTTE8JWmzSvM7Swl8H0="))
    )
    install(Authentication) {
        basic { validate { users.authenticate(it) } }
    }

    routing {
        get("/") {
            call.respond(FreeMarkerContent("index.ftl", null))
        }

        static("static") {
            resources("webapp/css")
            resources("webapp/js")
        }

        authenticate {
            webSocket("/ssh/{connectionString}") {
                try {
                    val connectionString = SshConnectionString(call.parameters["connectionString"]!!)
                    logger.info("New client connected, connectionString: {}", connectionString)
                    // TODO: handle ClosedSendChannelException gracefully
                    Ssh(connectionString).use { ssh ->
                        val launch = launch {
                            ssh.readCommand(incoming)
                        }
                        while (launch.isActive) {
                            ssh.processOutput(outgoing)
                            flush()

                            if (!ssh.isActive()) {
                                break
                            }

                            delay(20)
                        }
                    }
                } catch (e: SSHException) {
                    outgoing.send(Frame.Text("Exception occurred: $e"))
                } catch (e: KlaxonException) {
                    outgoing.send(Frame.Text("Parameter parsing failed: $e"))
                }
            }
        }
    }
}
