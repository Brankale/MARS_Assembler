package mars.venus.registers_panels;

import mars.*;
import mars.simulator.*;
import mars.mips.hardware.*;
import mars.util.Binary;
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
public class RegistersWindow extends JPanel implements Observer {
    private static JTable table;
    private static Register[] registers;
    private Object[][] tableData;
    private static final int NAME_COLUMN = 0;
    private static final int NUMBER_COLUMN = 1;
    private static final int VALUE_COLUMN = 2;
    private static Settings settings;

    private final String[] columnNames = {"Name", "Number", "Value"};

    // TODO: handle highlighting in MyTippedJTable
    private final RegisterCellRenderer nameColumnCellRenderer;
    private final RegisterCellRenderer numberColumnCellRenderer;
    private final RegisterCellRenderer valueColumnCellRenderer;

    private String[] regToolTips = {
            /* $zero */  "constant 0",
            /* $at   */  "reserved for assembler",
            /* $v0   */  "expression evaluation and results of a function",
            /* $v1   */  "expression evaluation and results of a function",
            /* $a0   */  "argument 1",
            /* $a1   */  "argument 2",
            /* $a2   */  "argument 3",
            /* $a3   */  "argument 4",
            /* $t0   */  "temporary (not preserved across call)",
            /* $t1   */  "temporary (not preserved across call)",
            /* $t2   */  "temporary (not preserved across call)",
            /* $t3   */  "temporary (not preserved across call)",
            /* $t4   */  "temporary (not preserved across call)",
            /* $t5   */  "temporary (not preserved across call)",
            /* $t6   */  "temporary (not preserved across call)",
            /* $t7   */  "temporary (not preserved across call)",
            /* $s0   */  "saved temporary (preserved across call)",
            /* $s1   */  "saved temporary (preserved across call)",
            /* $s2   */  "saved temporary (preserved across call)",
            /* $s3   */  "saved temporary (preserved across call)",
            /* $s4   */  "saved temporary (preserved across call)",
            /* $s5   */  "saved temporary (preserved across call)",
            /* $s6   */  "saved temporary (preserved across call)",
            /* $s7   */  "saved temporary (preserved across call)",
            /* $t8   */  "temporary (not preserved across call)",
            /* $t9   */  "temporary (not preserved across call)",
            /* $k0   */  "reserved for OS kernel",
            /* $k1   */  "reserved for OS kernel",
            /* $gp   */  "pointer to global area",
            /* $sp   */  "stack pointer",
            /* $fp   */  "frame pointer",
            /* $ra   */  "return address (used by function call)",
            /* pc    */  "program counter",
            /* hi    */  "high-order word of multiply product, or divide remainder",
            /* lo    */  "low-order word of multiply product, or divide quotient"
    };

    private String[] columnToolTips = {
            /* name */   "Each register has a tool tip describing its usage convention",
            /* number */ "Corresponding register number",
            /* value */  "Current 32 bit value"
    };

    /**
     * Constructor which sets up a fresh window with a table that contains the register values.
     **/

    public RegistersWindow() {
        Simulator.getInstance().addObserver(this);
        settings = Globals.getSettings();

        RegTableModel regTableModel = new RegTableModel(columnNames, getTableEntries());

        table = new TippedJTable(regTableModel, NAME_COLUMN, regToolTips, columnToolTips);
        table.getColumnModel().getColumn(NAME_COLUMN).setPreferredWidth(25);
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
        this.setLayout(new BorderLayout()); // table display will occupy entire width if widened
        this.add(new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
    }

    /**
     * Sets up the data for the window.
     *
     * @return The array object with the data for the window.
     **/

    public Object[][] getTableEntries() {
        int valueBase = NumberDisplayBaseChooser.getBase(settings.getBooleanSetting(Settings.DISPLAY_VALUES_IN_HEX));
        tableData = new Object[35][3];
        registers = RegisterFile.getRegisters();
        for (int i = 0; i < registers.length; i++) {
            tableData[i][0] = registers[i].getName();
            tableData[i][1] = registers[i].getNumber();
            tableData[i][2] = NumberDisplayBaseChooser.formatNumber(registers[i].getValue(), valueBase);
        }
        tableData[32][0] = "pc";
        tableData[32][1] = "";//new Integer(32);
        tableData[32][2] = NumberDisplayBaseChooser.formatUnsignedInteger(RegisterFile.getProgramCounter(), valueBase);

        tableData[33][0] = "hi";
        tableData[33][1] = "";//new Integer(33);
        tableData[33][2] = NumberDisplayBaseChooser.formatNumber(RegisterFile.getValue(33), valueBase);

        tableData[34][0] = "lo";
        tableData[34][1] = "";//new Integer(34);
        tableData[34][2] = NumberDisplayBaseChooser.formatNumber(RegisterFile.getValue(34), valueBase);

        return tableData;
    }

    /**
     * clear and redisplay registers
     */
    public void clearWindow() {
        this.clearHighlighting();
        RegisterFile.resetRegisters();
        this.updateRegisters(Globals.getGui().getMainPane().getExecutePane().getValueDisplayBase());
    }

    /**
     * Clear highlight background color from any cell currently highlighted.
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
     * update register display using current number base (10 or 16)
     */
    public void updateRegisters() {
        updateRegisters(Globals.getGui().getMainPane().getExecutePane().getValueDisplayBase());
    }

    /**
     * update register display using specified number base (10 or 16)
     *
     * @param base desired number base
     */
    public void updateRegisters(int base) {
        registers = RegisterFile.getRegisters();
        for (Register register : registers) {
            updateRegisterValue(register.getNumber(), register.getValue(), base);
        }
        updateRegisterUnsignedValue(32, RegisterFile.getProgramCounter(), base);
        updateRegisterValue(33, RegisterFile.getValue(33), base);
        updateRegisterValue(34, RegisterFile.getValue(34), base);
    }

    /**
     * This method handles the updating of the GUI.
     *
     * @param number The number of the register to update.
     * @param val    New value.
     **/

    public void updateRegisterValue(int number, int val, int base) {
        ((AbstractRegTableModel) table.getModel()).setValueAtProgrammatically(NumberDisplayBaseChooser.formatNumber(val, base), number, 2);
    }


    private void updateRegisterUnsignedValue(int number, int val, int base) {
        ((AbstractRegTableModel) table.getModel()).setValueAtProgrammatically(NumberDisplayBaseChooser.formatUnsignedInteger(val, base), number, 2);
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
                    RegisterFile.addRegistersObserver(this);
                    nameColumnCellRenderer.setHighlighting(true);
                    numberColumnCellRenderer.setHighlighting(true);
                    valueColumnCellRenderer.setHighlighting(true);
                }
            } else {
                // Simulated MIPS execution stops.  Stop responding.
                RegisterFile.deleteRegistersObserver(this);
            }
        } else if (obj instanceof RegisterAccessNotice) {
            // NOTE: each register is a separate Observable
            RegisterAccessNotice access = (RegisterAccessNotice) obj;
            if (access.getAccessType() == AccessNotice.WRITE) {
                // Uses the same highlighting technique as for Text Segment -- see
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
        int highlightedRow = register.getNumber();
        nameColumnCellRenderer.highlightRow(highlightedRow);
        numberColumnCellRenderer.highlightRow(highlightedRow);
        valueColumnCellRenderer.highlightRow(highlightedRow);
        // Tell the system that table contents have changed.  This will trigger re-rendering 
        // during which cell renderers are obtained.  The row of interest (identified by 
        // instance variabls this.registerRow) will get a renderer
        // with highlight background color and all others get renderer with default background. 
        table.tableChanged(new TableModelEvent(table.getModel()));
    }

    private static class RegTableModel extends AbstractRegTableModel {

        public RegTableModel(String[] columnNames, Object[][] data) {
            super(columnNames, data);
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            try {
                int val = Binary.stringToInt((String) value);

                // Assures that if changed during MIPS program execution,
                // the update will occur only between MIPS instructions.
                synchronized (Globals.memoryAndRegistersLock) {
                    RegisterFile.updateRegister(row, val);
                }

                int valueBase = Globals.getGui().getMainPane().getExecutePane().getValueDisplayBase();
                data[row][column] = NumberDisplayBaseChooser.formatNumber(val, valueBase);

            } catch (NumberFormatException nfe) {
                data[row][column] = "INVALID";
            }

            fireTableCellUpdated(row, column);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            // these registers are not editable: $zero (0), $pc (32), $ra (31)
            return column == VALUE_COLUMN && row != 0 && row != 32 && row != 31;
        }

    }

}