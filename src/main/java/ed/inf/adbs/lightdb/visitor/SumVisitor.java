package ed.inf.adbs.lightdb.visitor;

import ed.inf.adbs.lightdb.type.Tuple;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

/**
 * The SumVisitor class is responsible for visiting expressions and computing
 * the sum of
 * values based on the given tuple.
 */
public class SumVisitor extends ExpressionDeParser {

    private int result; // Result of the summation
    private Tuple tuple; // Tuple to compute the sum for

    /**
     * Constructs a SumVisitor object with the given tuple.
     * 
     * @param tuple the tuple to compute the sum for
     */
    public SumVisitor(Tuple tuple) {
        this.tuple = tuple;
        this.result = 1; // Initialize result to 1
    }

    /**
     * Updates the result by multiplying it with the given LongValue.
     * 
     * @param value the LongValue to multiply with the result
     */
    private void updateResult(LongValue value) {
        this.result *= value.getValue();
    }

    /**
     * Visits a Multiplication expression.
     * 
     * @param multiplication the Multiplication expression to visit
     */
    @Override
    public void visit(Multiplication multiplication) {
        Expression leftExpression = multiplication.getLeftExpression();
        Expression rightExpression = multiplication.getRightExpression();

        leftExpression.accept(this);
        LongValue rightValue = expressionToValue(rightExpression, tuple);

        updateResult(rightValue);
    }

    /**
     * Visits a Column expression.
     * 
     * @param column the Column expression to visit
     */
    @Override
    public void visit(Column column) {
        updateResult(expressionToValue(column, tuple));
    }

    /**
     * Visits a LongValue expression.
     * 
     * @param longValue the LongValue expression to visit
     */
    public void visit(LongValue longValue) {
        updateResult(longValue);
    }

    /**
     * Gets the result of the summation.
     * 
     * @return the result of the summation
     */
    public int getResult() {
        return this.result;
    }

    /**
     * Converts an Expression to a LongValue.
     * 
     * @param expression the Expression to convert
     * @param tuple      the tuple from which to retrieve values
     * @return the LongValue representation of the expression
     */
    private LongValue expressionToValue(Expression expression, Tuple tuple) {
        if (expression instanceof LongValue) {
            return (LongValue) expression;
        }
        return new LongValue((tuple.getValueAt((Column) expression)).longValue());
    }

}
