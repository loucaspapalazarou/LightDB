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
import ed.inf.adbs.lightdb.type.Tuple;

/**
 * The ExpressionVisitor class is responsible for visiting expressions and
 * evaluating their truth value based on the given tuple.
 */
public class ExpressionVisitor extends ExpressionDeParser {

    private Tuple tuple; // Tuple used for evaluation
    private boolean result; // Result of the evaluation

    /**
     * Constructs an ExpressionVisitor object.
     * 
     * @param tuple the tuple to evaluate expressions against
     */
    public ExpressionVisitor(Tuple tuple) {
        this.tuple = tuple;
        this.result = true; // Initialize result to true
    }

    /**
     * Gets the result of the expression evaluation
     * 
     * @return the result of the expression evaluation
     */
    public boolean getResult() {
        return this.result;
    }

    /**
     * Updates the result based on the given expression. This system only supports
     * (non-nested) conjunctive queries, therefore to update the result we can
     * logically AND it with any new result.
     * 
     * @param expr the expression to update the result with
     */
    private void updateResult(boolean expr) {
        this.result &= expr;
    }

    /**
     * Converts an Expression to a LongValue if the expression is a Column
     * reference. If the expression is a LongValue, it is simply returned
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

    private static boolean checkIfColumnIsAvailable(Expression expression, Tuple tuple) {
        if (expression instanceof LongValue) {
            return true;
        }
        return tuple.getValueAt((Column) expression) == null ? false : true;
    }

    /**
     * Visits an EqualsTo expression.
     * 
     * @param equalsTo the EqualsTo expression to visit
     */
    @Override
    public void visit(EqualsTo equalsTo) {
        if (!checkIfColumnIsAvailable(equalsTo.getLeftExpression(), this.tuple)) {
            updateResult(true);
            return;
        }
        if (!checkIfColumnIsAvailable(equalsTo.getRightExpression(), this.tuple)) {
            updateResult(true);
            return;
        }
        LongValue leftValue = expressionToValue(equalsTo.getLeftExpression(), this.tuple);
        LongValue rightValue = expressionToValue(equalsTo.getRightExpression(), this.tuple);
        updateResult(leftValue.getValue() == rightValue.getValue());
    }

    /**
     * Visits a NotEqualsTo expression.
     * 
     * @param notEqualsTo the NotEqualsTo expression to visit
     */
    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        if (!checkIfColumnIsAvailable(notEqualsTo.getLeftExpression(), this.tuple)) {
            updateResult(true);
            return;
        }
        if (!checkIfColumnIsAvailable(notEqualsTo.getRightExpression(), this.tuple)) {
            updateResult(true);
            return;
        }
        LongValue leftValue = expressionToValue(notEqualsTo.getLeftExpression(), tuple);
        LongValue rightValue = expressionToValue(notEqualsTo.getRightExpression(), tuple);
        updateResult(leftValue.getValue() != rightValue.getValue());
    }

    /**
     * Visits a MinorThan expression.
     * 
     * @param minorThan the MinorThan expression to visit
     */
    @Override
    public void visit(MinorThan minorThan) {
        if (!checkIfColumnIsAvailable(minorThan.getLeftExpression(), this.tuple)) {
            updateResult(true);
            return;
        }
        if (!checkIfColumnIsAvailable(minorThan.getRightExpression(), this.tuple)) {
            updateResult(true);
            return;
        }
        LongValue leftValue = expressionToValue(minorThan.getLeftExpression(), tuple);
        LongValue rightValue = expressionToValue(minorThan.getRightExpression(), tuple);
        updateResult(leftValue.getValue() < rightValue.getValue());
    }

    /**
     * Visits a MinorThanEquals expression.
     * 
     * @param minorThanEquals the MinorThanEquals expression to visit
     */
    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        if (!checkIfColumnIsAvailable(minorThanEquals.getLeftExpression(), this.tuple)) {
            updateResult(true);
            return;
        }
        if (!checkIfColumnIsAvailable(minorThanEquals.getRightExpression(), this.tuple)) {
            updateResult(true);
            return;
        }
        LongValue leftValue = expressionToValue(minorThanEquals.getLeftExpression(), tuple);
        LongValue rightValue = expressionToValue(minorThanEquals.getRightExpression(), tuple);
        updateResult(leftValue.getValue() <= rightValue.getValue());
    }

    /**
     * Visits a GreaterThan expression.
     * 
     * @param greaterThan the GreaterThan expression to visit
     */
    @Override
    public void visit(GreaterThan greaterThan) {
        if (!checkIfColumnIsAvailable(greaterThan.getLeftExpression(), this.tuple)) {
            updateResult(true);
            return;
        }
        if (!checkIfColumnIsAvailable(greaterThan.getRightExpression(), this.tuple)) {
            updateResult(true);
            return;
        }
        LongValue leftValue = expressionToValue(greaterThan.getLeftExpression(), tuple);
        LongValue rightValue = expressionToValue(greaterThan.getRightExpression(), tuple);
        updateResult(leftValue.getValue() > rightValue.getValue());
    }

    /**
     * Visits a GreaterThanEquals expression.
     * 
     * @param greaterThanEquals the GreaterThanEquals expression to visit
     */
    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        if (!checkIfColumnIsAvailable(greaterThanEquals.getLeftExpression(), this.tuple)) {
            updateResult(true);
            return;
        }
        if (!checkIfColumnIsAvailable(greaterThanEquals.getRightExpression(), this.tuple)) {
            updateResult(true);
            return;
        }
        LongValue leftValue = expressionToValue(greaterThanEquals.getLeftExpression(), tuple);
        LongValue rightValue = expressionToValue(greaterThanEquals.getRightExpression(), tuple);
        updateResult(leftValue.getValue() >= rightValue.getValue());
    }

    /**
     * Visits an AndExpression.
     * 
     * @param andExpression the AndExpression to visit
     */
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
}
