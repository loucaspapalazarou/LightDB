package ed.inf.adbs.lightdb.query;

import java.io.PrintStream;

import ed.inf.adbs.lightdb.operator.Operator;

public class QueryPlan {

    private Operator rootOperator;

    public QueryPlan(Operator s) {
        this.rootOperator = s;
    }

    public void evaluate(PrintStream printStream) {
        this.rootOperator.dump(printStream);
        this.rootOperator.reset();
    }
}
