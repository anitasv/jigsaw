package me.anitasv.circuit;

public record BoolXor(BoolExpr a, BoolExpr b) implements BoolExpr {

    @Override
    public String toString() {
        return "(" + a.toString() + " \\oplus " + b.toString() + ")";
    }
}
