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

public class JoinExpressionVisitor extends ExpressionDeParser {
    private DatabaseCatalog catalog;
    private Tuple leftTuple;
    private Tuple rightTuple;
    private boolean result;

    public JoinExpressionVisitor(Tuple leftTuple, Tuple rightTuple, DatabaseCatalog catalog) {
        this.catalog = catalog;
        this.leftTuple = leftTuple;
        this.rightTuple = rightTuple;
        this.result = true;
    }

    public JoinExpressionVisitor(Tuple leftTuple, DatabaseCatalog catalog) {
        this.catalog = catalog;
        this.leftTuple = leftTuple;
        this.rightTuple = null;
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
        LongValue leftValue = Converter.expressionToLongValue(equalsTo.getLeftExpression(), leftTuple, catalog);
        LongValue rightValue = Converter.expressionToLongValue(equalsTo.getRightExpression(), rightTuple,
                catalog);
        updateResult(leftValue.getValue() == rightValue.getValue());
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        LongValue leftValue = Converter.expressionToLongValue(notEqualsTo.getLeftExpression(), leftTuple,
                catalog);
        LongValue rightValue = Converter.expressionToLongValue(notEqualsTo.getRightExpression(), rightTuple,
                catalog);
        updateResult(leftValue.getValue() != rightValue.getValue());
    }

    @Override
    public void visit(MinorThan minorThan) {
        LongValue leftValue = Converter.expressionToLongValue(minorThan.getLeftExpression(), leftTuple, catalog);
        LongValue rightValue = Converter.expressionToLongValue(minorThan.getRightExpression(), rightTuple,
                catalog);
        updateResult(leftValue.getValue() < rightValue.getValue());
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        LongValue leftValue = Converter.expressionToLongValue(minorThanEquals.getLeftExpression(), leftTuple,
                catalog);
        LongValue rightValue = Converter.expressionToLongValue(minorThanEquals.getRightExpression(), rightTuple,
                catalog);
        updateResult(leftValue.getValue() <= rightValue.getValue());
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        LongValue leftValue = Converter.expressionToLongValue(greaterThan.getLeftExpression(), leftTuple,
                catalog);
        LongValue rightValue = Converter.expressionToLongValue(greaterThan.getRightExpression(), rightTuple,
                catalog);
        updateResult(leftValue.getValue() > rightValue.getValue());
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        LongValue leftValue = Converter.expressionToLongValue(greaterThanEquals.getLeftExpression(), leftTuple,
                catalog);
        LongValue rightValue = Converter.expressionToLongValue(greaterThanEquals.getRightExpression(), rightTuple,
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
