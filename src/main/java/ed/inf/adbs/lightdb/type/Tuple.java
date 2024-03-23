package ed.inf.adbs.lightdb.type;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.FromItem;

/**
 * Represents a tuple in a database. It serves as a representation of the tuples
 * that the operators retrieve and manipulate. Provides methods for creation,
 * addition, index/element fetcing etc.
 */
public class Tuple {
    private List<TupleElement> elements; // List of elements in the tuple

    /**
     * Constructs a Tuple with the specified list of elements.
     * 
     * @param elements the list of elements
     */
    public Tuple(List<TupleElement> elements) {
        this.elements = elements;
    }

    /**
     * Constructs an empty Tuple.
     */
    public Tuple() {
        this.elements = new ArrayList<>();
    }

    /**
     * Constructs a Tuple based on the given columns, values, and FromItem.
     * 
     * @param columns  the list of columns
     * @param values   the list of values
     * @param fromItem the FromItem representing the table
     * @throws Exception if the number of columns in schema does not match the data
     */
    public Tuple(List<Column> columns, int[] values, FromItem fromItem) throws Exception {
        if (columns.size() != values.length) {
            throw new Exception("Number of columns in schema does not match data.");
        }
        this.elements = new ArrayList<>();
        for (int i = 0; i < columns.size(); i++) {
            TupleElement newTupleElement = null;
            Column c = columns.get(i);
            // there may not be an alias, so if there is one try to get it
            String aliasName = fromItem.getAlias() != null ? fromItem.getAlias().getName() : null;
            newTupleElement = new TupleElement(c.getColumnName(), c.getTable().getName(), aliasName, values[i]);
            this.elements.add(newTupleElement);
        }
    }

    /**
     * Gets the list of elements in the tuple.
     * 
     * @return the list of elements
     */
    public List<TupleElement> getElements() {
        return this.elements;
    }

    /**
     * Concatenates two tuples.
     * 
     * @param a the first tuple
     * @param b the second tuple
     * @return the concatenated tuple
     */
    public static Tuple concatTuples(Tuple a, Tuple b) {
        Tuple t = new Tuple();
        t.elements.addAll(a.getElements());
        t.elements.addAll(b.getElements());
        return t;
    }

    /**
     * Returns a string representation of the tuple.
     * 
     * @return a string representation of the tuple
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < this.elements.size(); i++) {
            sb.append(this.elements.get(i));
            if (i < this.elements.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    /**
     * Gets the value of the tuple element corresponding to the given column.
     * 
     * @param column the column to search for
     * @return the value of the element if found, otherwise null
     */
    public Integer getValueAt(Column c) {
        // System.out.println("NEW CALL");
        for (TupleElement te : this.elements) {
            // System.out.println(te.getFullName() + " " + c + " " + te.columnsMatch(c));
            if (te.columnsMatch(c)) {
                return te.getValue();
            }
        }
        return null;
    }

    public String getFullName() {
        String s = "(";
        for (TupleElement te : this.elements) {
            s += te.getFullName() + "=" + te.getValue() + ", ";
        }
        s += ")";
        return s.replace(", )", ")");
    }

    /**
     * Gets the index of the tuple element corresponding to the given column.
     * 
     * @param c the column to search for
     * @return the index of the element if found, otherwise null
     */
    public Integer getIndexOf(Column c) {
        for (TupleElement te : this.elements) {
            if (te.columnsMatch(c)) {
                return this.elements.indexOf(te);
            }
        }
        return null;
    }

    /**
     * Adds a new element to the tuple based on the given column and value.
     * 
     * @param c   the column
     * @param val the value
     */
    public void add(Column c, int val) {
        String tableName = c.getTable().getName();
        String columnName = c.getColumnName();
        Alias alias = c.getTable().getAlias();
        String aliasName = null;
        if (alias != null) {
            aliasName = alias.getName();
        }
        TupleElement te = new TupleElement(columnName, tableName, aliasName, val);
        this.elements.add(te);
    }

    /**
     * Adds a TupleElement to the tuple.
     * 
     * @param te the TupleElement to add
     */
    public void add(TupleElement te) {
        this.elements.add(te);
    }

    /**
     * Computes a hash code for the tuple based on the hashcode of its elements
     * 
     * @return a hash code value for this tuple
     */
    @Override
    public int hashCode() {
        return this.elements.hashCode();
    }
}
