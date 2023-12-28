package me.anitasv.jigsaw;

import me.anitasv.algo.DisjointSet;

import java.math.BigInteger;
import java.util.*;

public class JigsawCanonical {

    private final Map<List<JigsawPoke>, Integer> pieceIndex = new HashMap<>();
    private final int size;
    private final int interiorSize;
    private final int borderSize;

    public JigsawCanonical() {
        DisjointSet<List<JigsawPoke>> borderCanonicals = new DisjointSet<>();
        DisjointSet<List<JigsawPoke>> interiorCanonicals = new DisjointSet<>();

        JigsawPoke[] pokes = JigsawPoke.values();
        int pokeCount = pokes.length;
        // It just sucks there is no math.intPow()
        int totalCubes = BigInteger.valueOf(pokeCount).pow(Jigsaw.SIDES).intValue();

        for (int i = 0; i < totalCubes; i++) {
            List<JigsawPoke> side = new ArrayList<>();
            int elem = i;
            boolean hasFlat = false;
            for (int j = 0; j < Jigsaw.SIDES; j++) {
                JigsawPoke poke = pokes[elem % pokeCount];
                side.add(poke);
                elem = elem / pokeCount;
                if (poke == JigsawPoke.FLAT) {
                    hasFlat = true;
                }
            }
            if (hasFlat) {
                borderCanonicals.add(side);
            } else {
                interiorCanonicals.add(side);
            }
        }

        for (List<JigsawPoke> str : borderCanonicals.ogElements()) {
            for (int i = 0; i < Jigsaw.SIDES; i++) {
                List<JigsawPoke> rotated = new ArrayList<>();
                rotated.addAll(str.subList(i, Jigsaw.SIDES));
                rotated.addAll(str.subList(0, i));
                borderCanonicals.union(str, rotated);
            }
        }

        for (List<JigsawPoke> str : interiorCanonicals.ogElements()) {
            for (int i = 0; i < Jigsaw.SIDES; i++) {
                List<JigsawPoke> rotated = new ArrayList<>();
                rotated.addAll(str.subList(i, Jigsaw.SIDES));
                rotated.addAll(str.subList(0, i));
                interiorCanonicals.union(str, rotated);
            }
        }

        Map<List<JigsawPoke>, Integer> knownRoots = new HashMap<>();
        int nextIndex = 0;

        for (List<JigsawPoke> str : borderCanonicals.ogElements()) {
            List<JigsawPoke> canonical = borderCanonicals.findCanonical(str);
            Integer currentIndex = knownRoots.get(canonical);
            if (currentIndex == null) {
                currentIndex = nextIndex;
                knownRoots.put(canonical, currentIndex);
                nextIndex++;
            }
            pieceIndex.put(str, currentIndex);
        }
        borderSize = nextIndex;

        for (List<JigsawPoke> str : interiorCanonicals.ogElements()) {
            List<JigsawPoke> canonical = interiorCanonicals.findCanonical(str);
            Integer currentIndex = knownRoots.get(canonical);
            if (currentIndex == null) {
                currentIndex = nextIndex;
                knownRoots.put(canonical, currentIndex);
                nextIndex++;
            }
            pieceIndex.put(str, currentIndex);
        }

        size = nextIndex;
        interiorSize = size - borderSize;
    }

    public int getCanonicalIndex(JigsawPiece sides) {
        return pieceIndex.get(Arrays.asList(sides.pokes));
    }

    public int size() {
        return size;
    }

    public int interiorSize() {
        return interiorSize;
    }

    public int borderSize() {
        return borderSize;
    }
}
