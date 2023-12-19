package me.anitasv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

class JigSaw {

    public static final int SIDES = 4;
    public static final int TOP = 0, RIGHT = 1, BOT = 2, LEFT = 3;

    public final int M;
    public final int N;
    public final Piece[][] pieces;

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
                this.pieces[m][n].pokes[JigSaw.RIGHT] = pokeRight;
                this.pieces[m][n + 1].pokes[JigSaw.LEFT] = pokeRight.flip();
            }
        }
        for (int n = 0; n < N; n++) {
            for (int m = 0; m < M - 1; m++) {
                Poke pokeBot = random.nextBoolean() ? Poke.IN : Poke.OUT;
                this.pieces[m][n].pokes[JigSaw.BOT] = pokeBot;
                this.pieces[m + 1][n].pokes[JigSaw.TOP] = pokeBot.flip();
            }
        }
    }

    record RetainPos(int m, int n, Piece piece) {
    }

    record RetainPosRot(JigsawLocation loc, Piece piece) {
    }

    /**
     * Retains original jigsaw structure as is.
     *
     * @return new random permutation+rotation of pieces.
     */
    public RetainPosRot[] shuffle() {
        List<RetainPos> moveAround = new ArrayList<>(M * N);
        for (int m = 0; m < M; m++) {
            for (int n = 0; n < N; n++) {
                moveAround.add(new RetainPos(m, n, pieces[m][n]));
            }
        }
        Collections.shuffle(moveAround, ThreadLocalRandom.current());

        RetainPosRot[] rotatePieces = new RetainPosRot[M * N];
        for (int i = 0; i < moveAround.size(); i++) {
            int k = ThreadLocalRandom.current().nextInt(JigSaw.SIDES);
            RetainPos info = moveAround.get(i);
            Piece piece = new Piece();
            for (int j = 0; j < JigSaw.SIDES; j++) {
                piece.pokes[j] = info.piece.pokes[(j + k) % JigSaw.SIDES];
            }
            rotatePieces[i] = new RetainPosRot(new JigsawLocation(info.m, info.n, k), piece);
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
                switch (this.pieces[m][n].pokes[JigSaw.TOP]) {
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
                switch (this.pieces[m][n].pokes[JigSaw.LEFT]) {
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
                switch (this.pieces[m][n].pokes[JigSaw.RIGHT]) {
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
                switch (this.pieces[m][n].pokes[JigSaw.BOT]) {
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
