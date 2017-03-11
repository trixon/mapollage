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
package se.trixon.mapollage.ui.config;

import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;
import org.apache.commons.lang3.SystemUtils;
import se.trixon.almond.util.swing.LogPanel;
import se.trixon.almond.util.swing.SwingHelper;
import se.trixon.mapollage.profile.Profile;

/**
 *
 * @author Patrik Karlsson
 */
public class ConfigPanel extends javax.swing.JPanel {

    private final ModuleSourcePanel mModuleSourcePanel = new ModuleSourcePanel();
    private final ModuleFoldersPanel mModuleFoldersPanel = new ModuleFoldersPanel();
    private final ModulePlacemarkPanel mModulePlacemarksPanel = new ModulePlacemarkPanel();
    private final ModuleDescriptionPanel mModuleDescriptionPanel = new ModuleDescriptionPanel();
    private final ModulePhotoPanel mModulePhotoPanel = new ModulePhotoPanel();
    private final ModulePathPanel mModuleLinePanel = new ModulePathPanel();
    private final ModuleInfoPanel mModuleInfoPanel = new ModuleInfoPanel();
    private Profile mProfile;

    /**
     * Creates new form ConfigPanel
     */
    public ConfigPanel() {
        initComponents();
        init();
    }

    public void loadProfile(Profile profile) {
        mProfile = profile;

        for (Component component : tabbedPane.getComponents()) {
            if (component instanceof ModulePanel) {
                ModulePanel modulePanel = (ModulePanel) component;
                modulePanel.load(profile);
            }
        }
    }

    public LogPanel getLogErrPanel() {
        return mModuleSourcePanel.getLogErrPanel();
    }

    public LogPanel getLogOutPanel() {
        return mModuleSourcePanel.getLogOutPanel();
    }

    public JProgressBar getProgressBar() {
        return mModuleSourcePanel.getProgressBar();
    }

    public int getSelectedIndex() {
        return tabbedPane.getSelectedIndex();
    }

    public boolean hasValidSettings() {
        boolean validSettings = true;

        for (Component component : tabbedPane.getComponents()) {
            if (component instanceof ModulePanel) {
                ModulePanel modulePanel = (ModulePanel) component;
                if (!modulePanel.hasValidSettings()) {
                    validSettings = false;
                    break;
                }
            }
        }

        return validSettings;
    }

    public void refreshIcons() {
        for (Component component : tabbedPane.getComponents()) {
            if (component instanceof ModulePanel) {
                ModulePanel modulePanel = (ModulePanel) component;
                JLabel label = (JLabel) tabbedPane.getTabComponentAt(tabbedPane.indexOfComponent(modulePanel));
                label.setIcon(SystemUtils.IS_OS_MAC ? new ImageIcon() : modulePanel.getIcon());
            }
        }
    }

    public void reset() {
        mModuleSourcePanel.reset();
        selectTab(0);
    }

    public void selectTab(int index) {
        tabbedPane.setSelectedIndex(index);
    }

    @Override
    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);

        for (Component component : mModuleSourcePanel.getComponents()) {
            if (component != mModuleSourcePanel.getTabbedPane()) {
                component.setEnabled(enabled);
                if (component instanceof JPanel) {
                    SwingHelper.enableComponents((JPanel) component, enabled);
                }
            }
        }

        for (int i = 1; i < tabbedPane.getTabCount(); i++) {
            tabbedPane.setEnabledAt(i, enabled);
        }

        int photoIndex = tabbedPane.indexOfComponent(mModulePhotoPanel);
        if (mProfile != null) {
            tabbedPane.setEnabledAt(photoIndex, enabled && mProfile.getDescription().hasPhoto());
        }

        getProgressBar().setEnabled(true);
    }

    private void addModulePanel(ModulePanel modulePanel) {
        tabbedPane.add(modulePanel);
        JLabel label = new JLabel(modulePanel.getTitle(), JLabel.CENTER);
        label.setIconTextGap(4);
        label.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        tabbedPane.setTabComponentAt(tabbedPane.getTabCount() - 1, label);
    }

    private boolean disableTab(Component tab) {
        try {
            tabbedPane.setEnabledAt(tabbedPane.indexOfComponent(tab), false);
            return true;
        } catch (IndexOutOfBoundsException e) {
            //Tab not found
            return false;
        }
    }

    private void init() {
        addModulePanel(mModuleSourcePanel);
        addModulePanel(mModuleFoldersPanel);
        addModulePanel(mModuleLinePanel);
        addModulePanel(mModulePlacemarksPanel);
        addModulePanel(mModuleDescriptionPanel);
        addModulePanel(mModulePhotoPanel);
        addModulePanel(mModuleInfoPanel);

        for (Component component : tabbedPane.getComponents()) {
            if (component instanceof ModulePanel) {
                ModulePanel modulePanel = (ModulePanel) component;
                modulePanel.setBorder(new EmptyBorder(8, 8, 8, 8));
            }
        }

        mModuleDescriptionPanel.setPhotoDescriptionMonitor((boolean hasPhoto) -> {
            tabbedPane.setEnabledAt(tabbedPane.indexOfComponent(mModulePhotoPanel), hasPhoto);
        });

        setEnabled(true);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabbedPane = new javax.swing.JTabbedPane();

        tabbedPane.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPane)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables

}
