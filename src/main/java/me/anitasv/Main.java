package me.anitasv;

import com.google.ortools.Loader;
import me.anitasv.jigsaw.*;
import me.anitasv.sat.CnfModel;
import me.anitasv.sat.GoogleModel;
import me.anitasv.sat.SatModel;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;


/**
 * Solves White Jigsaw Puzzles.
 */
public class Main {

    private static void formulateAndSolve(int M, int N, SatModel model,
                                          JigsawSolverFactory toSolver)
            throws IOException {
        Jigsaw jigSaw = new Jigsaw(M, N);
        System.out.println("Source diagram:");
        System.out.println(jigSaw);
        JigsawLocPiece[] withSoln = jigSaw.shuffle();

        JigsawPiece[] B = new JigsawPiece[withSoln.length];
        JigsawLocation[] L = new JigsawLocation[withSoln.length];
        for (int i = 0; i < withSoln.length; i++) {
            B[i] = withSoln[i].piece();
            L[i] = withSoln[i].loc();
        }

        JigsawSolver jigsawSolver = toSolver.newSolver(M, N, B);

        System.out.println("Formulating SAT problem.");
        jigsawSolver.formulate(model);
        List<JigsawLocation> solution = jigsawSolver.solve(model);

        if (solution != null) {
            for (int i = 0; i < withSoln.length; i++) {
                System.out.println((i + 1) + ". " + L[i] + " -> " + solution.get(i));
            }
        }
    }

    public static void main(String[] args) throws IOException {
        boolean randomProblem = false;
        Integer M = null, N = null;
        String satSolverPath = null;
        int formulation = 3;
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
            } else if (arg.startsWith("--sat_solver_path=")) {
                satSolverPath = arg.substring("--sat_solver_path=".length());
            } else if (arg.startsWith("--formulation=")) {
                try {
                    formulation = Integer.parseInt(arg.substring("--formulation=".length()));
                } catch (NumberFormatException e) {
                    System.out.println("formulation: " + e.getMessage());
                }
            }
        }


        JigsawSolverFactory jigsawSolverFactory = switch (formulation) {
            case 1 -> JigsawSolver1::new;
            case 2 -> JigsawSolver2::new;
            case 3 -> JigsawSolver3::new;
            default -> null;
        }
                ;
        if (jigsawSolverFactory == null) {
            System.out.println("Unsupported formulation: " + formulation);
            System.exit(1);
        }
        if (randomProblem) {
            System.out.println("Using random jigsaw puzzle.");
        } else {
            System.out.println("User input not supported, please use --random for example.");
        }
        if (M == null) {
            System.out.println("Argument M not found pass --M=[rows].");
        }
        if (N == null) {
            System.out.println("Argument N not found pass --N=[cols].");
        }


        if (randomProblem && M != null && N != null) {
            SatModel model;

            if (satSolverPath == null) {
                System.out.println("Argument --sat_solver_path=[path] missing, using Google OR Tools.");
                Loader.loadNativeLibraries();
                model = new GoogleModel();
            } else {
                System.out.println("Formulation 3 is not supported in cnf model yet.");
                model = new CnfModel("Jigsaw " + M + "x" + N,
                        "jig_rand_" + M + "x" + N + ".",
                        satSolverPath);
            }
            formulateAndSolve(M, N, model, jigsawSolverFactory);
        } else {
            System.exit(1);
        }
    }
}
