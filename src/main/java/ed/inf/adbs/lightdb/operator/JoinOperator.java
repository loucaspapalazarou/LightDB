package ed.inf.adbs.lightdb.operator;

import ed.inf.adbs.lightdb.type.Tuple;
import ed.inf.adbs.lightdb.visitor.ExpressionVisitor;
import net.sf.jsqlparser.expression.Expression;

public class JoinOperator extends Operator {

    private Operator left;
    private Operator right;
    private Expression expression;
    private Tuple currentLeftTuple;
    private ExpressionVisitor expressionVisitor;

    public JoinOperator(Operator left, Operator right, Expression expression) {
        this.left = left;
        this.right = right;
        this.expression = expression;
        this.currentLeftTuple = null;
    }

    // PROBLEM HERE?
    @Override
    public Tuple getNextTuple() {
        // if you don't have a left tuple try and get one
        if (this.currentLeftTuple == null) {
            this.currentLeftTuple = this.left.getNextTuple();
        }
        // if you tried and still got none, that means left child is done
        if (this.currentLeftTuple == null) {
            return null;
        }
        // if you have a left tuple, try and get a right one
        Tuple rightTuple = this.right.getNextTuple();
        // if it's null, get a new left tuple, reset the right and try again
        if (rightTuple == null) {
            this.right.reset();
            this.currentLeftTuple = this.left.getNextTuple();
            return this.getNextTuple();
        }
        // if you are here it means the left and right tuple are not null
        // thus, try and concatenate them
        Tuple newTuple = Tuple.concatTuples(this.currentLeftTuple, rightTuple);
        // if the new tuple passes the where condition, return it
        if (tuplesSatisfyExpression(newTuple, this.expression)) {
            return newTuple;
        }
        // otherwise return the next tuple
        return this.getNextTuple();
    }

    @Override
    public void reset() {
        this.left.reset();
        this.right.reset();
        this.currentLeftTuple = this.left.getNextTuple();
    }

    private boolean tuplesSatisfyExpression(Tuple tempTuple, Expression expression) {
        if (expression == null) {
            return true;
        }
        return evaluateExpression(tempTuple);
    }

    public boolean evaluateExpression(Tuple tuple) {
        expressionVisitor = new ExpressionVisitor(tuple);
        this.expression.accept(expressionVisitor);
        return this.expressionVisitor.getResult();
    }

}
