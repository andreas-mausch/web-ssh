import io.ktor.http.cio.websocket.Frame
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.channel.direct.Session
import net.schmizz.sshj.transport.verification.ConsoleKnownHostsVerifier
import net.schmizz.sshj.transport.verification.OpenSSHKnownHosts
import java.io.File

class Ssh(hostname: String, username: String) {

    private val shell: Session.Shell

    init {
        val sshClient = SSHClient()
        val knownHosts = File(OpenSSHKnownHosts.detectSSHDir(), "known_hosts")
        sshClient.addHostKeyVerifier(ConsoleKnownHostsVerifier(knownHosts, System.console()))
        sshClient.connect(hostname)
        sshClient.authPublickey(username)
        val session = sshClient.startSession()
        session.allocateDefaultPTY()
        shell = session.startShell()
    }
}
