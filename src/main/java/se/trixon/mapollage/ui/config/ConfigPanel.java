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
import javax.swing.border.EmptyBorder;
import se.trixon.almond.util.Dict;
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

    public LogPanel getLogPanel() {
        return mModuleSourcePanel.getLogPanel();
    }

    public int getSelectedIndex() {
        return tabbedPane.getSelectedIndex();
    }

    public StringBuilder getHeaderBuilder() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s: %s\n", Dict.PROFILE.toString(), mProfile.getName()));
        sb.append(mModuleSourcePanel.getHeaderBuilder());
        sb.append(mModuleFoldersPanel.getHeaderBuilder());
        sb.append(mModulePlacemarksPanel.getHeaderBuilder());
        sb.append(mModuleDescriptionPanel.getHeaderBuilder());
        sb.append(mModulePhotoPanel.getHeaderBuilder());

        return sb;
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
                tabbedPane.setIconAt(tabbedPane.indexOfComponent(modulePanel), modulePanel.getIcon());
            }
        }
    }

    public void selectTab(int index) {
        tabbedPane.setSelectedIndex(index);
    }

    @Override
    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);
        SwingHelper.enableComponents(mModuleSourcePanel, enabled);
        if (!enabled) {
            SwingHelper.enableComponents(getLogPanel(), true);
        }

        for (int i = 1; i < tabbedPane.getTabCount(); i++) {
            tabbedPane.setEnabledAt(i, enabled);
        }

        int photoIndex = tabbedPane.indexOfComponent(mModulePhotoPanel);
        if (mProfile != null) {
            tabbedPane.setEnabledAt(photoIndex, enabled && mProfile.getDescription().hasPhoto());
        }
    }

    private void addModulePanel(ModulePanel modulePanel) {
        tabbedPane.addTab(null, modulePanel.getIcon(), modulePanel, modulePanel.getTitle());
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
        addModulePanel(mModulePlacemarksPanel);
        addModulePanel(mModuleDescriptionPanel);
        addModulePanel(mModulePhotoPanel);

        for (Component component : tabbedPane.getComponents()) {
            if (component instanceof ModulePanel) {
                ModulePanel modulePanel = (ModulePanel) component;
                modulePanel.setBorder(new EmptyBorder(8, 8, 8, 8));
            }
        }

        mModuleDescriptionPanel.setPhotoDescriptionMonitor(new ModuleDescriptionPanel.PhotoDescriptionMonitor() {
            @Override
            public void onPhotoDescriptionChange(boolean hasPhoto) {
                tabbedPane.setEnabledAt(tabbedPane.indexOfComponent(mModulePhotoPanel), hasPhoto);
            }
        });

        setEnabled(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabbedPane = new javax.swing.JTabbedPane();

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