package ed.inf.adbs.lightdb.operators;

import ed.inf.adbs.lightdb.types.Tuple;

public class SelectOperator extends Operator {
    private Operator child;
    private Expression condition;
    private Tuple currentTuple;

    public SelectOperator(Operator child, Expression condition) {
        this.child = child;
        this.condition = condition;
    }

    @Override
    public Tuple getNextTuple() {
        while (true) {
            Tuple nextTuple = child.getNextTuple();

            if (nextTuple == null) {
                // No more tuples from the child operator
                return null;
            }

            if (evaluateCondition(nextTuple)) {
                // Tuple satisfies the condition, return it
                currentTuple = nextTuple;
                return currentTuple;
            }
            // Continue to the next tuple from the child operator
        }
    }

    @Override
    void reset() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'reset'");
    }
}