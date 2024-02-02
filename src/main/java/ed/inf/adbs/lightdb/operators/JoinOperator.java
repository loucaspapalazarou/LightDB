package ed.inf.adbs.lightdb.operators;

import ed.inf.adbs.lightdb.catalog.DatabaseCatalog;
import ed.inf.adbs.lightdb.types.Tuple;
import ed.inf.adbs.lightdb.visitors.JoinExpressionVisitor;
import net.sf.jsqlparser.expression.Expression;

public class JoinOperator extends Operator {

    private Operator left;
    private Operator right;
    private Expression expression;
    private DatabaseCatalog catalog;
    private Tuple currentLeftTuple;
    private JoinExpressionVisitor expressionVisitor;

    public JoinOperator(Operator left, Operator right, Expression whereExpression, DatabaseCatalog catalog) {
        this.left = left;
        this.right = right;
        this.expression = whereExpression;
        this.catalog = catalog;
        this.currentLeftTuple = this.left.getNextTuple();
    }

    @Override
    public Tuple getNextTuple() {
        Tuple rightTuple = this.right.getNextTuple();
        if (rightTuple == null) {
            this.right.reset();
            this.currentLeftTuple = this.left.getNextTuple();
            rightTuple = this.right.getNextTuple();
        }
        if (this.currentLeftTuple == null) {
            return null;
        }
        if (tuplesSatisfyExpression(this.currentLeftTuple, rightTuple, this.expression)) {
            return Tuple.concatTuples(this.currentLeftTuple, rightTuple);
        }
        return this.getNextTuple();

    }

    @Override
    public void reset() {
        this.left.reset();
        this.right.reset();
        this.currentLeftTuple = this.left.getNextTuple();
    }

    private boolean tuplesSatisfyExpression(Tuple leftTuple, Tuple rightTuple, Expression expression) {
        return evaluateExpression(leftTuple, rightTuple);
    }

    public boolean evaluateExpression(Tuple leftTuple, Tuple righTuple) {
        expressionVisitor = new JoinExpressionVisitor(leftTuple, righTuple, catalog);
        this.expression.accept(expressionVisitor);
        return this.expressionVisitor.getResult();
    }

}
