package me.anitasv.circuit;

public record BoolOr(BoolExpr a, BoolExpr b) implements BoolExpr {

    @Override
    public String toString() {
        return "(" + a.toString() + " \\lor " + b.toString() + ")";
    }
}
