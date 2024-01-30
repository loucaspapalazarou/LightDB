package ed.inf.adbs.lightdb.evaluators;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import ed.inf.adbs.lightdb.catalog.DatabaseCatalog;
import ed.inf.adbs.lightdb.types.Tuple;

public class ExpressionVisitor extends ExpressionDeParser {
    private DatabaseCatalog catalog;
    private boolean result;
    private Tuple tuple;

    public ExpressionVisitor(Tuple tuple, DatabaseCatalog catalog) {
        this.catalog = catalog;
        this.tuple = tuple;
    }

    @Override
    public void visit(Column column) {
        referenceColumnToNumericColumn(column, tuple, catalog);
    }

    public boolean getResult() {
        return result;
    }

    private Column referenceColumnToNumericColumn(Column column, Tuple tuple, DatabaseCatalog catalog) {
        String columnName = column.getColumnName();
        String tableName = column.getTable().getName();
        System.out.println(tableName + " " + columnName);

        int idx = catalog.getColumnIndex(tableName, columnName);
        int value = tuple.getValueAt(idx);
        System.out.println(value);
        return null;
    }
}
