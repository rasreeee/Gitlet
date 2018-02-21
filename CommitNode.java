package gitlet;

public class CommitNode {
    private Commit _commit;
    private Commit _parent;

    public CommitNode(Commit parent, Commit commit) {
         _commit = commit;
         _parent = parent;
    }

    /** Returns this node's commit. */
    Commit node() {
        return _commit;
    }

    /** Returns this commit's parent commit. */
    Commit parent() {
        return _parent;
    }

    /** Return if this commit is the initial commit. */
    boolean isInitial() {
        return _parent == null;
    }
}
