package ed.inf.adbs.lightdb.visitors;

import ed.inf.adbs.lightdb.catalog.DatabaseCatalog;
import ed.inf.adbs.lightdb.types.Tuple;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.schema.Column;

public class Converter {

    public static LongValue expressionToLongValue(Expression expression, Tuple tuple, DatabaseCatalog catalog) {
        if (expression instanceof Column) {
            int idx = catalog.getColumnIndex((Column) expression);
            return new LongValue().withValue(tuple.getValueAt(idx));
        } else {
            return (LongValue) expression;
        }
    }

}
