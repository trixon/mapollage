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
package se.trixon.mapollage.ui;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
import javax.swing.JProgressBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import se.trixon.almond.util.AlmondAction;
import se.trixon.almond.util.AlmondOptions;
import se.trixon.almond.util.AlmondUI;
import se.trixon.almond.util.BundleHelper;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.PomInfo;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.almond.util.swing.LogPanel;
import se.trixon.almond.util.swing.SwingHelper;
import se.trixon.almond.util.swing.dialogs.Message;
import se.trixon.almond.util.swing.dialogs.SimpleDialog;
import se.trixon.mapollage.Mapollage;
import se.trixon.mapollage.Operation;
import se.trixon.mapollage.OperationListener;
import se.trixon.mapollage.Options;
import se.trixon.mapollage.ProfileManager;
import se.trixon.mapollage.profile.Profile;

/**
 *
 * @author Patrik Karlsson
 */
public class MainFrame extends javax.swing.JFrame implements AlmondOptions.AlmondOptionsWatcher {

    private final ResourceBundle mBundle = BundleHelper.getBundle(Mapollage.class, "Bundle");
    private final ResourceBundle mBundleUI = BundleHelper.getBundle(MainFrame.class, "Bundle");
    private ActionManager mActionManager;
    private final AlmondUI mAlmondUI = AlmondUI.getInstance();
    private final LinkedList<AlmondAction> mBaseActions = new LinkedList<>();
    private final LinkedList<AlmondAction> mAllActions = new LinkedList<>();
    private final AlmondOptions mAlmondOptions = AlmondOptions.getInstance();
    private Thread mOperationThread;
    private final ProfileManager mProfileManager = ProfileManager.getInstance();
    private final LinkedList<Profile> mProfiles = mProfileManager.getProfiles();
    private DefaultComboBoxModel mModel;
    private File mDestination;
    private OperationListener mOperationListener;
    private LogPanel mLogErrPanel;
    private LogPanel mLogOutPanel;
    private JProgressBar mProgressBar;
    private final Options mOptions = Options.getInstance();
    private final StatusPanel mStatusPanel = new StatusPanel();

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

                configPanel.refreshIcons();

                break;

            case LOOK_AND_FEEL:
                for (Window window : Window.getWindows()) {
                    SwingUtilities.updateComponentTreeUI(window);
                }
                SwingUtilities.updateComponentTreeUI(mPopupMenu);
                configPanel.refreshIcons();
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

        configPanel.addStatusPanel(mStatusPanel);
        mProgressBar = mStatusPanel.getProgressBar();

        mModel = (DefaultComboBoxModel) profileComboBox.getModel();
        mActionManager = new ActionManager();
        mActionManager.initActions();
        setRunningState(false);

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
        populateProfiles(null, 0);
        initListeners();

        mLogErrPanel = mStatusPanel.getLogErrPanel();
        mLogOutPanel = mStatusPanel.getLogOutPanel();

        mLogOutPanel.println(mBundleUI.getString("welcome_1"));
        mLogOutPanel.println(Mapollage.getHelp());
        mLogOutPanel.println(mBundleUI.getString("welcome_3"));
        mLogOutPanel.scrollToTop();
    }

    private void initListeners() {
        mOperationListener = new OperationListener() {
            private boolean success;

            @Override
            public void onOperationError(String message) {
                mLogErrPanel.println(message);
            }

            @Override
            public void onOperationFailed(String message) {
                onOperationFinished(message);
                success = false;
            }

            @Override
            public void onOperationFinished(String message) {
                setRunningState(false);
                mLogOutPanel.println(message);

                if (mOptions.isAutoOpen() && success) {
                    try {
                        Desktop.getDesktop().open(mDestination);
                    } catch (IOException ex) {
                        Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

            @Override
            public void onOperationInterrupted() {
                setRunningState(false);
                success = false;
            }

            @Override
            public void onOperationLog(String message) {
                mLogOutPanel.println(message);
            }

            @Override
            public void onOperationProcessingStarted() {
                SwingUtilities.invokeLater(() -> {
                    mProgressBar.setIndeterminate(true);
                });
            }

            @Override
            public void onOperationProgress(String message) {
                SwingUtilities.invokeLater(() -> {
                    mProgressBar.setString(message);
                    mProgressBar.setValue(mProgressBar.getValue() + 1);
                });
            }

            @Override
            public void onOperationProgressInit(int fileCount) {
                SwingUtilities.invokeLater(() -> {
                    mProgressBar.setIndeterminate(false);
                    mProgressBar.setMinimum(0);
                    mProgressBar.setValue(0);
                    mProgressBar.setMaximum(fileCount);
                });
                success = true;
            }

            @Override
            public void onOperationStarted() {
                setRunningState(true);
            }
        };
    }

    private void setRunningState(boolean state) {
        SwingUtilities.invokeLater(() -> {
            mActionManager.getAction(ActionManager.START).setEnabled(!state);
            mActionManager.getAction(ActionManager.CANCEL).setEnabled(state);
            mActionManager.getAction(ActionManager.ADD).setEnabled(!state);
            mActionManager.getAction(ActionManager.REMOVE).setEnabled(!state);
            mActionManager.getAction(ActionManager.CLONE).setEnabled(!state);
            mActionManager.getAction(ActionManager.OPTIONS).setEnabled(!state);
            mActionManager.getAction(ActionManager.REMOVE_ALL).setEnabled(!state);
            mActionManager.getAction(ActionManager.RENAME).setEnabled(!state);

            startButton.setVisible(!state);
            cancelButton.setVisible(state);

            configPanel.setEnabled(!state);
            mProgressBar.setString(" ");
        });
    }

    private void populateProfiles(Profile profile, int tab) {
        mModel.removeAllElements();
        Collections.sort(mProfiles);

        mProfiles.stream().forEach((item) -> {
            mModel.addElement(item);
        });

        if (profile != null) {
            mModel.setSelectedItem(profile);
        }

        configPanel.setEnabled(!mProfiles.isEmpty());
        configPanel.selectTab(tab);
    }

    private void loadProfiles() {
        configPanel.setEnabled(false);

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
                populateProfiles(p, 0);
            } else {
                Message.error(this, Dict.Dialog.ERROR.toString(), String.format(Dict.Dialog.ERROR_PROFILE_EXIST.toString(), s));
                profileAdd(s);
            }
        }
    }

    private void profileClone() throws CloneNotSupportedException {
        Profile original = getSelectedProfile();
        Profile p = new Profile(original.getJson());
        mProfiles.add(p);
        populateProfiles(p, configPanel.getSelectedIndex());
        String s = requestProfileName(Dict.Dialog.TITLE_PROFILE_CLONE.toString(), p.getName());

        if (s != null) {
            p.setName(s);
            populateProfiles(getSelectedProfile(), configPanel.getSelectedIndex());
        } else {
            mProfiles.remove(p);
            populateProfiles(original, configPanel.getSelectedIndex());
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
                populateProfiles(null, 0);
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
                populateProfiles(null, 0);
            }
        }
    }

    private void profileRename(String defaultName) {
        String s = requestProfileName(Dict.Dialog.TITLE_PROFILE_RENAME.toString(), defaultName);
        if (s != null) {
            Profile existingProfile = mProfileManager.getProfile(s);
            if (existingProfile == null) {
                getSelectedProfile().setName(s);
                populateProfiles(getSelectedProfile(), configPanel.getSelectedIndex());
            } else if (existingProfile != getSelectedProfile()) {
                Message.error(this, Dict.Dialog.ERROR.toString(), String.format(Dict.Dialog.ERROR_PROFILE_EXIST.toString(), s));
                profileRename(s);
            }
        }
    }

    private void profileRun() {
        saveProfiles();
        Profile profile = new Profile(getSelectedProfile().getJson());

        if (profile.isValid()) {
            requestKmlFileObject();
        } else {
            mLogErrPanel.clear();
            mLogOutPanel.println(profile.getValidationError());
        }
    }

    private void requestKmlFileObject() {
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Keyhole Markup Language (*.kml)", "kml");

        SimpleDialog.clearFilters();
        SimpleDialog.addFilter(filter);
        SimpleDialog.setFilter(filter);
        SimpleDialog.setParent(this);

        if (mDestination == null) {
            SimpleDialog.setPath(FileUtils.getUserDirectory());
        } else {
            SimpleDialog.setPath(mDestination.getParentFile());
            SimpleDialog.setSelectedFile(new File(""));
        }

        if (SimpleDialog.saveFile(new String[]{"kml"})) {
            mDestination = SimpleDialog.getPath();
            Profile profile = getSelectedProfile();
            profile.setDestinationFile(mDestination);
            profile.isValid();

            mStatusPanel.reset();
            configPanel.reset();
            if (profile.hasValidRelativeSourceDest()) {
                Operation operation = new Operation(mOperationListener, profile);
                mOperationThread = new Thread(operation);
                mOperationThread.start();
            } else {
                mLogOutPanel.println(mBundle.getString("invalid_relative_source_dest"));
                mLogOutPanel.println(Dict.ABORTING.toString());
            }
        }
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
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        mPopupMenu = new javax.swing.JPopupMenu();
        renameMenuItem = new javax.swing.JMenuItem();
        cloneMenuItem = new javax.swing.JMenuItem();
        removeAllProfilesMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        optionsMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        helpMenuItem = new javax.swing.JMenuItem();
        aboutDateFormatMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        quitMenuItem = new javax.swing.JMenuItem();
        topPanel = new javax.swing.JPanel();
        profileComboBox = new javax.swing.JComboBox<>();
        toolBar = new javax.swing.JToolBar();
        startButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        menuButton = new javax.swing.JButton();
        configPanel = new se.trixon.mapollage.ui.config.ConfigPanel();

        mPopupMenu.add(renameMenuItem);
        mPopupMenu.add(cloneMenuItem);
        mPopupMenu.add(removeAllProfilesMenuItem);
        mPopupMenu.add(jSeparator1);
        mPopupMenu.add(optionsMenuItem);
        mPopupMenu.add(jSeparator2);
        mPopupMenu.add(helpMenuItem);
        mPopupMenu.add(aboutDateFormatMenuItem);
        mPopupMenu.add(aboutMenuItem);
        mPopupMenu.add(jSeparator6);
        mPopupMenu.add(quitMenuItem);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("se/trixon/mapollage/ui/Bundle"); // NOI18N
        setTitle(bundle.getString("MainFrame.title")); // NOI18N
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        topPanel.setLayout(new java.awt.GridBagLayout());

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, configPanel, org.jdesktop.beansbinding.ELProperty.create("${enabled}"), profileComboBox, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

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
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        topPanel.add(profileComboBox, gridBagConstraints);

        toolBar.setFloatable(false);
        toolBar.setRollover(true);

        startButton.setFocusable(false);
        startButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        startButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(startButton);

        cancelButton.setFocusable(false);
        cancelButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cancelButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(cancelButton);

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
        getContentPane().add(configPanel, java.awt.BorderLayout.CENTER);

        bindingGroup.bind();

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
            SwingUtilities.invokeLater(() -> {
                configPanel.loadProfile(p);
            });
        }
    }//GEN-LAST:event_profileComboBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutDateFormatMenuItem;
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JButton addButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JMenuItem cloneMenuItem;
    private se.trixon.mapollage.ui.config.ConfigPanel configPanel;
    private javax.swing.JMenuItem helpMenuItem;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JPopupMenu mPopupMenu;
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
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables

    class ActionManager {

        static final String ABOUT = "about";
        static final String ABOUT_DATE_FORMAT = "about_date_format";
        static final String HELP = "help";
        static final String ADD = "add";
        static final String CANCEL = "cancel";
        static final String CLONE = "clone";
        static final String MENU = "menu";
        static final String OPTIONS = "options";
        static final String QUIT = "quit";
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

            initAction(action, MENU, keyStroke, MaterialIcon._Navigation.MENU, true);
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

            initAction(action, OPTIONS, keyStroke, MaterialIcon._Action.SETTINGS, true);
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

            initAction(action, START, keyStroke, MaterialIcon._Av.PLAY_ARROW, false);
            startButton.setAction(action);

            //cancel
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
            action = new AlmondAction(Dict.CANCEL.toString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    mOperationThread.interrupt();
                }
            };

            initAction(action, CANCEL, keyStroke, MaterialIcon._Content.CLEAR, false);
            cancelButton.setAction(action);

            //add
            keyStroke = null;

            action = new AlmondAction(Dict.ADD.toString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    profileAdd(null);
                }
            };

            initAction(action, ADD, keyStroke, MaterialIcon._Content.ADD, true);
            addButton.setAction(action);

            //clone
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_C, commandMask + InputEvent.SHIFT_DOWN_MASK);
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

            initAction(action, CLONE, keyStroke, MaterialIcon._Content.CONTENT_COPY, false);
            cloneMenuItem.setAction(action);

            //rename
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_R, commandMask);
            action = new AlmondAction(Dict.RENAME.toString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!mProfiles.isEmpty()) {
                        profileRename(getSelectedProfile().getName());
                    }
                }
            };

            initAction(action, RENAME, keyStroke, MaterialIcon._Editor.MODE_EDIT, false);
            renameMenuItem.setAction(action);

            //remove
            keyStroke = null;
            action = new AlmondAction(Dict.REMOVE.toString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    profileRemove();
                }
            };

            initAction(action, REMOVE, keyStroke, MaterialIcon._Content.REMOVE, false);
            removeButton.setAction(action);

            //remove all
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, commandMask + InputEvent.SHIFT_DOWN_MASK);
            action = new AlmondAction(Dict.REMOVE_ALL.toString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    profileRemoveAll();
                }
            };

            initAction(action, REMOVE_ALL, keyStroke, MaterialIcon._Content.CLEAR, false);
            removeAllProfilesMenuItem.setAction(action);

            //about
            keyStroke = null;
            action = new AlmondAction(Dict.ABOUT.toString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    PomInfo pomInfo = new PomInfo(Mapollage.class, "se.trixon", "mapollage");
                    String versionInfo = String.format(mBundle.getString("version_info"), pomInfo.getVersion());
                    Message.information(MainFrame.this, Dict.ABOUT.toString(), versionInfo);
                }
            };

            initAction(action, ABOUT, keyStroke, null, true);
            aboutMenuItem.setAction(action);

            //help
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0);
            action = new AlmondAction(Dict.DOCUMENTATION.toString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        Desktop.getDesktop().browse(new URI("https://trixon.se/projects/mapollage/documentation/"));
                    } catch (URISyntaxException | IOException ex) {
                        Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };

            initAction(action, HELP, keyStroke, null, true);
            helpMenuItem.setAction(action);

            //about date format
            keyStroke = null;
            String title = String.format(Dict.ABOUT_S.toString(), Dict.DATE_PATTERN.toString().toLowerCase());
            action = new AlmondAction(title) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        Desktop.getDesktop().browse(new URI("https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html"));
                    } catch (URISyntaxException | IOException ex) {
                        Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };

            initAction(action, ABOUT_DATE_FORMAT, keyStroke, null, true);
            aboutDateFormatMenuItem.setAction(action);

            //quit
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Q, commandMask);
            action = new AlmondAction(Dict.QUIT.toString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    quit();
                }
            };

            initAction(action, QUIT, keyStroke, MaterialIcon._Content.CLEAR, true);
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
