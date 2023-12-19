package me.anitasv;

import com.google.ortools.sat.BoolVar;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.Literal;

import java.util.ArrayList;
import java.util.List;

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
