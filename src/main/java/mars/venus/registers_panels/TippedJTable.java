package mars.venus.registers_panels;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseEvent;

public class TippedJTable extends JTable {

    private final int regTooltipColumn;
    private final String[] regTooltips;
    private final String[] columnTooltips;

    public TippedJTable(AbstractRegTableModel tableModel,
                        int regTooltipsColumn,
                        String[] regToolTips,
                        String[] columnTooltips)
    {
        super(tableModel);
        this.regTooltipColumn = regTooltipsColumn;
        this.regTooltips = regToolTips;
        this.columnTooltips = columnTooltips;
        setRowSelectionAllowed(true);
        setSelectionBackground(Color.GREEN);
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        Point point = event.getPoint();
        int row = rowAtPoint(point);
        int column = columnAtPoint(point);
        int modelColumn = convertColumnIndexToModel(column);

        if (modelColumn == regTooltipColumn) {
            return regTooltips[row];
        }

        return super.getToolTipText(event);
    }

    @Override
    protected JTableHeader createDefaultTableHeader() {
        return new JTableHeader(columnModel) {
            public String getToolTipText(MouseEvent e) {
                Point point = e.getPoint();
                int index = columnModel.getColumnIndexAtX(point.x);
                int modelColumn = columnModel.getColumn(index).getModelIndex();
                return columnTooltips[modelColumn];
            }
        };
    }

}
