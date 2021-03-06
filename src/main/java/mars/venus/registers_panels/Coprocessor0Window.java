package mars.venus.registers_panels;

import mars.*;
import mars.util.*;
import mars.simulator.*;
import mars.mips.hardware.*;
import mars.venus.MonoRightCellRenderer;
import mars.venus.NumberDisplayBaseChooser;
import mars.venus.RunSpeedPanel;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import javax.swing.event.*;

/*
Copyright (c) 2003-2009,  Pete Sanderson and Kenneth Vollmar

Developed by Pete Sanderson (psanderson@otterbein.edu)
and Kenneth Vollmar (kenvollmar@missouristate.edu)

Permission is hereby granted, free of charge, to any person obtaining 
a copy of this software and associated documentation files (the 
"Software"), to deal in the Software without restriction, including 
without limitation the rights to use, copy, modify, merge, publish, 
distribute, sublicense, and/or sell copies of the Software, and to 
permit persons to whom the Software is furnished to do so, subject 
to the following conditions:

The above copyright notice and this permission notice shall be 
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION 
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

(MIT license, http://www.opensource.org/licenses/mit-license.html)
 */

/**
 * Sets up a window to display registers in the UI.
 */
public class Coprocessor0Window extends JPanel implements Observer {
    private static JTable table;
    private static Register[] registers;
    private Object[][] tableData;
    private int[] rowGivenRegNumber; // translate register number to table row.
    private static final int NAME_COLUMN = 0;
    private static final int NUMBER_COLUMN = 1;
    private static final int VALUE_COLUMN = 2;
    private final String[] columnNames = {"Name", "Number", "Value"};

    // TODO: handle highlighting in MyTippedJTable
    private final RegisterCellRenderer nameColumnCellRenderer;
    private final RegisterCellRenderer numberColumnCellRenderer;
    private final RegisterCellRenderer valueColumnCellRenderer;

    private final String[] regToolTips = {
            /* $8  */  "Memory address at which address exception occurred",
            /* $12 */  "Interrupt mask and enable bits",
            /* $13 */  "Exception type and pending interrupt bits",
            /* $14 */  "Address of instruction that caused exception"
    };

    private final String[] columnToolTips = {
            /* name */   "Each register has a tool tip describing its usage convention",
            /* number */ "Register number.  In your program, precede it with $",
            /* value */  "Current 32 bit value"
    };

    /**
     * Constructor which sets up a fresh window with a table that contains the register values.
     */
    public Coprocessor0Window() {
        Simulator.getInstance().addObserver(this);
        RegTableModel tableModel = new RegTableModel(columnNames, getTableEntries());
        table = new TippedJTable(tableModel, NAME_COLUMN, regToolTips, columnToolTips);
        table.getColumnModel().getColumn(NAME_COLUMN).setPreferredWidth(50);
        table.getColumnModel().getColumn(NUMBER_COLUMN).setPreferredWidth(25);
        table.getColumnModel().getColumn(VALUE_COLUMN).setPreferredWidth(60);
        // Display register values (String-ified) right-justified in mono font
        nameColumnCellRenderer = new RegisterCellRenderer(MonoRightCellRenderer.MONOSPACED_PLAIN_12POINT, SwingConstants.LEFT);
        numberColumnCellRenderer = new RegisterCellRenderer(MonoRightCellRenderer.MONOSPACED_PLAIN_12POINT, SwingConstants.RIGHT);
        valueColumnCellRenderer = new RegisterCellRenderer(MonoRightCellRenderer.MONOSPACED_PLAIN_12POINT, SwingConstants.RIGHT);
        table.getColumnModel().getColumn(NAME_COLUMN).setCellRenderer(nameColumnCellRenderer);
        table.getColumnModel().getColumn(NUMBER_COLUMN).setCellRenderer(numberColumnCellRenderer);
        table.getColumnModel().getColumn(VALUE_COLUMN).setCellRenderer(valueColumnCellRenderer);
        table.setPreferredScrollableViewportSize(new Dimension(200, 700));
        this.setLayout(new BorderLayout());  // table display will occupy entire width if widened
        this.add(new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
    }

    /**
     * Sets up the data for the window.
     *
     * @return The array object with the data for the window.
     */
    public Object[][] getTableEntries() {
        Settings settings = Globals.getSettings();
        registers = Coprocessor0.getRegisters();
        tableData = new Object[registers.length][3];
        rowGivenRegNumber = new int[32]; // maximum number of registers
        for (int i = 0; i < registers.length; i++) {
            rowGivenRegNumber[registers[i].getNumber()] = i;
            tableData[i][0] = registers[i].getName();
            tableData[i][1] = registers[i].getNumber();
            tableData[i][2] = NumberDisplayBaseChooser.formatNumber(
                    registers[i].getValue(),
                    NumberDisplayBaseChooser.getBase(settings.getBooleanSetting(Settings.DISPLAY_VALUES_IN_HEX))
            );
        }
        return tableData;
    }

    /**
     * Reset and redisplay registers
     */
    public void clearWindow() {
        this.clearHighlighting();
        Coprocessor0.resetRegisters();
        this.updateRegisters(Globals.getGui().getMainPane().getExecutePane().getValueDisplayBase());
    }

    /**
     * Clear highlight background color from any row currently highlighted.
     */
    public void clearHighlighting() {
        if (table != null) {
            table.tableChanged(new TableModelEvent(table.getModel()));
            nameColumnCellRenderer.clearHighlighting();
            numberColumnCellRenderer.clearHighlighting();
            valueColumnCellRenderer.clearHighlighting();
        }
    }

    /**
     * Refresh the table, triggering re-rendering.
     */
    public void refresh() {
        if (table != null) {
            table.tableChanged(new TableModelEvent(table.getModel()));
        }
    }

    /**
     * Update register display using current display base (10 or 16)
     */
    public void updateRegisters() {
        this.updateRegisters(Globals.getGui().getMainPane().getExecutePane().getValueDisplayBase());
    }

    /**
     * Update register display using specified display base
     *
     * @param base number base for display (10 or 16)
     */
    public void updateRegisters(int base) {
        registers = Coprocessor0.getRegisters();
        for (Register register : registers) {
            this.updateRegisterValue(register.getNumber(), register.getValue(), base);
        }
    }

    /**
     * This method handles the updating of the GUI.
     *
     * @param number The number of the register to update.
     * @param val    New value.
     */
    public void updateRegisterValue(int number, int val, int base) {
        ((RegTableModel) table.getModel()).setValueAtProgrammatically(
                NumberDisplayBaseChooser.formatNumber(val, base), rowGivenRegNumber[number], 2);
    }


    /**
     * Required by Observer interface.  Called when notified by an Observable that we are registered with.
     * Observables include:
     * The Simulator object, which lets us know when it starts and stops running
     * A register object, which lets us know of register operations
     * The Simulator keeps us informed of when simulated MIPS execution is active.
     * This is the only time we care about register operations.
     *
     * @param observable The Observable object who is notifying us
     * @param obj        Auxiliary object with additional information.
     */
    public void update(Observable observable, Object obj) {
        if (observable == mars.simulator.Simulator.getInstance()) {
            SimulatorNotice notice = (SimulatorNotice) obj;
            if (notice.getAction() == SimulatorNotice.SIMULATOR_START) {
                // Simulated MIPS execution starts.  Respond to memory changes if running in timed
                // or stepped mode.
                if (notice.getRunSpeed() != RunSpeedPanel.UNLIMITED_SPEED || notice.getMaxSteps() == 1) {
                    Coprocessor0.addRegistersObserver(this);

                    nameColumnCellRenderer.setHighlighting(true);
                    numberColumnCellRenderer.setHighlighting(true);
                    valueColumnCellRenderer.setHighlighting(true);
                }
            } else {
                // Simulated MIPS execution stops.  Stop responding.
                Coprocessor0.deleteRegistersObserver(this);
            }
        } else if (obj instanceof RegisterAccessNotice) {
            // NOTE: each register is a separate Observable
            RegisterAccessNotice access = (RegisterAccessNotice) obj;
            if (access.getAccessType() == AccessNotice.WRITE) {
                // For now, use highlighting technique used by Label Window feature to highlight
                // memory cell corresponding to a selected label.  The highlighting is not
                // as visually distinct as changing the background color, but will do for now.
                // Ideally, use the same highlighting technique as for Text Segment -- see
                // AddressCellRenderer class in DataSegmentWindow.java.
                nameColumnCellRenderer.setHighlighting(true);
                numberColumnCellRenderer.setHighlighting(true);
                valueColumnCellRenderer.setHighlighting(true);

                this.highlightCellForRegister((Register) observable);
                Globals.getGui().getRegistersPane().setSelectedComponent(this);
            }
        }
    }

    /**
     * Highlight the row corresponding to the given register.
     *
     * @param register Register object corresponding to row to be selected.
     */
    void highlightCellForRegister(Register register) {
        int registerRow = Coprocessor0.getRegisterPosition(register);
        if (registerRow < 0)
            return; // not valid coprocessor0 register
        nameColumnCellRenderer.highlightRow(registerRow);
        numberColumnCellRenderer.highlightRow(registerRow);
        valueColumnCellRenderer.highlightRow(registerRow);
        table.tableChanged(new TableModelEvent(table.getModel()));
    }

    private static class RegTableModel extends AbstractRegTableModel {

        public RegTableModel(String[] columnNames, Object[][] data) {
            super(columnNames, data);
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            try {
                int val = Binary.stringToInt((String) value);

                //  Assures that if changed during MIPS program execution, the update will
                //  occur only between MIPS instructions.
                synchronized (Globals.memoryAndRegistersLock) {
                    Coprocessor0.updateRegister(registers[row].getNumber(), val);
                }

                int valueBase = Globals.getGui().getMainPane().getExecutePane().getValueDisplayBase();
                data[row][col] = NumberDisplayBaseChooser.formatNumber(val, valueBase);
            } catch (NumberFormatException nfe) {
                data[row][col] = "INVALID";
            }

            fireTableCellUpdated(row, col);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column == VALUE_COLUMN;
        }

    }

}