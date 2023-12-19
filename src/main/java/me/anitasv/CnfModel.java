package me.anitasv;

import java.io.*;
import java.nio.charset.StandardCharsets;
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
        int phi = newVariable("ignore");
        writeClause(new int[]{phi});

        int[] terms = new int[literals.length];
        for (int i = 0; i < literals.length; i++) {
            terms[i] = newVariable("ignore");
        }

        int[] phiTerm = new int[1 + terms.length];
        phiTerm[0] = -phi;
        System.arraycopy(terms, 0, phiTerm, 1, terms.length);
        writeClause(phiTerm);

        for (int term : terms) {
            writeClause(new int[]{phi, -term});
        }

        for (int i = 0; i < terms.length; i++) {
            int[] xCons = new int[1 + literals.length];
            xCons[0] = terms[0];
            for (int j = 0; j < literals.length; j++) {
                xCons[1 + j] = i == j ? -literals[j] : literals[j];
            }
            writeClause(xCons);

            for (int j = 0; j < literals.length; j++) {
                writeClause(new int[]{-terms[0], (i == j ? literals[j] : -literals[j])});
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
        throw new UnsupportedOperationException("CNF Model doesn't implement solve yet");
    }
}
