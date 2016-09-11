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
package se.trixon.photokml.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.SystemUtils;
import se.trixon.almond.util.AlmondAction;
import se.trixon.almond.util.AlmondOptions;
import se.trixon.almond.util.AlmondUI;
import se.trixon.almond.util.BundleHelper;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.icon.Pict;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.almond.util.swing.SwingHelper;
import se.trixon.almond.util.swing.dialogs.Message;
import se.trixon.photokml.PhotoKml;
import se.trixon.photokml.ProfileManager;
import se.trixon.photokml.profile.Profile;

/**
 *
 * @author Patrik Karlsson
 */
public class MainFrame extends javax.swing.JFrame implements AlmondOptions.AlmondOptionsWatcher {

    private final ResourceBundle mBundle = BundleHelper.getBundle(PhotoKml.class, "Bundle");
    private final ResourceBundle mBundleUI = BundleHelper.getBundle(MainFrame.class, "Bundle");
    private ActionManager mActionManager;
    private final AlmondUI mAlmondUI = AlmondUI.getInstance();
    private final LinkedList<AlmondAction> mBaseActions = new LinkedList<>();
    private final LinkedList<AlmondAction> mAllActions = new LinkedList<>();
    private final AlmondOptions mAlmondOptions = AlmondOptions.getInstance();
    private final ProfileManager mProfileManager = ProfileManager.getInstance();
    private final LinkedList<Profile> mProfiles = mProfileManager.getProfiles();
    private DefaultComboBoxModel mModel;

    /**
     * Creates new form MainFrame
     */
    public MainFrame() {
        initComponents();
        init();
    }

    @Override
    public void onAlmondOptions(AlmondOptions.AlmondOptionsEvent almondOptionsEvent) {
        switch (almondOptionsEvent) {
            case ICON_THEME:
                mAllActions.stream().forEach((almondAction) -> {
                    almondAction.updateIcon();
                });
                break;

            case LOOK_AND_FEEL:
                SwingUtilities.updateComponentTreeUI(this);
                SwingUtilities.updateComponentTreeUI(mPopupMenu);
                break;

            case MENU_ICONS:
                ActionMap actionMap = getRootPane().getActionMap();
                for (Object key : actionMap.allKeys()) {
                    Action action = actionMap.get(key);
                    Icon icon = null;
                    if (mAlmondOptions.isDisplayMenuIcons()) {
                        icon = (Icon) action.getValue(AlmondAction.ALMOND_SMALL_ICON_KEY);
                    }
                    action.putValue(Action.SMALL_ICON, icon);
                }
                break;

            default:
                throw new AssertionError();
        }
    }

    private Profile getSelectedProfile() {
        if (mModel.getSize() == 0) {
            return new Profile();
        } else {
            return (Profile) mModel.getSelectedItem();
        }
    }

    private void init() {
        String fileName = String.format("/%s/icon-1024px.png", getClass().getPackage().getName().replace(".", "/"));
        ImageIcon imageIcon = new ImageIcon(getClass().getResource(fileName));
        setIconImage(imageIcon.getImage());

        mModel = (DefaultComboBoxModel) profileComboBox.getModel();
        mActionManager = new ActionManager();
        mActionManager.initActions();

        mAlmondUI.addOptionsWatcher(this);
        mAlmondUI.addWindowWatcher(this);
        mAlmondUI.initoptions();

        InputMap inputMap = mPopupMenu.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = mPopupMenu.getActionMap();
        Action action = new AbstractAction("HideMenu") {

            @Override
            public void actionPerformed(ActionEvent e) {
                mPopupMenu.setVisible(false);
            }
        };

        String key = "HideMenu";
        actionMap.put(key, action);
        KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        inputMap.put(keyStroke, key);

        loadProfiles();
        populateProfiles(null);
        initListeners();
    }

    private void initListeners() {
    }

    private void populateProfiles(Profile profile) {
        mModel.removeAllElements();
        Collections.sort(mProfiles);

        mProfiles.stream().forEach((item) -> {
            mModel.addElement(item);
        });

        if (profile != null) {
            mModel.setSelectedItem(profile);
        }

        boolean hasProfiles = !mProfiles.isEmpty();
        SwingHelper.enableComponents(mainPanel, hasProfiles);
    }

    private void loadProfiles() {
        SwingHelper.enableComponents(mainPanel, false);

        try {
            mProfileManager.load();
        } catch (IOException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void profileAdd(String defaultName) {
        String s = requestProfileName(Dict.Dialog.TITLE_PROFILE_ADD.toString(), defaultName);
        if (s != null) {
            Profile existingProfile = mProfileManager.getProfile(s);
            if (existingProfile == null) {
                Profile p = new Profile();
                p.setName(s);

                mProfiles.add(p);
                populateProfiles(p);
            } else {
                Message.error(this, Dict.ERROR.toString(), String.format(Dict.Dialog.ERROR_PROFILE_EXIST.toString(), s));
                profileAdd(s);
            }
        }
    }

    private void profileClone() throws CloneNotSupportedException {
        Profile original = getSelectedProfile();
        Profile p = original.clone();
        mProfiles.add(p);
        populateProfiles(p);
        String s = requestProfileName(Dict.Dialog.TITLE_PROFILE_CLONE.toString(), p.getName());
        if (s != null) {
            p.setName(s);
            populateProfiles(getSelectedProfile());
        } else {
            mProfiles.remove(p);
            populateProfiles(original);
        }
    }

    private void profileRemove() {
        if (!mProfiles.isEmpty()) {
            String message = String.format(Dict.Dialog.MESSAGE_PROFILE_REMOVE.toString(), getSelectedProfile().getName());
            int retval = JOptionPane.showConfirmDialog(this,
                    message,
                    Dict.Dialog.TITLE_PROFILE_REMOVE.toString(),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (retval == JOptionPane.OK_OPTION) {
                mProfiles.remove(getSelectedProfile());
                populateProfiles(null);
            }
        }
    }

    private void profileRemoveAll() {
        if (!mProfiles.isEmpty()) {
            String message = String.format(Dict.Dialog.MESSAGE_PROFILE_REMOVE_ALL.toString(), getSelectedProfile().getName());
            int retval = JOptionPane.showConfirmDialog(this,
                    message,
                    Dict.Dialog.TITLE_PROFILE_REMOVE_ALL.toString(),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (retval == JOptionPane.OK_OPTION) {
                mProfiles.clear();
                populateProfiles(null);
            }
        }
    }

    private void profileRename(String defaultName) {
        String s = requestProfileName(Dict.Dialog.TITLE_PROFILE_RENAME.toString(), defaultName);
        if (s != null) {
            Profile existingProfile = mProfileManager.getProfile(s);
            if (existingProfile == null) {
                getSelectedProfile().setName(s);
                populateProfiles(getSelectedProfile());
            } else if (existingProfile != getSelectedProfile()) {
                Message.error(this, Dict.ERROR.toString(), String.format(Dict.Dialog.ERROR_PROFILE_EXIST.toString(), s));
                profileRename(s);
            }
        }
    }

    private void profileRun() {
        saveProfiles();
        Profile profile = getSelectedProfile().clone();
        profile.isValid();
        Object[] options = {Dict.RUN.toString(), Dict.DRY_RUN.toString(), Dict.CANCEL.toString()};
        String message = String.format(Dict.Dialog.TITLE_PROFILE_RUN.toString(), profile.getName());

        logPanel.clear();
        logPanel.println(profile.toDebugString());
        logPanel.println("->");
        logPanel.println(profile.getValidationError());
        logPanel.println("<-");

    }

    private String requestProfileName(String title, String value) {
        return (String) JOptionPane.showInputDialog(
                this,
                null,
                title,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                value);
    }

    private void showOptions() {
        OptionsPanel optionsPanel = new OptionsPanel();
        SwingHelper.makeWindowResizable(optionsPanel);

        int retval = JOptionPane.showOptionDialog(this,
                optionsPanel,
                Dict.OPTIONS.toString(),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                null);

        if (retval == JOptionPane.OK_OPTION) {
            optionsPanel.save();
        }
    }

    private void saveProfiles() {
        try {
            mProfileManager.save();
        } catch (IOException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void quit() {
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
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

        mPopupMenu = new javax.swing.JPopupMenu();
        renameMenuItem = new javax.swing.JMenuItem();
        cloneMenuItem = new javax.swing.JMenuItem();
        removeAllProfilesMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        optionsMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        aboutMenuItem = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        quitMenuItem = new javax.swing.JMenuItem();
        topPanel = new javax.swing.JPanel();
        profileComboBox = new javax.swing.JComboBox<>();
        toolBar = new javax.swing.JToolBar();
        startButton = new javax.swing.JButton();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        menuButton = new javax.swing.JButton();
        mainPanel = new javax.swing.JPanel();
        configPanel1 = new se.trixon.photokml.ui.config.ConfigPanel();
        logPanel = new se.trixon.almond.util.swing.LogPanel();

        mPopupMenu.add(renameMenuItem);
        mPopupMenu.add(cloneMenuItem);
        mPopupMenu.add(removeAllProfilesMenuItem);
        mPopupMenu.add(jSeparator1);
        mPopupMenu.add(optionsMenuItem);
        mPopupMenu.add(jSeparator2);
        mPopupMenu.add(aboutMenuItem);
        mPopupMenu.add(jSeparator6);
        mPopupMenu.add(quitMenuItem);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("se/trixon/photokml/ui/Bundle"); // NOI18N
        setTitle(bundle.getString("MainFrame.title")); // NOI18N
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        topPanel.setLayout(new java.awt.GridBagLayout());

        profileComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                profileComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        topPanel.add(profileComboBox, gridBagConstraints);

        toolBar.setFloatable(false);
        toolBar.setRollover(true);

        startButton.setFocusable(false);
        startButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        startButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(startButton);

        addButton.setFocusable(false);
        addButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        addButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(addButton);

        removeButton.setFocusable(false);
        removeButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        removeButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(removeButton);

        menuButton.setFocusable(false);
        menuButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        menuButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        menuButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                menuButtonMousePressed(evt);
            }
        });
        toolBar.add(menuButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        topPanel.add(toolBar, gridBagConstraints);

        getContentPane().add(topPanel, java.awt.BorderLayout.PAGE_START);

        mainPanel.setLayout(new java.awt.BorderLayout());
        mainPanel.add(configPanel1, java.awt.BorderLayout.PAGE_START);
        mainPanel.add(logPanel, java.awt.BorderLayout.CENTER);

        getContentPane().add(mainPanel, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void menuButtonMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_menuButtonMousePressed
        if (evt == null || evt.getButton() == MouseEvent.BUTTON1) {
            if (mPopupMenu.isVisible()) {
                mPopupMenu.setVisible(false);
            } else {
                mPopupMenu.show(menuButton, menuButton.getWidth() - mPopupMenu.getWidth(), mPopupMenu.getHeight());

                int x = menuButton.getLocationOnScreen().x + menuButton.getWidth() - mPopupMenu.getWidth();
                int y = menuButton.getLocationOnScreen().y + menuButton.getHeight();

                mPopupMenu.setLocation(x, y);
            }
        }
    }//GEN-LAST:event_menuButtonMousePressed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        saveProfiles();
    }//GEN-LAST:event_formWindowClosing

    private void profileComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_profileComboBoxActionPerformed
        Profile p = getSelectedProfile();
        if (p != null) {
            configPanel1.loadProfile(p);
        }

    }//GEN-LAST:event_profileComboBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JButton addButton;
    private javax.swing.JMenuItem cloneMenuItem;
    private se.trixon.photokml.ui.config.ConfigPanel configPanel1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private se.trixon.almond.util.swing.LogPanel logPanel;
    private javax.swing.JPopupMenu mPopupMenu;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JButton menuButton;
    private javax.swing.JMenuItem optionsMenuItem;
    private javax.swing.JComboBox<Profile> profileComboBox;
    private javax.swing.JMenuItem quitMenuItem;
    private javax.swing.JMenuItem removeAllProfilesMenuItem;
    private javax.swing.JButton removeButton;
    private javax.swing.JMenuItem renameMenuItem;
    private javax.swing.JButton startButton;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JPanel topPanel;
    // End of variables declaration//GEN-END:variables

    class ActionManager {

        static final String ABOUT = "about";
        static final String ADD = "add";
        static final String CLONE = "clone";
        static final String MENU = "menu";
        static final String OPTIONS = "options";
        static final String QUIT = "shutdownServerAndWindow";
        static final String REMOVE = "remove";
        static final String REMOVE_ALL = "remove_all";
        static final String RENAME = "rename";
        static final String START = "start";

        private ActionManager() {
            initActions();
        }

        Action getAction(String key) {
            return getRootPane().getActionMap().get(key);
        }

        private void initAction(AlmondAction action, String key, KeyStroke keyStroke, Enum iconEnum, boolean baseAction) {
            InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            ActionMap actionMap = getRootPane().getActionMap();

            action.putValue(Action.ACCELERATOR_KEY, keyStroke);
            action.putValue(Action.SHORT_DESCRIPTION, action.getValue(Action.NAME));
            action.putValue("hideActionText", true);
            action.setIconEnum(iconEnum);
            action.updateIcon();

            inputMap.put(keyStroke, key);
            actionMap.put(key, action);

            if (baseAction) {
                mBaseActions.add(action);
            }

            mAllActions.add(action);
        }

        private void initActions() {
            AlmondAction action;
            KeyStroke keyStroke;
            int commandMask = SystemHelper.getCommandMask();

            //menu
            int menuKey = KeyEvent.VK_M;
            keyStroke = KeyStroke.getKeyStroke(menuKey, commandMask);
            action = new AlmondAction(Dict.MENU.toString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (e.getSource() != menuButton) {
                        menuButtonMousePressed(null);
                    }
                }
            };

            initAction(action, MENU, keyStroke, MaterialIcon.Navigation.MENU, true);
            menuButton.setAction(action);

            //options
            int optionsKey = SystemUtils.IS_OS_MAC ? KeyEvent.VK_COMMA : KeyEvent.VK_P;
            keyStroke = KeyStroke.getKeyStroke(optionsKey, commandMask);
            action = new AlmondAction(Dict.OPTIONS.toString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    showOptions();
                }
            };

            initAction(action, OPTIONS, keyStroke, MaterialIcon.Action.SETTINGS, true);
            optionsMenuItem.setAction(action);

            //start
            keyStroke = null;
            action = new AlmondAction(Dict.START.toString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!mProfiles.isEmpty()) {
                        profileRun();
                    }
                }
            };

            initAction(action, START, keyStroke, MaterialIcon.Av.PLAY_ARROW, false);
            startButton.setAction(action);

            //add
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0);

            action = new AlmondAction(Dict.ADD.toString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    profileAdd(null);
                }
            };

            initAction(action, ADD, keyStroke, MaterialIcon.Content.ADD, true);
            addButton.setAction(action);

            //clone
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_C, commandMask);
            action = new AlmondAction(Dict.CLONE.toString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        profileClone();
                    } catch (CloneNotSupportedException ex) {
                        Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };

            initAction(action, CLONE, keyStroke, MaterialIcon.Content.CONTENT_COPY, false);
            cloneMenuItem.setAction(action);

            //rename
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_R, commandMask);
            action = new AlmondAction(Dict.RENAME.toString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    profileRename(getSelectedProfile().getName());
                }
            };

            initAction(action, RENAME, keyStroke, MaterialIcon.Editor.MODE_EDIT, false);
            renameMenuItem.setAction(action);

            //remove
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
            action = new AlmondAction(Dict.REMOVE.toString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    profileRemove();
                }
            };

            initAction(action, REMOVE, keyStroke, MaterialIcon.Content.REMOVE, false);
            removeButton.setAction(action);

            //remove all
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.SHIFT_DOWN_MASK);
            action = new AlmondAction(Dict.REMOVE_ALL.toString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    profileRemoveAll();
                }
            };

            initAction(action, REMOVE_ALL, keyStroke, MaterialIcon.Content.CLEAR, false);
            removeAllProfilesMenuItem.setAction(action);

            //about
            keyStroke = null;
            action = new AlmondAction(Dict.ABOUT.toString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    String versionInfo = String.format(mBundle.getString("version_info"), SystemHelper.getJarVersion(PhotoKml.class));
                    Message.information(MainFrame.this, Dict.ABOUT.toString(), versionInfo);
                }
            };

            initAction(action, ABOUT, keyStroke, null, true);
            aboutMenuItem.setAction(action);

            //quit
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Q, commandMask);
            action = new AlmondAction(Dict.QUIT.toString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    quit();
                }
            };

            initAction(action, QUIT, keyStroke, Pict.Actions.APPLICATION_EXIT, true);
            quitMenuItem.setAction(action);

            for (Component component : mPopupMenu.getComponents()) {
                if (component instanceof AbstractButton) {
                    ((AbstractButton) component).setToolTipText(null);
                }
            }

            for (Component component : toolBar.getComponents()) {
                if (component instanceof AbstractButton) {
                    ((AbstractButton) component).setText(null);
                }
            }
        }
    }
}