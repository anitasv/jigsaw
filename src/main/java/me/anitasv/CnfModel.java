package me.anitasv;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

class CnfModel implements SatModel {

    // All these expressions must be simultaneously be true
//    private final List<int[]> cnf = new ArrayList<>();
    private int variableIndex = 0;
    private int numClauses = 0;

    private final String title;
    private final PrintWriter printWriter;
    private final String fileName;


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
                    String fileName) throws FileNotFoundException {

        FileOutputStream tailStream = new FileOutputStream(fileName + ".tail");
        this.printWriter = new PrintWriter(tailStream, true, StandardCharsets.UTF_8);
        this.fileName = fileName;
        this.title = title;
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

        int[] terms = new int[literals.length];
        for (int i = 0; i < literals.length; i++) {
            terms[i] = newVariable("ignore");
        }

        writeClause(terms);

        for (int i = 0; i < terms.length; i++) {
            int[] xCons = new int[1 + literals.length];
            xCons[0] = terms[i];
            for (int j = 0; j < literals.length; j++) {
                xCons[1 + j] = i == j ? -literals[j] : literals[j];
            }
            writeClause(xCons);

            for (int j = 0; j < literals.length; j++) {
                writeClause(new int[]{-terms[i], (i == j ? literals[j] : -literals[j])});
            }
        }
    }

    public void addBoolOr(int[] lhs) {
        writeClause(lhs);
    }

    public void close() throws FileNotFoundException {
        printWriter.close();

        FileOutputStream headStream = new FileOutputStream(fileName + ".head");
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
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        System.out.println("File Written");
        ProcessBuilder concat = new ProcessBuilder("/bin/cat",
                this.fileName + ".head",
                this.fileName + ".tail");
        concat.redirectOutput(new File(this.fileName));

        try {
            Process concatProcess = concat.start();
            System.out.println("Waiting for concat");
            int concatExit = concatProcess.waitFor();
            if (concatExit != 0) {
                throw new RuntimeException("/bin/cat failed with status: " + concatExit);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("File Concatenated");

        ProcessBuilder miniSat = new ProcessBuilder("/Users/anita/bin/bin/minisat",
                this.fileName,
                this.fileName + ".out");

        miniSat.environment()
                .put("DYLD_LIBRARY_PATH", "/Users/anita/bin/lib");

        try {
            Process miniProcess = miniSat.start();
            System.out.println("Waiting for miniSAT");
            miniProcess.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("miniSAT done");

        String fileContents;
        try {
            fileContents = Files.readString(
                    new File(this.fileName + ".out").toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Reading solution");

        Set<Integer> solution = new HashSet<>();

        String[] line2 = fileContents.split("\n")[1].split(" ");
        for (String var : line2) {
            int number = Integer.parseInt(var);
            if (number > 0) {
                solution.add(number);
            }
        }
        System.out.println("Done parsing solution");
        return solution;
    }
}
