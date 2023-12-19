package me.anitasv;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

import java.util.concurrent.ThreadLocalRandom;

import com.google.ortools.Loader;
//import com.google.ortools.sat.*;


public class Main {

    enum Poke {
        FLAT(0),
        IN(1),
        OUT(-1);

        private final int val;

        Poke(int val) {
            this.val = val;
        }


        Poke flip() {
            switch (this) {
                case FLAT: return Poke.FLAT;
                case IN: return Poke.OUT;
                case OUT: return Poke.IN;
                default: return this;
            }
        }
    }

    private static final int TOP = 0, RIGHT = 1, BOT = 2, LEFT = 3;
    private static final int SIDES = 4;

    static class Piece  {
        private final Poke[] pokes = new Poke[SIDES];
        public Piece() {
            for (int i = 0; i < SIDES; i++) {
                this.pokes[i] = Poke.FLAT;
            }
        }
    }

    static class JigSaw {

        private final int M;
        private final int N;
        private final Piece[][] pieces;

        JigSaw(int M, int N) {
            this.M = M;
            this.N = N;
            this.pieces = new Piece[M][N];
            for (int i = 0; i < M; i++) {
                for (int j = 0; j < N; j++) {
                    this.pieces[i][j] = new Piece();
                }
            }
            this.generate();
        }

        void generate() {
            ThreadLocalRandom random = ThreadLocalRandom.current();

            // Flip LEFT | RIGHT banners.
            for (int m = 0; m < M; m++) {
                for (int n = 0; n < N - 1; n++) {
                    Poke pokeRight = random.nextBoolean() ? Poke.IN : Poke.OUT;
                    this.pieces[m][n].pokes[RIGHT] = pokeRight;
                    this.pieces[m][n + 1].pokes[LEFT] = pokeRight.flip();
                }
            }
            for (int n = 0; n < N; n++) {
                for (int m = 0; m < M - 1; m++) {
                    Poke pokeBot = random.nextBoolean() ? Poke.IN : Poke.OUT;
                    this.pieces[m][n].pokes[BOT] = pokeBot;
                    this.pieces[m + 1][n].pokes[TOP] = pokeBot.flip();
                }
            }
        }

        /**
         * Retains original jigsaw structure as is.
         * @return new random permutation+rotation of pieces.
         */
        public Piece[] shuffle() {
            List<Piece> moveAround = new ArrayList<>(M * N);
            for (int m = 0; m < M; m++) {
                for (int n = 0; n < N; n++) {
                    moveAround.add(pieces[m][n]);
                }
            }
            Collections.shuffle(moveAround, ThreadLocalRandom.current());

            Piece[] rotatePieces = new Piece[M * N];
            for (int i = 0; i < moveAround.size(); i++) {
                rotatePieces[i] = new Piece();
                int k = ThreadLocalRandom.current().nextInt(SIDES);
                for (int j = 0; j < SIDES; j++) {
                    rotatePieces[i].pokes[j] = moveAround.get(i).pokes[(j + k) % SIDES];
                }
            }
            return rotatePieces;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            for (int m = 0; m < M; m++) {
                // top line
                for (int n = 0; n < N; n++) {
                    builder.append("   ");
                    switch (this.pieces[m][n].pokes[TOP])  {
                        case FLAT: builder.append("-"); break;
                        case IN: builder.append("V"); break;
                        case OUT: builder.append("^"); break;
                    }
                    builder.append("  ");
                    if (n != N - 1) {
                        builder.append(" ");
                    }
                }
                builder.append(" \n");
                for (int n = 0; n < N; n++) {
                    switch (this.pieces[m][n].pokes[LEFT])  {
                        case FLAT: builder.append("|"); break;
                        case OUT: builder.append("<"); break;
                        case IN: builder.append(">"); break;
                    }
                    builder.append("     ");
                    switch (this.pieces[m][n].pokes[RIGHT])  {
                        case FLAT: builder.append("|"); break;
                        case OUT: builder.append(">"); break;
                        case IN: builder.append("<"); break;
                    }
                }
                builder.append("\n");

                for (int n = 0; n < N; n++) {
                    builder.append("   ");
                    switch (this.pieces[m][n].pokes[BOT])  {
                        case FLAT: builder.append("-"); break;
                        case OUT: builder.append("V"); break;
                        case IN: builder.append("^"); break;
                    }
                    builder.append("  ");
                    if (n != N - 1) {
                        builder.append(" ");
                    }
                }
                builder.append(" \n");
            }
            return builder.toString();
        }
    }

    static class CnfModel {

        // All these expressions must be simultaneously be true
        private final List<int[]> cnf = new ArrayList<>();
        private int variableIndex = 0;

        public void addExactlyOne(int[] literals) {
            if (literals.length == 1) {
                cnf.add(literals);
                return;
            }

            // Tseitin Transform
            int phi = newVariable();
            cnf.add(new int[]{phi});

            int[] terms = new int[literals.length];
            for (int i = 0; i < literals.length; i++) {
                terms[i] = newVariable();
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

        // finalExpr = AND(orExpr) ^ OR(AndExpr)
        public void write(String title,
                          OutputStream out) throws IOException {


            int numLines = cnf.size();

            BufferedOutputStream bos = new BufferedOutputStream(out, 1024 * 1024);
            PrintWriter printWriter = new PrintWriter(bos, true, StandardCharsets.UTF_8);
            printWriter.println("c");
            printWriter.println("c Title: " +title);
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

        public int newVariable() {
            return ++variableIndex;
        }
    }

    private static String solve(int size) throws IOException {
        JigSaw jigSaw = new JigSaw(size, size);
        Piece[] B = jigSaw.shuffle();

        Loader.loadNativeLibraries();
        CnfModel model = new CnfModel();

        int[][][] X = new int[jigSaw.M * jigSaw.N][jigSaw.M][jigSaw.N];
        int[][] Y = new int[jigSaw.M * jigSaw.N][SIDES];

        int tot = jigSaw.M * jigSaw.N;


        for (int k = 0; k < tot; k++) {
            for (int m = 0; m < jigSaw.M; m++) {
                for (int n = 0; n < jigSaw.N; n++) {
                    X[k][m][n] = model.newVariable();
                }
            }
        }

        for (int k = 0; k < tot; k++) {
            for (int s = 0; s < SIDES; s++) {
                Y[k][s] = model.newVariable();
            }
        }

        for (int k = 0; k < tot; k++) {
            int[] selectCell = new int[jigSaw.M * jigSaw.N];
            for (int m = 0; m < jigSaw.M; m++) {
                for (int n = 0; n < jigSaw.N; n++) {
                    selectCell[m * jigSaw.N + n] = X[k][m][n];
                }
            }
            model.addExactlyOne(selectCell);
        }

        for (int m = 0; m < jigSaw.M; m++) {
            for (int n = 0; n < jigSaw.N; n++) {
                int[] selectCell = new int[tot];
                for (int k = 0; k < tot; k++) {
                    selectCell[k] = X[k][m][n];
                }
                model.addExactlyOne(selectCell);
            }
        }

        for (int k = 0; k < tot; k++) {
            int[] selectCell = new int[SIDES];
            for (int s = 0; s < SIDES; s++) {
                selectCell[s] = Y[k][s];
            }
            model.addExactlyOne(selectCell);
        }

        for (int k = 0; k < tot; k++) {
            for (int s = 0; s < SIDES; s++) {
                for (int l = 0; l < tot; l++) {
                    for (int t = 0; t < SIDES; t++) {
                        boolean rhsBotTop = B[k].pokes[(BOT + s ) % SIDES].val +
                                B[l].pokes[(TOP + t) % SIDES].val == 0;
                        // A => rhs
                        if (!rhsBotTop) {
                            // then A must be false.
                            for (int m = 0; m < jigSaw.M - 1; m++) {
                                for (int n = 0; n < jigSaw.N; n++) {
                                    int[] lhs = new int[] {
                                            -X[k][m][n],
                                            -Y[k][s],
                                            -X[l][m+1][n],
                                            -Y[l][t]
                                    };
                                    model.addBoolOr(lhs);
                                }
                            }
                        }

                        boolean rhsRightLeft = B[k].pokes[(RIGHT + s ) % SIDES].val +
                                B[l].pokes[(LEFT + t) % SIDES].val == 0;
                        // A => rhs
                        if (!rhsRightLeft) {
                            // then A must be false.
                            for (int m = 0; m < jigSaw.M; m++) {
                                for (int n = 0; n < jigSaw.N - 1; n++) {
                                    int[] lhs = new int[] {
                                            -X[k][m][n],
                                            -Y[k][s],
                                            -X[l][m][n+1],
                                            -Y[l][t]
                                    };
                                    model.addBoolOr(lhs);
                                }
                            }
                        }
                    }
                }
            }
        }

        for (int k = 0; k < tot; k++) {
            for (int s = 0; s < SIDES; s++) {
                boolean rhsTop = B[k].pokes[(TOP+s) % SIDES] == Poke.FLAT;
                if (!rhsTop) {
                    for (int n = 0; n < jigSaw.N; n++) {
                        int []lhs = new int[] {
                                -X[k][0][n],
                                -Y[k][s]
                        };
                        model.addBoolOr(lhs);
                    }
                }

                boolean rhsBot = B[k].pokes[(BOT+s) % SIDES] == Poke.FLAT;
                if (!rhsBot) {
                    for (int n = 0; n < jigSaw.N; n++) {
                        int []lhs = new int[] {
                                -X[k][jigSaw.M - 1][n],
                                -Y[k][s]
                        };
                        model.addBoolOr(lhs);
                    }
                }
                boolean rhsRight = B[k].pokes[(RIGHT+s) % SIDES] == Poke.FLAT;
                if (!rhsRight) {
                    for (int m = 0; m < jigSaw.M; m++) {
                        int []lhs = new int[] {
                                -X[k][m][jigSaw.N - 1],
                                -Y[k][s]
                        };
                        model.addBoolOr(lhs);
                    }
                }
                boolean rhsLeft = B[k].pokes[(LEFT+s) % SIDES] == Poke.FLAT;
                if (!rhsLeft) {
                    for (int m = 0; m < jigSaw.M; m++) {
                        int []lhs = new int[] {
                                -X[k][m][0],
                                -Y[k][s]
                        };
                        model.addBoolOr(lhs);
                    }
                }
            }
        }

        FileOutputStream fos = new FileOutputStream("/tmp/jigsaw" + "_" + size + ".cnf");

        model.write("Jigsaw : " + size + "x" + size,
                fos);

        return "do stuff";

        // Create a solver and solve the model.
//        CpSolver solver = new CpSolver();
//        CpSolverStatus status = solver.solve(model);
//
//        return status.toString();
//        if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
//
//            JigSaw jigSawRecons = new JigSaw(jigSaw.M, jigSaw.N);
//            for (int k = 0; k < jigSaw.M * jigSaw.N; k++) {
//
//                int finalM = 0, finalN = 0;
//                for (int m = 0; m < jigSaw.M; m++) {
//                    for (int n = 0; n < jigSaw.N; n++) {
//                        if (solver.booleanValue(X[k][m][n])) {
//                            finalM = m;
//                            finalN = n;
//                            break;
//                        }
//                    }
//                }
//                int finalS = 0;
//                for (int s = 0; s < SIDES; s++) {
//                    if (solver.booleanValue(Y[k][s])) {
//                        finalS = s;
//                        break;
//                    }
//                }
//
//                for (int s = 0; s < SIDES; s++) {
//                    jigSawRecons.pieces[finalM][finalN].pokes[s]
//                            = B[k].pokes[(s + finalS) % SIDES];
//                }
//            }
//            System.out.println(jigSawRecons);
//        } else {
//            System.out.println("No solution found.");
//        }
    }

    public static void main(String[] args) throws IOException {
        for (int size = 1; size < 100; size++) {
            System.out.print("Solving: " + size);
            System.out.flush();
            long startNanos = System.nanoTime();
            String status = solve(size);
            long endNanos = System.nanoTime();
            System.out.println(" " + status + " took: " + (TimeUnit.NANOSECONDS.toSeconds(endNanos - startNanos))
                    + " seconds");
            System.gc();
        }
    }
}
