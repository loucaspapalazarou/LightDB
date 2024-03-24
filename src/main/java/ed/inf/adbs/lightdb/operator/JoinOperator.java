package ed.inf.adbs.lightdb.operator;

import ed.inf.adbs.lightdb.type.Tuple;
import ed.inf.adbs.lightdb.visitor.ExpressionVisitor;
import net.sf.jsqlparser.expression.Expression;

/**
 * The JoinOperator class represents an operator that performs a join operation
 * between two child operators. It applies the given expression to each pair of
 * tuples retrieved from its child operators and returns only those that satisfy
 * the expression.
 */
public class JoinOperator extends Operator {

    private Operator left; // Left child operator
    private Operator right; // Right child operator
    private Expression expression; // Join condition expression
    private Tuple currentLeftTuple; // Current left tuple being processed
    private ExpressionVisitor expressionVisitor; // Expression visitor to evaluate expressions

    /**
     * Constructs a JoinOperator with the specified left and right child operators,
     * and join condition expression.
     * 
     * @param left       the left child operator
     * @param right      the right child operator
     * @param expression the join condition expression
     */
    public JoinOperator(Operator left, Operator right, Expression expression) {
        this.left = left;
        this.right = right;
        this.expression = expression;
        this.currentLeftTuple = null;
    }

    /**
     * Retrieves the next tuple from the operator that satisfies the join condition.
     * 
     * @return the next joined tuple, or null if there are no more tuples
     */
    @Override
    public Tuple getNextTuple() {
        // If there is no current left tuple, try to retrieve one from the left child
        // operator
        if (this.currentLeftTuple == null) {
            this.currentLeftTuple = this.left.getNextTuple();
        }
        // If there are no more tuples from the left child operator, return null
        if (this.currentLeftTuple == null) {
            return null;
        }
        // Try to retrieve the next tuple from the right child operator
        Tuple rightTuple = this.right.getNextTuple();
        // If there are no more tuples from the right child operator, reset it and try
        // again with the next left tuple
        if (rightTuple == null) {
            this.right.reset();
            this.currentLeftTuple = this.left.getNextTuple();
            return this.getNextTuple();
        }
        // If both left and right tuples are not null, concatenate them
        Tuple mergedTuple = Tuple.concatTuples(this.currentLeftTuple, rightTuple);
        // If the merged tuple satisfies the join condition, return it
        if (evaluateExpression(mergedTuple)) {
            return mergedTuple;
        }
        // Otherwise, try again with the next right tuple
        return this.getNextTuple();
    }

    /**
     * Resets the state of the operator by resetting both child operators and
     * clearing the current left tuple.
     */
    @Override
    public void reset() {
        this.left.reset();
        this.right.reset();
        this.currentLeftTuple = null;
    }

    /**
     * Evaluates the given expression for the given tuple.
     * 
     * @param tuple      the tuple to evaluate the expression against
     * @param expression the expression to evaluate
     * @return true if the tuple satisfies the expression or expression cannot be
     *         evaluated, false otherwise
     */
    public boolean evaluateExpression(Tuple tuple) {
        if (expression == null) {
            return true;
        }
        // System.out.println(tuple + "\t" + this.expression);
        expressionVisitor = new ExpressionVisitor(tuple);
        this.expression.accept(expressionVisitor);
        return this.expressionVisitor.getResult();
    }

}
