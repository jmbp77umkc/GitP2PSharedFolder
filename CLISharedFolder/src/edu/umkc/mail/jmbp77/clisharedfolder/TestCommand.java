package edu.umkc.mail.jmbp77.clisharedfolder;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.shell.ProcessShellFactory;
import org.apache.sshd.server.subsystem.SubsystemFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by jorh on 2/13/17.
 */
public class TestCommand implements Command {
    InputStream in;
    OutputStream out;
    OutputStream err;
    String str;
    ExitCallback exit;

    public TestCommand() {

    }

    public TestCommand(String s) {
        str = s;
    }

    @Override
    public void setInputStream(InputStream inputStream) {
        in = inputStream;
    }

    @Override
    public void setOutputStream(OutputStream outputStream) {
        out = outputStream;
    }

    @Override
    public void setErrorStream(OutputStream outputStream) {
        err = outputStream;
    }

    @Override
    public void setExitCallback(ExitCallback exitCallback) {
        exit = exitCallback;
    }

    @Override
    public void start(Environment environment) throws IOException {
        //do stuff
        System.out.println(environment.getPtyModes().keySet().toString());
        System.out.println("inside");
        out.write("hullo\n".getBytes());

        byte[] b = new byte[256];
        int i = in.read(b);
        out.write(b,0,i);
        System.out.println("weh");
        exit.onExit(0);
    }

    @Override
    public void destroy() throws Exception {

    }

    public static class Factory implements CommandFactory {

        @Override
        public Command createCommand(String s) {
            System.out.println(s);
            return new TestCommand(s);
        }
    }

    public static class ProFactory extends ProcessShellFactory {
        public ProFactory() {
            super();
            setCommand("TestCommand");
        }

        public ProFactory(String... command) {
            super(command);
        }

        public ProFactory(List<String> ls) {
            super(ls);
        }

        @Override
        public Command create() {
            return new TestCommand();
        }
    }
}
