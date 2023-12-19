package me.anitasv;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

class CnfModel implements SatModel {

    // All these expressions must be simultaneously be true
    private final List<int[]> cnf = new ArrayList<>();
    private int variableIndex = 0;

    public void addExactlyOne(int[] literals) {
        if (literals.length == 1) {
            cnf.add(literals);
            return;
        }

        // Tseitin Transform
        int phi = newVariable("ignore");
        cnf.add(new int[]{phi});

        int[] terms = new int[literals.length];
        for (int i = 0; i < literals.length; i++) {
            terms[i] = newVariable("ignore");
        }

        int[] phiTerm = new int[1 + terms.length];
        phiTerm[0] = -phi;
        System.arraycopy(terms, 0, phiTerm, 1, terms.length);
        cnf.add(phiTerm);

        for (int term : terms) {
            cnf.add(new int[]{phi, -term});
        }

        for (int i = 0; i < terms.length; i++) {
            int[] xCons = new int[1 + literals.length];
            xCons[0] = terms[0];
            for (int j = 0; j < literals.length; j++) {
                xCons[1 + j] = i == j ? -literals[j] : literals[j];
            }
            cnf.add(xCons);

            for (int j = 0; j < literals.length; j++) {
                cnf.add(new int[]{-terms[0], (i == j ? literals[j] : -literals[j])});
            }
        }
    }

    public void addBoolOr(int[] lhs) {
        cnf.add(lhs);
    }

    public void write(String title,
                      OutputStream out) throws IOException {


        int numLines = cnf.size();

        BufferedOutputStream bos = new BufferedOutputStream(out, 1024 * 1024);
        PrintWriter printWriter = new PrintWriter(bos, true, StandardCharsets.UTF_8);
        printWriter.println("c");
        printWriter.println("c Title: " + title);
        printWriter.println("c");
        printWriter.println("p cnf " + variableIndex + " " + numLines);

        for (int[] constraint : cnf) {
            StringBuilder line = new StringBuilder();
            for (int c : constraint) {
                line.append(c);
                line.append(" ");
            }
            line.append("0");
            printWriter.println(line);
        }
        printWriter.flush();
        bos.flush();
        out.flush();
    }

    public int newVariable(String name) {
        return ++variableIndex;
    }
}
