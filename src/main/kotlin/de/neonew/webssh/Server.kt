package de.neonew.webssh

import freemarker.cache.ClassTemplateLoader
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.content.resources
import io.ktor.content.static
import io.ktor.features.CallLogging
import io.ktor.freemarker.FreeMarker
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.http.cio.websocket.Frame
import io.ktor.locations.Locations
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import net.schmizz.sshj.common.SSHException
import org.slf4j.LoggerFactory.getLogger
import org.slf4j.event.Level.INFO
import kotlin.concurrent.thread

fun main(args: Array<String>) {
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

    routing {
        get("/") {
            call.respond(FreeMarkerContent("index.ftl", null))
        }

        static("static") {
            resources("webapp/css")
            resources("webapp/js")
        }

        webSocket("/ssh/{connectionString}") {
            val connectionString = SshConnectionString(call.parameters["connectionString"]!!)
            logger.info("New client connected, connectionString: {}", connectionString)
            try {
                Ssh(connectionString, incoming, outgoing).use { ssh ->
                    thread {
                        while (true) {
                            ssh.readCommand()
                            Thread.sleep(20)
                        }
                    }
                    while (true) {
                        ssh.processOutput()
                        flush()
                        Thread.sleep(20)
                    }
                }
            } catch (e: SSHException) {
                outgoing.send(Frame.Text("Exception occured: $e"))
            }
        }
    }
}
