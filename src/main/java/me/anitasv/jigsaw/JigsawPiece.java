package me.anitasv.jigsaw;

import java.util.Arrays;

public class JigsawPiece {
    public final JigsawPoke[] pokes;

    public JigsawPiece() {
        this.pokes = new JigsawPoke[Jigsaw.SIDES];
        Arrays.fill(this.pokes, JigsawPoke.FLAT);
    }

    public JigsawPiece(JigsawPoke[] pokes) {
        this.pokes = pokes;
    }
}
