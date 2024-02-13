package ed.inf.adbs.lightdb.query;

import java.io.PrintStream;

import ed.inf.adbs.lightdb.operator.Operator;

/**
 * The QueryPlan class represents a query plan that executes a query against the
 * database. It serves as a wrapper around the root operator that provides the
 * ability to evaluate the query plan in a specified printstream
 */
public class QueryPlan {

    private Operator rootOperator;

    /**
     * Constructs a QueryPlan object with the specified root operator.
     * 
     * @param op the root operator of the query plan
     */
    public QueryPlan(Operator op) {
        this.rootOperator = op;
    }

    /**
     * Evaluates the query plan and dumps the result to the specified print stream.
     * 
     * @param printStream the print stream to dump the result to
     */
    public void evaluate(PrintStream printStream) {
        this.rootOperator.dump(printStream);
    }
}
