package ed.inf.adbs.lightdb.operator;

import ed.inf.adbs.lightdb.visitor.ExpressionVisitor;
import ed.inf.adbs.lightdb.type.Tuple;
import net.sf.jsqlparser.expression.Expression;

/**
 * The SelectOperator class represents an operator that filters tuples based on
 * a given expression. It applies the expression to each tuple retrieved from
 * its child operator and returns only those that satisfy the expression.
 */
public class SelectOperator extends Operator {

    private Operator child; // Child operator
    private Tuple currentTuple; // Current tuple being evaluated
    private Expression expression; // Expression to evaluate
    private ExpressionVisitor expressionVisitor; // Expression visitor to evaluate expressions

    /**
     * Constructs a SelectOperator with the specified child operator and expression.
     * 
     * @param child      the child operator
     * @param expression the expression to evaluate
     */
    public SelectOperator(Operator child, Expression expression) {
        this.child = child;
        this.expression = expression;
    }

    /**
     * Retrieves the next tuple from the operator that satisfies the expression.
     * 
     * @return the next tuple, or null if there are no more tuples
     */
    @Override
    public Tuple getNextTuple() {
        while (true) {
            Tuple nextTuple = child.getNextTuple();
            if (nextTuple == null) {
                return null;
            }
            if (evaluateExpression(nextTuple)) {
                this.currentTuple = nextTuple;
                return this.currentTuple;
            }
        }
    }

    /**
     * Evaluates the expression for the given tuple.
     * 
     * @param tuple the tuple to evaluate the expression against
     * @return true if the tuple satisfies the expression, false otherwise
     */
    public boolean evaluateExpression(Tuple tuple) {
        expressionVisitor = new ExpressionVisitor(tuple);
        try {
            this.expression.accept(expressionVisitor);
            return this.expressionVisitor.getResult();
        } catch (Exception e) {
            // Handle exception when where columns are not available yet. It might be the
            // case that this method is called during a join and the columns in the
            // expression do not exist in the current tuple. In this case an exception will
            // be thrown. Trivially, this means that the tuple cannot be discarded as the
            // result of the expression evaluation cannot be known, and thus the method
            // returns true
            return true;
        }
    }

    /**
     * Resets the state of the operator by resetting its child operator.
     */
    @Override
    public void reset() {
        this.currentTuple = null;
        this.child.reset();
    }
}
