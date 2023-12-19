package me.anitasv.jigsaw;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Jigsaw {

    public static final int SIDES = 4;
    public static final int TOP = 0, RIGHT = 1, BOT = 2, LEFT = 3;

    public final int M;
    public final int N;
    public final JigsawPiece[][] pieces;

    public Jigsaw(int M, int N) {
        this.M = M;
        this.N = N;
        this.pieces = new JigsawPiece[M][N];
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) {
                this.pieces[i][j] = new JigsawPiece();
            }
        }
        this.generate();
    }

    void generate() {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        // Flip LEFT | RIGHT banners.
        for (int m = 0; m < M; m++) {
            for (int n = 0; n < N - 1; n++) {
                JigsawPoke pokeRight = random.nextBoolean() ? JigsawPoke.IN : JigsawPoke.OUT;
                this.pieces[m][n].pokes[Jigsaw.RIGHT] = pokeRight;
                this.pieces[m][n + 1].pokes[Jigsaw.LEFT] = pokeRight.flip();
            }
        }
        for (int n = 0; n < N; n++) {
            for (int m = 0; m < M - 1; m++) {
                JigsawPoke pokeBot = random.nextBoolean() ? JigsawPoke.IN : JigsawPoke.OUT;
                this.pieces[m][n].pokes[Jigsaw.BOT] = pokeBot;
                this.pieces[m + 1][n].pokes[Jigsaw.TOP] = pokeBot.flip();
            }
        }
    }

    record RetainPos(int m, int n, JigsawPiece piece) {
    }

    /**
     * Retains original jigsaw structure as is.
     *
     * @return new random permutation+rotation of pieces.
     */
    public JigsawLocPiece[] shuffle() {
        List<RetainPos> moveAround = new ArrayList<>(M * N);
        for (int m = 0; m < M; m++) {
            for (int n = 0; n < N; n++) {
                moveAround.add(new RetainPos(m, n, pieces[m][n]));
            }
        }
        Collections.shuffle(moveAround, ThreadLocalRandom.current());

        JigsawLocPiece[] rotatePieces = new JigsawLocPiece[M * N];
        for (int i = 0; i < moveAround.size(); i++) {
            int k = ThreadLocalRandom.current().nextInt(Jigsaw.SIDES);
            RetainPos info = moveAround.get(i);
            JigsawPiece piece = new JigsawPiece();
            for (int j = 0; j < Jigsaw.SIDES; j++) {
                piece.pokes[j] = info.piece.pokes[(j + k) % Jigsaw.SIDES];
            }
            rotatePieces[i] = new JigsawLocPiece(new JigsawLocation(info.m, info.n, k), piece);
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
                switch (this.pieces[m][n].pokes[Jigsaw.TOP]) {
                    case FLAT:
                        builder.append("-");
                        break;
                    case IN:
                        builder.append("V");
                        break;
                    case OUT:
                        builder.append("^");
                        break;
                }
                builder.append("  ");
                if (n != N - 1) {
                    builder.append(" ");
                }
            }
            builder.append(" \n");
            for (int n = 0; n < N; n++) {
                switch (this.pieces[m][n].pokes[Jigsaw.LEFT]) {
                    case FLAT:
                        builder.append("|");
                        break;
                    case OUT:
                        builder.append("<");
                        break;
                    case IN:
                        builder.append(">");
                        break;
                }
                builder.append("     ");
                switch (this.pieces[m][n].pokes[Jigsaw.RIGHT]) {
                    case FLAT:
                        builder.append("|");
                        break;
                    case OUT:
                        builder.append(">");
                        break;
                    case IN:
                        builder.append("<");
                        break;
                }
            }
            builder.append("\n");

            for (int n = 0; n < N; n++) {
                builder.append("   ");
                switch (this.pieces[m][n].pokes[Jigsaw.BOT]) {
                    case FLAT:
                        builder.append("-");
                        break;
                    case OUT:
                        builder.append("V");
                        break;
                    case IN:
                        builder.append("^");
                        break;
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
