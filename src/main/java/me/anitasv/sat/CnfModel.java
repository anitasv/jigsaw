package me.anitasv.sat;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

public class CnfModel implements SatModel {

    // All these expressions must be simultaneously be true
    private int variableIndex = 0;
    private long numClauses = 0;

    private final String title;
    private final PrintWriter printWriter;
    private final String fileName;

    private final String satSolverPath;
    private final File headFile;
    private final File tailFile;

    /**
     * Writes the clauses to the file as it gets excecuted incrementally to avoid
     * taking too much JVM space.
     *
     * Will create two files:
     *   fileName.head
     *   fileName.tail
     *
     * To finish writing call close() function.
     *
     * Make sure to call
     *  `cat $fileName.head $fileName.tail > $fileName`
     *
     * once done before calling your SAT solver.
     */
    public CnfModel(String title,
                    String fileName,
                    String satSolverPath) throws IOException {

        this.tailFile = File.createTempFile(fileName, ".tail");
        this.headFile = File.createTempFile(fileName, ".head");
        FileOutputStream tailStream = new FileOutputStream(tailFile);
        this.printWriter = new PrintWriter(tailStream, true, StandardCharsets.UTF_8);
        this.fileName = fileName;
        this.title = title;
        this.satSolverPath = satSolverPath;
    }

    private void writeClause(int[] literals) {
        StringBuilder line = new StringBuilder();
        for (int c : literals) {
            line.append(c);
            line.append(" ");
        }
        line.append("0");
        printWriter.println(line);
        ++numClauses;
    }

    public void addExactlyOne(int[] literals) {
        if (literals.length == 1) {
            writeClause(literals);
            return;
        }

        // Tseitin Transform
        for (int i = 0; i < literals.length - 1; i++) {
            for (int j = i + 1; j < literals.length; j++) {
                writeClause(new int[]{-literals[i], -literals[j]});
            }
        }

//        int[] terms = new int[literals.length];
//        for (int i = 0; i < literals.length; i++) {
//            terms[i] = newVariable("ignore");
//        }
//
//        writeClause(terms);
//
//        for (int i = 0; i < terms.length; i++) {
//            int[] xCons = new int[1 + literals.length];
//            xCons[0] = terms[i];
//            for (int j = 0; j < literals.length; j++) {
//                xCons[1 + j] = i == j ? -literals[j] : literals[j];
//            }
//            writeClause(xCons);
//
//            for (int j = 0; j < literals.length; j++) {
//                writeClause(new int[]{-terms[i], (i == j ? literals[j] : -literals[j])});
//            }
//        }
    }

    public void addBoolOr(int[] lhs) {
        writeClause(lhs);
    }

    public void close() throws IOException {
        printWriter.close();

        FileOutputStream headStream = new FileOutputStream(headFile);
        PrintWriter headWriter = new PrintWriter(headStream, true, StandardCharsets.UTF_8);
        headWriter.println("c");
        headWriter.println("c Title: " + title);
        headWriter.println("c");
        // Due to this header I cannot build an incremental writer which doesn't take exce
        headWriter.println("p cnf " + variableIndex + " " + numClauses);
        headWriter.close();
    }

    public int newVariable(String name) {
        return ++variableIndex;
    }

    @Override
    public Set<Integer> solve() {
        try {
            this.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Files Written.");
        ProcessBuilder concat = new ProcessBuilder("/bin/cat",
                headFile.getAbsolutePath(),
                tailFile.getAbsolutePath());

        File satInput;
        try {
            satInput = File.createTempFile(this.fileName, ".cnf");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        concat.redirectOutput(satInput);

        try {
            Process concatProcess = concat.start();
            System.out.println("Waiting for concat.");
            int concatExit = concatProcess.waitFor();
            if (concatExit != 0) {
                throw new RuntimeException("/bin/cat failed with status: " + concatExit);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("File Concatenated.");


        File satOutput;
        try {
            satOutput = File.createTempFile(this.fileName, ".out");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("SAT input: " + satInput.getAbsolutePath());
        System.out.println("SAT output: " + satOutput.getAbsolutePath());
        ProcessBuilder miniSat = new ProcessBuilder(satSolverPath,
                satInput.getAbsolutePath(),
                satOutput.getAbsolutePath())
                .inheritIO();

        try {
            Process miniProcess = miniSat.start();
            System.out.println("Waiting for SAT solver.");
            miniProcess.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("SAT solver done");

        String fileContents;
        try {
            fileContents = Files.readString(satOutput.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Reading solution.");

        Set<Integer> solution = new HashSet<>();

        String[] line2 = fileContents.split("\n")[1].split(" ");
        for (String var : line2) {
            int number = Integer.parseInt(var);
            if (number > 0) {
                solution.add(number);
            }
        }
        System.out.println("Done parsing solution.");
        return solution;
    }
}
