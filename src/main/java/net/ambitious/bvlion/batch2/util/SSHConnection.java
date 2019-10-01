package net.ambitious.bvlion.batch2.util;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.Setter;

import java.nio.charset.StandardCharsets;

class SSHConnection {
    private static final int LOCAl_PORT = 3307;
    private static final int REMOTE_PORT = 3306;

    private SSHConnection() {}

    private static final SSHConnection instance = new SSHConnection();

    static SSHConnection getInstance() {
        return instance;
    }

    static final String RSA_KEY_PATH = "id_rsa";
    static final String KNOWN_HOSTS_PATH = "known_hosts";
    @Setter
    private String sPassPhrase;
    @Setter
    private int sshRemotePort;
    @Setter
    private String sshUser;
    @Setter
    private String sshRemoteServer;
    @Setter
    private String mysqlRemoteServer;

    private Session session;

    void closeSSH() {
        if (session != null) {
            session.disconnect();
        }
    }

    void connectSSH() throws JSchException {
        JSch jsch = new JSch();
        jsch.setKnownHosts(KNOWN_HOSTS_PATH);
        jsch.addIdentity(RSA_KEY_PATH, sPassPhrase.getBytes(StandardCharsets.UTF_8));

        session = jsch.getSession(sshUser, sshRemoteServer, sshRemotePort);

        if (session != null) {
            session.connect();
            session.setPortForwardingL(LOCAl_PORT, mysqlRemoteServer, REMOTE_PORT);
        }
    }

    void reConnectSSH() throws JSchException {
        closeSSH();
        connectSSH();
    }
}
