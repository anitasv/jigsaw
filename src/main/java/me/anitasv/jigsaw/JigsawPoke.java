package me.anitasv.jigsaw;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum JigsawPoke {
    FLAT(0, '-'),
    IN(1, '<'),
    OUT(-1, '>');

    public final int val;
    public final char rep;

    private static final Map<Character, JigsawPoke> repMap;

    static {
        Map<Character, JigsawPoke> innerMap = new HashMap<>();
        for (JigsawPoke poke : JigsawPoke.values()) {
            innerMap.put(poke.rep, poke);
        }
        repMap = Collections.unmodifiableMap(innerMap);
    }

    JigsawPoke(int val, char rep) {
        this.val = val;
        this.rep = rep;
    }

    public static JigsawPoke from(char rep) {
        return repMap.get(rep);
    }

    public JigsawPoke flip() {
        return switch (this) {
            case FLAT -> JigsawPoke.FLAT;
            case IN -> JigsawPoke.OUT;
            case OUT -> JigsawPoke.IN;
        };
    }
}
