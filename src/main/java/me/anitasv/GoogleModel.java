package me.anitasv;

import com.google.ortools.sat.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GoogleModel implements SatModel {

    private final CpModel model = new CpModel();

    private final List<BoolVar> variables = new ArrayList<>();

    @Override
    public void addExactlyOne(int[] literals) {
        model.addExactlyOne(getInternalLiterals(literals));
    }

    @Override
    public void addBoolOr(int[] lhs) {
        model.addBoolOr(getInternalLiterals(lhs));
    }

    @Override
    public int newVariable(String name) {
        variables.add(model.newBoolVar(name));
        int idx = variables.size();
        return idx;
    }

    @Override
    public Set<Integer> solve() {
        CpSolver cpSolver = new CpSolver();
        CpSolverStatus status = cpSolver.solve(model);
        if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
            Set<Integer> trueValues = new HashSet<>();
            for (int i = 1; i < variables.size() + 1; i++) {
                if (cpSolver.booleanValue(getInternalVar(i))) {
                    trueValues.add(i);
                }
            }
            return trueValues;
        } else {
            return null;
        }
    }

    public Literal[] getInternalLiterals(int[] literals) {
        Literal[] internalLiterals = new Literal[literals.length];
        for (int i = 0; i < literals.length; i++) {
            internalLiterals[i] = getInternalVar(literals[i]);
        }
        return internalLiterals;
    }

    public CpModel getInternalModel() {
        return model;
    }

    public Literal getInternalVar(int idx) {
        if (idx > 0) {
            return variables.get(idx - 1);
        } else {
            return variables.get((-idx) - 1).not();
        }
    }
}
