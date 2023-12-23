package me.anitasv.jigsaw;
import me.anitasv.sat.SatModel;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import static me.anitasv.jigsaw.Jigsaw.*;

/**
 * An improved Jigsaw formulation:
 *
 */
public class JigsawSolver3 {

    private final int M;
    private final int N;
    private final JigsawPiece[] B;
    private final int tot;
    private final int[][] H;
    private final int[][] V;

    private final int[][][] J;

    private final JigsawCanonical canonical;

    public JigsawSolver3(int M, int N, JigsawPiece[] B) {
        this.M = M;
        this.N = N;
        this.B = B;
        this.tot = M * N;
        this.canonical = new JigsawCanonical();


        // True indicates inwards, and False indicates outwards.
        // H indicates horizontal bottom wall orientation with respect to that box.
        // V indicates vertical right wall orientation with respect to that box.
        this.H = new int[M - 1][N]; // Every cell except last row has non-trivial bottom
        this.V = new int[M][N - 1]; // Every cell except last column has a non-trivial right.

        // Every cell can be canonically be mapped to one of the 24 options.
        this.J = new int[M][N][canonical.size()];
    }

    public void createVariables(SatModel model) {
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

        for (int m = 0; m < M; m++) {
            for (int n = 0; n < N; n++) {
                for (int j = 0; j < canonical.size(); j++) {
                    J[m][n][j] = model.newVariable("J_" + m + "," + n + "," + j + "}");
                }
            }
        }
    }

    public void setOneHot(SatModel model) {
        for (int m = 0; m < M; m++) {
            for (int n = 0; n < N; n++) {
                int[] selectCell = new int[canonical.size()];
                for (int j = 0; j < canonical.size(); j++) {
                    selectCell[j] = J[m][n][j];
                }
                model.addExactlyOne(selectCell);
            }
        }
    }

    sealed interface Wall permits Wall.FlatWall, Wall.VarWall {
        record FlatWall() implements Wall {
            @Override
            public void match(Runnable flatCase, IntConsumer varCase) {
                flatCase.run();
            }

            @Override
            public <R> R match(Supplier<R> flatCase, IntFunction<R> varCase) {
                return flatCase.get();
            }

            @Override
            public Wall flip() {
                return this;
            }
        }
        record VarWall(int var) implements Wall {
            @Override
            public void match(Runnable flatCase, IntConsumer varCase) {
                varCase.accept(this.var);
            }

            @Override
            public <R> R match(Supplier<R> flatCase, IntFunction<R> varCase) {
                return varCase.apply(this.var);
            }

            @Override
            public Wall flip() {
                return new VarWall(-var);
            }
        }

        void match(Runnable flatCase, IntConsumer varCase);

        <R> R match(Supplier<R> flatCase, IntFunction<R> varCase);

        Wall flip();
    }

    private final Wall.FlatWall flatWall = new Wall.FlatWall();

    private Wall.VarWall varWall(int v) {
        return new Wall.VarWall(v);
    }

    Wall getWall(int m, int n, int s) {
        // H indicates horizontal bottom wall orientation with respect to that box.
        // V indicates vertical right wall orientation with respect to that box.
//        this.H = new int[M - 1][N]; // Every cell except last row has non-trivial bottom
//        this.V = new int[M][N - 1]; // Every cell except last column has a non-trivial right.

        if (s == RIGHT) {
            if (n < N - 1) {
                return varWall(V[m][n]);
            } else {
                return flatWall;
            }
        }
        if (s == BOT) {
            if (m < M - 1) {
                return varWall(H[m][n]);
            } else {
                return flatWall;
            }
        }
        if (s == LEFT) {
            if (n > 0) {
                return getWall(m, n - 1, RIGHT).flip();
            } else {
                return flatWall;
            }
        }
        if (s == TOP) {
            if (m > 0) {
                return getWall(m - 1, n, BOT).flip();
            } else {
                return flatWall;
            }
        }
        throw new IllegalArgumentException("side constraint 0 <= " + s + " < " + SIDES);
    }

    record PieceConstraint(JigsawPiece piece, int[] constraint) {
    }


    static class PCGenerator {
        private final Wall[] walls;
        private final Deque<JigsawPoke> pokes = new ArrayDeque<>();
        private final Deque<Integer> vars = new ArrayDeque<>();

        private final List<PieceConstraint> pc = new ArrayList<>();

        PCGenerator(Wall[] walls) {
            this.walls = walls;
        }

        void init() {
            start(0);
        }

        void addPoke(JigsawPoke poke, Runnable task) {
            pokes.push(poke);
            try {
                task.run();
            } finally {
                pokes.pop();
            }
        }
        void addVar(int var, Runnable task) {
            vars.push(var);
            try {
                task.run();
            } finally {
                vars.pop();
            }
        }

        void start(int s) {
            if (s == walls.length) {
                List<JigsawPoke> reversed = new ArrayList<>(this.pokes);
                Collections.reverse(reversed);
                pc.add(new PieceConstraint(
                        new JigsawPiece(reversed.toArray(new JigsawPoke[0])),
                        this.vars.stream().mapToInt(x -> x).toArray()));
                return;
            }
            Wall wall = walls[s];
            wall.match(() -> {
                addPoke(JigsawPoke.FLAT, () -> {
                    start(s + 1);
                });
            }, (var) -> {
                addPoke(JigsawPoke.IN, () -> {
                    addVar(var, () -> {
                        start(s + 1);
                    });
                });
                addPoke(JigsawPoke.OUT, () -> {
                    addVar(-var, () -> {
                        start(s + 1);
                    });
                });
            });
        }
    }

    public static String toDebug(int[] arr) {
        List<Integer> pcs = new ArrayList<>();
        for (int c : arr) {
            pcs.add(c);
        }
        return pcs.toString();
    }

    public void formulate(SatModel model) {
        createVariables(model);
        setOneHot(model);

        Map<Integer, Integer> canonicalLimits = new HashMap<>();
        for (int k = 0; k < tot; k++) {
            int index = canonical.getCanonicalIndex(B[k]);
            canonicalLimits.compute(index, (ignore, val) -> {
               if (val == null) {
                   return 1;
               } else {
                   return val + 1;
               }
            });
        }

        for (int j = 0; j < canonical.size(); j++) {
            int[] selectCell = new int[tot];
            for (int m = 0; m < M; m++) {
                for (int n = 0; n < N; n++) {
                    selectCell[m * N + n] = J[m][n][j];
                }
            }
            int limit = canonicalLimits.getOrDefault(j, 0);
            model.addExactly(selectCell, limit);
        }

        // (-, -, <, >) -> J variable is true.
        // (J or ~Cond)
        // Cond = matches J.
        for (int m = 0; m < M; m++) {
            for (int n = 0; n < N; n++) {
                Wall[] walls = new Wall[SIDES];
                for (int s = 0; s < SIDES; s++) {
                    walls[s] = getWall(m, n, s);
                }

                PCGenerator pcGen = new PCGenerator(walls);
                pcGen.init();
                List<PieceConstraint> pieceConstraints = pcGen.pc;

                for (PieceConstraint pc : pieceConstraints) {
                    int canonicalIndex = canonical.getCanonicalIndex(pc.piece);
                    model.addBoolAndImplies(pc.constraint, J[m][n][canonicalIndex]);
                }
            }
        }
    }

    public List<JigsawLocation> solve(SatModel model) {
        Set<Integer> solution = model.solve();

        if (solution == null) {
            System.out.println("No solution exists");
            return null;
        }
        Jigsaw jigSaw = new Jigsaw(M, N);

        for (int m = 0; m < M; m++) {
            for (int n = 0; n < N; n++) {
                JigsawPoke[] pokes = jigSaw.pieces[m][n].pokes;
                for (int s = 0; s < SIDES; s++) {
                    pokes[s] = getWall(m, n, s).match(() -> JigsawPoke.FLAT,
                            (v) -> solution.contains(v) ? JigsawPoke.IN : JigsawPoke.OUT);
                }
            }
        }

        Map<Integer, Deque<Integer>> remapping = new TreeMap<>();

        for (int k = 0; k < tot; k++) {
            int index = canonical.getCanonicalIndex(B[k]);
            Deque<Integer> dq = remapping.computeIfAbsent(index, (ignore) -> new ArrayDeque<>());
            dq.push(k);
        }


        Map<Integer, Integer> regenCounter = new TreeMap<>();
        for (int m = 0; m < M; m++) {
            for (int n = 0; n < N; n++) {
                int index = canonical.getCanonicalIndex(jigSaw.pieces[m][n]);
                regenCounter.compute(index, (ignore, val) -> {
                    if (val == null) {
                        return 1;
                    } else {
                        return val + 1;
                    }
                });
            }
        }

        ArrayList<JigsawLocation> output = new ArrayList<>(tot);
        for (int i = 0; i < tot; i++) {
            output.add(null);
        }
        boolean hasError = false;



        for (int m = 0; m < M; m++) {
            for (int n = 0; n < N; n++) {
                int index = canonical.getCanonicalIndex(jigSaw.pieces[m][n]);
                Deque<Integer> bDq = remapping.get(index);
                if (bDq == null || bDq.isEmpty()) {
                    hasError = true;
                    break;
                }
                int bIndex = remapping.get(index).pop();
                // TODO determine rotation.
                output.set(bIndex, new JigsawLocation(m, n, 0));
            }
        }

        if (hasError) {
            System.out.println("formulation has bugs.");
            return null;
        }
        System.out.println("Reconstituted Diagram:");
        System.out.println(jigSaw);
        return output;
    }
}
