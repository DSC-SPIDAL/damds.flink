package edu.indiana.soic.spidal.damds.flink;

public class ParallelOps {
    public static int nodeCount=1;
    public static int threadCount=1;

    // Number of memory mapped groups per process
    public static int mmapsPerNode;
    public static String mmapScratchDir;
}
