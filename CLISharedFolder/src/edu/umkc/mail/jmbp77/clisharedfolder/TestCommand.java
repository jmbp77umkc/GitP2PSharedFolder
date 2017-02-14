package edu.umkc.mail.jmbp77.clisharedfolder;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by jorh on 2/13/17.
 */
public class TestCommand implements Command {
    InputStream in;
    OutputStream out;
    OutputStream err;
    String str;
    ExitCallback exit;

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
        while (in.available() > 0) {
            byte[] b = new byte[256];
            int i = in.read(b);
            out.write(b,0,i);
        }
        exit.onExit(0);
    }

    @Override
    public void destroy() throws Exception {

    }

    public static class Factory implements CommandFactory {

        @Override
        public Command createCommand(String s) {
            return new TestCommand(s);
        }
    }
}
