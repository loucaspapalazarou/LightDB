package ed.inf.adbs.lightdb.visitors;

import ed.inf.adbs.lightdb.catalog.DatabaseCatalog;
import ed.inf.adbs.lightdb.types.Tuple;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.schema.Column;

public class Converter {

    private static LongValue columnReferenceToLongValue(Column column, Tuple tuple, DatabaseCatalog catalog) {
        int idx = catalog.getColumnIndex(column);
        return new LongValue().withValue(tuple.getValueAt(idx));
    }

    public static LongValue expressionToLongValue(Expression expression, Tuple tuple, DatabaseCatalog catalog) {
        if (expression instanceof Column) {
            return columnReferenceToLongValue((Column) expression, tuple, catalog);
        } else {
            return (LongValue) expression;
        }
    }

}
