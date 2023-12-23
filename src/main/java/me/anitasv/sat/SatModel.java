package me.anitasv.sat;

import java.util.Set;

public interface SatModel {


    /**
     * Creates a boolean variable with given name.
     * Returned value will always be a positive integer.
     * Negative value of that integer will be treated as (not variable)
     *
     * @param name Not idempotent in all solvers, if same name is used a new variable may be returned.
     * @return positive integer.
     */
    int newVariable(String name);

    /**
     * Adds a constraint at least one of the LHS must be true.
     * This is a simple clause in CNF formulation.
     *
     * @param literals list of positive and negative integers.
     */
    void addBoolOr(int[] literals);


    void addBoolAndImplies(int[] literals, int literal);

    /**
     * Adds a constraint that at exactly one of the literals must be true. This may
     * create multiple clauses in a CNF solver.
     *
     * @param literals list of positive and negative integers.
     */
    void addExactlyOne(int[] literals);


    /**
     * Returns the solution as a set.
     * If integer belongs to this set that variable has value true.
     * Does not support looking up be negative numbers.
     *
     * @return set of true variables.
     */
    Set<Integer> solve();

    void addExactly(int[] selectCell, int sum);
}
