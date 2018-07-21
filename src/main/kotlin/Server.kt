import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.http.ContentType
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.close
import io.ktor.http.cio.websocket.readText
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import org.slf4j.LoggerFactory.getLogger
import org.slf4j.event.Level.INFO


fun main(args: Array<String>) {
    embeddedServer(Netty, port = 8080, module = Application::main).start(wait = true)
}

fun Application.main() {
    val logger = getLogger(javaClass.name)

    install(CallLogging) {
        level = INFO
    }
    install(WebSockets)

    routing {
        get("/") {
            call.respondText("Hello World!", ContentType.Text.Plain)
        }

        webSocket("/ssh") {
            logger.info("New client connected")
            val hostname = readTextFrame(incoming)
            logger.info("hostname: {}", hostname)
            Ssh(hostname, "nuc")

            while (true) {
                val text = readTextFrame(incoming)
                outgoing.send(Frame.Text("YOU SAID: $text"))
                if (text.equals("bye", ignoreCase = true)) {
                    close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                }
            }
        }
    }
}

suspend fun readTextFrame(incoming: ReceiveChannel<Frame>): String {
    val frame = incoming.receive()
    when (frame) {
        is Frame.Text -> {
            return frame.readText()
        }
        else -> {
            throw IllegalStateException()
        }
    }
}
