import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.connection.channel.direct.Session
import net.schmizz.sshj.transport.verification.ConsoleKnownHostsVerifier
import net.schmizz.sshj.transport.verification.OpenSSHKnownHosts
import java.io.File

class Ssh(connectionString: SshConnectionString) {

    private val shell: Session.Shell

    init {
        val sshClient = SSHClient()
        val knownHosts = File(OpenSSHKnownHosts.detectSSHDir(), "known_hosts")
        sshClient.addHostKeyVerifier(ConsoleKnownHostsVerifier(knownHosts, System.console()))
        sshClient.connect(connectionString.hostname, connectionString.port)
        connectionString.username?.let { sshClient.authPublickey(it) }
        val session = sshClient.startSession()
        session.allocateDefaultPTY()
        shell = session.startShell()
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
}
