package ed.inf.adbs.lightdb.operator;

import java.io.PrintStream;

import ed.inf.adbs.lightdb.type.Tuple;

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