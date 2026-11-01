import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

/**
 * The PGE build script, written in plain Java so it works the same way on
 * macOS, Linux and Windows. Run it from the PGE folder:
 *
 *   java Build.java          compile the engine and every game into out/
 *   java Build.java demo     compile everything, then run the game in games/demo
 *
 * Each game is a folder in games/ with a Main.java (the class with main) and,
 * optionally, an assets/ folder. Games run with their own folder as the working
 * directory, so they can load files as "assets/hero.png".
 */
public class Build {

    static final int REQUIRED_JAVA = 25;

    public static void main(String[] args) throws IOException, InterruptedException {
        checkJavaVersion();
        if (!new File("engine/src").isDirectory()) {
            fail("Run this from the PGE folder (the one containing Build.java).");
        }

        deleteFolder(new File("out"));

        System.out.println("Compiling engine...");
        compile(new File("engine/src"), null, new File("out/engine"));
        copyResources(new File("engine/src"), new File("out/engine"));

        File[] games = new File("games").listFiles();
        if (games != null) {
            for (File game : games) {
                if (game.isDirectory()) {
                    System.out.println("Compiling game '" + game.getName() + "'...");
                    compile(game, "out/engine", new File("out/games/" + game.getName()));
                }
            }
        }
        System.out.println("Build OK.");

        if (args.length > 0) {
            run(args[0]);
        }
    }

    /** Compiles every .java file under sourceFolder into outputFolder. */
    static void compile(File sourceFolder, String classPath, File outputFolder) {
        List<String> sources = new ArrayList<>();
        findFiles(sourceFolder, ".java", sources);
        if (sources.isEmpty()) {
            System.out.println("  (no .java files in " + sourceFolder + ", skipped)");
            return;
        }

        List<String> arguments = new ArrayList<>();
        arguments.add("--release");
        arguments.add(String.valueOf(REQUIRED_JAVA));
        if (classPath == null) {
            arguments.add("-Xlint:all");     // the engine: every warning
        } else {
            // Games: every warning except "this-escape", which complains about calling
            // methods like setName(...) in a constructor. That is fine in game code.
            arguments.add("-Xlint:all,-this-escape");
        }
        arguments.add("-encoding");
        arguments.add("UTF-8");
        arguments.add("-d");
        arguments.add(outputFolder.getPath());
        if (classPath != null) {
            arguments.add("-cp");
            arguments.add(classPath);
        }
        arguments.addAll(sources);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        int result = compiler.run(null, null, null, arguments.toArray(new String[0]));
        if (result != 0) {
            fail("Compilation failed, see the errors above.");
        }
    }

    /** Starts a game in its own Java process and waits for it to finish. */
    static void run(String gameName) throws IOException, InterruptedException {
        File gameFolder = new File("games/" + gameName);
        if (!new File("out/games/" + gameName + "/Main.class").isFile()) {
            fail("No game called '" + gameName + "' with a Main class. Games are folders in games/.");
        }
        String java = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        String classPath = new File("out/engine").getAbsolutePath()
                + File.pathSeparator + new File("out/games/" + gameName).getAbsolutePath();

        System.out.println("Running '" + gameName + "'...");
        ProcessBuilder process = new ProcessBuilder(java, "-cp", classPath, "Main");
        process.directory(gameFolder);
        process.inheritIO();
        int exitCode = process.start().waitFor();
        System.exit(exitCode);
    }

    static void checkJavaVersion() {
        int version = Runtime.version().feature();
        if (version < REQUIRED_JAVA) {
            fail("PGE needs Java " + REQUIRED_JAVA + " or newer, but this is Java " + version + ".\n"
                    + "If you use SDKMAN, run:  sdk env install   (this reads the .sdkmanrc file)");
        }
        if (ToolProvider.getSystemJavaCompiler() == null) {
            fail("This Java has no compiler. Install a full JDK, not just a JRE.");
        }
    }

    /** Adds every file under folder whose name ends with the given ending. */
    static void findFiles(File folder, String ending, List<String> found) {
        File[] children = folder.listFiles();
        if (children == null) {
            return;
        }
        for (File child : children) {
            if (child.isDirectory()) {
                findFiles(child, ending, found);
            } else if (child.getName().endsWith(ending)) {
                found.add(child.getPath());
            }
        }
    }

    /** Copies non-.java files (images, fonts...) next to the compiled classes. */
    static void copyResources(File sourceFolder, File outputFolder) throws IOException {
        File[] children = sourceFolder.listFiles();
        if (children == null) {
            return;
        }
        for (File child : children) {
            File target = new File(outputFolder, child.getName());
            if (child.isDirectory()) {
                copyResources(child, target);
            } else if (!child.getName().endsWith(".java")) {
                target.getParentFile().mkdirs();
                Files.copy(child.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    static void deleteFolder(File folder) {
        File[] children = folder.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteFolder(child);
            }
        }
        folder.delete();
    }

    static void fail(String message) {
        System.err.println("ERROR: " + message);
        System.exit(1);
    }
}
