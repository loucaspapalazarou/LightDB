package ed.inf.adbs.lightdb.operator;

import java.util.List;

import ed.inf.adbs.lightdb.type.Tuple;
import ed.inf.adbs.lightdb.type.TupleElement;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.SelectItem;

/**
 * The ProjectionOperator class represents an operator that performs projection
 * on tuples based on the select items in a SQL query.
 */
public class ProjectionOperator extends Operator {

    private Operator child; // Child operator
    private List<SelectItem<?>> selectItems;
    private boolean needToProccessTuple;

    /**
     * Constructs a ProjectionOperator with the specified child operator and
     * PlainSelect statement.
     * 
     * @param child  the child operator
     * @param select the PlainSelect statement representing the SQL query
     */
    public ProjectionOperator(Operator child, List<SelectItem<?>> selectItems) {
        this.child = child;
        this.selectItems = selectItems;
        this.needToProccessTuple = true;

        for (SelectItem<?> selectItem : this.selectItems) {
            // If '*' in the selection, we don't need the ProjectionOperator, therefore set
            // the flag to false
            if (selectItem.getExpression() instanceof AllColumns) {
                this.needToProccessTuple = false;
                break;
            }
        }
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
        Column column;
        Expression expression;
        // Process each select item in the select items
        for (SelectItem<?> selectItem : this.selectItems) {
            // If the select item is not a function (i.e. a sum function), add the
            // corresponding column value to the output tuple
            expression = selectItem.getExpression();
            if (!(expression instanceof Function)) {
                try {
                    column = (Column) expression;
                    outputTuple.add(column, initialTuple.getValueAt(column));
                } catch (Exception e) {

                }
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
            return this.needToProccessTuple ? this.processTuple(nextTuple) : nextTuple;
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
