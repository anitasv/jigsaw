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
        return switch (this) {
            case FLAT -> JigsawPoke.FLAT;
            case IN -> JigsawPoke.OUT;
            case OUT -> JigsawPoke.IN;
        };
    }
}
