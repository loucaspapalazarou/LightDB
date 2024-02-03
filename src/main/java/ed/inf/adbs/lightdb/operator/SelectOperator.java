package ed.inf.adbs.lightdb.operator;

import ed.inf.adbs.lightdb.visitor.ExpressionVisitor;
import ed.inf.adbs.lightdb.catalog.DatabaseCatalog;
import ed.inf.adbs.lightdb.type.Tuple;
import net.sf.jsqlparser.expression.Expression;

public class SelectOperator extends Operator {
    private Operator child;
    private Expression expression;
    private Tuple currentTuple;
    private DatabaseCatalog catalog;
    private ExpressionVisitor expressionVisitor;

    public SelectOperator(Operator child, Expression expression, DatabaseCatalog catalog) {
        this.child = child;
        this.expression = expression;
        this.catalog = catalog;
    }

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

    public boolean evaluateExpression(Tuple tuple) {
        expressionVisitor = new ExpressionVisitor(tuple, catalog);
        try {
            this.expression.accept(expressionVisitor);
            return this.expressionVisitor.getResult();
        } catch (Exception e) {
            // System.out.println("Where columns not available yet");
        }
        return true;
    }

    @Override
    public void reset() {
        this.currentTuple = null;
        this.child.reset();
    }
}
