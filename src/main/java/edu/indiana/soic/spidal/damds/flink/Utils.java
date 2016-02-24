package edu.indiana.soic.spidal.damds.flink;

public class Utils {
    public static void printAndThrowRuntimeException(RuntimeException e) {
        e.printStackTrace(System.out);
        throw e;
    }

    public static void printMessage(String msg) {
        System.out.println(msg);
    }
}
