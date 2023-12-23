package me.anitasv.jigsaw;

public interface JigsawSolverFactory {

    JigsawSolver newSolver(int M, int N, JigsawPiece[] B);
}
