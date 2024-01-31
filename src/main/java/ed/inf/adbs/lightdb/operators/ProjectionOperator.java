package ed.inf.adbs.lightdb.operators;

import ed.inf.adbs.lightdb.catalog.DatabaseCatalog;
import ed.inf.adbs.lightdb.types.Tuple;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;

public class ProjectionOperator extends Operator {

    private Operator child;
    private PlainSelect select;
    private DatabaseCatalog catalog;
    private FromItem fromItem;

    public ProjectionOperator(Operator child, PlainSelect select, DatabaseCatalog catalog) {
        this.child = child;
        this.select = select;
        this.catalog = catalog;
        this.fromItem = select.getFromItem();
    }

    private Tuple processTuple(Tuple initialTuple) {
        Tuple outpuTuple = new Tuple();
        for (SelectItem<?> selectItem : select.getSelectItems()) {
            int idx = catalog.getColumnIndex(this.fromItem.toString(), selectItem.toString());
            outpuTuple.append(initialTuple.getValueAt(idx));
        }
        return outpuTuple;
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
