package me.anitasv;

record JigsawLocation(int m, int n, int s) {

    @Override
    public String toString() {
        return "(" + m + "," + n + "," + s + ")";
    }
}
