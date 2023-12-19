package me.anitasv;

import java.util.Set;

interface SatModel {
    void addExactlyOne(int[] literals);

    public void addBoolOr(int[] lhs);

    int newVariable(String name);

    /**
     * Returns the solution as a set.
     *
     * If integer belongs to this set that variable has value true.
     * Otherwise false.
     *
     * @return set of true variables.
     */
    Set<Integer> solve();
}
