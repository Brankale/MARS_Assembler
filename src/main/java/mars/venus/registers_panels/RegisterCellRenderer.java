package mars.venus.registers_panels;

import mars.Globals;
import mars.Settings;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Cell renderer for displaying register entries.  This does highlighting, so if you
 * don't want highlighting for a given column, don't use this.
 * Currently we highlight all columns.
 */
public class RegisterCellRenderer extends DefaultTableCellRenderer {

    private final Font font;
    private final int alignment;

    private int highlightedRow;
    private boolean highlighting;

    public RegisterCellRenderer(Font font, int alignment) {
        this.font = font;
        this.alignment = alignment;
        clearHighlighting();
    }

    // TODO: I think this can be done more efficiently. Figure out how to do it.
    public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column
    ) {
        JLabel cell = (JLabel) super.getTableCellRendererComponent(table, value,
                isSelected, hasFocus, row, column);
        cell.setFont(font);
        cell.setHorizontalAlignment(alignment);

        Settings settings = Globals.getSettings();
        if (settings.getBooleanSetting(Settings.REGISTERS_HIGHLIGHTING) && highlighting && row == highlightedRow) {
            cell.setBackground(settings.getColorSettingByPosition(Settings.REGISTER_HIGHLIGHT_BACKGROUND));
            cell.setForeground(settings.getColorSettingByPosition(Settings.REGISTER_HIGHLIGHT_FOREGROUND));
            cell.setFont(settings.getFontByPosition(Settings.REGISTER_HIGHLIGHT_FONT));
        } else if (row % 2 == 0) {
            cell.setBackground(settings.getColorSettingByPosition(Settings.EVEN_ROW_BACKGROUND));
            cell.setForeground(settings.getColorSettingByPosition(Settings.EVEN_ROW_FOREGROUND));
            cell.setFont(settings.getFontByPosition(Settings.EVEN_ROW_FONT));
        } else {
            cell.setBackground(settings.getColorSettingByPosition(Settings.ODD_ROW_BACKGROUND));
            cell.setForeground(settings.getColorSettingByPosition(Settings.ODD_ROW_FOREGROUND));
            cell.setFont(settings.getFontByPosition(Settings.ODD_ROW_FONT));
        }
        return cell;
    }

    public void clearHighlighting() {
        this.highlighting = false;
        highlightedRow = -1;
    }

    public void highlightRow(int highlightedRow) {
        this.highlightedRow = highlightedRow;
    }

    public void setHighlighting(boolean highlighting) {
        this.highlighting = highlighting;
    }

    // TODO: highlighting can be handled by MyTippedJTable

}
