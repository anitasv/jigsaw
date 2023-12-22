package me.anitasv.circuit;

import java.util.ArrayList;
import java.util.List;

public class Adder {

    List<BoolExpr> halfAdder(BoolExpr a,
                             BoolExpr b) {
        List<BoolExpr> result = new ArrayList<>();
        result.add(new BoolXor(a, b));
        result.add(new BoolAnd(a, b));
        return result;
    }

    List<BoolExpr> fullAdder(BoolExpr a,
                             BoolExpr b,
                             BoolExpr c) {
        if (c == null) {
            return halfAdder(a, b);
        }
        List<BoolExpr> result = new ArrayList<>();
        result.add(new BoolXor(new BoolXor(a, b), c));
        result.add(new BoolOr(new BoolAnd(a, b),
                new BoolOr(new BoolAnd(a, c),
                        new BoolAnd(b, c))));
        return result;
    }

    List<BoolExpr> increment(List<BoolExpr> a,
                             BoolExpr bit) {
        if (bit == null) {
            return a;
        }
        List<BoolExpr> result = new ArrayList<>();

        BoolExpr carry = bit;
        for (BoolExpr boolExpr : a) {
            result.add(new BoolXor(boolExpr, carry));
            carry = new BoolAnd(boolExpr, carry);
        }
        result.add(carry);

        return result;
    }

    List<BoolExpr> add(List<BoolExpr> a,
                       List<BoolExpr> b,
                       BoolExpr bit) {
        if (a.isEmpty()) {
            return increment(b, bit);
        } else if (b.isEmpty()) {
            return increment(a, bit);
        }
        List<BoolExpr> result = new ArrayList<>();

        int commonBits = Math.min(a.size(), b.size());

        BoolExpr carry = bit;
        for (int i = 0; i < commonBits; i++) {
            List<BoolExpr> out = fullAdder(a.get(i), b.get(i), carry);

            result.add(out.get(0));
            carry = out.get(1);
        }

        if (commonBits < a.size()) {
            result.addAll(increment(a.subList(commonBits, a.size()), carry));
        } else if (commonBits < b.size()) {
            result.addAll(increment(b.subList(commonBits, b.size()), carry));
        } else {
            result.add(carry);
        }

        return result;
    }

    List<BoolExpr> count(List<BoolExpr> X) {

        if (X.isEmpty()) {
            return List.of();
        }

        List<List<BoolExpr>> groups = new ArrayList<>();
        for (BoolExpr x : X) {
            groups.add(List.of(x));
        }

        while (groups.size() > 1) {
            List<List<BoolExpr>> ogGroups = groups;

            groups = new ArrayList<>();

            for (int i = 0; i < (ogGroups.size() / 2); i++) {
                groups.add(
                        add (ogGroups.get(2 * i), ogGroups.get(2 * i + 1), null));
            }
            if (ogGroups.size() % 2 == 1) {
                groups.add(ogGroups.get(ogGroups.size() - 1));
            }
        }

        return groups.get(0);
    }

    public static void main(String[] args) {

        List<BoolExpr> list = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            list.add(new BoolConst(i + 1));
        }
        System.out.println(new Adder().count(list));
    }
}
