package me.anitasv.jigsaw;

public record JigsawLocation(int m, int n, int s) {

    @Override
    public String toString() {
        return m + "," + n + "," + s;
    }
}
