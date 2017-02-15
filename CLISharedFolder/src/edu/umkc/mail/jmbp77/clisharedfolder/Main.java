package edu.umkc.mail.jmbp77.clisharedfolder;

import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.AcceptAllPasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.shell.ProcessShellFactory;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
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
//            sshd.setShellFactory(new TestCommand.ProFactory("TestCommand"));
//            sshd.setShellFactory(new ProcessShellFactory("/bin/sh"));
            sshd.setShellFactory(new TestShell.Factory("TestShell"));
            System.out.println("starting sshd");
            sshd.start();
            Thread.sleep(240*1000);
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
}
