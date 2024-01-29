package ed.inf.adbs.lightdb.operators;

import ed.inf.adbs.lightdb.evaluators.ExpressionEvaluator;
import ed.inf.adbs.lightdb.types.Tuple;
import ed.inf.adbs.lightdb.catalog.DatabaseCatalog;
import net.sf.jsqlparser.expression.Expression;

import java.util.Map;

public class SelectOperator extends Operator {
    private Operator child;
    private Expression expression;
    private Tuple currentTuple;
    private ExpressionEvaluator expressionEvaluator;
    private DatabaseCatalog catalog;

    public SelectOperator(Operator child, Expression expression, DatabaseCatalog catalog) {
        this.child = child;
        this.expression = expression;
        this.catalog = catalog;
        this.expressionEvaluator = new ExpressionEvaluator();
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
                this.currentTuple = nextTuple;
                return this.currentTuple;
            }
            // Continue to the next tuple from the child operator
        }
    }

    @Override
    public void reset() {
        // Reset the state of the SelectOperator
        this.currentTuple = null;
        this.child.reset();
    }

    boolean evaluateCondition(Tuple tuple) {
        this.expression.accept(expressionEvaluator);
        return true;
    }
}
