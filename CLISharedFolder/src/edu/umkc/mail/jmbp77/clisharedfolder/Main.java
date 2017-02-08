package edu.umkc.mail.jmbp77.clisharedfolder;

import org.eclipse.jgit.api.Git;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;

public class Main {


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
            workdir = new File(workuri);
            gitdir = new File(gituri);
        } catch (Exception e) {
            System.err.println("Error loading config.json because:\n");
            e.printStackTrace();
            return;
        }
        Git git;
        try {
            git = Git.init().setDirectory(workdir.getCanonicalFile()).setGitDir(gitdir.getCanonicalFile()).call();
            git.commit();
        } catch (Exception e) {
            System.err.println("Error testing git because:\n");
            e.printStackTrace();
            return;
        }
        System.out.println("Program exiting at the end like I wanted it to do.");
    }
}
