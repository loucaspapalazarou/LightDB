package ed.inf.adbs.lightdb.operators;

import java.io.PrintStream;

import ed.inf.adbs.lightdb.types.Tuple;

public abstract class Operator {
    public abstract Tuple getNextTuple();

    public abstract void reset();

    public void dump(PrintStream printStream) {
        Tuple nextTuple;
        while ((nextTuple = getNextTuple()) != null) {
            printStream.println(nextTuple.toString());
        }
    }
}