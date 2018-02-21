package gitlet;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Lesley Chang
 */
import java.io.File;
/** Main class.
 * @author Lesley Chang */
public class Main {
    /** Gitlet object to run Main method on. */
    private static Gitlet gitlet;
    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        if (args.length == 0) {
            noCommand();
        } else {
            if (args[0].equals("init")) {
                if (args.length > 1) {
                    operandError();
                } else {
                    File git = new File(".gitlet");
                    if (git.exists()) {
                        System.out.println("A Gitlet version-control system already exists in the"
                                + "current directory.");
                    } else {
                        gitlet = new Gitlet();
                        Gitlet.store(gitlet);
                    }
                }
            } else if (args[0].equals("add")) {
                if (args.length != 2) {
                    operandError();
                } else if (!isInitialized()) {
                    initError();
                } else {
                    gitlet = Gitlet.retrieve();
                    String[] op = new String[1];
                    op[0] = args[1];
                    gitlet.add(op);
                    Gitlet.store(gitlet);
                }
            } else if (args[0].equals("commit")) {
                if (args.length == 1 || args[1].equals("")) {
                    noMessage();
                } else if (args.length != 2) {
                    operandError();
                } else if (!isInitialized()) {
                    initError();
                } else {
                    gitlet = Gitlet.retrieve();
                    String[] op = new String[1];
                    op[0] = args[1];
                    gitlet.commit(op);
                    Gitlet.store(gitlet);
                }
            } else if (args[0].equals("rm")) {
                if (args.length != 2) {
                    operandError();
                } else if (!isInitialized()) {
                    initError();
                } else {
                    gitlet = Gitlet.retrieve();
                    String[] op = new String[1];
                    op[0] = args[1];
                    gitlet.rm(op);
                    Gitlet.store(gitlet);
                }
            } else {
                main2(args);
            }
        }
    }

    /** Main method helper because the one above was too long
     * for StyleCheck with ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main2(String... args) {
        if (args[0].equals("log")) {
            if (args.length > 1) {
                operandError();
            } else if (!isInitialized()) {
                initError();
            } else {
                gitlet = Gitlet.retrieve();
                gitlet.log();
                Gitlet.store(gitlet);
            }
        } else if (args[0].equals("global-log")) {
            if (args.length > 1) {
                operandError();
            } else if (!isInitialized()) {
                initError();
            } else {
                gitlet = Gitlet.retrieve();
                gitlet.globalLog();
                Gitlet.store(gitlet);
            }
        } else if (args[0].equals("find")) {
            if (args.length != 2) {
                operandError();
            } else if (!isInitialized()) {
                initError();
            } else {
                gitlet = Gitlet.retrieve();
                String[] op = new String[1];
                op[0] = args[1];
                gitlet.find(op);
                Gitlet.store(gitlet);
            }
        } else if (args[0].equals("status")) {
            if (args.length > 1) {
                operandError();
            } else if (!isInitialized()) {
                initError();
            } else {
                gitlet = Gitlet.retrieve();
                gitlet.status();
                Gitlet.store(gitlet);
            }
        } else {
            main3(args);
        }
    }

    /** Continues main method for the third time because of Style
     * with ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main3(String... args) {
        if (args[0].equals("checkout")) {
            String[] oper = new String[args.length - 1];
            if (!isInitialized()) {
                initError();
            } else if (oper.length == 2) {
                oper[0] = args[1];
                oper[1] = args[2];
                if (!oper[0].equals("--")) {
                    operandError();
                }
                gitlet = Gitlet.retrieve();
                gitlet.checkout1(oper);
                Gitlet.store(gitlet);
            } else if (oper.length == 3) {
                oper[0] = args[1];
                oper[1] = args[2];
                oper[2] = args[3];
                if (!oper[1].equals("--")) {
                    operandError();
                }
                gitlet = Gitlet.retrieve();
                gitlet.checkout2(oper);
                Gitlet.store(gitlet);
            } else if (oper.length == 1) {
                oper[0] = args[1];
                gitlet = Gitlet.retrieve();
                gitlet.checkout3(oper);
                Gitlet.store(gitlet);
            } else {
                operandError();
            }
        } else if (args[0].equals("branch")) {
            if (args.length != 2) {
                operandError();
            } else if (!isInitialized()) {
                initError();
            } else {
                gitlet = Gitlet.retrieve();
                String[] op = new String[1];
                op[0] = args[1];
                gitlet.branch(op);
                Gitlet.store(gitlet);
            }
        } else if (args[0].equals("rm-branch")) {
            if (args.length != 2) {
                operandError();
            } else if (!isInitialized()) {
                initError();
            } else {
                gitlet = Gitlet.retrieve();
                String[] op = new String[1];
                op[0] = args[1];
                gitlet.rmBranch(op);
                Gitlet.store(gitlet);
            }
        } else {
            main4(args);
        }
    }

    /** Final continuation of main method due to Style
     * with ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main4(String... args) {
         if (args[0].equals("reset")) {
            if (args.length != 2) {
                operandError();
            } else if (!isInitialized()) {
                initError();
            } else {
                gitlet = Gitlet.retrieve();
                String[] op = new String[1];
                op[0] = args[1];
                gitlet.reset(op);
                Gitlet.store(gitlet);
            }
        } else if (args[0].equals("merge")) {
            if (args.length != 2) {
                operandError();
            } else if (!isInitialized()) {
                initError();
            } else {
                gitlet = Gitlet.retrieve();
                String[] op = new String[1];
                op[0] = args[1];
                gitlet.merge(op);
                Gitlet.store(gitlet);
            }
        } else {
            commandError();
        }
    }
     /** Returns true if there exists .gitlet. */
    static boolean isInitialized() {
        File g = new File(".gitlet");
        return g.exists();
    }

    /** Prints error stating to enter command. */
    static void noCommand() {
        System.out.println("Please enter a command.");
        System.exit(0);
    }

    /** Prints error stating incorrect input operands. */
    static void operandError() {
        System.out.println("Incorrect operands.");
        System.exit(0);
    }

    /** Prints error stating incorrect input command. */
    static void commandError() {
        System.out.println("No command with that name exists.");
        System.exit(0);
    }

    /** Prints error stating gitlet hasn't been initialized yet. */
    static void initError() {
        System.out.println("Not in an initialized Gitlet directory.");
    }

    /** Prints error that there was no message with commit command. */
    static void noMessage() {
        System.out.println("Please enter a commit message.");
    }
}

