package me.anitasv.jigsaw;

import java.util.Arrays;

public class JigsawPiece {
    public final JigsawPoke[] pokes = new JigsawPoke[Jigsaw.SIDES];

    public JigsawPiece() {
        Arrays.fill(this.pokes, JigsawPoke.FLAT);
    }
}
