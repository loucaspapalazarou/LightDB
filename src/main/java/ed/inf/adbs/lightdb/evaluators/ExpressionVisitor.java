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

    private LongValue columnReferenceToLongValue(Column column, Tuple tuple, DatabaseCatalog catalog) {
        String columnName = column.getColumnName();
        String tableName = column.getTable().getName();
        int idx = catalog.getColumnIndex(tableName, columnName);
        return new LongValue().withValue(tuple.getValueAt(idx));
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(MinorThan minorThan) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        throw new UnsupportedOperationException();
    }

    // TODO: Abstract this method and just change the operator on the bottom
    // then just implement the rest of the methods here.
    @Override
    public void visit(GreaterThan greaterThan) {
        Expression left = greaterThan.getLeftExpression();
        Expression right = greaterThan.getRightExpression();

        LongValue leftValue, rightValue;

        if (left instanceof Column) {
            leftValue = columnReferenceToLongValue((Column) left, this.tuple, this.catalog);
        } else {
            leftValue = (LongValue) left;
        }

        if (right instanceof Column) {
            rightValue = columnReferenceToLongValue((Column) right, this.tuple, this.catalog);
        } else {
            rightValue = (LongValue) right;
        }
        updateResult(leftValue.getValue() > rightValue.getValue());
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(AndExpression andExpression) {
        throw new UnsupportedOperationException();
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
