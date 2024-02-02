package ed.inf.adbs.lightdb.evaluators;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import ed.inf.adbs.lightdb.catalog.DatabaseCatalog;
import ed.inf.adbs.lightdb.types.Tuple;

public class ExpressionVisitor extends ExpressionDeParser {
    private DatabaseCatalog catalog;
    private Tuple tuple;
    private boolean result;

    public ExpressionVisitor(Tuple tuple, DatabaseCatalog catalog) {
        this.catalog = catalog;
        this.tuple = tuple;
        this.result = true;
    }

    public boolean getResult() {
        return this.result;
    }

    private void updateResult(boolean expr) {
        this.result &= expr;
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        LongValue leftValue = expressionToLongValue(equalsTo.getLeftExpression(), tuple, catalog);
        LongValue rightValue = expressionToLongValue(equalsTo.getRightExpression(), tuple,
                catalog);
        updateResult(leftValue.getValue() == rightValue.getValue());
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        LongValue leftValue = expressionToLongValue(notEqualsTo.getLeftExpression(), tuple,
                catalog);
        LongValue rightValue = expressionToLongValue(notEqualsTo.getRightExpression(), tuple,
                catalog);
        updateResult(leftValue.getValue() != rightValue.getValue());
    }

    @Override
    public void visit(MinorThan minorThan) {
        LongValue leftValue = expressionToLongValue(minorThan.getLeftExpression(), tuple, catalog);
        LongValue rightValue = expressionToLongValue(minorThan.getRightExpression(), tuple,
                catalog);
        updateResult(leftValue.getValue() < rightValue.getValue());
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        LongValue leftValue = expressionToLongValue(minorThanEquals.getLeftExpression(), tuple,
                catalog);
        LongValue rightValue = expressionToLongValue(minorThanEquals.getRightExpression(), tuple,
                catalog);
        updateResult(leftValue.getValue() <= rightValue.getValue());
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        LongValue leftValue = expressionToLongValue(greaterThan.getLeftExpression(), tuple,
                catalog);
        LongValue rightValue = expressionToLongValue(greaterThan.getRightExpression(), tuple,
                catalog);
        updateResult(leftValue.getValue() > rightValue.getValue());
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        LongValue leftValue = expressionToLongValue(greaterThanEquals.getLeftExpression(), tuple,
                catalog);
        LongValue rightValue = expressionToLongValue(greaterThanEquals.getRightExpression(), tuple,
                catalog);
        updateResult(leftValue.getValue() >= rightValue.getValue());
    }

    @Override
    public void visit(AndExpression andExpression) {
        Expression leftExpression = andExpression.getLeftExpression();
        Expression rightExpression = andExpression.getRightExpression();

        leftExpression.accept(this);
        boolean leftResult = getResult();
        rightExpression.accept(this);
        boolean rightResult = getResult();

        updateResult(leftResult && rightResult);
    }

    @Override
    public void visit(Column column) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(LongValue longValue) {
        throw new UnsupportedOperationException();
    }

    private static LongValue columnReferenceToLongValue(Column column, Tuple tuple, DatabaseCatalog catalog) {
        int idx = catalog.getColumnIndex(column);
        return new LongValue().withValue(tuple.getValueAt(idx));
    }

    private static LongValue expressionToLongValue(Expression expression, Tuple tuple, DatabaseCatalog catalog) {
        if (expression instanceof Column) {
            return columnReferenceToLongValue((Column) expression, tuple, catalog);
        } else {
            return (LongValue) expression;
        }
    }
}
