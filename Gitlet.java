package gitlet;
import java.io.Serializable;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Arrays;
import java.util.ArrayList;

/** Gitlet class.
 * @author Lesley Chang*/
public class Gitlet implements Serializable {
    /** Maps branch names to their branch objects. */
    private HashMap<String, Branch> _branches;
    /** Maps names of staged files to their readContentsAsString values. */
    private HashMap<String, String> _staged;
    /** Maps IDs of commits to their objects. */
    private HashMap<String, Commit> _commits;
    /** Maps names of removed files to their readContentsAsString values. */
    private HashMap<String, String> _removed;
    /** Maps names of files to remove to their readContentsAsString values. */
    private HashMap<String, String> _toRemove;
    /** ArrayList of CommitNodes. */
    private ArrayList<CommitNode> _nodes;
    /** Pointer to the current branch. */
    private Branch master;
    /** Pointer to commit at the furthest end of MASTER. */
    private Commit head;
    /** Folder for staging area in .gitlet. */
    private File staging;
    /** Folder containing contents of removed files (names = file names). */
    private File removed;
    /** Directory .gitlet. */
    private File gitlet;
    /** Instantiates new Gitlet object with initial commit. */
    public Gitlet() {
        File git = new File(".gitlet");
        gitlet = new File(".gitlet");
        boolean isGitMade = gitlet.mkdir();
        staging = new File(gitlet, ".staging");
        boolean isSMade = staging.mkdirs();
        removed = new File(gitlet, ".removed");
        boolean isRMade = removed.mkdirs();
        _branches = new HashMap<>();
        _staged = new HashMap<>();
        _toRemove = new HashMap<>();
        _removed = new HashMap<>();
        _commits = new HashMap<>();
        Commit initial = new Commit();
        master = new Branch("master", initial);
        master.mkCurrent();
        head = master.leaf();
        _branches.put("master", master);
        _commits.put(initial.sha1Val(), initial);
    }

    /** Return _staged. */
    HashMap<String, String> staged() {
        return _staged;
    }

    /** Return _removed. */
    HashMap<String, String> removed() {
        return _removed;
    }

    /** Return _toRemove. */
    HashMap<String, String> toRemove() {
        return _toRemove;
    }

    /** Return _commits. */
    HashMap<String, Commit> commits() {
        return _commits;
    }

    /** Return _branches. */
    HashMap<String, Branch> branches() {
        return _branches;
    }

    /** Return current head commit. */
    Commit head() {
        return head;
    }

    /** Return current branch. */
    Branch current() {
        for (String name : _branches.keySet()) {
            Branch b = _branches.get(name);
            if (b.isCurrent()) {
                return b;
            }
        }
        return null;
    }

    /** Write G into memory upon init(). */
    static void store(Gitlet g) {
        File serialized = new File(".gitlet/serialized");
        try {
            ObjectOutputStream out =
                    new ObjectOutputStream(new FileOutputStream(serialized));
            out.writeObject(g);
            out.close();
        } catch (IOException excp) {
        }
    }

    /** Return Gitlet from memory upon exit. */
    static Gitlet retrieve() {
        Gitlet result = new Gitlet();
        File serialized = new File(".gitlet/serialized");
        try {
            ObjectInputStream in =
                    new ObjectInputStream(new FileInputStream(serialized));
            result = (Gitlet) in.readObject();
            in.close();
        } catch (IOException excp) {
            return null;
        } catch (ClassNotFoundException e) {
            return null;
        }
        return result;
    }

    /** ADD file in ARGS to be tracked. */
    void add(String[] args) {
        String name = args[0];
        File wFile = new File(name);
        if (!wFile.exists()) {
            System.out.println("File does not exist.");
        } else {
            byte[] newRead = Utils.readContents(wFile);
            String newContents = Utils.readContentsAsString(wFile);
            String newSHA = Utils.sha1(newContents);
            if (_staged.containsKey(name)) {
                if (_staged.containsValue(newContents)) {
                    return;
                } else {
                    String oldContents = _staged.get(name);
                    String oldSHA = Utils.sha1(oldContents);
                    File old = new File(".gitlet/.staging/" + oldSHA);
                    boolean isDelete = old.delete();
                    File newF = new File(".gitlet/.staging/" + newSHA);
                    Utils.writeContents(newF, newContents);
                    _staged.put(newSHA, newContents);
                }
            } else if (_toRemove.containsKey(name)) {
                _toRemove.remove(name);
            } else {
                String commitContents = head.tracked().get(name);
                if (commitContents != null
                        && commitContents.equals(newContents)) {
                    return;
                }
                File staged = new File(".gitlet/.staging/" + newSHA);
                Utils.writeContents(staged, newContents);
                _staged.put(name, newContents);
            }
        }
    }

    /** COMMIT all tracked files given in ARGS. */
    void commit(String[] args) {
        if (_staged.isEmpty() && _toRemove.isEmpty()) {
            System.out.println("No changes added to the commit.");
        } else {
            String message = args[0];
            HashMap<String, String> toTrack = new HashMap<>();
            toTrack.putAll(_staged);
            String parentSHA = head.sha1Val();
            for (String f : head.tracked().keySet()) {
                if (!toTrack.containsKey(f)) {
                    String fContents = head.tracked().get(f);
                    toTrack.put(f, fContents);
                } else if (_toRemove.containsKey(f)) {
                    toTrack.remove(f);
                    _removed.put(f, _toRemove.get(f));
                }
            }
            Date current = new Date();
            Commit newCommit = new Commit(toTrack, parentSHA, message, current);
            Commit parent = _commits.get(parentSHA);
            parent.addChild(newCommit.sha1Val());
            _commits.put(newCommit.sha1Val(), newCommit);
            Branch curr = current();
            curr.add(newCommit);
            head = curr.leaf();
            _staged.clear();
            _toRemove.clear();
        }
    }

    /** Unstage the given file in ARGS if it is currently staged.
     * Mark it to indicate that it is NOT to be included in next
     * commit. Remove the file from working directory if user has
     * not already done so. */
    void rm(String[] args) {
        String name = args[0];
        File toRemove = new File(name);
        if (!toRemove.exists()) {
            if (head.tracked().containsKey(name)) {
                String content = head.tracked().get(name);
                _toRemove.put(name, content);
                return;
            }
        }
        String fileContents = Utils.readContentsAsString(toRemove);
        if (_staged.containsKey(name)) {
            _staged.remove(name);
            if (head.tracked().containsKey(name)) {
                _toRemove.put(name, fileContents);
            }
        } else if (head.tracked().containsKey(name)) {
            if (toRemove.exists()) {
                toRemove.delete();
            }
            _toRemove.put(name, fileContents);
        } else {
            System.out.println("No reason to remove the file.");
        }
    }

    /** Display information about each commit backwards along commit
     * tree until initial commit in correct format. */
    void log() {
        Commit c = head;
        while (c != null && !c.message().equals("initial commit")) {
            System.out.println("===");
            System.out.println("commit " + c.sha1Val());
            System.out.println(c.timeString());
            System.out.println(c.message());
            System.out.println();
            String parent = c.parentID();
            c = _commits.get(parent);
        }
        System.out.println("===");
        System.out.println("commit" + c.sha1Val());
        System.out.println(c.timeString());
        System.out.println(c.message());
    }

    /** Like log, except about all commits ever created in whichever order. */
    void globalLog() {
        for (String id : _commits.keySet()) {
            Commit c = _commits.get(id);
            System.out.println("===");
            System.out.println("commit " + c.sha1Val());
            System.out.println(c.timeString());
            System.out.println(c.message());
            System.out.println();
        }
    }

    /** Print IDs of all commits with given commit message
     * (one per line) in ARGS. */
    void find(String[] args) {
        String message = args[0];
        boolean exists = false;
        if (_commits.isEmpty()) {
            System.out.println("Found no commit with that message.");
        } else {
            for (String id : _commits.keySet()) {
                String cm = _commits.get(id).message();
                if (cm.equals(message)) {
                    exists = true;
                    System.out.println(id);
                }
            }
            if (!exists) {
                System.out.println("Found no commit with that message.");
            }
        }
    }

    /** Display currently existing branches with current branch starred.
     * Also displays what files have been staged or marked for untracking. */
    void status() {
        System.out.println("=== Branches ===");
        String[] branchNames = new String[_branches.size()];
        int i = 0;
        for (String name : _branches.keySet()) {
            branchNames[i] = name;
            i++;
        }
        Arrays.sort(branchNames, 0, i);
        for (int j = 0; j < branchNames.length; j++) {
            if (branchNames[j].equals(current().name())) {
                System.out.println("*" + branchNames[j]);
            } else {
                System.out.println(branchNames[j]);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        String[] fileNames = new String[_staged.size()];
        int a = 0;
        for (String f : _staged.keySet()) {
            fileNames[a] = f;
            a++;
        }
        Arrays.sort(fileNames, 0, a);
        for (int b = 0; b < fileNames.length; b++) {
            System.out.println(fileNames[b]);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        String[] rNames = new String[_toRemove.size()];
        int d = 0;
        for (String r : _toRemove.keySet()) {
            rNames[d] = r;
            d++;
        }
        Arrays.sort(rNames, 0, rNames.length);
        for (int e = 0; e < rNames.length; e++) {
            System.out.println(rNames[e]);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    /** Put version of file given in ARGS from HEAD into working directory,
     * overwrite if it already exists. New version of file is not staged. */
    void checkout1(String[] args) {
        String name = args[1];
        if (!head.tracked().containsKey(name)) {
            System.out.println("File does not exist in that commit.");
        } else {
            String headContents = head.tracked().get(name);
            File old = new File(name);
            Utils.writeContents(old, headContents);
        }
    }

    /** Take the version of the file in commit with the given ID in
     * ARGS and put it in the working directory, overwriting if already there. New version
     * of file is not staged. */
    void checkout2(String[] args) {
        String commitID = args[0];
        if (!_commits.containsKey(commitID)) {
            System.out.println("No commit with that id exists.");
        } else {
            Commit commit = _commits.get(commitID);
            if (!commit.tracked().containsKey(commitID)) {
                System.out.println("File does not exist in that commit.");
                return;
            }
            String fileName = args[2];
            File oldF = new File(fileName);
            if (oldF.exists()) {
                String newSHA = commit.tracked().get(fileName);
                Utils.writeContents(oldF, newSHA);
            } else {
                try {
                    boolean isCreated = oldF.createNewFile();
                } catch (IOException e) {
                    return;
                }
                String newSHA = commit.tracked().get(fileName);
                Utils.writeContents(oldF, newSHA);
            }
        }
    }

    /** Takes all files in the commit at the head of the given branch in ARGS,
     * and puts them in the working directory, overwriting the versions of the files that
     * are already there if they exist. */
    void checkout3(String[] args) {
        String name = args[0];
        if (!_branches.containsKey(name)) {
            System.out.println("No such branch exists.");
        } else if (_branches.get(name).equals(current().name())) {
            System.out.println("No need to checkout the current branch.");
        } else {
            Branch branch = _branches.get(name);
            Commit newHead = branch.leaf();
            HashMap<String, String> currTracked = head.tracked();
            HashMap<String, String> newTracked = newHead.tracked();
            for (String t : newTracked.keySet()) {
                if (!currTracked.containsKey(t)) {
                    File working = new File(t);
                    if (working.exists()) {
                        System.out.println("There is an untracked file in the way; " +
                                "delete it or add it first.");
                        return;
                    }
                }
            }
            for (String f : newTracked.keySet()) {
                File file = new File(f);
                String content = newTracked.get(f);
                if (file.exists()) {
                    if (!currTracked.containsKey(f)) {
                        boolean deleted = file.delete();
                    } else {
                        Utils.writeContents(file, content);
                    }
                } else {
                    try {
                        boolean created = file.createNewFile();
                    } catch (IOException e) {
                        return;
                    }
                    Utils.writeContents(file, content);
                }
            }
            Branch old = current();
            branch.mkCurrent();
            old.unCurrent();
            head = current().leaf();
            _staged.clear();
        }
    }

    /** Create new branch with given name. Its pointer should point
     * to the current head node. Default branch called "master". */
    void branch(String[] args) {
        String name = args[0];
        if (_branches.containsKey(name)) {
            System.out.println("A branch with that name already exists.");
        } else {
            Branch b = new Branch(name, head);
            _branches.put(name, b);
        }
    }

    /** Delete branch with given branch name. Delete only
     * the pointer associated, not all commits under it. */
    void rmBranch(String[] args) {
        String name = args[0];
        if (!_branches.containsKey(name)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        Branch given = _branches.get(name);
        if (given == current()) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        _branches.remove(name, given);
        given.delete();
    }

    /** Checkout all tracked files in given commit, and remove tracked
     * files not present in that commit. Also move current branch's head
     * to that commit node. Clear the staging area. */
    void reset(String[] args) {
        String name = args[0];
        if (!_commits.containsKey(name)) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Branch currBranch = current();
        HashMap<String, String> currTracked = currBranch.leaf().tracked();
        File proj3 = new File("repo/proj3");
        File[] files = proj3.listFiles();
        for (int i = 0; i < files.length; i++) {
            String contents = Utils.readContentsAsString(files[i]);
            String fname = files[i].getName();
            if (!currTracked.containsKey(name)
                    && !currTracked.containsValue(contents)) {
                System.out.println("There is an untracked file " +
                        "in the way; delete it or add it first.");
                return;
            }
        }
        Commit c = _commits.get(name);
        HashMap<String, String> cTracked = c.tracked();
        for (String i : currTracked.keySet()) {
            if (!cTracked.containsKey(i)) {
                File deleted = new File(i);
                if (deleted.exists()) {
                    deleted.delete();
                }
            }
        }
        currBranch.add(c);
        _staged.clear();
        _toRemove.clear();
    }

    /** MERGE the files from given branch into the current one. */
    void merge(String[] args) {
        String name = args[0];
        if (!_branches.containsKey(name)) {
            System.out.println("A branch with that name does not exist.");
            return;
        } else if (!_staged.isEmpty() || !_toRemove.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return;
        } else if (_branches.get(name) == current()) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        Branch given = _branches.get(name);
        Commit sp = split(given);
        if (sp == given.leaf()) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }
        if (sp == current().leaf()) {
            current().add(given.leaf());
            System.out.println("Current branch fast-forwarded.");
            return;
        }
        if (sp != null) {

        }
    }

    /** Return split point's commit of the given branch and the current one. */
    Commit split(Branch b) {
        Commit current = head;
        Commit given = b.leaf();
        ArrayList<String> cAncestry = new ArrayList<>();
        while (current != null) {
            cAncestry.add(current.parentID());
            current = _commits.get(current.parentID());
        }
        while (given != null) {
            if (cAncestry.contains(given.parentID())) {
                return _commits.get(given.parentID());
            }
            given = _commits.get(given.parentID());
        }
        return null;
    }
}
