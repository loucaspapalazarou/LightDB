package ed.inf.adbs.lightdb.operator;

import ed.inf.adbs.lightdb.type.Tuple;
import ed.inf.adbs.lightdb.type.TupleElement;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;

public class ProjectionOperator extends Operator {

    private Operator child;
    private PlainSelect select;

    public ProjectionOperator(Operator child, PlainSelect select) {
        this.child = child;
        this.select = select;
    }

    private Tuple processTuple(Tuple initialTuple) {
        Tuple outputTuple = new Tuple();
        Column c = null;
        for (SelectItem<?> selectItem : this.select.getSelectItems()) {
            if (!(selectItem.getExpression() instanceof Function)) {
                c = (Column) selectItem.getExpression();
                outputTuple.add(c, initialTuple.getValueAt(c));
            }
        }
        for (TupleElement te : initialTuple.getElements()) {
            if (te.isPersistent()) {
                outputTuple.add(te);
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
