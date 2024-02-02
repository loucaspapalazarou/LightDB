package ed.inf.adbs.lightdb.operators;

import ed.inf.adbs.lightdb.types.Tuple;
import ed.inf.adbs.lightdb.visitors.SelectExpressionVisitor;
import ed.inf.adbs.lightdb.catalog.DatabaseCatalog;
import net.sf.jsqlparser.expression.Expression;

public class SelectOperator extends Operator {
    private Operator child;
    private Expression expression;
    private Tuple currentTuple;
    private DatabaseCatalog catalog;
    private SelectExpressionVisitor expressionVisitor;

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
        expressionVisitor = new SelectExpressionVisitor(tuple, catalog);
        this.expression.accept(expressionVisitor);
        return this.expressionVisitor.getResult();
    }

    @Override
    public void reset() {
        this.currentTuple = null;
        this.child.reset();
    }
}
