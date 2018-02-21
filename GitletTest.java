package gitlet;

import static org.junit.Assert.*;
import org.junit.Test;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

/** Tests the Gitlet functionality.
 * @author Lesley Chang. */
public class GitletTest {

    public GitletTest() {
    }

    @Test
    public void doubleinit() {
        Gitlet g = new Gitlet();
        g = new Gitlet();
    }

    @Test
    public void add() {
        Gitlet g = new Gitlet();
        String[] name1 = new String[1];
        String[] name2 = new String[1];
        name1[0] = "testFile1.txt";
        name2[0] = "wug.txt";
        g.add(name1);
        g.add(name2);
        HashMap<String, String> staged = g.staged();
        String sha1 = staged.get(name1[0]);
        String sha2 = staged.get(name2[0]);
        File file1 = new File(".gitlet/.staging/" + sha1);
        File file2 = new File(".gitlet/.staging/" + sha2);
        assertTrue(staged.containsKey(name1[0]));
        assertTrue(file1.exists());
        assertTrue(staged.containsKey(name2[0]));
        assertTrue(file2.exists());
    }

    @Test
    public void commit() {
        Gitlet g = new Gitlet();
        String[] name1 = new String[1];
        String[] name2 = new String[1];
        name1[0] = "testFile1.txt";
        name2[0] = "wug.txt";
        g.add(name1);
        g.add(name2);
        String[] message = new String[1];
        message[0] = "added testFile1 and wug";
        g.commit(message);
        assertTrue(g.staged().isEmpty());
        assertTrue(g.toRemove().isEmpty());
        HashMap<String, Commit> commits = g.commits();
        boolean isCommit = false;
        for (Object c : commits.keySet()) {
            Commit commit = commits.get(c);
            if (commit.message().equals(message[0])); {
                isCommit = true;
                break;
            }
        }
        assertTrue(isCommit);
    }

    @Test
    public void find() {
        Gitlet g = new Gitlet();
        Gitlet.store(g);
        String[] name = new String[1];
        String[] message = new String[1];
        File wug = new File("wug.txt");
        Utils.writeContents(wug, "is a wug");
        name[0] = "wug.txt";
        message[0] = "added wug";
        g.add(name);
        g.commit(message);

        Utils.writeContents(wug, "not a wug");

        message[0] = "edited wug";
        g.add(name);
        g.commit(message);

        Utils.writeContents(wug, "not a wug.");
        g.add(name);
        g.commit(message);

        String[] tofind = new String[1];
        tofind[0] = "edited wug";
        g.find(tofind);
    }

    @Test
    public void rm() {
        Gitlet g = new Gitlet();
        String[] name1 = new String[1];
        String[] name2 = new String[1];
        name1[0] = "testFile1.txt";
        name2[0] = "wug.txt";
        g.add(name1);
        g.add(name2);
        String[] message = new String[1];
        message[0] = "added testFile1 and wug";
        g.commit(message);
    }

    @Test
    public void checkout1() {
        Gitlet g = new Gitlet();
        String[] name1 = new String[1];
        String[] name2 = new String[1];
        name1[0] = "testFile1.txt";
        name2[0] = "wug.txt";
        g.add(name1);
        g.add(name2);
        String[] message = new String[1];
        message[0] = "added testFile1 and wug";
        g.commit(message);
    }

    @Test
    public void test2() {
        Gitlet g = new Gitlet();
        Gitlet.store(g);
        String[] name = new String[1];
        name[0] = "wug.txt";
        g.add(name);
        String[] message = new String[1];
        message[0] = "added wug";
        g.commit(message);
        String[] co1 = new String[2];
        co1[0] = "--";
        co1[1] = "wug.txt";
        g.checkout1(co1);
        File wug = new File("wug.txt");
        String wugContents = Utils.readContentsAsString(wug);
        assertTrue("This is a wug.".equals(wugContents));
    }

    @Test
    public void main2() {
        String[] arg = new String[1];
        arg[0] = "init";
        Main.main(arg);
        arg = new String[2];
        arg[0] = "add";
        arg[1] = "wug.txt";
        Main.main(arg);
    }

    @Test
    public void test4() {
        Gitlet g = new Gitlet();
        Gitlet.store(g);
        File wug = new File("wug.txt");
        Utils.writeContents(wug, "This is a wug.\n");
        String[] name1 = new String[1];
        name1[0] = "wug.txt";
        g.add(name1);
        String[] message1 = new String[1];
        message1[0] = "version 1 of wug";
        g.commit(message1);

        File notwug = new File("notwug.txt");
        String nwcontent = Utils.readContentsAsString(notwug);
        Utils.writeContents(wug, nwcontent);
        g.add(name1);
        String[] message2 = new String[1];
        message2[0] = "version 2 of wug";
        g.commit(message2);
        g.log();
    }

    @Test
    public void branch() {
        Gitlet g = new Gitlet();
        Gitlet.store(g);
        File wug = new File("wug.txt");
        Utils.writeContents(wug, "This is a wg\n");
        String[] name = new String[1];
        name[0] = "wug.txt";
        g.add(name);
        String[] message = new String[1];
        message[0] = "version 1 of wug";
        g.commit(message);
        Utils.writeContents(wug, "This is a wug\n");
        g.add(name);
        message[0] = "version 2 of wug";
        g.commit(message);
        Utils.writeContents(wug, "This is a wug.\n");
        g.add(name);
        message[0] = "version 3 of wug";
        g.commit(message);

        String[] cb = new String[1];
        cb[0] = "cool-beans";
        g.branch(cb);
        g.checkout3(cb);

        Utils.writeContents(wug, "This is not a wug.\n");
        g.add(name);
        message[0] = "version 4";
        g.commit(message);

        String[] m = new String[1];
        m[0] = "master";
        g.checkout3(m);

        Utils.writeContents(wug, "This is definitely a wug.\n");
        g.add(name);
        message[0] = "version 5";
        g.commit(message);

        assertTrue(g.branches().size() == 2);
        assertTrue(g.branches().containsKey("cool-beans"));
        assertTrue(g.branches().containsKey("master"));

        Commit cbcommit = g.branches().get("cool-beans").leaf();
        Commit mcommit = g.head();
        Commit cbparent = g.commits().get(cbcommit.parentID());
        Commit mparent = g.commits().get(mcommit.parentID());

        assertTrue(cbparent == mparent);
        assertTrue(mcommit.tracked().get("wug.txt").equals("This is definitely a wug.\n"));
        assertTrue(cbcommit.tracked().get("wug.txt").equals("This is not a wug.\n"));
        assertTrue(g.head() == mcommit);
    }

    @Test
    public void test() {

    }

}
