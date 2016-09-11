/*
 * Copyright 2016 Patrik Karlsson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.trixon.photokml.ui.config;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.apache.commons.lang3.StringUtils;
import se.trixon.almond.util.Dict;
import se.trixon.photokml.profile.Profile;
import se.trixon.photokml.profile.ProfileFolder;

/**
 *
 * @author Patrik Karlsson <patrik@trixon.se>
 */
public class ModuleFoldersPanel extends ModulePanel {

    private ProfileFolder mProfileFolder;

    private boolean mInvalidDateFormat;

    /**
     * Creates new form ModuleFoldersPanel
     */
    public ModuleFoldersPanel() {
        initComponents();
        mTitle = Dict.FOLDERS.toString();
        init();
    }

    @Override
    public StringBuilder getHeaderBuilder() {
        StringBuilder sb = new StringBuilder();

        sb.append(Dict.FOLDERS.toString().toUpperCase()).append("\n");

        sb.append(mHeaderPrefix).append(rootNameLabel.getText()).append(": ").append(mProfile.getFolder().getRootName()).append("\n");
        if (!StringUtils.isEmpty(mProfile.getFolder().getRootDescription())) {
            optAppend(sb, true, rootDescriptionLabel.getText());
            sb.append(MULTILINE_DIVIDER).append("\n");
            sb.append(mProfile.getFolder().getRootDescription()).append("\n");
            sb.append(MULTILINE_DIVIDER).append("\n");
        }

        String folderBy = "";

        if (folderByDirectoryRadioButton.isSelected()) {
            folderBy = folderByDirectoryRadioButton.getText();
        } else if (folderByDateRadioButton.isSelected()) {
            folderBy = Dict.DATE_PATTERN.toString();
        } else if (folderByRegexRadioButton.isSelected()) {
            folderBy = String.format("%s: %s", folderByRegexRadioButton.getText(), mProfileFolder.getRegex());
        }

        optAppend(sb, mProfileFolder.isCreateFolders(), subFoldersCheckBox.getText() + ": " + folderBy);

        sb.append("\n");

        return sb;
    }

    @Override
    public boolean hasValidSettings() {
        if (mInvalidDateFormat && mProfileFolder.isCreateFolders() && mProfileFolder.getFoldersBy() == 1) {
            invalidSettings(Dict.INVALID_DATE_PATTERN.toString());

            return false;
        }

        if (mProfileFolder.isCreateFolders() && mProfileFolder.getFoldersBy() == 2) {
            try {
                Pattern pattern = Pattern.compile(mProfileFolder.getRegex());
            } catch (PatternSyntaxException e) {
                String message = "PatternSyntaxException: " + e.getLocalizedMessage();
                invalidSettings(message);

                return false;
            }
        }

        return true;
    }

    @Override
    public void restoreEnabledStates() {
        subFoldersCheckBoxActionPerformed(null);
    }

    private void init() {
        DocumentListener documentListener = new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                saveOption(e.getDocument());
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                saveOption(e.getDocument());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                saveOption(e.getDocument());
            }

            private void saveOption(Document document) {
                if (document == rootNameTextField.getDocument()) {
                    mProfile.getFolder().setRootName(rootNameTextField.getText());
                } else if (document == rootDescriptionTextArea.getDocument()) {
                    mProfile.getFolder().setRootDescription(rootDescriptionTextArea.getText());
                } else if (document == dateFormatTextField.getDocument()) {
                    previewDateFormat();
                } else if (document == regexTextField.getDocument()) {
                    try {
                        mProfileFolder.setRegex(regexTextField.getText());
                    } catch (NumberFormatException e) {
                    }
                } else if (document == defaultRegexTextField.getDocument()) {
                    mProfileFolder.setRegexDefault(defaultRegexTextField.getText());
                }
            }

            private void previewDateFormat() {
                String datePreview;

                try {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormatTextField.getText());
                    datePreview = simpleDateFormat.format(new Date(System.currentTimeMillis()));
                    mInvalidDateFormat = false;
                } catch (IllegalArgumentException ex) {
                    datePreview = Dict.ERROR.toString();
                    mInvalidDateFormat = true;
                }

                String dateLabel = String.format("%s (%s)", Dict.DATE_PATTERN.toString(), datePreview);
                folderByDateRadioButton.setText(dateLabel);
                mProfileFolder.setDatePattern(dateFormatTextField.getText());
            }
        };

        rootNameTextField.getDocument().addDocumentListener(documentListener);
        rootDescriptionTextArea.getDocument().addDocumentListener(documentListener);
        dateFormatTextField.getDocument().addDocumentListener(documentListener);
        regexTextField.getDocument().addDocumentListener(documentListener);
        defaultRegexTextField.getDocument().addDocumentListener(documentListener);
    }

    private void saveFolderNameBy() {
        if (folderByDirectoryRadioButton.isSelected()) {
            mProfile.getFolder().setFoldersBy(0);
        } else if (folderByDateRadioButton.isSelected()) {
            mProfile.getFolder().setFoldersBy(1);
        } else if (folderByRegexRadioButton.isSelected()) {
            mProfile.getFolder().setFoldersBy(2);
        }

        dateFormatTextField.setEnabled(folderByDateRadioButton.isSelected());
        regexTextField.setEnabled(folderByRegexRadioButton.isSelected());
        defaultRegexTextField.setEnabled(folderByRegexRadioButton.isSelected());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        subButtonGroup = new javax.swing.ButtonGroup();
        rootNameLabel = new javax.swing.JLabel();
        rootNameTextField = new javax.swing.JTextField();
        rootDescriptionLabel = new javax.swing.JLabel();
        rootDescriptionScrollPane = new javax.swing.JScrollPane();
        rootDescriptionTextArea = new javax.swing.JTextArea();
        subFoldersCheckBox = new javax.swing.JCheckBox();
        folderByDirectoryRadioButton = new javax.swing.JRadioButton();
        folderByDateRadioButton = new javax.swing.JRadioButton();
        dateFormatTextField = new javax.swing.JTextField();
        folderByRegexRadioButton = new javax.swing.JRadioButton();
        regexTextField = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        defaultRegexLabel = new javax.swing.JLabel();
        defaultRegexTextField = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("se/trixon/photokml/ui/config/Bundle"); // NOI18N
        rootNameLabel.setText(bundle.getString("ModuleFoldersPanel.rootNameLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        add(rootNameLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        add(rootNameTextField, gridBagConstraints);

        rootDescriptionLabel.setText(bundle.getString("ModuleFoldersPanel.rootDescriptionLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        add(rootDescriptionLabel, gridBagConstraints);

        rootDescriptionTextArea.setColumns(20);
        rootDescriptionTextArea.setRows(5);
        rootDescriptionScrollPane.setViewportView(rootDescriptionTextArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        add(rootDescriptionScrollPane, gridBagConstraints);

        subFoldersCheckBox.setText(bundle.getString("ModuleFoldersPanel.subFoldersCheckBox.text")); // NOI18N
        subFoldersCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                subFoldersCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        add(subFoldersCheckBox, gridBagConstraints);

        subButtonGroup.add(folderByDirectoryRadioButton);
        folderByDirectoryRadioButton.setText(bundle.getString("ModuleFoldersPanel.folderByDirectoryRadioButton.text")); // NOI18N
        folderByDirectoryRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                folderByDirectoryRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        add(folderByDirectoryRadioButton, gridBagConstraints);

        subButtonGroup.add(folderByDateRadioButton);
        folderByDateRadioButton.setText(Dict.DATE_PATTERN.getString());
        folderByDateRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                folderByDateRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(folderByDateRadioButton, gridBagConstraints);

        dateFormatTextField.setToolTipText("<html>  <h3>Date and Time Patterns</h3>  <p>  Date and time formats are specified by <em>date and time pattern</em>  strings.  Within date and time pattern strings, unquoted letters from  <code>'A'</code> to <code>'Z'</code> and from <code>'a'</code> to  <code>'z'</code> are interpreted as pattern letters representing the  components of a date or time string.  Text can be quoted using single quotes (<code>'</code>) to avoid  interpretation.  <code>\"''\"</code> represents a single quote.  All other characters are not interpreted; they're simply copied into the  output string during formatting or matched against the input string  during parsing.  <p> The following pattern letters are defined (all other characters from  <code>'A'</code> to <code>'Z'</code> and from <code>'a'</code> to  <code>'z'</code> are reserved):  <blockquote>  <table border=0 cellspacing=3 cellpadding=0 summary=\"Chart shows pattern letters, date/time component, presentation, and examples.\">      <tr style=\"background-color: rgb(204, 204, 255);\">          <th align=left>Letter          <th align=left>Date or Time Component          <th align=left>Presentation          <th align=left>Examples      <tr>          <td><code>G</code>          <td>Era designator          <td><a href=\"#text\">Text</a>          <td><code>AD</code>      <tr style=\"background-color: rgb(238, 238, 255);\">          <td><code>y</code>          <td>Year          <td><a href=\"#year\">Year</a>          <td><code>1996</code>; <code>96</code>      <tr>          <td><code>Y</code>          <td>Week year          <td><a href=\"#year\">Year</a>          <td><code>2009</code>; <code>09</code>      <tr style=\"background-color: rgb(238, 238, 255);\">          <td><code>M</code>          <td>Month in year (context sensitive)          <td><a href=\"#month\">Month</a>          <td><code>July</code>; <code>Jul</code>; <code>07</code>      <tr>          <td><code>L</code>          <td>Month in year (standalone form)          <td><a href=\"#month\">Month</a>          <td><code>July</code>; <code>Jul</code>; <code>07</code>      <tr style=\"background-color: rgb(238, 238, 255);\">          <td><code>w</code>          <td>Week in year          <td><a href=\"#number\">Number</a>          <td><code>27</code>      <tr>          <td><code>W</code>          <td>Week in month          <td><a href=\"#number\">Number</a>          <td><code>2</code>      <tr style=\"background-color: rgb(238, 238, 255);\">          <td><code>D</code>          <td>Day in year          <td><a href=\"#number\">Number</a>          <td><code>189</code>      <tr>          <td><code>d</code>          <td>Day in month          <td><a href=\"#number\">Number</a>          <td><code>10</code>      <tr style=\"background-color: rgb(238, 238, 255);\">          <td><code>F</code>          <td>Day of week in month          <td><a href=\"#number\">Number</a>          <td><code>2</code>      <tr>          <td><code>E</code>          <td>Day name in week          <td><a href=\"#text\">Text</a>          <td><code>Tuesday</code>; <code>Tue</code>      <tr style=\"background-color: rgb(238, 238, 255);\">          <td><code>u</code>          <td>Day number of week (1 = Monday, ..., 7 = Sunday)          <td><a href=\"#number\">Number</a>          <td><code>1</code>      <tr>          <td><code>a</code>          <td>Am/pm marker          <td><a href=\"#text\">Text</a>          <td><code>PM</code>      <tr style=\"background-color: rgb(238, 238, 255);\">          <td><code>H</code>          <td>Hour in day (0-23)          <td><a href=\"#number\">Number</a>          <td><code>0</code>      <tr>          <td><code>k</code>          <td>Hour in day (1-24)          <td><a href=\"#number\">Number</a>          <td><code>24</code>      <tr style=\"background-color: rgb(238, 238, 255);\">          <td><code>K</code>          <td>Hour in am/pm (0-11)          <td><a href=\"#number\">Number</a>          <td><code>0</code>      <tr>          <td><code>h</code>          <td>Hour in am/pm (1-12)          <td><a href=\"#number\">Number</a>          <td><code>12</code>      <tr style=\"background-color: rgb(238, 238, 255);\">          <td><code>m</code>          <td>Minute in hour          <td><a href=\"#number\">Number</a>          <td><code>30</code>      <tr>          <td><code>s</code>          <td>Second in minute          <td><a href=\"#number\">Number</a>          <td><code>55</code>      <tr style=\"background-color: rgb(238, 238, 255);\">          <td><code>S</code>          <td>Millisecond          <td><a href=\"#number\">Number</a>          <td><code>978</code>      <tr>          <td><code>z</code>          <td>Time zone          <td><a href=\"#timezone\">General time zone</a>          <td><code>Pacific Standard Time</code>; <code>PST</code>; <code>GMT-08:00</code>      <tr style=\"background-color: rgb(238, 238, 255);\">          <td><code>Z</code>          <td>Time zone          <td><a href=\"#rfc822timezone\">RFC 822 time zone</a>          <td><code>-0800</code>      <tr>          <td><code>X</code>          <td>Time zone          <td><a href=\"#iso8601timezone\">ISO 8601 time zone</a>          <td><code>-08</code>; <code>-0800</code>;  <code>-08:00</code>  </table>  </blockquote>"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 3, 0);
        add(dateFormatTextField, gridBagConstraints);

        subButtonGroup.add(folderByRegexRadioButton);
        folderByRegexRadioButton.setText(bundle.getString("ModuleFoldersPanel.folderByRegexRadioButton.text")); // NOI18N
        folderByRegexRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                folderByRegexRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        add(folderByRegexRadioButton, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 3, 0);
        add(regexTextField, gridBagConstraints);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(jPanel1, gridBagConstraints);

        defaultRegexLabel.setText(Dict.DEFAULT_VALUE.getString());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 20, 0, 0);
        add(defaultRegexLabel, gridBagConstraints);

        defaultRegexTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultRegexTextFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 0);
        add(defaultRegexTextField, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void subFoldersCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_subFoldersCheckBoxActionPerformed
        boolean state = subFoldersCheckBox.isSelected();
        folderByDateRadioButton.setEnabled(state);
        folderByDirectoryRadioButton.setEnabled(state);
        folderByRegexRadioButton.setEnabled(state);
        dateFormatTextField.setEnabled(state && folderByDateRadioButton.isSelected());
        regexTextField.setEnabled(state && folderByRegexRadioButton.isSelected());
        defaultRegexTextField.setEnabled(state && folderByRegexRadioButton.isSelected());

        try {
            mProfileFolder.setCreateFolders(state);
        } catch (Exception e) {
        }
    }//GEN-LAST:event_subFoldersCheckBoxActionPerformed

    private void folderByDirectoryRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_folderByDirectoryRadioButtonActionPerformed
        saveFolderNameBy();
    }//GEN-LAST:event_folderByDirectoryRadioButtonActionPerformed

    private void folderByDateRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_folderByDateRadioButtonActionPerformed
        saveFolderNameBy();
    }//GEN-LAST:event_folderByDateRadioButtonActionPerformed

    private void folderByRegexRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_folderByRegexRadioButtonActionPerformed
        saveFolderNameBy();
    }//GEN-LAST:event_folderByRegexRadioButtonActionPerformed

    private void defaultRegexTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaultRegexTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_defaultRegexTextFieldActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField dateFormatTextField;
    private javax.swing.JLabel defaultRegexLabel;
    private javax.swing.JTextField defaultRegexTextField;
    private javax.swing.JRadioButton folderByDateRadioButton;
    private javax.swing.JRadioButton folderByDirectoryRadioButton;
    private javax.swing.JRadioButton folderByRegexRadioButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField regexTextField;
    private javax.swing.JLabel rootDescriptionLabel;
    private javax.swing.JScrollPane rootDescriptionScrollPane;
    private javax.swing.JTextArea rootDescriptionTextArea;
    private javax.swing.JLabel rootNameLabel;
    private javax.swing.JTextField rootNameTextField;
    private javax.swing.ButtonGroup subButtonGroup;
    private javax.swing.JCheckBox subFoldersCheckBox;
    // End of variables declaration//GEN-END:variables

    @Override
    public void load(Profile profile) {
        mProfile = profile;
        mProfileFolder = profile.getFolder();

        rootNameTextField.setText(mProfileFolder.getRootName());
        rootDescriptionTextArea.setText(mProfileFolder.getRootDescription());
        subFoldersCheckBox.setSelected(mProfileFolder.isCreateFolders());

        dateFormatTextField.setText(mProfileFolder.getDatePattern());
        regexTextField.setText(mProfileFolder.getRegex());
        defaultRegexTextField.setText(mProfileFolder.getRegexDefault());
        subButtonGroup.setSelected(folderByDateRadioButton.getModel(), true);

        if (mProfileFolder.getFoldersBy() == 0) {
            folderByDirectoryRadioButton.setSelected(true);
        } else if (mProfileFolder.getFoldersBy() == 1) {
            folderByDateRadioButton.setSelected(true);
        } else if (mProfileFolder.getFoldersBy() == 2) {
            folderByRegexRadioButton.setSelected(true);
        }

        restoreEnabledStates();
    }

    @Override
    public void save(Profile profile) {
    }
}
