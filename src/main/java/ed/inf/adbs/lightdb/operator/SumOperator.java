package ed.inf.adbs.lightdb.operator;

import java.beans.Expression;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import ed.inf.adbs.lightdb.type.Tuple;
import ed.inf.adbs.lightdb.type.TupleElement;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.GroupByElement;
import net.sf.jsqlparser.statement.select.SelectItem;

public class SumOperator extends Operator {
    private Operator child;
    // map of hash -> tuples
    // hash contains info about the tuple elements of each tuple
    // the elements that are hashed are the ones found in the groupBy
    private Map<Integer, List<Tuple>> groups;
    private List<Tuple> tuples;

    public SumOperator(Operator child, GroupByElement groupByElement, Function sumFunction) {
        this.child = child;
        this.tuples = new ArrayList<>();

        // create groups
        if (groupByElement != null) {
            createGroupsWithGroupBy(groupByElement);
        } else {
            createGroupsWithoutGroupBy();
        }

        // merge group items based on sum
        if (sumFunction != null) {

        } else {

        }
        System.out.println(groups);
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
            // create key based on groupBy and tuple
            List<Integer> hashCodeValues = new ArrayList<>();
            for (TupleElement te : tuple.getElements()) {
                hashCodeValues.add(te.hashCode());
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

}
