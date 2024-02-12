package ed.inf.adbs.lightdb.visitor;

import ed.inf.adbs.lightdb.type.Tuple;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

public class SumVisitor extends ExpressionDeParser {

    private int result;
    private Tuple tuple;

    public SumVisitor(Tuple tuple) {
        this.tuple = tuple;
        this.result = 1;
    }

    private void updateResult(LongValue value) {
        this.result *= value.getValue();
    }

    @Override
    public void visit(Multiplication multiplication) {
        Expression leftExpression = multiplication.getLeftExpression();
        Expression rightExpression = multiplication.getRightExpression();

        leftExpression.accept(this);
        LongValue rightValue = expressionToValue(rightExpression, tuple);

        updateResult(rightValue);
    }

    @Override
    public void visit(Column column) {
        updateResult(expressionToValue(column, tuple));
    }

    public void visit(LongValue longValue) {
        updateResult(longValue);
    }

    public int getResult() {
        return this.result;
    }

    private LongValue expressionToValue(Expression expression, Tuple tuple) {
        if (expression instanceof LongValue) {
            return (LongValue) expression;
        }
        return new LongValue((tuple.getValueAt((Column) expression)).longValue());
    }

}
