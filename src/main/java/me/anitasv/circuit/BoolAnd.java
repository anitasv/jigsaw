package me.anitasv.circuit;

public record BoolAnd(BoolExpr a, BoolExpr b) implements BoolExpr {


    @Override
    public String toString() {
        return "(" + a.toString() + " \\land " + b.toString() + ")";
    }
}
