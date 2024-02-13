package ed.inf.adbs.lightdb.operator;

import java.io.PrintStream;

import ed.inf.adbs.lightdb.type.Tuple;

/**
 * The Operator class represents an abstract operator in the query execution
 * plan.
 * It provides methods for retrieving tuples, resetting the operator, and
 * dumping
 * the output to a print stream.
 */
public abstract class Operator {

    /**
     * Retrieves the next tuple from the operator.
     * 
     * @return the next tuple, or null if there are no more tuples
     */
    public abstract Tuple getNextTuple();

    /**
     * Resets the state of the operator.
     */
    public abstract void reset();

    /**
     * Dumps the output of the operator to the specified print stream.
     * 
     * @param printStream the print stream to dump the output to
     */
    public void dump(PrintStream printStream) {
        Tuple nextTuple;
        while ((nextTuple = getNextTuple()) != null) {
            printStream.println(nextTuple.toString());
        }
    }
}
