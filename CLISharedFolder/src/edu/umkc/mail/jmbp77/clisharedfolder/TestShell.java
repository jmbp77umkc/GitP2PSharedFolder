package edu.umkc.mail.jmbp77.clisharedfolder;

import org.apache.sshd.common.util.GenericUtils;
import org.apache.sshd.common.util.Int2IntFunction;
import org.apache.sshd.common.util.Readable;
import org.apache.sshd.common.util.buffer.Buffer;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.shell.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TestShell extends ProcessShell implements Runnable {

    boolean alive = false;
    int signal = 0;

    public TestShell(String... str) {
        super(str);
    }

    public TestShell(Collection<String> str) {
        super(str);
    }

    @Override
    public void start(Environment env) throws IOException {
//        Map varsMap = this.resolveShellEnvironment(env.getEnv());

//        for(int builder = 0; builder < this.command.size(); ++builder) {
//            String modes = (String)this.command.get(builder);
//            if("$USER".equals(modes)) {
//                modes = (String)varsMap.get("USER");
//                this.command.set(builder, modes);
//                this.cmdValue = GenericUtils.join(this.command, ' ');
//            }
//        }

//        ProcessBuilder var6 = new ProcessBuilder(this.command);
//        Map var7;
//        if(GenericUtils.size(varsMap) > 0) {
//            try {
//                var7 = var6.environment();
//                var7.putAll(varsMap);
//            } catch (Exception var5) {
//                this.log.warn("start() - Failed ({}) to set environment for command={}: {}", new Object[]{var5.getClass().getSimpleName(), this.cmdValue, var5.getMessage()});
//                if(this.log.isDebugEnabled()) {
//                    this.log.debug("start(" + this.cmdValue + ") failure details", var5);
//                }
//            }
//        }

//        if(this.log.isDebugEnabled()) {
//            this.log.debug("Starting shell with command: \'{}\' and env: {}", var6.command(), var6.environment());
//        }

//        this.process = var6.start();

//        var7 = this.resolveShellTtyOptions(env.getPtyModes());
//        this.out = new TtyFilterInputStream(this.process.getInputStream(), var7);
//        this.err = new TtyFilterInputStream(this.process.getErrorStream(), var7);
//        this.in = new TtyFilterOutputStream(this.process.getOutputStream(), this.err, var7);

        //how do I get io streams?!
        Buffer buf = new TestShell.Buffer();
        this.getServerSession().getIoSession().write(buf);
        System.out.println("TestShell.start()");
//        alive = true;
//        signal = 0;
//        new Thread(this).start();
    }

    @Override
    public void run() {
        boolean quitting = false;
        List<Byte> buf = new LinkedList<>();
        while (!quitting) {
            if (signal != 0) quitting = true;
            if (getOutputStream() == null) System.out.println("getOutputStream() == null");
            if (getInputStream() == null) System.out.println("getIbputStream() == null");
            try {
                if (getOutputStream().available() > 0) {
                    byte[] b = new byte[256];
                    int len = 0;
                    len = getOutputStream().read(b);
                    for (int i = 0; i < len; ++i) {
                        if (b[i] == '$') {
                            b[i] = '\n';
                            quitting = true;
                        }
                        buf.add(b[i]);
                        if (b[i] == '\n') {
                            byte[] wrbuf = new byte[buf.size()];
                            for (int j = 0; j < buf.size(); ++j)
                                wrbuf[j] = buf.get(j);
                            this.getInputStream().write(wrbuf);
                            getInputStream().flush();
                            buf.clear();
                        }
                    }
                }
            } catch (Exception e) {
                quitting = true;
                e.printStackTrace();
            }
        }

        alive = false;
    }

    @Override
    public boolean isAlive() {
        return alive;
    }

    @Override
    public int exitValue() {
        if (alive) return 1;
        return 0;
    }

    @Override
    public void destroy() {
        signal = 1;
    }

    public static class Factory extends ProcessShellFactory {

        public Factory() {
            super();
        }

        public Factory(String... str) {
            super(str);
        }

        @Override
        public Command create() {
            return new InvertedShellWrapper(new TestShell(this.getCommand()));
        }
    }

    public static class Buffer extends org.apache.sshd.common.util.buffer.Buffer {

        @Override
        public int rpos() {
            return 0;
        }

        @Override
        public void rpos(int i) {

        }

        @Override
        public int wpos() {
            return 0;
        }

        @Override
        public void wpos(int i) {

        }

        @Override
        public int capacity() {
            return 0;
        }

        @Override
        public byte[] array() {
            return new byte[0];
        }

        @Override
        public void compact() {

        }

        @Override
        public void clear() {

        }

        @Override
        public String getString(Charset charset) {
            return "Test Buffer\n";
        }

        @Override
        public int putBuffer(Readable readable, boolean b) {
            return 0;
        }

        @Override
        public void putRawBytes(byte[] bytes, int i, int i1) {

        }

        @Override
        public void ensureCapacity(int i, Int2IntFunction int2IntFunction) {

        }

        @Override
        protected int size() {
            return 0;
        }

        @Override
        public int available() {
            return 0;
        }

        @Override
        public void getRawBytes(byte[] bytes, int i, int i1) {

        }
    }

}
