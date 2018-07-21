package de.neonew.webssh

import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.channels.mapNotNull
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.channel.direct.Session
import net.schmizz.sshj.transport.verification.ConsoleKnownHostsVerifier
import net.schmizz.sshj.transport.verification.OpenSSHKnownHosts
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.io.InputStream


class Ssh(connectionString: SshConnectionString) : Closeable {

    private val sshClient: SSHClient = SSHClient()
    private val session: Session
    private val shell: Session.Shell

    init {
        val knownHosts = File(OpenSSHKnownHosts.detectSSHDir(), "known_hosts")
        sshClient.addHostKeyVerifier(ConsoleKnownHostsVerifier(knownHosts, System.console()))
        sshClient.connect(connectionString.hostname, connectionString.port)
        connectionString.username.let { sshClient.authPublickey(it) }
        session = sshClient.startSession()
        session.allocateDefaultPTY()
        shell = session.startShell()
    }

    override fun close() {
        session.close()
        sshClient.disconnect()
    }

    suspend fun readCommand(incoming: ReceiveChannel<Frame>) {
        incoming.mapNotNull { it as? Frame.Text }.consumeEach { frame ->
            val text = frame.readText()

            shell.outputStream.write(text.toInt())
            shell.outputStream.flush()
        }
    }

    suspend fun processOutput(outgoing: SendChannel<Frame>) {
        val data = ByteArray(1024)
        val bytesRead = readInputStreamWithTimeout(shell.inputStream, data, 2000)
        if (bytesRead > 0) {
            val text = String(data, 0, bytesRead)
            outgoing.send(Frame.Text(text))
        }
    }

    @Throws(IOException::class)
    fun readInputStreamWithTimeout(inputStream: InputStream, buffer: ByteArray, timeoutMillis: Int): Int {
        var offset = 0
        val maxTimeMillis = System.currentTimeMillis() + timeoutMillis
        while (System.currentTimeMillis() < maxTimeMillis && offset < buffer.size) {
            val readLength = java.lang.Math.min(inputStream.available(), buffer.size - offset)

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

    private val regex = Regex("^([A-Za-z][A-Za-z0-9_]*)(@([A-Za-z][A-Za-z0-9_.]*))?\$")

    init {
        val matchResult = regex.find(connectionString)!!
        hostname = matchResult.groups[1]!!.value
        username = matchResult.groups[3]?.value
    }

    override fun toString(): String {
        return "$username@$hostname:$port"
    }
}
