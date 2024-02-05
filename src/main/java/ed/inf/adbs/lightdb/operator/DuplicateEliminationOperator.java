package ed.inf.adbs.lightdb.operator;

import java.util.HashSet;
import ed.inf.adbs.lightdb.type.Tuple;

public class DuplicateEliminationOperator extends Operator {

    private Operator child;
    private HashSet<Integer> set;

    public DuplicateEliminationOperator(Operator child) {
        this.child = child;
        this.set = new HashSet<Integer>();
    }

    @Override
    public Tuple getNextTuple() {
        Tuple t = this.child.getNextTuple();
        if (t == null) {
            return null;
        }
        if (this.set.contains(t.hashCode())) {
            return this.getNextTuple();
        }
        this.set.add(t.hashCode());
        return t;
    }

    @Override
    public void reset() {
        this.child.reset();
    }
}
