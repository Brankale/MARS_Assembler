package mars.venus.registers_panels;

import javax.swing.table.AbstractTableModel;

/**
 * AbstractRegTableModel implements most of the methods of AbstractTableModel.
 *
 * When using AbstractRegTableModel you should just implement:
 * - setValueAt(Object value, int row, int column)
 * - isCellEditable(int row, int column)
 *
 * NB: setValueAt() must be used just for user inputs and should
 *     validate the input value. To set the value programmatically
 *     use setValueAtProgrammatically().
 */
public abstract class AbstractRegTableModel extends AbstractTableModel {

    protected final String[] columnNames;
    protected final Object[][] data;

    public AbstractRegTableModel(String[] columnNames, Object[][] data) {
        this.columnNames = columnNames;
        this.data = data;
    }

    @Override
    public final int getRowCount() {
        return data.length;
    }

    @Override
    public final int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public final String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public final Object getValueAt(int row, int column) {
        return data[row][column];
    }

    /**
     * Use this method instead of setValueAt() if it's not a user edits.
     * This method doesn't validate the value so performance are higher.
     */
    // TODO: find a better name for this method
    public final void setValueAtProgrammatically(Object value, int row, int column) {
        data[row][column] = value;
        fireTableCellUpdated(row, column);
    }

    /**
     * JTable uses this method to determine the default
     * renderer/editor for each cell.
     */
    @Override
    public final Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
    }

    // TODO: isCellEditable() can be handled here adding methods
    //       to add editable and non-editable columns/rows

}
