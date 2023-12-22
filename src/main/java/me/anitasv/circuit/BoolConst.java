package me.anitasv.circuit;

public record BoolConst(int id) implements BoolExpr {

    @Override
    public String toString() {
        return "X_{" + id + "}";
    }
}
