package ed.inf.adbs.lightdb.operator;

import java.util.ArrayList;
import java.util.List;

import ed.inf.adbs.lightdb.type.Tuple;
import net.sf.jsqlparser.statement.select.OrderByElement;

public class SortOperator extends Operator {
    private List<Tuple> tuples;
    private Operator child;
    private int index;
    private OrderByElement orderByElement;

    public SortOperator(Operator child, OrderByElement orderByElement) {
        this.child = child;
        this.index = 0;
        this.orderByElement = orderByElement;
        this.tuples = new ArrayList<>();
        Tuple t = this.child.getNextTuple();
        while (t != null) {
            tuples.add(t);
            t = this.child.getNextTuple();
        }
        this.child.reset();
    }

    @Override
    public Tuple getNextTuple() {
        if (this.index >= this.tuples.size()) {
            return null;
        }
        Tuple t = this.tuples.get(index);
        this.index++;
        return t;
    }

    @Override
    public void reset() {
        this.child.reset();
        this.index = 0;
        this.tuples = new ArrayList<>();
        Tuple t = this.child.getNextTuple();
        while (t != null) {
            tuples.add(t);
            t = this.child.getNextTuple();
        }
    }
}
