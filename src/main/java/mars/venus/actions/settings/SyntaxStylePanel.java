package mars.venus.actions.settings;

import mars.venus.editors.jeditsyntax.SyntaxStyle;
import mars.venus.editors.jeditsyntax.SyntaxUtilities;
import mars.venus.editors.jeditsyntax.tokenmarker.MIPSTokenMarker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SyntaxStylePanel extends JPanel {

    private static final int gridVGap = 2;
    private static final int gridHGap = 2;

    private static final String SAMPLE_TOOL_TIP_TEXT = "Current setting; modify using buttons to the right";
    private static final String FOREGROUND_TOOL_TIP_TEXT = "Click, to select text color";
    private static final String BOLD_TOOL_TIP_TEXT = "Toggle text bold style";
    private static final String ITALIC_TOOL_TIP_TEXT = "Toggle text italic style";
    private static final String DEFAULT_TOOL_TIP_TEXT = "Check, to select defaults (disables buttons)";
    private static final String BOLD_BUTTON_TOOL_TIP_TEXT = "B";
    private static final String ITALIC_BUTTON_TOOL_TIP_TEXT = "I";

    private final SyntaxStyle[] defaultStyles;
    private final SyntaxStyle[] initialStyles;
    private final SyntaxStyle[] currentStyles;

    private boolean syntaxStylesAction;

    private JButton[] foregroundButtons;
    private JLabel[] samples;
    private JToggleButton[] bold, italic;
    private JCheckBox[] useDefault;

    private final Font previewFont;

    private int[] syntaxStyleIndex;

    public SyntaxStylePanel() {

        defaultStyles = SyntaxUtilities.getDefaultSyntaxStyles();
        initialStyles = SyntaxUtilities.getCurrentSyntaxStyles();
        String[] labels = MIPSTokenMarker.getMIPSTokenLabels();
        String[] sampleText = MIPSTokenMarker.getMIPSTokenExamples();
        syntaxStylesAction = false;
        int count = 0;
        // Count the number of actual styles specified
        for (String s : labels) {
            if (s != null) {
                count++;
            }
        }
        // create new arrays (no gaps) for grid display, refer to original index
        syntaxStyleIndex = new int[count];
        currentStyles = new SyntaxStyle[count];
        String[] label = new String[count];
        samples = new JLabel[count];
        foregroundButtons = new JButton[count];
        bold = new JToggleButton[count];
        italic = new JToggleButton[count];
        useDefault = new JCheckBox[count];
        Font genericFont = new JLabel().getFont();
        previewFont = new Font(Font.MONOSPACED, Font.PLAIN, genericFont.getSize());// no bold on button text
        Font boldFont = new Font(Font.SERIF, Font.BOLD, genericFont.getSize());
        Font italicFont = new Font(Font.SERIF, Font.ITALIC, genericFont.getSize());
        count = 0;
        // Set all the fixed features.  Changeable features set/reset in initializeSyntaxStyleChangeables
        for (int i = 0; i < labels.length; i++) {
            if (labels[i] != null) {
                syntaxStyleIndex[count] = i;
                samples[count] = new JLabel();
                samples[count].setOpaque(true);
                samples[count].setHorizontalAlignment(SwingConstants.CENTER);
                samples[count].setBorder(BorderFactory.createLineBorder(Color.black));
                samples[count].setText(sampleText[i]);
                samples[count].setBackground(Color.WHITE);
                samples[count].setToolTipText(SAMPLE_TOOL_TIP_TEXT);
                foregroundButtons[count] = new ColorSelectButton(); // defined in SettingsHighlightingAction
                foregroundButtons[count].addActionListener(new ForegroundChanger(count));
                foregroundButtons[count].setToolTipText(FOREGROUND_TOOL_TIP_TEXT);
                BoldItalicChanger boldItalicChanger = new BoldItalicChanger(count);
                bold[count] = new JToggleButton(BOLD_BUTTON_TOOL_TIP_TEXT, false);
                bold[count].setFont(boldFont);
                bold[count].addActionListener(boldItalicChanger);
                bold[count].setToolTipText(BOLD_TOOL_TIP_TEXT);
                italic[count] = new JToggleButton(ITALIC_BUTTON_TOOL_TIP_TEXT, false);
                italic[count].setFont(italicFont);
                italic[count].addActionListener(boldItalicChanger);
                italic[count].setToolTipText(ITALIC_TOOL_TIP_TEXT);
                label[count] = labels[i];
                useDefault[count] = new JCheckBox();
                useDefault[count].addItemListener(new DefaultChanger(count));
                useDefault[count].setToolTipText(DEFAULT_TOOL_TIP_TEXT);
                count++;
            }
        }
        initializeSyntaxStyleChangeables();
        // build a grid
        this.setLayout(new BorderLayout());
        JPanel labelPreviewPanel = new JPanel(new GridLayout(syntaxStyleIndex.length, 2, gridVGap, gridHGap));
        JPanel buttonsPanel = new JPanel(new GridLayout(syntaxStyleIndex.length, 4, gridVGap, gridHGap));
        // column 1: label,  column 2: preview, column 3: foreground chooser, column 4/5: bold/italic, column 6: default
        for (int i = 0; i < syntaxStyleIndex.length; i++) {
            labelPreviewPanel.add(new JLabel(label[i], SwingConstants.RIGHT));
            labelPreviewPanel.add(samples[i]);
            buttonsPanel.add(foregroundButtons[i]);
            buttonsPanel.add(bold[i]);
            buttonsPanel.add(italic[i]);
            buttonsPanel.add(useDefault[i]);
        }
        JPanel instructions = new JPanel(new FlowLayout(FlowLayout.CENTER));
        // create deaf, dumb and blind checkbox, for illustration
        JCheckBox illustrate =
                new JCheckBox() {
                    protected void processMouseEvent(MouseEvent e) {
                    }

                    protected void processKeyEvent(KeyEvent e) {
                    }
                };
        illustrate.setSelected(true);
        instructions.add(illustrate);
        instructions.add(new JLabel("= use defaults (disables buttons)"));
        labelPreviewPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        this.add(instructions, BorderLayout.NORTH);
        this.add(labelPreviewPanel, BorderLayout.WEST);
        this.add(buttonsPanel, BorderLayout.CENTER);
    }

    private void initializeSyntaxStyleChangeables() {
        for (int count = 0; count < samples.length; count++) {
            int i = syntaxStyleIndex[count];
            samples[count].setFont(previewFont);
            samples[count].setForeground(initialStyles[i].getColor());
            foregroundButtons[count].setBackground(initialStyles[i].getColor());
            foregroundButtons[count].setEnabled(true);
            currentStyles[count] = initialStyles[i];
            bold[count].setSelected(initialStyles[i].isBold());
            if (bold[count].isSelected()) {
                Font f = samples[count].getFont();
                samples[count].setFont(f.deriveFont(f.getStyle() ^ Font.BOLD));
            }
            italic[count].setSelected(initialStyles[i].isItalic());
            if (italic[count].isSelected()) {
                Font f = samples[count].getFont();
                samples[count].setFont(f.deriveFont(f.getStyle() ^ Font.ITALIC));
            }
            useDefault[count].setSelected(initialStyles[i].toString().equals(defaultStyles[i].toString()));
            if (useDefault[count].isSelected()) {
                foregroundButtons[count].setEnabled(false);
                bold[count].setEnabled(false);
                italic[count].setEnabled(false);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Toggle bold or italic style on preview button when B or I button clicked
    private class BoldItalicChanger implements ActionListener {
        private int row;

        public BoldItalicChanger(int row) {
            this.row = row;
        }

        public void actionPerformed(ActionEvent e) {
            Font f = samples[row].getFont();
            if (e.getActionCommand().equals(BOLD_BUTTON_TOOL_TIP_TEXT)) {
                if (bold[row].isSelected()) {
                    samples[row].setFont(f.deriveFont(f.getStyle() | Font.BOLD));
                } else {
                    samples[row].setFont(f.deriveFont(f.getStyle() ^ Font.BOLD));
                }
            } else {
                if (italic[row].isSelected()) {
                    samples[row].setFont(f.deriveFont(f.getStyle() | Font.ITALIC));
                } else {
                    samples[row].setFont(f.deriveFont(f.getStyle() ^ Font.ITALIC));
                }
            }
            currentStyles[row] = new SyntaxStyle(foregroundButtons[row].getBackground(),
                    italic[row].isSelected(), bold[row].isSelected());
            syntaxStylesAction = true;

        }
    }

    /////////////////////////////////////////////////////////////////
    //
    //  Class that handles click on the foreground selection button
    //
    private class ForegroundChanger implements ActionListener {
        private int row;

        public ForegroundChanger(int pos) {
            row = pos;
        }

        public void actionPerformed(ActionEvent e) {
            JButton button = (JButton) e.getSource();
            Color newColor = JColorChooser.showDialog(null, "Set Text Color", button.getBackground());
            if (newColor != null) {
                button.setBackground(newColor);
                samples[row].setForeground(newColor);
            }
            currentStyles[row] = new SyntaxStyle(button.getBackground(),
                    italic[row].isSelected(), bold[row].isSelected());
            syntaxStylesAction = true;
        }
    }

    /////////////////////////////////////////////////////////////////
    //
    // Class that handles action (check, uncheck) on the Default checkbox.
    //
    private class DefaultChanger implements ItemListener {
        private int row;

        public DefaultChanger(int pos) {
            row = pos;
        }

        public void itemStateChanged(ItemEvent e) {

            // If selected: disable buttons, save current settings, set to defaults
            // If deselected:restore current settings, enable buttons
            Color newBackground = null;
            Font newFont = null;
            if (e.getStateChange() == ItemEvent.SELECTED) {
                foregroundButtons[row].setEnabled(false);
                bold[row].setEnabled(false);
                italic[row].setEnabled(false);
                currentStyles[row] = new SyntaxStyle(foregroundButtons[row].getBackground(),
                        italic[row].isSelected(), bold[row].isSelected());
                SyntaxStyle defaultStyle = defaultStyles[syntaxStyleIndex[row]];
                setSampleStyles(samples[row], defaultStyle);
                foregroundButtons[row].setBackground(defaultStyle.getColor());
                bold[row].setSelected(defaultStyle.isBold());
                italic[row].setSelected(defaultStyle.isItalic());
            } else {
                setSampleStyles(samples[row], currentStyles[row]);
                foregroundButtons[row].setBackground(currentStyles[row].getColor());
                bold[row].setSelected(currentStyles[row].isBold());
                italic[row].setSelected(currentStyles[row].isItalic());
                foregroundButtons[row].setEnabled(true);
                bold[row].setEnabled(true);
                italic[row].setEnabled(true);
            }
            syntaxStylesAction = true;
        }


        // set the foreground color, bold and italic of sample (a JLabel)
        private void setSampleStyles(JLabel sample, SyntaxStyle style) {
            Font f = previewFont;
            if (style.isBold()) {
                f = f.deriveFont(f.getStyle() ^ Font.BOLD);
            }
            if (style.isItalic()) {
                f = f.deriveFont(f.getStyle() ^ Font.ITALIC);
            }
            sample.setFont(f);
            sample.setForeground(style.getColor());
        }
    }

}
