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

import com.apple.eawt.AppEvent;
import com.apple.eawt.Application;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import se.trixon.almond.util.AlmondOptions;
import se.trixon.almond.util.AlmondOptionsPanel;
import se.trixon.almond.util.AlmondUI;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.PomInfo;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.swing.LogPanel;
import se.trixon.almond.util.swing.SwingHelper;
import se.trixon.almond.util.swing.dialogs.MenuModePanel;
import se.trixon.almond.util.swing.dialogs.Message;
import se.trixon.almond.util.swing.dialogs.SimpleDialog;
import se.trixon.almond.util.swing.dialogs.about.AboutModel;
import se.trixon.almond.util.swing.dialogs.about.AboutPanel;
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
public class MainFrame extends javax.swing.JFrame {

    private static final boolean IS_MAC = SystemUtils.IS_OS_MAC;
    private final ResourceBundle mBundle = SystemHelper.getBundle(Mapollage.class, "Bundle");
    private final ResourceBundle mBundleUI = SystemHelper.getBundle(MainFrame.class, "Bundle");
    private ActionManager mActionManager;
    private final AlmondUI mAlmondUI = AlmondUI.getInstance();
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

        mAlmondUI.addWindowWatcher(this);
        mAlmondUI.initoptions();

        initActions();
        init();
        if (IS_MAC) {
            initMac();
        }

        initMenus();
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

        loadProfiles();
        populateProfiles(null, 0);
        initListeners();

        mLogErrPanel = mStatusPanel.getLogErrPanel();
        mLogOutPanel = mStatusPanel.getLogOutPanel();

        mLogOutPanel.println(mBundleUI.getString("welcome_1"));
        mLogOutPanel.println(Mapollage.getHelp());
        mLogOutPanel.println(mBundleUI.getString("welcome_3"));
        mLogOutPanel.scrollToTop();

        setRunningState(false);
        SwingUtilities.invokeLater(() -> {
            configPanel.setEnabled(!mProfiles.isEmpty());
        });
    }

    private void initActions() {
        mActionManager = ActionManager.getInstance().init(getRootPane().getActionMap(), getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW));

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

        //about
        PomInfo pomInfo = new PomInfo(Mapollage.class, "se.trixon", "mapollage");
        AboutModel aboutModel = new AboutModel(SystemHelper.getBundle(Mapollage.class, "about"), SystemHelper.getResourceAsImageIcon(MainFrame.class, "icon-1024px.png"));
        aboutModel.setAppVersion(pomInfo.getVersion());
        AboutPanel aboutPanel = new AboutPanel(aboutModel);
        action = AboutPanel.getAction(MainFrame.this, aboutPanel);
        getRootPane().getActionMap().put(ActionManager.ABOUT, action);

        //File
        quitMenuItem.setAction(mActionManager.getAction(ActionManager.QUIT));

        //Profile
        addMenuItem.setAction(mActionManager.getAction(ActionManager.ADD));
        cloneMenuItem.setAction(mActionManager.getAction(ActionManager.CLONE));
        renameMenuItem.setAction(mActionManager.getAction(ActionManager.RENAME));
        removeMenuItem.setAction(mActionManager.getAction(ActionManager.REMOVE));
        removeAllMenuItem.setAction(mActionManager.getAction(ActionManager.REMOVE_ALL));

        //Tools
        optionsMenuItem.setAction(mActionManager.getAction(ActionManager.OPTIONS));

        //Help
        helpMenuItem.setAction(mActionManager.getAction(ActionManager.HELP));
        aboutDateFormatMenuItem.setAction(mActionManager.getAction(ActionManager.ABOUT_DATE_FORMAT));
        aboutMenuItem.setAction(mActionManager.getAction(ActionManager.ABOUT));

        //Toolbar
        addButton.setAction(mActionManager.getAction(ActionManager.ADD));
        startButton.setAction(mActionManager.getAction(ActionManager.START));
        cancelButton.setAction(mActionManager.getAction(ActionManager.CANCEL));
        menuButton.setAction(mActionManager.getAction(ActionManager.MENU));

        SwingHelper.clearText(toolBar);

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
                onOperationFinished(message, 0);
                success = false;
            }

            @Override
            public void onOperationFinished(String message, int placemarkCount) {
                setRunningState(false);
                mLogOutPanel.println(message);

                if (mOptions.isAutoOpen() && success && placemarkCount > 0) {
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

        mActionManager.addAppListener(new ActionManager.AppListener() {
            @Override
            public void onCancel(ActionEvent actionEvent) {
                mOperationThread.interrupt();
            }

            @Override
            public void onMenu(ActionEvent actionEvent) {
                if (actionEvent.getSource() != menuButton) {
                    menuButtonMousePressed(null);
                }
            }

            @Override
            public void onOptions(ActionEvent actionEvent) {
                showOptions();
            }

            @Override
            public void onQuit(ActionEvent actionEvent) {
                quit();
            }

            @Override
            public void onStart(ActionEvent actionEvent) {
                if (!mProfiles.isEmpty()) {
                    profileRun();
                }
            }
        });

        mActionManager.addProfileListener(new ActionManager.ProfileListener() {
            @Override
            public void onAdd(ActionEvent actionEvent) {
                profileAdd(null);
            }

            @Override
            public void onClone(ActionEvent actionEvent) {
                try {
                    profileClone();
                } catch (CloneNotSupportedException ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            @Override
            public void onEdit(ActionEvent actionEvent) {
                profileRename(getSelectedProfile().getName());
            }

            @Override
            public void onRemove(ActionEvent actionEvent) {
                profileRemove();
            }

            @Override
            public void onRemoveAll(ActionEvent actionEvent) {
                profileRemoveAll();
            }
        });
    }

    private void initMac() {
        Application macApplication = Application.getApplication();
        macApplication.setAboutHandler((AppEvent.AboutEvent ae) -> {
            mActionManager.getAction(ActionManager.ABOUT).actionPerformed(null);
        });

        macApplication.setPreferencesHandler((AppEvent.PreferencesEvent pe) -> {
            mActionManager.getAction(ActionManager.OPTIONS).actionPerformed(null);
        });
    }

    private void initMenus() {
        if (mAlmondOptions.getMenuMode() == MenuModePanel.MenuMode.BUTTON) {
            mPopupMenu.add(removeMenuItem);
            mPopupMenu.add(renameMenuItem);
            mPopupMenu.add(cloneMenuItem);
            mPopupMenu.add(removeAllMenuItem);
            mPopupMenu.add(new JSeparator());

            if (!IS_MAC) {
                mPopupMenu.add(optionsMenuItem);
                mPopupMenu.add(new JSeparator());
            }

            mPopupMenu.add(helpMenuItem);
            mPopupMenu.add(aboutDateFormatMenuItem);

            if (!IS_MAC) {
                mPopupMenu.add(aboutMenuItem);
            }

            if (!IS_MAC) {
                mPopupMenu.add(new JSeparator());
                mPopupMenu.add(quitMenuItem);
            }

        } else {
            setJMenuBar(menuBar);
            if (IS_MAC) {
                fileMenu.remove(quitMenuItem);
                toolsMenu.remove(optionsMenuItem);
                helpMenu.remove(aboutMenuItem);
            }

            fileMenu.setVisible(fileMenu.getComponents().length > 0 || !IS_MAC);
            toolsMenu.setVisible(toolsMenu.getComponents().length > 0 || !IS_MAC);
        }

        menuButton.setVisible(mAlmondOptions.getMenuMode() == MenuModePanel.MenuMode.BUTTON);
        SwingHelper.clearToolTipText(menuBar);
        SwingHelper.clearToolTipText(mPopupMenu);
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
            mProgressBar.setIndeterminate(false);
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

        SwingUtilities.invokeLater(() -> {
            configPanel.setEnabled(!mProfiles.isEmpty());
            configPanel.selectTab(tab);
            mStatusPanel.getAutoOpenCheckBox().setEnabled(!mProfiles.isEmpty());
        });
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

        Object[] options = new Object[]{AlmondOptionsPanel.getGlobalOptionsButton(optionsPanel), new JSeparator(), Dict.CANCEL, Dict.OK};
        int retval = JOptionPane.showOptionDialog(this,
                optionsPanel,
                Dict.OPTIONS.toString(),
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                Dict.OK);

        if (retval == Arrays.asList(options).indexOf(Dict.OK)) {
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
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        mPopupMenu = new javax.swing.JPopupMenu();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        quitMenuItem = new javax.swing.JMenuItem();
        profileMenu = new javax.swing.JMenu();
        addMenuItem = new javax.swing.JMenuItem();
        removeMenuItem = new javax.swing.JMenuItem();
        renameMenuItem = new javax.swing.JMenuItem();
        cloneMenuItem = new javax.swing.JMenuItem();
        removeAllMenuItem = new javax.swing.JMenuItem();
        toolsMenu = new javax.swing.JMenu();
        optionsMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        helpMenuItem = new javax.swing.JMenuItem();
        aboutDateFormatMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();
        topPanel = new javax.swing.JPanel();
        profileComboBox = new javax.swing.JComboBox<>();
        toolBar = new javax.swing.JToolBar();
        startButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        addButton = new javax.swing.JButton();
        menuButton = new javax.swing.JButton();
        configPanel = new se.trixon.mapollage.ui.config.ConfigPanel();

        fileMenu.setText(Dict.FILE_MENU.toString());
        fileMenu.add(quitMenuItem);

        menuBar.add(fileMenu);

        profileMenu.setText(Dict.PROFILE.toString());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("se/trixon/mapollage/ui/Bundle"); // NOI18N
        addMenuItem.setText(bundle.getString("MainFrame.addMenuItem.text")); // NOI18N
        profileMenu.add(addMenuItem);
        profileMenu.add(removeMenuItem);
        profileMenu.add(renameMenuItem);
        profileMenu.add(cloneMenuItem);
        profileMenu.add(removeAllMenuItem);

        menuBar.add(profileMenu);

        toolsMenu.setText(Dict.TOOLS.toString());
        toolsMenu.add(optionsMenuItem);

        menuBar.add(toolsMenu);

        helpMenu.setText(Dict.HELP.toString());
        helpMenu.add(helpMenuItem);
        helpMenu.add(aboutDateFormatMenuItem);
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
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

        SwingUtilities.invokeLater(() -> {
            configPanel.loadProfile(p);
        });
    }//GEN-LAST:event_profileComboBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutDateFormatMenuItem;
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JButton addButton;
    private javax.swing.JMenuItem addMenuItem;
    private javax.swing.JButton cancelButton;
    private javax.swing.JMenuItem cloneMenuItem;
    private se.trixon.mapollage.ui.config.ConfigPanel configPanel;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenuItem helpMenuItem;
    private javax.swing.JPopupMenu mPopupMenu;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JButton menuButton;
    private javax.swing.JMenuItem optionsMenuItem;
    private javax.swing.JComboBox<Profile> profileComboBox;
    private javax.swing.JMenu profileMenu;
    private javax.swing.JMenuItem quitMenuItem;
    private javax.swing.JMenuItem removeAllMenuItem;
    private javax.swing.JMenuItem removeMenuItem;
    private javax.swing.JMenuItem renameMenuItem;
    private javax.swing.JButton startButton;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JMenu toolsMenu;
    private javax.swing.JPanel topPanel;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables
}
