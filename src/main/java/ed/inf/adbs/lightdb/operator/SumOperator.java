package ed.inf.adbs.lightdb.operator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import ed.inf.adbs.lightdb.type.Tuple;
import ed.inf.adbs.lightdb.type.TupleElement;
import ed.inf.adbs.lightdb.visitor.SumVisitor;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.GroupByElement;

/**
 * The SumOperator class represents an operator that performs summation or
 * grouping operations on tuples. It might be the case that only one of groupBy
 * or sum is present. The main approach is to create groups in a map with or
 * without the grouping contraint and then collapse the maps, performing the sum
 * if present. This is done to avoid handling all possible cases separately
 * (groupBy + sum, groupBy + not sum, not groupBy + sum, not groupBy + not sum)
 */
public class SumOperator extends Operator {

    private Operator child; // Child operator
    private Map<Integer, List<Tuple>> groups; // Map of hash (given by groupBy columns) -> tuples for grouping
    private List<Tuple> tuples; // List of output tuples

    /**
     * Constructs a SumOperator with the specified child operator, group by element,
     * and sum function.
     * 
     * @param child          the child operator
     * @param groupByElement the group by element
     * @param sumFunction    the sum function
     */
    public SumOperator(Operator child, GroupByElement groupByElement, Function sumFunction) {
        this.child = child;
        this.tuples = new ArrayList<>();
        this.groups = new HashMap<>();

        // Create groups based on group by or without group by
        if (groupByElement != null) {
            createGroupsWithGroupBy(groupByElement);
        } else {
            createGroupsWithoutGroupBy();
        }

        // Collapse groups based on sum function or without sum function
        if (sumFunction != null) {
            collapseGroupsWithSum(sumFunction);
        } else {
            collapseGroupsWithoutSum();
        }
    }

    /**
     * Retrieves the next tuple from the operator.
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
     * Resets the state of the operator by resetting its child operator.
     */
    @Override
    public void reset() {
        this.tuples = new ArrayList<>();
        this.groups = new HashMap<>();
        this.child.reset();
    }

    /**
     * Creates groups based on group by element.
     * 
     * @param groupByElement the group by element
     */
    public void createGroupsWithGroupBy(GroupByElement groupByElement) {
        @SuppressWarnings("unchecked")
        // get the group by, can be my multiple columns
        List<Column> groupByExpressionList = groupByElement.getGroupByExpressionList();
        Tuple tuple = child.getNextTuple();
        // foreach tuple
        while (tuple != null) {
            // we need to hash the columns that are referenced in the group by
            // this way, the hash key will be created based on the specified columns
            List<Integer> hashCodeValues = new ArrayList<>();
            for (TupleElement te : tuple.getElements()) {
                for (Column c : groupByExpressionList) {
                    // if the column of the groupBy matches the column of the current tuple element
                    // that means that the hash key should take that column into account
                    if (te.columnsMatch(c)) {
                        hashCodeValues.add(te.hashCode());
                    }
                }
            }
            // create this key
            int key = Objects.hash(hashCodeValues.toArray());
            // if the key is not inside the map create a new list for that key
            if (!this.groups.containsKey(key)) {
                this.groups.put(key, new ArrayList<>());
            }
            // add the tuple to the list corresponding to the key
            this.groups.get(key).add(tuple);
            // get the next tuple
            tuple = child.getNextTuple();
        }
    }

    /**
     * Creates groups without group by element. When a groupBy is not available,
     * only one group exists
     */
    public void createGroupsWithoutGroupBy() {
        List<Tuple> singleGroup = new ArrayList<>();
        Tuple tuple = child.getNextTuple();
        while (tuple != null) {
            singleGroup.add(tuple);
            tuple = child.getNextTuple();
        }
        // we store the single group in any key, for here we chose 0
        this.groups.put(0, singleGroup);
    }

    /**
     * Collapses groups based on sum function. This function iterates all entries in
     * the gropus map, and produces a single tuple for each group. The summation as
     * specified in the sum function is stored as the last column of the tuple
     * 
     * @param sumFunction the sum function
     */
    public void collapseGroupsWithSum(Function sumFunction) {
        SumVisitor sumVisitor;
        int result;
        // this part calculates the sum
        // foreach group
        for (int key : this.groups.keySet()) {
            result = 0;
            // create a visitor that recursively evaluates the sum function
            for (Tuple tuple : this.groups.get(key)) {
                sumVisitor = new SumVisitor(tuple);
                sumFunction.accept(sumVisitor);
                result += sumVisitor.getResult();
            }

            // as we only need to return one tulpe from the group, just take the first one,
            // add the new sum column and add it to the tuples list
            Tuple resTuple = this.groups.get(key).get(0);
            TupleElement sumRes = new TupleElement(null, null, null, result, true);
            resTuple.add(sumRes);
            this.tuples.add(resTuple);
        }
    }

    /**
     * Collapses groups without sum function. Just adds the first tuple of the group
     * in the tuple list to be returned
     */
    public void collapseGroupsWithoutSum() {
        for (int key : this.groups.keySet()) {
            this.tuples.add(this.groups.get(key).get(0));
        }
    }
}
