package ed.inf.adbs.lightdb.type;

import java.util.Objects;

import net.sf.jsqlparser.schema.Column;

public class TupleElement {

    private String columnName;
    private String tableName;
    private String alias;
    private int value;
    private boolean persistent;

    public TupleElement(String columnName, String tableName, String alias, int value) {
        this.columnName = columnName;
        this.tableName = tableName;
        this.value = value;
        this.alias = alias;
        this.persistent = false;
    }

    // persistent element indicates that the field should not be filtered out by
    // projection
    public TupleElement(String columnName, String tableName, String alias, int value, boolean persistent) {
        this.columnName = columnName;
        this.tableName = tableName;
        this.value = value;
        this.alias = alias;
        this.persistent = true;
    }

    public boolean isPersistent() {
        return this.persistent;
    }

    public boolean columnsMatch(Column c) {
        if (alias == null) {
            return this.tableName.equals(c.getTable().getName())
                    && this.columnName.equals(c.getColumnName());
        }
        return this.alias.equals(c.getTable().getName())
                && this.columnName.equals(c.getColumnName());
    }

    public String getFullName() {
        if (this.alias == null) {
            return this.tableName + "." + this.columnName;
        }
        return this.alias + "." + this.columnName;
    }

    public int getValue() {
        return this.value;
    }

    public String toString() {
        return Integer.toString(value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.tableName, this.columnName, this.alias, this.value);
    }
}