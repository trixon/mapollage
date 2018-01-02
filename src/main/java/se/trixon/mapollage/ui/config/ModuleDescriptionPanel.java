/*
 * Copyright 2018 Patrik Karlsson.
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
package se.trixon.mapollage.ui.config;

import javax.swing.ImageIcon;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.apache.commons.lang3.StringUtils;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.almond.util.swing.SwingHelper;
import se.trixon.mapollage.profile.Profile;
import se.trixon.mapollage.profile.ProfileDescription;
import se.trixon.mapollage.profile.ProfileDescription.DescriptionMode;
import se.trixon.mapollage.profile.ProfileDescription.DescriptionSegment;

/**
 *
 * @author Patrik Karlsson
 */
public class ModuleDescriptionPanel extends ModulePanel {

    private ProfileDescription mDescription;
    private DescriptionMode mMode;
    private PhotoDescriptionMonitor mPhotoDescriptionMonitor;

    /**
     * Creates new form ModuleDescriptionPanel
     */
    public ModuleDescriptionPanel() {
        initComponents();
        mTitle = Dict.DESCRIPTION.toString();
        init();
    }

    @Override
    public ImageIcon getIcon() {
        return MaterialIcon._Action.DESCRIPTION.get(ICON_SIZE, getIconColor());
    }

    @Override
    public boolean hasValidSettings() {
        return true;
    }

    public void setPhotoDescriptionMonitor(PhotoDescriptionMonitor photoDescriptionMonitor) {
        mPhotoDescriptionMonitor = photoDescriptionMonitor;
    }

    private void descriptionModeChanged() {
        if (customRadioButton.isSelected()) {
            mMode = DescriptionMode.CUSTOM;
        } else if (externalRadioButton.isSelected()) {
            mMode = DescriptionMode.EXTERNAL;
        } else if (staticRadioButton.isSelected()) {
            mMode = DescriptionMode.STATIC;
        }

        mDescription.setMode(mMode);

        externalFileTextField.setEnabled(mMode == DescriptionMode.EXTERNAL);
        defaultToCheckBox.setEnabled(mMode == DescriptionMode.EXTERNAL);
        defaultCustomRadioButton.setEnabled(defaultToCheckBox.isSelected() && mMode == DescriptionMode.EXTERNAL);
        defaultStaticRadioButton.setEnabled(defaultToCheckBox.isSelected() && mMode == DescriptionMode.EXTERNAL);

        customTextArea.setEnabled(mMode == DescriptionMode.CUSTOM);
        defaultButton.setEnabled(mMode == DescriptionMode.CUSTOM);

        SwingHelper.enableComponents(staticPanel, mMode == DescriptionMode.STATIC);
        notifyPhotoDescriptionListener();
    }

    private void init() {
        DocumentListener documentListener = new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                saveOption(e.getDocument());
                notifyPhotoDescriptionListener();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                saveOption(e.getDocument());
                notifyPhotoDescriptionListener();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                saveOption(e.getDocument());
                notifyPhotoDescriptionListener();
            }

            private void saveOption(Document document) {
                if (document == customTextArea.getDocument()) {
                    mDescription.setCustomValue(customTextArea.getText());
                } else if (document == externalFileTextField.getDocument()) {
                    mDescription.setExternalFileValue(externalFileTextField.getText());
                }
            }
        };

        customTextArea.getDocument().addDocumentListener(documentListener);
        externalFileTextField.getDocument().addDocumentListener(documentListener);
    }

    private void notifyPhotoDescriptionListener() {
        boolean hasPhoto = (photoCheckBox.isSelected() && photoCheckBox.isEnabled())
                || (customRadioButton.isSelected() && StringUtils.containsIgnoreCase(customTextArea.getText(), "+photo"))
                || mMode == DescriptionMode.EXTERNAL;

        if (mProfile != null) {
            try {
                mPhotoDescriptionMonitor.onPhotoDescriptionChange(hasPhoto);
            } catch (NullPointerException e) {
                // nvm
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        buttonGroup = new javax.swing.ButtonGroup();
        defaultButtonGroup = new javax.swing.ButtonGroup();
        externalRadioButton = new javax.swing.JRadioButton();
        externalFileTextField = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        customRadioButton = new javax.swing.JRadioButton();
        defaultButton = new javax.swing.JButton();
        customScrollPane = new javax.swing.JScrollPane();
        customTextArea = new javax.swing.JTextArea();
        jPanel2 = new javax.swing.JPanel();
        staticRadioButton = new javax.swing.JRadioButton();
        staticPanel = new javax.swing.JPanel();
        photoCheckBox = new javax.swing.JCheckBox();
        filenameCheckBox = new javax.swing.JCheckBox();
        dateCheckBox = new javax.swing.JCheckBox();
        coordinateCheckBox = new javax.swing.JCheckBox();
        altitudeCheckBox = new javax.swing.JCheckBox();
        bearingCheckBox = new javax.swing.JCheckBox();
        defaultToCheckBox = new javax.swing.JCheckBox();
        defaultStaticRadioButton = new javax.swing.JRadioButton();
        defaultCustomRadioButton = new javax.swing.JRadioButton();

        setLayout(new java.awt.GridBagLayout());

        buttonGroup.add(externalRadioButton);
        externalRadioButton.setText(Dict.EXTERNAL_FILE.toString());
        externalRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                externalRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        add(externalRadioButton, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        add(externalFileTextField, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        buttonGroup.add(customRadioButton);
        customRadioButton.setText(Dict.CUSTOMIZED.toString());
        customRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                customRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(customRadioButton, gridBagConstraints);

        defaultButton.setText(Dict.RESET.toString());
        defaultButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(defaultButton, gridBagConstraints);

        customTextArea.setColumns(20);
        customTextArea.setFont(new java.awt.Font("Monospaced", 0, 15)); // NOI18N
        customTextArea.setRows(5);
        customScrollPane.setViewportView(customTextArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 0);
        jPanel1.add(customScrollPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 0);
        add(jPanel1, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        buttonGroup.add(staticRadioButton);
        staticRadioButton.setText(Dict.STATIC.toString());
        staticRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                staticRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        jPanel2.add(staticRadioButton, gridBagConstraints);

        staticPanel.setLayout(new javax.swing.BoxLayout(staticPanel, javax.swing.BoxLayout.PAGE_AXIS));

        photoCheckBox.setText(Dict.PHOTO.toString());
        photoCheckBox.setToolTipText(DescriptionSegment.PHOTO.toString());
        photoCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                photoCheckBoxActionPerformed(evt);
            }
        });
        staticPanel.add(photoCheckBox);

        filenameCheckBox.setText(Dict.FILENAME.toString());
        filenameCheckBox.setToolTipText(DescriptionSegment.FILENAME.toString());
        filenameCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filenameCheckBoxActionPerformed(evt);
            }
        });
        staticPanel.add(filenameCheckBox);

        dateCheckBox.setText(Dict.DATE.toString());
        dateCheckBox.setToolTipText(DescriptionSegment.DATE.toString());
        dateCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dateCheckBoxActionPerformed(evt);
            }
        });
        staticPanel.add(dateCheckBox);

        coordinateCheckBox.setText(Dict.COORDINATE.toString());
        coordinateCheckBox.setToolTipText(DescriptionSegment.COORDINATE.toString());
        coordinateCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                coordinateCheckBoxActionPerformed(evt);
            }
        });
        staticPanel.add(coordinateCheckBox);

        altitudeCheckBox.setText(Dict.ALTITUDE.toString());
        altitudeCheckBox.setToolTipText(DescriptionSegment.ALTITUDE.toString());
        altitudeCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                altitudeCheckBoxActionPerformed(evt);
            }
        });
        staticPanel.add(altitudeCheckBox);

        bearingCheckBox.setText(Dict.BEARING.toString());
        bearingCheckBox.setToolTipText(DescriptionSegment.BEARING.toString());
        bearingCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bearingCheckBoxActionPerformed(evt);
            }
        });
        staticPanel.add(bearingCheckBox);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel2.add(staticPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        add(jPanel2, gridBagConstraints);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("se/trixon/mapollage/ui/config/Bundle"); // NOI18N
        defaultToCheckBox.setText(bundle.getString("ModuleDescriptionPanel.defaultToCheckBox.text")); // NOI18N
        defaultToCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultToCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 1.0;
        add(defaultToCheckBox, gridBagConstraints);

        defaultButtonGroup.add(defaultStaticRadioButton);
        defaultStaticRadioButton.setText(Dict.STATIC.toString());

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, defaultToCheckBox, org.jdesktop.beansbinding.ELProperty.create("${selected}"), defaultStaticRadioButton, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        defaultStaticRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultStaticRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 8);
        add(defaultStaticRadioButton, gridBagConstraints);

        defaultButtonGroup.add(defaultCustomRadioButton);
        defaultCustomRadioButton.setText(Dict.CUSTOMIZED.toString());

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ, defaultToCheckBox, org.jdesktop.beansbinding.ELProperty.create("${selected}"), defaultCustomRadioButton, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        defaultCustomRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultCustomRadioButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        add(defaultCustomRadioButton, gridBagConstraints);

        bindingGroup.bind();
    }// </editor-fold>//GEN-END:initComponents

    private void bearingCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bearingCheckBoxActionPerformed
        mDescription.setBearing(bearingCheckBox.isSelected());
    }//GEN-LAST:event_bearingCheckBoxActionPerformed

    private void coordinateCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_coordinateCheckBoxActionPerformed
        mDescription.setCoordinate(coordinateCheckBox.isSelected());
    }//GEN-LAST:event_coordinateCheckBoxActionPerformed

    private void dateCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dateCheckBoxActionPerformed
        mDescription.setDate(dateCheckBox.isSelected());
    }//GEN-LAST:event_dateCheckBoxActionPerformed

    private void photoCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_photoCheckBoxActionPerformed
        mDescription.setPhoto(photoCheckBox.isSelected());
        notifyPhotoDescriptionListener();
    }//GEN-LAST:event_photoCheckBoxActionPerformed

    private void altitudeCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_altitudeCheckBoxActionPerformed
        mDescription.setAltitude(altitudeCheckBox.isSelected());
    }//GEN-LAST:event_altitudeCheckBoxActionPerformed

    private void filenameCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filenameCheckBoxActionPerformed
        mDescription.setFilename(filenameCheckBox.isSelected());
    }//GEN-LAST:event_filenameCheckBoxActionPerformed

    private void customRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customRadioButtonActionPerformed
        descriptionModeChanged();
    }//GEN-LAST:event_customRadioButtonActionPerformed

    private void staticRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_staticRadioButtonActionPerformed
        descriptionModeChanged();
    }//GEN-LAST:event_staticRadioButtonActionPerformed

    private void defaultButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaultButtonActionPerformed
        customTextArea.setText(ProfileDescription.getDefaultCustomValue());
    }//GEN-LAST:event_defaultButtonActionPerformed

    private void externalRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_externalRadioButtonActionPerformed
        descriptionModeChanged();
    }//GEN-LAST:event_externalRadioButtonActionPerformed

    private void defaultStaticRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaultStaticRadioButtonActionPerformed
        mDescription.setDefaultMode(DescriptionMode.STATIC);
    }//GEN-LAST:event_defaultStaticRadioButtonActionPerformed

    private void defaultCustomRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaultCustomRadioButtonActionPerformed
        mDescription.setDefaultMode(DescriptionMode.CUSTOM);
    }//GEN-LAST:event_defaultCustomRadioButtonActionPerformed

    private void defaultToCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaultToCheckBoxActionPerformed
        mDescription.setDefaultTo(defaultToCheckBox.isSelected());
    }//GEN-LAST:event_defaultToCheckBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox altitudeCheckBox;
    private javax.swing.JCheckBox bearingCheckBox;
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JCheckBox coordinateCheckBox;
    private javax.swing.JRadioButton customRadioButton;
    private javax.swing.JScrollPane customScrollPane;
    private javax.swing.JTextArea customTextArea;
    private javax.swing.JCheckBox dateCheckBox;
    private javax.swing.JButton defaultButton;
    private javax.swing.ButtonGroup defaultButtonGroup;
    private javax.swing.JRadioButton defaultCustomRadioButton;
    private javax.swing.JRadioButton defaultStaticRadioButton;
    private javax.swing.JCheckBox defaultToCheckBox;
    private javax.swing.JTextField externalFileTextField;
    private javax.swing.JRadioButton externalRadioButton;
    private javax.swing.JCheckBox filenameCheckBox;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JCheckBox photoCheckBox;
    private javax.swing.JPanel staticPanel;
    private javax.swing.JRadioButton staticRadioButton;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables

    @Override
    public void load(Profile profile) {
        mProfile = profile;

        if (mProfile != null) {
            mDescription = mProfile.getDescription();
            staticRadioButton.setSelected(true);
            altitudeCheckBox.setSelected(mDescription.hasAltitude());
            bearingCheckBox.setSelected(mDescription.hasBearing());
            coordinateCheckBox.setSelected(mDescription.hasCoordinate());
            dateCheckBox.setSelected(mDescription.hasDate());
            photoCheckBox.setSelected(mDescription.hasPhoto());
            filenameCheckBox.setSelected(mDescription.hasFilename());
            externalFileTextField.setText(mDescription.getExternalFileValue());
            customTextArea.setText(mDescription.getCustomValue());

            defaultToCheckBox.setSelected(mDescription.isDefaultTo());
            mMode = mDescription.getMode();
            switch (mMode) {
                case CUSTOM:
                    customRadioButton.setSelected(true);
                    break;
                case EXTERNAL:
                    externalRadioButton.setSelected(true);
                    break;
                case STATIC:
                    staticRadioButton.setSelected(true);
                    break;

                default:
                    throw new AssertionError();
            }
            if (mDescription.getDefaultMode() == DescriptionMode.CUSTOM) {
                defaultCustomRadioButton.setSelected(true);
            } else {
                defaultStaticRadioButton.setSelected(true);
            }

            descriptionModeChanged();
        }
    }

    public interface PhotoDescriptionMonitor {

        void onPhotoDescriptionChange(boolean hasPhoto);
    }
}
