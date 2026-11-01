import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

/**
 * The PGE build script, written in plain Java so it works the same way on
 * macOS, Linux and Windows. Run it from the PGE folder:
 *
 *   java Build.java                compile the engine and every game into out/
 *   java Build.java demo           compile everything, then run the game in games/demo
 *   java Build.java package demo   compile everything, then make dist/demo/ and dist/demo.zip:
 *                                  the game as one .jar file, its assets, and start scripts,
 *                                  ready to hand in or share
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

        if (args.length == 2 && args[0].equals("package")) {
            packageGame(args[1]);
        } else if (args.length == 1) {
            run(args[0]);
        } else if (args.length > 2) {
            fail("Usage: java Build.java [game]   or   java Build.java package <game>");
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

    // ====================================================================== packaging

    /**
     * Makes dist/<game>/ with:
     *   <game>.jar   the engine and the game's classes in one file
     *   assets/      a copy of the game's assets
     *   run.sh, run.bat, README.txt
     * and zips that folder into dist/<game>.zip.
     */
    static void packageGame(String gameName) throws IOException {
        File gameFolder = new File("games/" + gameName);
        if (!new File("out/games/" + gameName + "/Main.class").isFile()) {
            fail("No game called '" + gameName + "' with a Main class. Games are folders in games/.");
        }
        File distFolder = new File("dist/" + gameName);
        deleteFolder(distFolder);
        distFolder.mkdirs();

        // A .jar is a zip file with a "manifest" inside that says which class has main().
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "Main");
        File jarFile = new File(distFolder, gameName + ".jar");
        JarOutputStream jar = new JarOutputStream(new FileOutputStream(jarFile), manifest);
        addToZip(jar, new File("out/engine"), "");
        addToZip(jar, new File("out/games/" + gameName), "");
        jar.close();

        File assets = new File(gameFolder, "assets");
        if (assets.isDirectory()) {
            copyResources(assets, new File(distFolder, "assets"));
        }
        writeLines(new File(distFolder, "run.sh"),
                "#!/bin/sh",
                "# Starts the game. The game must run from this folder to find its assets.",
                "cd \"$(dirname \"$0\")\"",
                "java -jar " + gameName + ".jar");
        new File(distFolder, "run.sh").setExecutable(true);
        writeLines(new File(distFolder, "run.bat"),
                "@echo off",
                "rem Starts the game. The game must run from this folder to find its assets.",
                "cd /d \"%~dp0\"",
                "java -jar " + gameName + ".jar");
        writeLines(new File(distFolder, "README.txt"),
                gameName + ", made with PGE (Pinto's Game Engine)",
                "",
                "Needs Java " + REQUIRED_JAVA + " or newer.",
                "",
                "To play:",
                "  macOS / Linux:  sh run.sh",
                "  Windows:        run.bat",
                "  or, in a terminal in this folder:  java -jar " + gameName + ".jar");

        File zipFile = new File("dist/" + gameName + ".zip");
        ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(zipFile));
        addToZip(zip, distFolder, gameName + "/");
        zip.close();

        System.out.println("Packaged into " + distFolder.getPath() + "/ and " + zipFile.getPath());
    }

    /** Adds every file under folder to the zip (or jar), with names starting with prefix. */
    static void addToZip(ZipOutputStream zip, File folder, String prefix) throws IOException {
        File[] children = folder.listFiles();
        if (children == null) {
            return;
        }
        for (File child : children) {
            // Zip files always use "/" between folder names, on every system.
            String name = prefix + child.getName();
            if (child.isDirectory()) {
                addToZip(zip, child, name + "/");
            } else {
                zip.putNextEntry(new ZipEntry(name));
                Files.copy(child.toPath(), zip);
                zip.closeEntry();
            }
        }
    }

    static void writeLines(File file, String... lines) throws IOException {
        PrintWriter out = new PrintWriter(file);
        for (String line : lines) {
            out.println(line);
        }
        out.close();
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
