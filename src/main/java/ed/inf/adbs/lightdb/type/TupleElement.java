package ed.inf.adbs.lightdb.type;

import java.util.Objects;
import net.sf.jsqlparser.schema.Column;

/**
 * Represents a tuple element. This class contains information about the column,
 * the table, the possible alias and the value of the element.
 * 
 */
public class TupleElement {

    private String columnName; // Name of the column
    private String tableName; // Name of the table
    private String alias; // Alias for the table
    private int value; // Value of the element
    private boolean persistent; // Indicates if the element should not be filtered out by projection

    /**
     * Constructs a TupleElement with the specified attributes.
     * 
     * @param columnName the name of the column
     * @param tableName  the name of the table
     * @param alias      the alias for the table
     * @param value      the value of the element
     */
    public TupleElement(String columnName, String tableName, String alias, int value) {
        this(columnName, tableName, alias, value, false);
    }

    /**
     * Constructs a TupleElement with the specified attributes.
     * 
     * @param columnName the name of the column
     * @param tableName  the name of the table
     * @param alias      the alias for the table
     * @param value      the value of the element
     * @param persistent indicates if the element should not be filtered out by
     *                   projection
     */
    public TupleElement(String columnName, String tableName, String alias, int value, boolean persistent) {
        this.columnName = columnName;
        this.tableName = tableName;
        this.value = value;
        this.alias = alias;
        this.persistent = persistent;
    }

    /**
     * Return the persistent flag. This flag is only used when the query contains a
     * SUM function. If this is the case, the projection operator must have a way of
     * knowing that the SUM should not be filtered out. As the projection operator
     * can only see a tuple and its columns, it cannot know which ones were a result
     * of a SUM. This flag helps indicate that.
     * 
     * @return true if the element is persistent, false otherwise
     */
    public boolean isPersistent() {
        return this.persistent;
    }

    /**
     * Retrieves the value of the element.
     * 
     * @return the value of the element
     */
    public int getValue() {
        return this.value;
    }

    /**
     * Checks if the column that the tuple element represents matches the given
     * column. Takes into account the table name (or possibe alias) and column name.
     * 
     * @param c the column to compare with
     * @return true if the columns match, false otherwise
     */
    public boolean columnsMatch(Column c) {
        return (alias == null ? tableName : alias).equals(c.getTable().getName())
                && columnName.equals(c.getColumnName());
    }

    /**
     * Returns a string representation of the value of the element.
     * 
     * @return a string representation of the value
     */
    @Override
    public String toString() {
        return Integer.toString(value);
    }

    /**
     * Returns a hash code value for the element. All the attributes are
     * taken into account as well as the value.
     * 
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.tableName, this.columnName, this.alias, this.value);
    }
}
