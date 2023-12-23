package me.anitasv.jigsaw;

import me.anitasv.sat.SatModel;

import java.util.List;

public interface JigsawSolver {

    void formulate(SatModel model);

    List<JigsawLocation> solve(SatModel model);
}
