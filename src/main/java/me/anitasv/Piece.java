package me.anitasv;

import java.util.Arrays;

class Piece {
    public final Poke[] pokes = new Poke[JigSaw.SIDES];

    public Piece() {
        Arrays.fill(this.pokes, Poke.FLAT);
    }
}
