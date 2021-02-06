package mars.venus.actions.settings;

import mars.*;
import mars.venus.AbstractFontSettingDialog;
import mars.venus.Editor;
import mars.venus.VenusUI;
import mars.venus.actions.GuiAction;
import mars.venus.editors.jeditsyntax.*;
import mars.venus.editors.jeditsyntax.tokenmarker.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.border.*;
	
	/*
Copyright (c) 2003-2011,  Pete Sanderson and Kenneth Vollmar

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
 * Action class for the Settings menu item for text editor settings.
 */
public class SettingsEditorAction extends GuiAction {

    JDialog editorDialog;

    /**
     * Create a new SettingsEditorAction.  Has all the GuiAction parameters.
     */
    public SettingsEditorAction(String name, Icon icon, String descrip,
                                Integer mnemonic, KeyStroke accel, VenusUI gui) {
        super(name, icon, descrip, mnemonic, accel, gui);
    }

    /**
     * When this action is triggered, launch a dialog to view and modify
     * editor settings.
     */
    public void actionPerformed(ActionEvent e) {
        editorDialog = new EditorFontDialog(Globals.getGui(), "Text Editor Settings", true, Globals.getSettings().getEditorFont());
        editorDialog.setVisible(true);

    }

    private static final String GENERIC_TOOL_TIP_TEXT = "Use generic editor (original MARS editor, similar to Notepad) instead of language-aware styled editor";

    // Concrete font chooser class.
    private static class EditorFontDialog extends AbstractFontSettingDialog {

        private JButton[] foregroundButtons;
        private JLabel[] samples;
        private JToggleButton[] bold, italic;
        private JCheckBox[] useDefault;

        private int[] syntaxStyleIndex;
        private SyntaxStyle[] defaultStyles, initialStyles, currentStyles;
        private Font previewFont;

        private JPanel dialogPanel, syntaxStylePanel, otherSettingsPanel;

        private JSlider tabSizeSelector;
        private JSpinner tabSizeSpinSelector, blinkRateSpinSelector, popupPrefixLengthSpinSelector;
        private JCheckBox lineHighlightCheck, genericEditorCheck, autoIndentCheck;
        private Caret blinkCaret;
        private JTextField blinkSample;
        private ButtonGroup popupGuidanceButtons;
        private JRadioButton[] popupGuidanceOptions;
        // Flag to indicate whether any syntax style buttons have been clicked
        // since dialog created or most recent "apply".
        private boolean syntaxStylesAction = false;

        private int initialEditorTabSize, initialCaretBlinkRate, initialPopupGuidance;
        private boolean initialLineHighlighting, initialGenericTextEditor, initialAutoIndent;

        public EditorFontDialog(Frame owner, String title, boolean modality, Font font) {
            super(owner, title, modality, font);
            if (Globals.getSettings().getBooleanSetting(Settings.GENERIC_TEXT_EDITOR)) {
                syntaxStylePanel.setVisible(false);
                otherSettingsPanel.setVisible(false);
            }
        }

        // build the dialog here
        protected JPanel buildDialogPanel() {
            JPanel dialog = new JPanel(new BorderLayout());

            JPanel fontDialogPanel = super.buildDialogPanel();
            JPanel syntaxStylePanel = new SyntaxStylePanel();
            JPanel otherSettingsPanel = new OtherSettingsPanel();

            fontDialogPanel.setBorder(BorderFactory.createTitledBorder("Editor Font"));
            syntaxStylePanel.setBorder(BorderFactory.createTitledBorder("Syntax Styling"));
            otherSettingsPanel.setBorder(BorderFactory.createTitledBorder("Other Editor Settings"));

            dialog.add(fontDialogPanel, BorderLayout.WEST);
            dialog.add(syntaxStylePanel, BorderLayout.CENTER);
            dialog.add(otherSettingsPanel, BorderLayout.SOUTH);

            this.dialogPanel = dialog;
            this.syntaxStylePanel = syntaxStylePanel;
            this.otherSettingsPanel = otherSettingsPanel;

            return dialog;
        }

        // Row of control buttons to be placed along the button of the dialog
        protected Component buildControlPanel() {
            Box controlPanel = Box.createHorizontalBox();
            JButton okButton = new JButton("Apply and Close");
            okButton.setToolTipText(SettingsHighlightingAction.CLOSE_TOOL_TIP_TEXT);
            okButton.addActionListener(
                    e -> {
                        performApply();
                        closeDialog();
                    });
            JButton applyButton = new JButton("Apply");
            applyButton.setToolTipText(SettingsHighlightingAction.APPLY_TOOL_TIP_TEXT);
            applyButton.addActionListener(
                    e -> performApply());
            JButton cancelButton = new JButton("Cancel");
            cancelButton.setToolTipText(SettingsHighlightingAction.CANCEL_TOOL_TIP_TEXT);
            cancelButton.addActionListener(
                    e -> closeDialog());
            JButton resetButton = new JButton("Reset");
            resetButton.setToolTipText(SettingsHighlightingAction.RESET_TOOL_TIP_TEXT);
            resetButton.addActionListener(
                    e -> reset());
            initialGenericTextEditor = Globals.getSettings().getBooleanSetting(Settings.GENERIC_TEXT_EDITOR);
            genericEditorCheck = new JCheckBox("Use Generic Editor", initialGenericTextEditor);
            genericEditorCheck.setToolTipText(GENERIC_TOOL_TIP_TEXT);
            genericEditorCheck.addItemListener(
                    e -> {
                        if (e.getStateChange() == ItemEvent.SELECTED) {
                            syntaxStylePanel.setVisible(false);
                            otherSettingsPanel.setVisible(false);
                        } else {
                            syntaxStylePanel.setVisible(true);
                            otherSettingsPanel.setVisible(true);
                        }
                    });

            controlPanel.add(Box.createHorizontalGlue());
            controlPanel.add(okButton);
            controlPanel.add(Box.createHorizontalGlue());
            controlPanel.add(applyButton);
            controlPanel.add(Box.createHorizontalGlue());
            controlPanel.add(cancelButton);
            controlPanel.add(Box.createHorizontalGlue());
            controlPanel.add(resetButton);
            controlPanel.add(Box.createHorizontalGlue());
            controlPanel.add(genericEditorCheck);
            controlPanel.add(Box.createHorizontalGlue());
            return controlPanel;
        }

        // User has clicked "Apply" or "Apply and Close" button.  Required method, is
        // abstract in superclass.
        protected void apply(Font font) {
            Globals.getSettings().setBooleanSetting(Settings.GENERIC_TEXT_EDITOR, genericEditorCheck.isSelected());
            Globals.getSettings().setBooleanSetting(Settings.EDITOR_CURRENT_LINE_HIGHLIGHTING, lineHighlightCheck.isSelected());
            Globals.getSettings().setBooleanSetting(Settings.AUTO_INDENT, autoIndentCheck.isSelected());
            Globals.getSettings().setCaretBlinkRate((Integer) blinkRateSpinSelector.getValue());
            Globals.getSettings().setEditorTabSize(tabSizeSelector.getValue());
            if (syntaxStylesAction) {
                for (int i = 0; i < syntaxStyleIndex.length; i++) {
                    Globals.getSettings().setEditorSyntaxStyleByPosition(syntaxStyleIndex[i],
                            new SyntaxStyle(samples[i].getForeground(),
                                    italic[i].isSelected(), bold[i].isSelected()));
                }
                syntaxStylesAction = false; // reset
            }
            Globals.getSettings().setEditorFont(font);
            for (int i = 0; i < popupGuidanceOptions.length; i++) {
                if (popupGuidanceOptions[i].isSelected()) {
                    if (i == 0) {
                        Globals.getSettings().setBooleanSetting(Settings.POPUP_INSTRUCTION_GUIDANCE, false);
                    } else {
                        Globals.getSettings().setBooleanSetting(Settings.POPUP_INSTRUCTION_GUIDANCE, true);
                        Globals.getSettings().setEditorPopupPrefixLength(i);
                    }
                    break;
                }
            }
        }

        // User has clicked "Reset" button.  Put everything back to initial state.
        protected void reset() {
            super.reset();
            initializeSyntaxStyleChangeables();
            resetOtherSettings();
            syntaxStylesAction = true;
            genericEditorCheck.setSelected(initialGenericTextEditor);
        }


        // Perform reset on miscellaneous editor settings
        private void resetOtherSettings() {
            tabSizeSelector.setValue(initialEditorTabSize);
            tabSizeSpinSelector.setValue(initialEditorTabSize);
            lineHighlightCheck.setSelected(initialLineHighlighting);
            autoIndentCheck.setSelected(initialAutoIndent);
            blinkRateSpinSelector.setValue(initialCaretBlinkRate);
            blinkCaret.setBlinkRate(initialCaretBlinkRate);
            popupGuidanceOptions[initialPopupGuidance].setSelected(true);
        }


        // Set or reset the changeable features of component for syntax style
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


    }

}