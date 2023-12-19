package me.anitasv;

interface SatModel {
    void addExactlyOne(int[] literals);

    public void addBoolOr(int[] lhs);

    int newVariable(String name);
}
