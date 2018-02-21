package gitlet;

import java.util.Date;
import java.io.Serializable;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/** Commit class.
 * @author Lesley Chang */
public class Commit implements Serializable {
    /** SHA1 ID of this commit's parent. */
    private String _parentID;
    /** Commit message for this commit. */
    private String _message;
    /** Time and date this commit was made. */
    private Date _timeStamp;
    /** Return ArrayList of children's IDs. */
    private ArrayList<String> _children;
    /** Maps tracked file names to their readContentsAsString values. */
    private HashMap<String, String> _tracked;
    /** Instantiates initial commit for new Gitlet object. */
    public Commit() {
        _parentID = "";
        _message = "initial commit";
        _timeStamp = null;
        _tracked = new HashMap<>();
        _children = new ArrayList<>();
    }

    /** Instantiates new commit with PARENT commit and its
     * log MESSAGE and its commit DATE. Include the PARENTID of its
     * parent node and it's HashMap of TRACKED files. */
    public Commit(HashMap<String, String> tracked, String parentID,
                  String message, Date date) {
        _parentID = parentID;
        _message = message;
        _timeStamp = date;
        _tracked = new HashMap<>();
        _tracked.putAll(tracked);
        _children = new ArrayList<>();
    }

    /** Return ArrayList of children. */
    ArrayList<String> children() {
        return _children;
    }

    /** Add given ID of child to _children. */
    void addChild(String id) {
        _children.add(id);
    }

    /** Return if _children contains given ID. */
    boolean isRelated(String id) {
        return _children.contains(id);
    }

    /** Returns this commit's parent1 node SHA. */
    String parentID() {
        return _parentID;
    }

    /** Returns this commit's log message. */
    String message() {
        return _message;
    }

    /** Returns this commit's date. */
    Date time() {
        return _timeStamp;
    }

    /** Returns the _timeStamp's string form. */
    String timeString() {
        if (_timeStamp == null) {
            return "Date: Thu Jan 1 00:00:00 1970 -0800";
        }
        SimpleDateFormat sdf =
                new SimpleDateFormat("EEE MMM d HH:mm:SS yyyy Z");
        String s = sdf.format(_timeStamp);
        return "Date: " + s;
    }

    /** Returns this commit's HashMap of tracked files. */
    HashMap<String, String> tracked() {
        return _tracked;
    }

    /** Return true if this commit is the root. In other words. */
    boolean hasParent() {
        return !_message.equals("initial commit");
    }

    /** Returns this commit's SHA1 value. */
    String sha1Val() {
        String result = "";
        StringBuilder sb = new StringBuilder();
        for (String key : _tracked.keySet()) {
            sb.append(_tracked.get(key));
        }
        result = sb.toString();
        result += _parentID + _message + timeString();
        return Utils.sha1(result);
    }
}
