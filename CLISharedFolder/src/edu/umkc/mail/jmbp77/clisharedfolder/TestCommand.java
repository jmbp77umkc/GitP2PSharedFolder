package edu.umkc.mail.jmbp77.clisharedfolder;

import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.shell.ProcessShellFactory;
import org.apache.sshd.server.subsystem.SubsystemFactory;

import java.io.*;
import java.util.List;

/**
 * By example: http://javajdk.net/tutorial/apache-mina-sshd-sshserver-example/
 */
public class TestCommand implements Command, Runnable {


    //ANSI escape sequences for formatting purposes
    public static final String ANSI_LOCAL_ECHO = "\u001B[12l";
    public static final String ANSI_NEWLINE_CRLF = "\u001B[20h";

    public static final String ANSI_RESET = "\u001B[0m";

    public static final String ANSI_BLACK = "\u001B[0;30m";
    public static final String ANSI_RED = "\u001B[0;31m";
    public static final String ANSI_GREEN = "\u001B[0;32m";
    public static final String ANSI_YELLOW = "\u001B[0;33m";
    public static final String ANSI_BLUE = "\u001B[0;34m";
    public static final String ANSI_PURPLE = "\u001B[0;35m";
    public static final String ANSI_CYAN = "\u001B[0;36m";
    public static final String ANSI_WHITE = "\u001B[0;37m";


    //IO streams for communication with the client
    private InputStream is;
    private OutputStream os;

    //Environment stuff
    @SuppressWarnings("unused")
    private Environment environment;
    private ExitCallback callback;

    private Thread sshThread;

    @Override
    public void start(Environment env) throws IOException {
        //must start new thread to free up the input stream
        environment = env;
        sshThread = new Thread(this, "EchoShell");
        sshThread.start();
    }

    @Override
    public void run() {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        //Make sure local echo is on (because password turned it off
        try {
            os.write(
                    (ANSI_LOCAL_ECHO + ANSI_NEWLINE_CRLF)
                            .getBytes());
            os.flush();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        try {

            boolean exit = false;
            String text;
            while (!exit) {
                text = br.readLine();
                if (text == null) {
                    exit = true;
                } else {
                    os.write((ANSI_GREEN + text + ANSI_RESET + "\r\n").getBytes());
                    os.flush();
                    if ("exit".equals(text)) {
                        exit = true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            callback.onExit(0);
        }
    }

    @Override
    public void destroy() throws Exception {
        sshThread.interrupt();
    }

    @Override
    public void setErrorStream(OutputStream errOS) {
    }

    @Override
    public void setExitCallback(ExitCallback ec) {
        callback = ec;
    }

    @Override
    public void setInputStream(InputStream is) {
        this.is = is;
    }

    @Override
    public void setOutputStream(OutputStream os) {
        this.os = os;
    }

    public static class TestFactory implements CommandFactory, Factory<Command> {

        @Override
        public Command createCommand(String command) {
            return new TestCommand();
        }

        @Override
        public Command create() {
            return createCommand("none");
        }
    }
}
