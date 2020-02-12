package de.neonew.webssh

import com.beust.klaxon.Klaxon
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.mapNotNull
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.channel.Channel
import net.schmizz.sshj.connection.channel.direct.Session
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import kotlin.math.min

class Ssh(connectionString: SshConnectionString) : Closeable {

    private val sshClient: SSHClient = SSHClient()
    private val session: Session
    private val channel: Channel

    init {
        sshClient.addHostKeyVerifier(PromiscuousVerifier())
        sshClient.connect(connectionString.hostname, connectionString.port)

        connectionString.options?.password?.let { sshClient.authPassword(connectionString.username, it) }
                ?: connectionString.username.let { sshClient.authPublickey(it) }

        session = sshClient.startSession()
        session.allocateDefaultPTY()

        channel = connectionString.options?.command?.let { session.exec(it) } ?: session.startShell()
    }

    override fun close() {
        session.close()
        sshClient.disconnect()
    }

    suspend fun readCommand(incoming: ReceiveChannel<Frame>) {
        incoming.mapNotNull { it as? Frame.Text }.consumeEach { frame ->
            val text = frame.readText()
            text.forEach { char -> channel.outputStream.write(char.toInt()) }

            channel.outputStream.flush()
        }
    }

    suspend fun processOutput(outgoing: SendChannel<Frame>) {
        val data = ByteArray(1024)
        val bytesRead = readInputStreamWithTimeout(channel.inputStream, data, 2000)
        if (bytesRead > 0) {
            val text = String(data, 0, bytesRead)
            outgoing.send(Frame.Text(text))
        }
    }

    fun isActive(): Boolean = channel.isOpen

    @Throws(IOException::class)
    fun readInputStreamWithTimeout(inputStream: InputStream, buffer: ByteArray, timeoutMillis: Int): Int {
        var offset = 0
        val maxTimeMillis = System.currentTimeMillis() + timeoutMillis
        while (System.currentTimeMillis() < maxTimeMillis && offset < buffer.size) {
            val readLength = min(inputStream.available(), buffer.size - offset)

            if (readLength == 0) {
                break
            }

            // can alternatively use bufferedReader, guarded by isReady():
            val bytesRead = inputStream.read(buffer, offset, readLength)
            if (bytesRead == -1) {
                break
            }
            offset += bytesRead
        }
        return offset
    }
}

class SshConnectionString(connectionString: String) {

    val hostname: String
    val username: String?
    val port: Int = 22
    val options: Options?

    data class Options(val command: String? = null, val password: String? = null)

    private val regex = Regex("""^(([A-Za-z][A-Za-z0-9_]*)@)?([A-Za-z][A-Za-z0-9_.]*)(\{.*})?$""")

    init {
        val matchResult = regex.find(connectionString)!!
        username = matchResult.groups[2]?.value
        hostname = matchResult.groups[3]!!.value
        options = matchResult.groups[4]?.let { Klaxon().parse<Options>(it.value) }
    }

    override fun toString(): String {
        return "$username@$hostname:$port"
    }
}
