package me.anitasv;

import com.google.ortools.Loader;

import java.io.IOException;
import java.util.List;


/**
 * Solves White Jigsaw Puzzles - meaning no image, only if information we have are the
 * shape of pieces.
 *
 * Formulation : https://mathb.in/77183
 *
 * It can be run in two modes using Google OR Tools (default), or change code
 * to run using any model that accepts DIMACS file as input like MiniSAT. Later one
 * is more scalable because it won't die because of JVM limits.
 *
 * MiniSat specifically is not better than Google OR Tools, and printing back
 * the solution is currently not supported, only creating the file and you have
 * to manually invoke the tool you like. Using Glucose may be better but I can't
 * get it to run on Mac M2 silicon.
 *
 * To create DIMACS file I implemented a variant of Tseitin Transform for "exactly one"
 * constraints. This may be what is causing poor MiniSat performance in comparison
 * to OR Tools, because OR tools directly work out of the circuit in SMT solver.
 */
public class Main {

    private static void formulateAndSolve(int M, int N) throws IOException {
        JigSaw jigSaw = new JigSaw(M, N);
        System.out.println("Source diagram:");
        System.out.println(jigSaw);
        JigSaw.RetainPosRot[] withSoln = jigSaw.shuffle();

        Piece[] B = new Piece[withSoln.length];
        JigsawLocation[] L = new JigsawLocation[withSoln.length];
        for (int i = 0; i < withSoln.length; i++) {
            B[i] = withSoln[i].piece();
            L[i] = withSoln[i].loc();
        }

        JigsawSolver jigsawSolver = new JigsawSolver(M, N, B);
        GoogleModel model = new GoogleModel();
        jigsawSolver.formulate(model);
        List<JigsawLocation> soln = jigsawSolver.solve(model);

        for (int i = 0; i < withSoln.length; i++) {
            System.out.println((i + 1) + ". " + L[i] + " -> " + soln.get(i));
        }
    }

    public static void main(String[] args) throws IOException {
        Loader.loadNativeLibraries();

        boolean randomProblem = false;
        Integer M = null, N = null;
        for (String arg : args) {
            if (arg.equals("--random")) {
                randomProblem = true;
            } else if (arg.startsWith("--M=")) {
                try {
                    M = Integer.parseInt(arg.substring("--M=".length()));
                } catch (NumberFormatException e) {
                    System.out.println("M value: " + e.getMessage());
                }
            } else if (arg.startsWith("--N=")) {
                try {
                    N = Integer.parseInt(arg.substring("--N=".length()));
                } catch (NumberFormatException e) {
                    System.out.println("N value: " + e.getMessage());
                }
            }
        }
        if (randomProblem) {
            System.out.println("Using random jigsaw puzzle");
        } else {
            System.out.println("User input not supported, please use --random for example");
        }
        if (M == null) {
            System.out.println("Argument M not found pass --M=[rows]");
        }
        if (N == null) {
            System.out.println("Argument N not found pass --N=[cols]");
        }

        if (randomProblem && M != null && N != null) {
            formulateAndSolve(M, N);
        } else {
            System.exit(1);
        }
    }
}
