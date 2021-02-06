package mars.venus.actions.settings;

import mars.Globals;
import mars.Settings;
import mars.venus.Editor;

import javax.swing.*;
import javax.swing.text.Caret;
import java.awt.*;

// Miscellaneous editor settings (cursor blinking, line highlighting, tab size, etc)
public class OtherSettingsPanel extends JPanel {

    private static final String TAB_SIZE_TOOL_TIP_TEXT = "Current tab size in characters";
    private static final String BLINK_SPINNER_TOOL_TIP_TEXT = "Current blinking rate in milliseconds";
    private static final String BLINK_SAMPLE_TOOL_TIP_TEXT = "Displays current blinking rate";
    private static final String CURRENT_LINE_HIGHLIGHT_TOOL_TIP_TEXT = "Check, to highlight line currently being edited";
    private static final String AUTO_INDENT_TOOL_TIP_TEXT = "Check, to enable auto-indent to previous line when Enter key is pressed";
    private static final String[] POPUP_GUIDANCE_TOOL_TIP_TEXT = {"Turns off instruction and directive guide popup while typing",
            "Generates instruction guide popup after first letter of potential instruction is typed",
            "Generates instruction guide popup after second letter of potential instruction is typed"
    };

    private JSlider tabSizeSelector;
    private JSpinner tabSizeSpinSelector, blinkRateSpinSelector;
    private JCheckBox lineHighlightCheck, autoIndentCheck;
    private Caret blinkCaret;
    private JTextField blinkSample;
    private JRadioButton[] popupGuidanceOptions;

    private int editorTabSize;
    private int caretBlinkRate;
    private int popupGuidance;
    private boolean lineHighlighting;
    private boolean autoIndent;

    public OtherSettingsPanel() {
        retrieveCurrentSettings();

        initTabSize();
        initLineHighlighting();
        initAutoIndent();
        initCursorBlinkRate();
        initPopupGuidance();

        JPanel tabPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tabPanel.add(new JLabel("Tab Size"));
        tabPanel.add(tabSizeSelector);
        tabPanel.add(tabSizeSpinSelector);

        JPanel blinkPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        blinkPanel.add(new JLabel("Cursor Blinking Rate in ms (0=no blink)"));
        blinkPanel.add(blinkRateSpinSelector);
        blinkPanel.add(blinkSample);

        this.setLayout(new GridLayout(1, 2));
        JPanel leftColumnSettingsPanel = new JPanel(new GridLayout(4, 1));
        leftColumnSettingsPanel.add(tabPanel);
        leftColumnSettingsPanel.add(blinkPanel);
        leftColumnSettingsPanel.add(lineHighlightCheck);
        leftColumnSettingsPanel.add(autoIndentCheck);

        // Combine instruction guide off/on and instruction prefix length into radio buttons
        JPanel rightColumnSettingsPanel = new JPanel(new GridLayout(4, 1));


        popupGuidanceOptions[popupGuidance].setSelected(true);
        rightColumnSettingsPanel.setBorder(BorderFactory.createTitledBorder("Popup Instruction Guide"));
        rightColumnSettingsPanel.add(popupGuidanceOptions[0]);
        rightColumnSettingsPanel.add(popupGuidanceOptions[1]);
        rightColumnSettingsPanel.add(popupGuidanceOptions[2]);

        this.add(leftColumnSettingsPanel);
        this.add(rightColumnSettingsPanel);
    }

    private void initPopupGuidance() {
        ButtonGroup popupGuidanceButtons = new ButtonGroup();
        popupGuidanceOptions = new JRadioButton[3];
        popupGuidanceOptions[0] = new JRadioButton("No popup instruction or directive guide");
        popupGuidanceOptions[1] = new JRadioButton("Display instruction guide after 1 letter typed");
        popupGuidanceOptions[2] = new JRadioButton("Display instruction guide after 2 letters typed");
        for (int i = 0; i < popupGuidanceOptions.length; i++) {
            popupGuidanceOptions[i].setSelected(false);
            popupGuidanceOptions[i].setToolTipText(POPUP_GUIDANCE_TOOL_TIP_TEXT[i]);
            popupGuidanceButtons.add(popupGuidanceOptions[i]);
        }
    }

    private void initCursorBlinkRate() {
        blinkSample = new JTextField("     ");
        blinkSample.setCaretPosition(2);
        blinkSample.setToolTipText(BLINK_SAMPLE_TOOL_TIP_TEXT);
        blinkSample.setEnabled(false);
        blinkCaret = blinkSample.getCaret();
        blinkCaret.setBlinkRate(caretBlinkRate);
        blinkCaret.setVisible(true);
        SpinnerNumberModel blinkRateSpinnerModel = new SpinnerNumberModel(caretBlinkRate, Editor.MIN_BLINK_RATE, Editor.MAX_BLINK_RATE, 100);
        blinkRateSpinSelector = new JSpinner(blinkRateSpinnerModel);
        blinkRateSpinSelector.setToolTipText(BLINK_SPINNER_TOOL_TIP_TEXT);
        blinkRateSpinSelector.addChangeListener(
                e -> {
                    Object value = ((JSpinner) e.getSource()).getValue();
                    blinkCaret.setBlinkRate((Integer) value);
                    blinkSample.requestFocus();
                    blinkCaret.setVisible(true);
                });
    }

    private void initTabSize() {
        tabSizeSelector = new JSlider(Editor.MIN_TAB_SIZE, Editor.MAX_TAB_SIZE, editorTabSize);
        tabSizeSelector.setToolTipText("Use slider to select tab size from " + Editor.MIN_TAB_SIZE + " to " + Editor.MAX_TAB_SIZE + ".");
        tabSizeSelector.addChangeListener(
                e -> {
                    Integer value = ((JSlider) e.getSource()).getValue();
                    tabSizeSpinSelector.setValue(value);
                });
        SpinnerNumberModel tabSizeSpinnerModel = new SpinnerNumberModel(editorTabSize, Editor.MIN_TAB_SIZE, Editor.MAX_TAB_SIZE, 1);
        tabSizeSpinSelector = new JSpinner(tabSizeSpinnerModel);
        tabSizeSpinSelector.setToolTipText(TAB_SIZE_TOOL_TIP_TEXT);
        tabSizeSpinSelector.addChangeListener(
                e -> {
                    Object value = ((JSpinner) e.getSource()).getValue();
                    tabSizeSelector.setValue((Integer) value);
                });
    }

    private void initLineHighlighting() {
        lineHighlightCheck = new JCheckBox("Highlight the line currently being edited");
        lineHighlightCheck.setSelected(lineHighlighting);
        lineHighlightCheck.setToolTipText(CURRENT_LINE_HIGHLIGHT_TOOL_TIP_TEXT);
    }

    private void initAutoIndent() {
        autoIndentCheck = new JCheckBox("Auto-Indent");
        autoIndentCheck.setSelected(autoIndent);
        autoIndentCheck.setToolTipText(AUTO_INDENT_TOOL_TIP_TEXT);
    }

    private void retrieveCurrentSettings() {
        Settings settings = Globals.getSettings();
        editorTabSize = settings.getEditorTabSize();
        lineHighlighting = settings.getBooleanSetting(Settings.EDITOR_CURRENT_LINE_HIGHLIGHTING);
        autoIndent = settings.getBooleanSetting(Settings.AUTO_INDENT);
        caretBlinkRate = settings.getCaretBlinkRate();
        popupGuidance = settings.getBooleanSetting(Settings.POPUP_INSTRUCTION_GUIDANCE)
                ? settings.getEditorPopupPrefixLength() : 0;
    }

}
