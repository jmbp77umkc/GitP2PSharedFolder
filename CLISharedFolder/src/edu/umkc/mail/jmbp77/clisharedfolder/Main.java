package edu.umkc.mail.jmbp77.clisharedfolder;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.future.SshFutureListener;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.common.util.io.NoCloseInputStream;
import org.apache.sshd.common.util.io.NoCloseOutputStream;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.AcceptAllPasswordAuthenticator;
import org.apache.sshd.server.auth.pubkey.AcceptAllPublickeyAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.json.JSONObject;

import java.io.*;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

public class Main {

    //
    static int noChangeInterval = 20;
    static int changeInterval = 5;

    public static void main(String[] args) {
        File workdir;
        File gitdir;
        try {
            //load config from a json file so I don't need to hardcode crap for testing
            //be sure to set working directory in your ide
            File configFile = new File(args[0]);
            FileInputStream fin = new FileInputStream(configFile);
            String configText = "";
            int c;
            //screw efficiency
            while ( (c = fin.read()) != -1 ) {
                configText += (char)c;
            }
            JSONObject configJSON = new JSONObject(configText);
            String workuri = configJSON.optString("workdir", "");
            String gituri = configJSON.optString("gitdir","");
            noChangeInterval = configJSON.optInt("noChangeInterval",noChangeInterval);
            changeInterval = configJSON.optInt("changeInterval",changeInterval);
            workdir = new File(workuri);
            gitdir = new File(gituri);
        } catch (Exception e) {
            System.err.println("Error loading config.json because:\n");
            e.printStackTrace();
            return;
        }
        Git git;
        try {
//            git = Git.init().setDirectory(workdir.getCanonicalFile()).setGitDir(gitdir.getCanonicalFile()).call();
//            git.commit();
            git = Git.open(gitdir);
            //test finding changes at a couple different intervals
//            for (int i=0; i<3; ++i) {
//                Thread.sleep(1000*noChangeInterval);
//                boolean changes;
//                while (changes = commitChanges(git, "No message")) {
//                    System.out.println("Changes have been made. Waiting for more.");
//                    Thread.sleep(1000*changeInterval);
//                }
//                if (changes) System.out.println("Sending changes.");
//                else System.out.println("No change.");
//            }
        } catch (Exception e) {
            System.err.println("Error testing git because:\n");
            e.printStackTrace();
            return;
        }

        try {
            SshServer sshd = SshServer.setUpDefaultServer();
            sshd.setPort(8888);
            SimpleGeneratorHostKeyProvider keygen = new SimpleGeneratorHostKeyProvider(new File("hostkey.ser"));
            keygen.setAlgorithm(KeyPairProvider.SSH_RSA);
            sshd.setKeyPairProvider(keygen);
            sshd.setPasswordAuthenticator(AcceptAllPasswordAuthenticator.INSTANCE);
            sshd.setShellFactory(new TestCommand.TestFactory());
            System.out.println("starting sshd");
            sshd.start();
//            Thread.sleep(240*1000);






//by example http://stackoverflow.com/questions/8490293/sshd-java-example#26615119
//  with changes to make it compatible with the current api
            SshClient client = SshClient.setUpDefaultClient();
            client.addPasswordIdentity("password");
            client.start();
            ConnectFuture connf = client.connect("bob", "localhost", 8888);
            connf.await();
            final ClientSession session = connf.getSession();
            Set<ClientSession.ClientSessionEvent> authStates = new LinkedHashSet<>();
            authStates.add(ClientSession.ClientSessionEvent.WAIT_AUTH);
            while (authStates.contains(ClientSession.ClientSessionEvent.WAIT_AUTH)) {

                System.out.println("authenticating...");
                AuthFuture authFuture = session.auth();
                authFuture.addListener(new SshFutureListener<AuthFuture>()
                {
                    @Override
                    public void operationComplete(AuthFuture arg0)
                    {
                        System.out.println("Authentication completed with " + ( arg0.isSuccess() ? "success" : "failure"));
                    }
                });

                authFuture.await();

                Collection<ClientSession.ClientSessionEvent> astates = new LinkedList<>();
                astates.add(ClientSession.ClientSessionEvent.WAIT_AUTH);
                astates.add(ClientSession.ClientSessionEvent.CLOSED);
                astates.add(ClientSession.ClientSessionEvent.AUTHED);

                authStates = session.waitFor(astates,-1);
            }

            if (authStates == null || authStates.contains(ClientSession.ClientSessionEvent.CLOSED)) {
                System.err.println("error");
                System.exit(-1);
            }

            ClientChannel channel = session.createShellChannel();
            channel.setOut(new NoCloseOutputStream(System.out)); //this'll be the stream that the server replies with
            channel.setErr(new NoCloseOutputStream(System.err));
            channel.open();

            executeCommand(channel, "hello ");
            executeCommand(channel, " world!");
            executeCommand(channel, "exit");


            Collection<ClientChannelEvent> cevents = new LinkedList<>();
            cevents.add(ClientChannelEvent.CLOSED);
            channel.waitFor(cevents, 0);

            session.close(false);
            client.stop();






            sshd.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }


        System.out.println("Program exiting at the end like I wanted it to do.");
    }

    public static boolean commitChanges(Git git, String message) {
        try {
            Status stat = git.status().call();
            if (stat.isClean()) return false;
            Set<String> files;
            //add untracked
            files = stat.getUntracked();
            for (String str : files) {
                git.add().addFilepattern(str).call();
            }
            //remove missing
            files = stat.getMissing();
            for (String str : files) {
                git.rm().addFilepattern(str).call();
            }
            //add uncommitted changes
            files = stat.getUncommittedChanges();
            for (String str : files) {
                git.add().addFilepattern(str).call();
            }
            //commit
            git.commit().setMessage(message).call();
            return true;
        } catch (Exception e) {
            System.err.println("Error finding and commiting changes because:\n");
            e.printStackTrace();
            return false;
        }
    }

    private static void executeCommand(final ClientChannel channel, final String command) throws IOException
    {
        final InputStream commandInput = new ByteArrayInputStream(command.getBytes());
        channel.setIn(new NoCloseInputStream(commandInput)); //this is how the client sends data to the server
    }
}
