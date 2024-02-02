package ed.inf.adbs.lightdb.operators;

import java.util.List;
import ed.inf.adbs.lightdb.catalog.DatabaseCatalog;
import ed.inf.adbs.lightdb.types.Tuple;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;

public class ProjectionOperator extends Operator {

    private Operator child;
    private PlainSelect select;
    private DatabaseCatalog catalog;

    public ProjectionOperator(Operator child, PlainSelect select, DatabaseCatalog catalog) {
        this.child = child;
        this.select = select;
        this.catalog = catalog;
    }

    private Tuple processTuple(Tuple initialTuple) {
        Tuple outputTuple = new Tuple();
        List<Join> joins = this.select.getJoins();
        if (this.select.getJoins() != null) {
            for (SelectItem<?> selectItem : select.getSelectItems()) {
                int idx = catalog.getColumnIndex((Column) selectItem.getExpression(), select.getFromItem(), joins);
                outputTuple.append(initialTuple.getValueAt(idx));
            }

        } else {
            for (SelectItem<?> selectItem : select.getSelectItems()) {
                int idx = catalog.getColumnIndex((Column) selectItem.getExpression());
                outputTuple.append(initialTuple.getValueAt(idx));
            }
        }
        return outputTuple;
    }

    @Override
    public Tuple getNextTuple() {
        Tuple nexTuple = this.child.getNextTuple();
        if (nexTuple != null) {
            return this.processTuple(nexTuple);
        }
        return null;
    }

    @Override
    public void reset() {
        this.child.reset();
    }
}
