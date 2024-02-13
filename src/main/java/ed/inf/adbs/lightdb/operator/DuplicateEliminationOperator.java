package ed.inf.adbs.lightdb.operator;

import java.util.HashSet;
import ed.inf.adbs.lightdb.type.Tuple;

/**
 * The DuplicateEliminationOperator class represents an operator that eliminates
 * duplicate tuples from its child operator's output. It uses a hash based
 * elimination. Whenever a tuple is returned, its hashcode is added to a set. If
 * the tuple has been returned before, try to fetch another that hasn't
 */
public class DuplicateEliminationOperator extends Operator {

    private Operator child; // Child operator
    private HashSet<Integer> set; // HashSet to store hash codes of seen tuples

    /**
     * Constructs a DuplicateEliminationOperator with the specified child operator.
     * 
     * @param child the child operator
     */
    public DuplicateEliminationOperator(Operator child) {
        this.child = child;
        this.set = new HashSet<Integer>();
    }

    /**
     * Retrieves the next non-duplicate tuple from the operator.
     * 
     * @return the next non-duplicate tuple, or null if there are no more tuples
     */
    @Override
    public Tuple getNextTuple() {
        // get a tuple
        Tuple t = this.child.getNextTuple();
        // if null, return null
        if (t == null) {
            return null;
        }
        // if the tuple is already in the hashset, it means it was returned before, thus
        // return another
        if (this.set.contains(t.hashCode())) {
            return this.getNextTuple();
        }
        // if this is a unique tuple, add it to the set and return it
        this.set.add(t.hashCode());
        return t;
    }

    /**
     * Resets the state of the operator by resetting its child operator.
     */
    @Override
    public void reset() {
        this.child.reset();
        this.set = new HashSet<Integer>();
    }
}
