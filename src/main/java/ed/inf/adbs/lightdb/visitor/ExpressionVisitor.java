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
        LongValue leftValue = exppressionToValue(equalsTo.getLeftExpression(), this.tuple);
        LongValue rightValue = exppressionToValue(equalsTo.getRightExpression(), this.tuple);
        updateResult(leftValue.getValue() == rightValue.getValue());
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        LongValue leftValue = exppressionToValue(notEqualsTo.getLeftExpression(), tuple);
        LongValue rightValue = exppressionToValue(notEqualsTo.getRightExpression(), tuple);
        updateResult(leftValue.getValue() != rightValue.getValue());
    }

    @Override
    public void visit(MinorThan minorThan) {
        LongValue leftValue = exppressionToValue(minorThan.getLeftExpression(), tuple);
        LongValue rightValue = exppressionToValue(minorThan.getRightExpression(), tuple);
        updateResult(leftValue.getValue() < rightValue.getValue());
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        LongValue leftValue = exppressionToValue(minorThanEquals.getLeftExpression(), tuple);
        LongValue rightValue = exppressionToValue(minorThanEquals.getRightExpression(), tuple);
        updateResult(leftValue.getValue() <= rightValue.getValue());
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        LongValue leftValue = exppressionToValue(greaterThan.getLeftExpression(), tuple);
        LongValue rightValue = exppressionToValue(greaterThan.getRightExpression(), tuple);
        updateResult(leftValue.getValue() > rightValue.getValue());
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        LongValue leftValue = exppressionToValue(greaterThanEquals.getLeftExpression(), tuple);
        LongValue rightValue = exppressionToValue(greaterThanEquals.getRightExpression(), tuple);
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

    private LongValue exppressionToValue(Expression expression, Tuple tuple) {
        if (expression instanceof LongValue) {
            return (LongValue) expression;
        }
        return new LongValue((tuple.getValueAt((Column) expression)).longValue());
    }
}
