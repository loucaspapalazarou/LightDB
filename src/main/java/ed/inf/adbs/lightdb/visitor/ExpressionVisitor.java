package ed.inf.adbs.lightdb.visitor;

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
import ed.inf.adbs.lightdb.type.Tuple;

public class ExpressionVisitor extends ExpressionDeParser {
    private Tuple tuple;
    private boolean result;

    public ExpressionVisitor(Tuple tuple, DatabaseCatalog catalog) {
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
        LongValue leftValue = expressionToValue(equalsTo.getLeftExpression(), this.tuple);
        LongValue rightValue = expressionToValue(equalsTo.getRightExpression(), this.tuple);
        updateResult(leftValue.getValue() == rightValue.getValue());
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        LongValue leftValue = expressionToValue(notEqualsTo.getLeftExpression(), tuple);
        LongValue rightValue = expressionToValue(notEqualsTo.getRightExpression(), tuple);
        updateResult(leftValue.getValue() != rightValue.getValue());
    }

    @Override
    public void visit(MinorThan minorThan) {
        LongValue leftValue = expressionToValue(minorThan.getLeftExpression(), tuple);
        LongValue rightValue = expressionToValue(minorThan.getRightExpression(), tuple);
        updateResult(leftValue.getValue() < rightValue.getValue());
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        LongValue leftValue = expressionToValue(minorThanEquals.getLeftExpression(), tuple);
        LongValue rightValue = expressionToValue(minorThanEquals.getRightExpression(), tuple);
        updateResult(leftValue.getValue() <= rightValue.getValue());
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        LongValue leftValue = expressionToValue(greaterThan.getLeftExpression(), tuple);
        LongValue rightValue = expressionToValue(greaterThan.getRightExpression(), tuple);
        updateResult(leftValue.getValue() > rightValue.getValue());
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        LongValue leftValue = expressionToValue(greaterThanEquals.getLeftExpression(), tuple);
        LongValue rightValue = expressionToValue(greaterThanEquals.getRightExpression(), tuple);
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

    private LongValue expressionToValue(Expression expression, Tuple tuple) {
        if (expression instanceof LongValue) {
            return (LongValue) expression;
        }
        return new LongValue((tuple.getValueAt((Column) expression)).longValue());
    }
}
