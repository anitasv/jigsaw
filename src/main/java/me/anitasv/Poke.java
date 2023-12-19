package me.anitasv;

enum Poke {
    FLAT(0),
    IN(1),
    OUT(-1);

    public final int val;

    Poke(int val) {
        this.val = val;
    }


    Poke flip() {
        switch (this) {
            case FLAT:
                return Poke.FLAT;
            case IN:
                return Poke.OUT;
            case OUT:
                return Poke.IN;
            default:
                return this;
        }
    }
}
