package me.anitasv.jigsaw;

import me.anitasv.algo.DisjointSet;

import java.math.BigInteger;
import java.util.*;

public class JigsawCanonical {

    private final Map<List<JigsawPoke>, Integer> pieceIndex = new HashMap<>();
    private final int size;

    public JigsawCanonical() {
        DisjointSet<List<JigsawPoke>> canonicals = new DisjointSet<>();

        JigsawPoke[] pokes = JigsawPoke.values();
        int pokeCount = pokes.length;
        // It just sucks there is no math.intPow()
        int totalCubes = BigInteger.valueOf(pokeCount).pow(Jigsaw.SIDES).intValue();

        for (int i = 0; i < totalCubes; i++) {
            List<JigsawPoke> side = new ArrayList<>();
            int elem = i;
            for (int j = 0; j < Jigsaw.SIDES; j++) {
                side.add(pokes[elem % pokeCount]);
                elem = elem / pokeCount;
            }
            canonicals.add(side);
        }

        for (List<JigsawPoke> str : canonicals.ogElements()) {
            for (int i = 0; i < Jigsaw.SIDES; i++) {
                List<JigsawPoke> rotated = new ArrayList<>();
                rotated.addAll(str.subList(i, Jigsaw.SIDES));
                rotated.addAll(str.subList(0, i));
                canonicals.union(str, rotated);
            }
        }

        Map<List<JigsawPoke>, Integer> knownRoots = new HashMap<>();
        int nextIndex = 0;

        for (List<JigsawPoke> str : canonicals.ogElements()) {
            List<JigsawPoke> canonical = canonicals.findCanonical(str);
            Integer currentIndex = knownRoots.get(canonical);
            if (currentIndex == null) {
                currentIndex = nextIndex;
                knownRoots.put(canonical, currentIndex);
                nextIndex++;
            }
            pieceIndex.put(str, currentIndex);
        }
        size = nextIndex;
    }

    public int getCanonicalIndex(JigsawPiece sides) {
        return pieceIndex.get(Arrays.asList(sides.pokes));
    }

    public int size() {
        return size;
    }

}
