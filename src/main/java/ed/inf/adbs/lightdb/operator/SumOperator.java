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

public class SumOperator extends Operator {
    private Operator child;
    // map of hash -> tuples
    // hash contains info about the tuple elements of each tuple
    // the elements that are hashed are the ones found in the groupBy
    private Map<Integer, List<Tuple>> groups;
    private List<Tuple> tuples;
    private GroupByElement groupByElement;

    public SumOperator(Operator child, GroupByElement groupByElement, Function sumFunction) {
        this.child = child;
        this.tuples = new ArrayList<>();
        this.groupByElement = groupByElement;

        // create groups
        if (groupByElement != null) {
            createGroupsWithGroupBy(groupByElement);
        } else {
            createGroupsWithoutGroupBy();
        }

        // merge group items based on sum
        if (sumFunction != null) {
            collapseGroupsWithSum(sumFunction);
        } else {
            collapseGroupsWithoutSum();
        }
        this.child.reset();
    }

    @Override
    public Tuple getNextTuple() {
        if (this.tuples.size() == 0) {
            return null;
        }
        return this.tuples.remove(0);
    }

    @Override
    public void reset() {
        this.child.reset();
    }

    public void createGroupsWithGroupBy(GroupByElement groupByElement) {
        this.groups = new HashMap<>();

        @SuppressWarnings("unchecked")
        List<Column> groupByExpressionList = groupByElement.getGroupByExpressionList();

        Tuple tuple = child.getNextTuple();
        while (tuple != null) {
            // create key based on groupBy and tuple
            List<Integer> hashCodeValues = new ArrayList<>();
            for (TupleElement te : tuple.getElements()) {
                for (Column c : groupByExpressionList) {
                    if (te.columnsMatch(c)) {
                        hashCodeValues.add(te.hashCode());
                    }
                }
            }
            int key = Objects.hash(hashCodeValues.toArray());
            // if key exists in map, append the tuple to the list of the key
            if (!this.groups.containsKey(key)) {
                this.groups.put(key, new ArrayList<>());
            }
            this.groups.get(key).add(tuple);
            tuple = child.getNextTuple();
        }
    }

    public void createGroupsWithoutGroupBy() {
        this.groups = new HashMap<>();
        Tuple tuple = child.getNextTuple();
        while (tuple != null) {
            int key = 0;
            // if key exists in map, append the tuple to the list of the key
            if (!this.groups.containsKey(key)) {
                this.groups.put(key, new ArrayList<>());
            }
            this.groups.get(key).add(tuple);
            tuple = child.getNextTuple();
        }
    }

    public void collapseGroupsWithSum(Function sumFunction) {
        // append the sum to the end of the tuple
        // mark the summed field as 'persistent' so projection does not filter it out
        // add this new tuple to the tuple list
        // now getNextTuple will remove and return these tuples
        SumVisitor sumVisitor;
        int result;
        // foreach group/map entry
        for (int key : this.groups.keySet()) {
            result = 0;
            // foreach tuple
            for (Tuple tuple : this.groups.get(key)) {
                // execute the inside of the sum using visitor
                sumVisitor = new SumVisitor(tuple);
                sumFunction.accept(sumVisitor);
                // add it to the group's total
                result += sumVisitor.getResult();
            }
            // System.out.println(key + " | " + this.groups.get(key) + " | " + result);
            // create the resulting tuple
            // resulting tuple will contain the fields that were not used in the groupby
            Tuple resultTuple = new Tuple();

            // if groupBy is present, we only want to display the fields of the groupby,
            // since the rest are aggregates
            // and will be appended at the end of the tuple
            if (this.groupByElement != null) {
                @SuppressWarnings("unchecked")
                List<Column> groupByExpressionList = this.groupByElement.getGroupByExpressionList();
                for (Column c : groupByExpressionList) {
                    for (TupleElement te : this.groups.get(key).get(0).getElements()) {
                        if (te.columnsMatch(c)) {
                            resultTuple.add(te);
                        }
                    }
                }
                // in the case that a groupBy is not present, we want all of the tuple elements
            } else {
                for (TupleElement te : this.groups.get(key).get(0).getElements()) {
                    resultTuple.add(te);
                }

            }
            resultTuple.add(new TupleElement(null, null, null, result, true));
            this.tuples.add(resultTuple);
        }
    }

    public void collapseGroupsWithoutSum() {
        // append the sum to the end of the tuple
        // mark the summed field as 'persistent' so projection does not filter it out
        // add this new tuple to the tuple list
        // now getNextTuple will remove and return these tuples
        // foreach group/map entry
        for (int key : this.groups.keySet()) {
            // System.out.println(key + " | " + this.groups.get(key) + " | " + result);
            // create the resulting tuple
            // resulting tuple will contain the fields that were not used in the groupby
            Tuple resultTuple = new Tuple();

            // if groupBy is present, we only want to display the fields of the groupby,
            // since the rest are aggregates
            // and will be appended at the end of the tuple
            if (this.groupByElement != null) {
                @SuppressWarnings("unchecked")
                List<Column> groupByExpressionList = this.groupByElement.getGroupByExpressionList();
                for (Column c : groupByExpressionList) {
                    for (TupleElement te : this.groups.get(key).get(0).getElements()) {
                        if (te.columnsMatch(c)) {
                            resultTuple.add(te);
                        }
                    }
                }
                // in the case that a groupBy is not present, we want all of the tuple elements
            } else {
                for (TupleElement te : this.groups.get(key).get(0).getElements()) {
                    resultTuple.add(te);
                }

            }
            this.tuples.add(resultTuple);
        }
    }
}
