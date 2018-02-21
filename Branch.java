package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.ArrayList;
/** Branch class containing all of its commits. *
 * @author Lesley Chang. */
public class Branch implements Serializable {
    /** Name of branch. */
    private String _name;
    /** Leaf commit on the furthest part of branch. */
    private Commit _leaf;
    /** Indicates whether this branch is the current one. */
    private boolean _isCurrent;
    /** New branch instance with leaf commit C and NAME. */
    public Branch(String name, Commit c) {
        _name = name;
        _leaf = c;
        _isCurrent = false;
    }

    /** Returns name of this branch. */
    String name() {
        return _name;
    }

    /** Returns leaf commit of this branch. */
    Commit leaf() {
        return _leaf;
    }

    /** Returns whether this branch is the current one. */
    boolean isCurrent() {
        return _isCurrent;
    }

    /** Makes this branch the current branch. */
    void mkCurrent() {
        _isCurrent = true;
    }

    /** Undo mkCurrent(). */
    void unCurrent() {
        _isCurrent = false;
    }

    /** Adds commit to the furthest end of this branch making
     * _leaf its parent commit and re-pointing _leaf to this
     * new commit C. */
    void add(Commit c) {
        _leaf = c;
    }

    /** Delete this Branch pointer. */
    void delete() {
        _leaf = null;
        _name = null;
    }
}
