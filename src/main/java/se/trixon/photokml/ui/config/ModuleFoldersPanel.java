/*
 * Copyright 2017 Patrik Karlsson.
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
import java.util.prefs.PreferenceChangeEvent;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.ImageIcon;
import javax.swing.JRadioButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.apache.commons.lang3.StringUtils;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.almond.util.swing.SwingHelper;
import se.trixon.photokml.profile.Profile;
import se.trixon.photokml.profile.ProfileFolder;

/**
 *
 * @author Patrik Karlsson
 */
public class ModuleFoldersPanel extends ModulePanel {

    private ProfileFolder mFolder;
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

        sb.append(Dict.FOLDERS.toString()).append("\n");
        append(sb, rootNameLabel.getText(), mFolder.getRootName());
        if (!StringUtils.isEmpty(mFolder.getRootDescription())) {
            optAppend(sb, true, rootDescriptionLabel.getText());
            sb.append(MULTILINE_DIVIDER).append("\n");
            sb.append(mFolder.getRootDescription()).append("\n");
            sb.append(MULTILINE_DIVIDER).append("\n");
        }

        JRadioButton folderByRadioButton = (JRadioButton) SwingHelper.getSelectedButton(subButtonGroup);
        if (folderByRadioButton != folderByNoneRadioButton) {
            String folderBy = folderByDirectoryRadioButton.getText();
            if (folderByRadioButton == folderByDateRadioButton) {
                folderBy = String.format("%s: %s", Dict.DATE_PATTERN.toString(), dateFormatTextField.getText());
            } else if (folderByRadioButton == folderByRegexRadioButton) {
                folderBy = String.format("%s: %s", folderByRegexRadioButton.getText(), regexTextField.getText());
            }

            append(sb, folderByLabel.getText(), folderBy);
        }

        sb.append("\n");

        return sb;
    }

    @Override
    public ImageIcon getIcon() {
        return MaterialIcon._File.FOLDER_OPEN.get(ICON_SIZE, getIconColor());
    }

    @Override
    public boolean hasValidSettings() {
        if (mInvalidDateFormat && mFolder.getFoldersBy() == 1) {
            invalidSettings(Dict.INVALID_DATE_PATTERN.toString());

            return false;
        }

        if (mFolder.getFoldersBy() == ProfileFolder.FOLDER_BY_REGEX) {
            try {
                Pattern pattern = Pattern.compile(mFolder.getRegex());
            } catch (PatternSyntaxException e) {
                String message = "PatternSyntaxException: " + e.getLocalizedMessage();
                invalidSettings(message);

                return false;
            }
        }

        return true;
    }

    @Override
    public void onPreferenceChange(PreferenceChangeEvent evt) {
        previewDateFormat();
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
                    mFolder.setRootName(rootNameTextField.getText());
                } else if (document == rootDescriptionTextArea.getDocument()) {
                    mFolder.setRootDescription(rootDescriptionTextArea.getText());
                } else if (document == dateFormatTextField.getDocument()) {
                    previewDateFormat();
                } else if (document == regexTextField.getDocument()) {
                    try {
                        mFolder.setRegex(regexTextField.getText());
                    } catch (NumberFormatException e) {
                    }
                } else if (document == defaultRegexTextField.getDocument()) {
                    mFolder.setRegexDefault(defaultRegexTextField.getText());
                }
            }

        };

        rootNameTextField.getDocument().addDocumentListener(documentListener);
        rootDescriptionTextArea.getDocument().addDocumentListener(documentListener);
        dateFormatTextField.getDocument().addDocumentListener(documentListener);
        regexTextField.getDocument().addDocumentListener(documentListener);
        defaultRegexTextField.getDocument().addDocumentListener(documentListener);
    }

    private void previewDateFormat() {
        String datePreview;

        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormatTextField.getText(), getDateFormatLocale());
            datePreview = simpleDateFormat.format(new Date(System.currentTimeMillis()));
            mInvalidDateFormat = false;
        } catch (IllegalArgumentException ex) {
            datePreview = Dict.Dialog.ERROR.toString();
            mInvalidDateFormat = true;
        }

        String dateLabel = String.format("%s (%s)", Dict.DATE_PATTERN.toString(), datePreview);
        folderByDateRadioButton.setText(dateLabel);
        mFolder.setDatePattern(dateFormatTextField.getText());
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
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        subButtonGroup = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        rootNameLabel = new javax.swing.JLabel();
        rootNameTextField = new javax.swing.JTextField();
        rootDescriptionLabel = new javax.swing.JLabel();
        rootDescriptionScrollPane = new javax.swing.JScrollPane();
        rootDescriptionTextArea = new javax.swing.JTextArea();
        jPanel2 = new javax.swing.JPanel();
        folderByLabel = new javax.swing.JLabel();
        folderByDirectoryRadioButton = new javax.swing.JRadioButton();
        folderByDateRadioButton = new javax.swing.JRadioButton();
        dateFormatTextField = new javax.swing.JTextField();
        folderByRegexRadioButton = new javax.swing.JRadioButton();
        regexTextField = new javax.swing.JTextField();
        defaultRegexLabel = new javax.swing.JLabel();
        defaultRegexTextField = new javax.swing.JTextField();
        folderByNoneRadioButton = new javax.swing.JRadioButton();
        jPanel3 = new javax.swing.JPanel();

        setLayout(new java.awt.GridLayout(1, 0, 8, 0));

        jPanel1.setLayout(new java.awt.GridBagLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("se/trixon/photokml/ui/config/Bundle"); // NOI18N
        rootNameLabel.setText(bundle.getString("ModuleFoldersPanel.rootNameLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        jPanel1.add(rootNameLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        jPanel1.add(rootNameTextField, gridBagConstraints);

        rootDescriptionLabel.setText(bundle.getString("ModuleFoldersPanel.rootDescriptionLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        jPanel1.add(rootDescriptionLabel, gridBagConstraints);

        rootDescriptionScrollPane.setMinimumSize(new java.awt.Dimension(20, 64));

        rootDescriptionTextArea.setColumns(20);
        rootDescriptionScrollPane.setViewportView(rootDescriptionTextArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 3, 0);
        jPanel1.add(rootDescriptionScrollPane, gridBagConstraints);

        add(jPanel1);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        folderByLabel.setText(bundle.getString("ModuleFoldersPanel.folderByLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        jPanel2.add(folderByLabel, gridBagConstraints);

        subButtonGroup.add(folderByDirectoryRadioButton);
        folderByDirectoryRadioButton.setText(bundle.getString("ModuleFoldersPanel.folderByDirectoryRadioButton.text")); // NOI18N
        folderByDirectoryRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                folderByDirectoryRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        jPanel2.add(folderByDirectoryRadioButton, gridBagConstraints);

        subButtonGroup.add(folderByDateRadioButton);
        folderByDateRadioButton.setText(Dict.DATE_PATTERN.getString());
        folderByDateRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                folderByDateRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel2.add(folderByDateRadioButton, gridBagConstraints);

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, folderByDateRadioButton, org.jdesktop.beansbinding.ELProperty.create("${selected}"), dateFormatTextField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 3, 0);
        jPanel2.add(dateFormatTextField, gridBagConstraints);

        subButtonGroup.add(folderByRegexRadioButton);
        folderByRegexRadioButton.setText(bundle.getString("ModuleFoldersPanel.folderByRegexRadioButton.text")); // NOI18N
        folderByRegexRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                folderByRegexRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        jPanel2.add(folderByRegexRadioButton, gridBagConstraints);

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, folderByRegexRadioButton, org.jdesktop.beansbinding.ELProperty.create("${selected}"), regexTextField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 3, 0);
        jPanel2.add(regexTextField, gridBagConstraints);

        defaultRegexLabel.setText(Dict.DEFAULT_VALUE.getString());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 8, 0, 0);
        jPanel2.add(defaultRegexLabel, gridBagConstraints);

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, folderByRegexRadioButton, org.jdesktop.beansbinding.ELProperty.create("${selected}"), defaultRegexTextField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 0);
        jPanel2.add(defaultRegexTextField, gridBagConstraints);

        subButtonGroup.add(folderByNoneRadioButton);
        folderByNoneRadioButton.setText(bundle.getString("ModuleFoldersPanel.folderByNoneRadioButton.text")); // NOI18N
        folderByNoneRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                folderByNoneRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        jPanel2.add(folderByNoneRadioButton, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 1.0;
        jPanel2.add(jPanel3, gridBagConstraints);

        add(jPanel2);

        bindingGroup.bind();
    }// </editor-fold>//GEN-END:initComponents

    private void folderByDirectoryRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_folderByDirectoryRadioButtonActionPerformed
        mFolder.setFoldersBy(ProfileFolder.FOLDER_BY_DIR);
    }//GEN-LAST:event_folderByDirectoryRadioButtonActionPerformed

    private void folderByDateRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_folderByDateRadioButtonActionPerformed
        mFolder.setFoldersBy(ProfileFolder.FOLDER_BY_DATE);
    }//GEN-LAST:event_folderByDateRadioButtonActionPerformed

    private void folderByRegexRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_folderByRegexRadioButtonActionPerformed
        mFolder.setFoldersBy(ProfileFolder.FOLDER_BY_REGEX);
    }//GEN-LAST:event_folderByRegexRadioButtonActionPerformed

    private void folderByNoneRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_folderByNoneRadioButtonActionPerformed
        mFolder.setFoldersBy(ProfileFolder.FOLDER_BY_NONE);
    }//GEN-LAST:event_folderByNoneRadioButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField dateFormatTextField;
    private javax.swing.JLabel defaultRegexLabel;
    private javax.swing.JTextField defaultRegexTextField;
    private javax.swing.JRadioButton folderByDateRadioButton;
    private javax.swing.JRadioButton folderByDirectoryRadioButton;
    private javax.swing.JLabel folderByLabel;
    private javax.swing.JRadioButton folderByNoneRadioButton;
    private javax.swing.JRadioButton folderByRegexRadioButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JTextField regexTextField;
    private javax.swing.JLabel rootDescriptionLabel;
    private javax.swing.JScrollPane rootDescriptionScrollPane;
    private javax.swing.JTextArea rootDescriptionTextArea;
    private javax.swing.JLabel rootNameLabel;
    private javax.swing.JTextField rootNameTextField;
    private javax.swing.ButtonGroup subButtonGroup;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables

    @Override
    public void load(Profile profile) {
        mProfile = profile;
        mFolder = profile.getFolder();

        rootNameTextField.setText(mFolder.getRootName());
        rootDescriptionTextArea.setText(mFolder.getRootDescription());
        dateFormatTextField.setText(mFolder.getDatePattern());
        regexTextField.setText(mFolder.getRegex());
        defaultRegexTextField.setText(mFolder.getRegexDefault());
        JRadioButton folderByRadioButton;

        switch (mFolder.getFoldersBy()) {
            case ProfileFolder.FOLDER_BY_DIR:
                folderByRadioButton = folderByDirectoryRadioButton;
                break;

            case ProfileFolder.FOLDER_BY_DATE:
                folderByRadioButton = folderByDateRadioButton;
                break;

            case ProfileFolder.FOLDER_BY_REGEX:
                folderByRadioButton = folderByRegexRadioButton;
                break;

            default:
                folderByRadioButton = folderByNoneRadioButton;
                break;
        }

        subButtonGroup.setSelected(folderByRadioButton.getModel(), true);
    }
}
