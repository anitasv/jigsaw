package me.anitasv;

import com.google.ortools.Loader;
import com.google.ortools.sat.BoolVar;
import me.anitasv.sat.GoogleModel;

import java.util.ArrayDeque;
import java.util.Deque;

public class Experiments {

    public static void main(String[] args) {

//        Loader.loadNativeLibraries();
//
//        GoogleModel gm = new GoogleModel();
//        int[] x = new int[10];
//        for (int i = 0; i < x.length; i++) {
//            x[i] = gm.newVariable("X_{" + i + "}");
//        }
//        gm.addExactly(x, 3);
//
//        System.out.println(gm.solve());

        Deque<Integer> dq = new ArrayDeque<>();
        dq.push(1);
        dq.push(2);
        dq.push(3);

        System.out.println(dq);
    }
}
