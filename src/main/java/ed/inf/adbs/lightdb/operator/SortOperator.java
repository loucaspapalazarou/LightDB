package ed.inf.adbs.lightdb.operator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ed.inf.adbs.lightdb.type.Tuple;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.OrderByElement;

/**
 * The SortOperator class represents an operator that sorts tuples based on
 * specified order by elements.
 */
public class SortOperator extends Operator {

    private List<Tuple> tuples; // List of tuples
    private Operator child; // Child operator
    private List<OrderByElement> orderByElements; // List of order by elements

    /**
     * Constructs a SortOperator with the specified child operator and order by
     * elements.
     * 
     * @param child           the child operator
     * @param orderByElements the list of order by elements
     */
    public SortOperator(Operator child, List<OrderByElement> orderByElements) {
        this.child = child;
        this.orderByElements = orderByElements;
        this.tuples = new ArrayList<>();
        // Populate list of tuples by fetching from child operator
        Tuple t = this.child.getNextTuple();
        while (t != null) {
            tuples.add(t);
            t = this.child.getNextTuple();
        }
        // Sort the tuples based on order by elements
        sortTuples();
    }

    /**
     * Retrieves the next tuple from the sorted list of tuples.
     * 
     * @return the next tuple, or null if there are no more tuples
     */
    @Override
    public Tuple getNextTuple() {
        if (this.tuples.size() == 0) {
            return null;
        }
        return this.tuples.remove(0);
    }

    /**
     * Resets the state of the operator by resetting its child operator,
     * repopulating the list of tuples, and resorting the tuples based on order by
     * elements.
     */
    @Override
    public void reset() {
        this.child.reset();
        this.tuples = new ArrayList<>();
        Tuple t = this.child.getNextTuple();
        while (t != null) {
            tuples.add(t);
            t = this.child.getNextTuple();
        }
        sortTuples();
    }

    /**
     * Sorts the list of tuples based on order by elements.
     */
    private void sortTuples() {
        Collections.sort(this.tuples, new Comparator<Tuple>() {
            @Override
            public int compare(Tuple tuple1, Tuple tuple2) {
                int value1, value2, res;
                // Go through all the order by elements
                for (OrderByElement orderByElement : SortOperator.this.orderByElements) {
                    value1 = tuple1.getValueAt((Column) orderByElement.getExpression());
                    value2 = tuple2.getValueAt((Column) orderByElement.getExpression());
                    res = Integer.compare(value1, value2);
                    // if the values are not the same just do nothing and let the next order by
                    // element do the comparisson
                    if (res != 0) {
                        return res;
                    }
                }
                return 0;
            }
        });
    }
}
