package me.anitasv.jigsaw;

public enum JigsawPoke {
    FLAT(0),
    IN(1),
    OUT(-1);

    public final int val;

    JigsawPoke(int val) {
        this.val = val;
    }


    public JigsawPoke flip() {
        switch (this) {
            case FLAT:
                return JigsawPoke.FLAT;
            case IN:
                return JigsawPoke.OUT;
            case OUT:
                return JigsawPoke.IN;
            default:
                return this;
        }
    }
}
