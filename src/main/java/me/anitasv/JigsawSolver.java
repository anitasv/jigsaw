package me.anitasv;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static me.anitasv.JigSaw.SIDES;

public class JigsawSolver {

    private final int M;
    private final int N;
    private final Piece[] B;
    private final int tot;
    private final int[][][] X;
    private final int[][] Y;

    JigsawSolver(int M, int N, Piece[] B) {
        this.M = M;
        this.N = N;
        this.B = B;
        this.tot = M * N;
        this.X = new int[tot][M][N];
        this.Y = new int[tot][SIDES];
    }

    public void formulate(SatModel model) throws FileNotFoundException {
        for (int k = 0; k < tot; k++) {
            for (int m = 0; m < M; m++) {
                for (int n = 0; n < N; n++) {
                    X[k][m][n] = model.newVariable("X_{" + k + "," + m + "," + n + "}");
                }
            }
        }

        for (int k = 0; k < tot; k++) {
            for (int s = 0; s < SIDES; s++) {
                Y[k][s] = model.newVariable("X_{" + k + "," + s + "}");
            }
        }

        for (int k = 0; k < tot; k++) {
            int[] selectCell = new int[M * N];
            for (int m = 0; m < M; m++) {
                for (int n = 0; n < N; n++) {
                    selectCell[m * N + n] = X[k][m][n];
                }
            }
            model.addExactlyOne(selectCell);
        }

        for (int m = 0; m < M; m++) {
            for (int n = 0; n < N; n++) {
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
                        boolean rhsBotTop = B[k].pokes[(JigSaw.BOT + s ) % SIDES].val +
                                B[l].pokes[(JigSaw.TOP + t) % SIDES].val == 0;
                        // A => rhs
                        if (!rhsBotTop) {
                            // then A must be false.
                            for (int m = 0; m < M - 1; m++) {
                                for (int n = 0; n < N; n++) {
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

                        boolean rhsRightLeft = B[k].pokes[(JigSaw.RIGHT + s ) % SIDES].val +
                                B[l].pokes[(JigSaw.LEFT + t) % SIDES].val == 0;
                        // A => rhs
                        if (!rhsRightLeft) {
                            // then A must be false.
                            for (int m = 0; m < M; m++) {
                                for (int n = 0; n < N - 1; n++) {
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
                boolean rhsTop = B[k].pokes[(JigSaw.TOP+s) % SIDES] == Poke.FLAT;
                if (!rhsTop) {
                    for (int n = 0; n < N; n++) {
                        int []lhs = new int[] {
                                -X[k][0][n],
                                -Y[k][s]
                        };
                        model.addBoolOr(lhs);
                    }
                }

                boolean rhsBot = B[k].pokes[(JigSaw.BOT+s) % SIDES] == Poke.FLAT;
                if (!rhsBot) {
                    for (int n = 0; n < N; n++) {
                        int []lhs = new int[] {
                                -X[k][M - 1][n],
                                -Y[k][s]
                        };
                        model.addBoolOr(lhs);
                    }
                }
                boolean rhsRight = B[k].pokes[(JigSaw.RIGHT+s) % SIDES] == Poke.FLAT;
                if (!rhsRight) {
                    for (int m = 0; m < M; m++) {
                        int []lhs = new int[] {
                                -X[k][m][N - 1],
                                -Y[k][s]
                        };
                        model.addBoolOr(lhs);
                    }
                }
                boolean rhsLeft = B[k].pokes[(JigSaw.LEFT+s) % SIDES] == Poke.FLAT;
                if (!rhsLeft) {
                    for (int m = 0; m < M; m++) {
                        int []lhs = new int[] {
                                -X[k][m][0],
                                -Y[k][s]
                        };
                        model.addBoolOr(lhs);
                    }
                }
            }
        }
    }

    List<JigsawLocation> solve(SatModel model) {
        //  Create a solver and solve the model.
        Set<Integer> solution = model.solve();

        if (solution == null) {
            System.out.println("No solution exists");
            return null;
        }

        JigSaw jigSaw = new JigSaw(M, N);
        List<JigsawLocation> locs = new ArrayList<>();
        for (int k = 0; k < M * jigSaw.N; k++) {

            int finalM = 0, finalN = 0;
            for (int m = 0; m < jigSaw.M; m++) {
                for (int n = 0; n < jigSaw.N; n++) {
                    if (solution.contains(X[k][m][n])) {
                        finalM = m;
                        finalN = n;
                        break;
                    }
                }
            }
            int finalS = 0;
            for (int s = 0; s < SIDES; s++) {
                if (solution.contains(Y[k][s])) {
                    finalS = s;
                    break;
                }
            }

            for (int s = 0; s < SIDES; s++) {
                jigSaw.pieces[finalM][finalN].pokes[s]
                        = B[k].pokes[(s + finalS) % SIDES];
            }

            locs.add(new JigsawLocation(finalM, finalN, finalS));
        }
        System.out.println("Reconstituted Diagram:");
        System.out.println(jigSaw);
        return locs;
    }
}
