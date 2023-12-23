package me.anitasv.jigsaw;
import me.anitasv.sat.SatModel;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static me.anitasv.jigsaw.Jigsaw.SIDES;

/**
 * An improved Jigsaw formulation:
 *
 */
public class JigsawSolver2  implements JigsawSolver {

    private final int M;
    private final int N;
    private final JigsawPiece[] B;
    private final int tot;
    private final int[][][] X;
    private final int[][] Y;
    private final int[][] H;
    private final int[][] V;

    public JigsawSolver2(int M, int N, JigsawPiece[] B) {
        this.M = M;
        this.N = N;
        this.B = B;
        this.tot = M * N;
        this.X = new int[tot][M][N];
        this.Y = new int[tot][SIDES];

        // True indicates inwards, and False indicates outwards.
        // H indicates horizontal bottom wall orientation with respect to that box.
        // V indicates vertical right wall orientation with respect to that box.
        this.H = new int[M - 1][N]; // Every cell except last row has non-trivial bottom
        this.V = new int[M][N - 1]; // Every cell except last column has a non-trivial right.
    }

    public void createVariables(SatModel model) {
        for (int k = 0; k < tot; k++) {
            for (int m = 0; m < M; m++) {
                for (int n = 0; n < N; n++) {
                    X[k][m][n] = model.newVariable("X_{" + k + "," + m + "," + n + "}");
                }
            }
        }
        for (int k = 0; k < tot; k++) {
            for (int s = 0; s < SIDES; s++) {
                Y[k][s] = model.newVariable("Y_{" + k + "," + s + "}");
            }
        }

        for (int m = 0; m < M - 1; m++) {
            for (int n = 0; n < N; n++) {
                H[m][n] = model.newVariable("H_{" + m + "," + n + "}");
            }
        }

        for (int m = 0; m < M; m++) {
            for (int n = 0; n < N - 1; n++) {
                V[m][n] = model.newVariable("V_{" + m + "," + n + "}");
            }
        }
    }

    public void setOneHot(SatModel model) {
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
    }

    interface LoopAction {
        void apply(int m, int n, int k, int s);
    }

    private record Looper(int M, int N, int K, int S) {
        void forEach(LoopAction action) {
            for (int m = 0; m < M; m++) {
                for (int n = 0; n < N; n++) {
                    for (int k = 0; k < K; k++) {
                        for (int s = 0; s < S; s++) {
                            action.apply(m, n, k, s);
                        }
                    }
                }
            }
        }
    }

    private void wallConstraint(SatModel model, JigsawPoke poke, int X_var, int Y_var, int W_var) {
        if (poke == JigsawPoke.FLAT) {
            model.addBoolOr(new int[]{-X_var, -Y_var});
        } else if (poke == JigsawPoke.IN) {
            model.addBoolOr(new int[]{-X_var,-Y_var, W_var});
        } else if (poke == JigsawPoke.OUT) {
            model.addBoolOr(new int[]{-X_var,-Y_var, -W_var});
        }
    }

    private static int rotate(int pos, int s) {
        return (pos + s) % SIDES;
    }

    @Override
    public void formulate(SatModel model) {
        createVariables(model);
        setOneHot(model);

        Looper V_looper = new Looper(M, N - 1, tot, SIDES);

        V_looper.forEach((m, n, k, s) -> {
            // Right Wall constraints
            // X[k][m][n] && Y[k][s] => B[k, RIGHT (-) s] = V[m][n]
            wallConstraint(model,
                    B[k].pokes[rotate(Jigsaw.RIGHT, s)],
                    X[k][m][n],
                    Y[k][s],
                    V[m][n]);

            // Left Wall constraints
            // X[k][m][n+1] && Y[k][s] => B[k, LEFT (-) s] = (not V[m][n])
            wallConstraint(model,
                    B[k].pokes[rotate(Jigsaw.LEFT, s)],
                    X[k][m][n + 1],
                    Y[k][s],
                    -V[m][n]);
        });

        Looper H_looper = new Looper(M - 1, N, tot, SIDES);

        H_looper.forEach((m, n, k, s) -> {
            // Bottom Wall constraints
            // X[k][m][n] && Y[k][s] => B[k, BOT (-) s] = H[m][n]
            wallConstraint(model,
                    B[k].pokes[rotate(Jigsaw.BOT, s)],
                    X[k][m][n],
                    Y[k][s],
                    H[m][n]);

            // Top Wall constraints
            // X[k][m + 1][n] && Y[k][s] => B[k, TOP (-) s] = (not H[m][n])
            wallConstraint(model,
                    B[k].pokes[rotate(Jigsaw.TOP, s)],
                    X[k][m + 1][n],
                    Y[k][s],
                    -H[m][n]);
        });

        Looper top_row = new Looper(1, N, tot, SIDES);
        top_row.forEach((m, n, k, s) -> {
            // X[k][m][n] && Y[k][s] => B[k, TOP (-s) ] = FLAT
            JigsawPoke poke = B[k].pokes[rotate(Jigsaw.TOP, s)];
            if (poke != JigsawPoke.FLAT) {
                model.addBoolOr(new int[]{-X[k][m][n], -Y[k][s]});
            }
        });
        top_row.forEach((mFlip, n, k, s) -> {
            int m = M - 1 - mFlip;
            JigsawPoke poke = B[k].pokes[rotate(Jigsaw.BOT, s)];
            if (poke != JigsawPoke.FLAT) {
                model.addBoolOr(new int[]{-X[k][m][n], -Y[k][s]});
            }
        });

        Looper first_col = new Looper(M, 1, tot, SIDES);
        first_col.forEach((m, n, k, s) -> {
            JigsawPoke poke = B[k].pokes[rotate(Jigsaw.LEFT, s)];
            if (poke != JigsawPoke.FLAT) {
                model.addBoolOr(new int[]{-X[k][m][n], -Y[k][s]});
            }
        });

        first_col.forEach((m, nFlip, k, s) -> {
            int n = N - 1 - nFlip;
            JigsawPoke poke = B[k].pokes[rotate(Jigsaw.RIGHT, s)];
            if (poke != JigsawPoke.FLAT) {
                model.addBoolOr(new int[]{-X[k][m][n], -Y[k][s]});
            }
        });
    }

    @Override
    public List<JigsawLocation> solve(SatModel model) {
        //  Create a solver and solve the model.
        Set<Integer> solution = model.solve();

        if (solution == null) {
            System.out.println("No solution exists");
            return null;
        }

        Jigsaw jigSaw = new Jigsaw(M, N);
        List<JigsawLocation> locs = new ArrayList<>();
        boolean hasError = false;
        for (int k = 0; k < M * jigSaw.N; k++) {

            int finalM = 0, finalN = 0;
            boolean locFound = false;
            for (int m = 0; m < jigSaw.M; m++) {
                for (int n = 0; n < jigSaw.N; n++) {
                    if (solution.contains(X[k][m][n])) {
                        finalM = m;
                        finalN = n;
                        if (locFound) {
                            System.out.println("Duplicate location!!");
                            hasError = true;
                        }
                        locFound = true;
                    }
                }
            }
            if (!locFound) {
                hasError = true;
                System.out.println("Location not found");
            }
            int finalS = 0;
            boolean orientFound = false;

            for (int s = 0; s < SIDES; s++) {
                if (solution.contains(Y[k][s])) {
                    finalS = s;
                    if (orientFound) {
                        System.out.println("Duplicate orientation!!");
                        hasError = true;
                    }
                    orientFound = true;
                }
            }
            if (!orientFound) {
                hasError = true;
                System.out.println("Orientation not found");
            }


            for (int s = 0; s < SIDES; s++) {
                jigSaw.pieces[finalM][finalN].pokes[s]
                        = B[k].pokes[(s + finalS) % SIDES];
            }

            locs.add(new JigsawLocation(finalM, finalN, finalS));
        }
        if (!hasError) {
            System.out.println("Reconstituted Diagram:");
            System.out.println(jigSaw);
            return locs;
        } else {
            return null;
        }
    }
}
