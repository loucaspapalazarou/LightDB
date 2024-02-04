package ed.inf.adbs.lightdb.operator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ed.inf.adbs.lightdb.type.Tuple;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.OrderByElement;

public class SortOperator extends Operator {
    private List<Tuple> tuples;
    private Operator child;
    private int index;
    private List<OrderByElement> orderByElements;

    // FIX, CAN SORT BY MULTIPLE
    public SortOperator(Operator child, List<OrderByElement> orderByElements) {
        this.child = child;
        this.index = 0;
        this.orderByElements = orderByElements;
        this.tuples = new ArrayList<>();
        Tuple t = this.child.getNextTuple();
        while (t != null) {
            tuples.add(t);
            t = this.child.getNextTuple();
        }
        this.child.reset();
        sortTuples();
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
        sortTuples();
    }

    private void sortTuples() {
        Collections.sort(this.tuples, new Comparator<Tuple>() {
            @Override
            public int compare(Tuple tuple1, Tuple tuple2) {
                int value1, value2, res;
                for (OrderByElement orderByElement : SortOperator.this.orderByElements) {
                    value1 = tuple1.getValueAt((Column) orderByElement.getExpression());
                    value2 = tuple2.getValueAt((Column) orderByElement.getExpression());
                    res = Integer.compare(value1, value2);
                    if (res != 0) {
                        return res;
                    }
                }
                return 0;
            }
        });
    }
}

// String x1 = ((Person) o1).getName();
// String x2 = ((Person) o2).getName();
// int sComp = x1.compareTo(x2);

// if(sComp!=0)
// {
// return sComp;
// }

// Integer x1 = ((Person) o1).getAge();
// Integer x2 = ((Person) o2).getAge();return x1.compareTo(x2)
// ;