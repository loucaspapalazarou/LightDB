package ed.inf.adbs.lightdb.visitors;

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

public class SelectExpressionVisitor extends ExpressionDeParser {
    private DatabaseCatalog catalog;
    private Tuple tuple;
    private boolean result;

    public SelectExpressionVisitor(Tuple tuple, DatabaseCatalog catalog) {
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
        LongValue leftValue = Converter.expressionToLongValue(equalsTo.getLeftExpression(), tuple, catalog);
        LongValue rightValue = Converter.expressionToLongValue(equalsTo.getRightExpression(), tuple,
                catalog);
        updateResult(leftValue.getValue() == rightValue.getValue());
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        LongValue leftValue = Converter.expressionToLongValue(notEqualsTo.getLeftExpression(), tuple,
                catalog);
        LongValue rightValue = Converter.expressionToLongValue(notEqualsTo.getRightExpression(), tuple,
                catalog);
        updateResult(leftValue.getValue() != rightValue.getValue());
    }

    @Override
    public void visit(MinorThan minorThan) {
        LongValue leftValue = Converter.expressionToLongValue(minorThan.getLeftExpression(), tuple, catalog);
        LongValue rightValue = Converter.expressionToLongValue(minorThan.getRightExpression(), tuple,
                catalog);
        updateResult(leftValue.getValue() < rightValue.getValue());
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        LongValue leftValue = Converter.expressionToLongValue(minorThanEquals.getLeftExpression(), tuple,
                catalog);
        LongValue rightValue = Converter.expressionToLongValue(minorThanEquals.getRightExpression(), tuple,
                catalog);
        updateResult(leftValue.getValue() <= rightValue.getValue());
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        LongValue leftValue = Converter.expressionToLongValue(greaterThan.getLeftExpression(), tuple,
                catalog);
        LongValue rightValue = Converter.expressionToLongValue(greaterThan.getRightExpression(), tuple,
                catalog);
        updateResult(leftValue.getValue() > rightValue.getValue());
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        LongValue leftValue = Converter.expressionToLongValue(greaterThanEquals.getLeftExpression(), tuple,
                catalog);
        LongValue rightValue = Converter.expressionToLongValue(greaterThanEquals.getRightExpression(), tuple,
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
}
