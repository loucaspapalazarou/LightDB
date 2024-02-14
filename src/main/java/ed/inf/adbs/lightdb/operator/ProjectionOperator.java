package ed.inf.adbs.lightdb.operator;

import java.util.List;

import ed.inf.adbs.lightdb.type.Tuple;
import ed.inf.adbs.lightdb.type.TupleElement;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;

/**
 * The ProjectionOperator class represents an operator that performs projection
 * on tuples based on the select items in a SQL query.
 */
public class ProjectionOperator extends Operator {

    private Operator child; // Child operator
    private List<SelectItem<?>> selectItems;

    /**
     * Constructs a ProjectionOperator with the specified child operator and
     * PlainSelect statement.
     * 
     * @param child  the child operator
     * @param select the PlainSelect statement representing the SQL query
     */
    public ProjectionOperator(Operator child, PlainSelect select) {
        this.child = child;
        this.selectItems = select.getSelectItems();
    }

    /**
     * Processes the input tuple by selecting the specified columns and creating a
     * new tuple.
     * 
     * @param initialTuple the input tuple
     * @return the projected tuple
     */
    private Tuple processTuple(Tuple initialTuple) {
        Tuple outputTuple = new Tuple();
        Column c;
        // Process each select item in the select items
        for (SelectItem<?> selectItem : this.selectItems) {
            // If the select item is not a function (i.e. a sum function), add the
            // corresponding column value to the output tuple
            if (!(selectItem.getExpression() instanceof Function)) {
                c = (Column) selectItem.getExpression();
                outputTuple.add(c, initialTuple.getValueAt(c));
            }
        }
        // Add persistent columns to the output tuple (i.e sum function results)
        for (TupleElement te : initialTuple.getElements()) {
            if (te.isPersistent()) {
                outputTuple.add(te);
            }
        }
        return outputTuple;
    }

    /**
     * Retrieves the next projected tuple from the child operator.
     * 
     * @return the next projected tuple, or null if there are no more tuples
     */
    @Override
    public Tuple getNextTuple() {
        Tuple nextTuple = this.child.getNextTuple();
        if (nextTuple != null) {
            return this.processTuple(nextTuple);
        }
        return null;
    }

    /**
     * Resets the state of the operator by resetting its child operator.
     */
    @Override
    public void reset() {
        this.child.reset();
    }
}
