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

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;
import org.apache.commons.lang3.SystemUtils;
import se.trixon.almond.util.AlmondAction;
import se.trixon.almond.util.AlmondOptions;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.almond.util.swing.dialogs.MenuModePanel;

/**
 *
 * @author Patrik Karlsson
 */
public class ActionManager {

    private static final boolean IS_MAC = SystemUtils.IS_OS_MAC;

    static final String ABOUT = "about";
    static final String ABOUT_DATE_FORMAT = "about_date_format";
    static final String ADD = "add";
    static final String CANCEL = "cancel";
    static final String CLONE = "clone";
    static final String HELP = "help";
    static final String MENU = "menu";
    static final String OPTIONS = "options";
    static final String QUIT = "shutdownServerAndWindow";
    static final String REMOVE = "remove";
    static final String REMOVE_ALL = "remove_all";
    static final String RENAME = "rename";
    static final String START = "start";

    private ActionMap mActionMap;
    private final LinkedList<AlmondAction> mAllActions = new LinkedList<>();
    private final AlmondOptions mAlmondOptions = AlmondOptions.getInstance();
    private final HashSet<AppListener> mAppListeners = new HashSet<>();
    private final LinkedList<AlmondAction> mBaseActions = new LinkedList<>();
    private InputMap mInputMap;
    private final HashSet<ProfileListener> mProfileListeners = new HashSet<>();

    public static ActionManager getInstance() {
        return Holder.INSTANCE;
    }

    private ActionManager() {
    }

    public void addAppListener(AppListener appListener) {
        mAppListeners.add(appListener);
    }

    public void addProfileListener(ProfileListener profileListener) {
        mProfileListeners.add(profileListener);
    }

    public Action getAction(String key) {
        return mActionMap.get(key);
    }

    public ActionManager init(ActionMap actionMap, InputMap inputMap) {
        mActionMap = actionMap;
        mInputMap = inputMap;
        AlmondAction action;
        KeyStroke keyStroke;
        int commandMask = SystemHelper.getCommandMask();

        if (mAlmondOptions.getMenuMode() == MenuModePanel.MenuMode.BUTTON) {
            //menu
            int menuKey = KeyEvent.VK_M;
            keyStroke = KeyStroke.getKeyStroke(menuKey, commandMask);
            action = new AlmondAction(Dict.MENU.toString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    mAppListeners.forEach((appActionListener) -> {
                        try {
                            appActionListener.onMenu(e);
                        } catch (Exception exception) {
                        }
                    });
                }
            };

            initAction(action, MENU, keyStroke, MaterialIcon._Navigation.MENU, true);
        }

        //options
        int optionsKey = SystemUtils.IS_OS_MAC ? KeyEvent.VK_COMMA : KeyEvent.VK_P;
        keyStroke = KeyStroke.getKeyStroke(optionsKey, commandMask);
        keyStroke = IS_MAC ? null : keyStroke;
        action = new AlmondAction(Dict.OPTIONS.toString()) {

            @Override
            public void actionPerformed(ActionEvent e) {
                mAppListeners.forEach((appActionListener) -> {
                    try {
                        appActionListener.onOptions(e);
                    } catch (Exception exception) {
                    }
                });
            }

        };

        initAction(action, OPTIONS, keyStroke, MaterialIcon._Action.SETTINGS, true);

        //start
        keyStroke = null;
        action = new AlmondAction(Dict.START.toString()) {

            @Override
            public void actionPerformed(ActionEvent e) {
                mAppListeners.forEach((appActionListener) -> {
                    try {
                        appActionListener.onStart(e);
                    } catch (Exception exception) {
                    }
                });
            }
        };

        initAction(action, START, keyStroke, MaterialIcon._Av.PLAY_ARROW, false);

        //cancel
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        action = new AlmondAction(Dict.CANCEL.toString()) {

            @Override
            public void actionPerformed(ActionEvent e) {
                mAppListeners.forEach((appActionListener) -> {
                    try {
                        appActionListener.onCancel(e);
                    } catch (Exception exception) {
                    }
                });
            }
        };

        initAction(action, CANCEL, keyStroke, MaterialIcon._Content.CLEAR, false);

        //add
        keyStroke = null;
        action = new AlmondAction(Dict.ADD.toString()) {

            @Override
            public void actionPerformed(ActionEvent e) {
                mProfileListeners.forEach((profileListener) -> {
                    try {
                        profileListener.onAdd(e);
                    } catch (Exception exception) {
                    }
                });
            }
        };

        initAction(action, ADD, keyStroke, MaterialIcon._Content.ADD, true);

        //clone
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_C, commandMask);
        action = new AlmondAction(Dict.CLONE.toString()) {

            @Override
            public void actionPerformed(ActionEvent e) {
                mProfileListeners.forEach((profileListener) -> {
                    try {
                        profileListener.onClone(e);
                    } catch (Exception exception) {
                    }
                });
            }
        };

        initAction(action, CLONE, keyStroke, MaterialIcon._Content.CONTENT_COPY, false);

        //edit
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_E, commandMask);
        action = new AlmondAction(Dict.EDIT.toString()) {

            @Override
            public void actionPerformed(ActionEvent e) {
                mProfileListeners.forEach((profileListener) -> {
                    try {
                        profileListener.onEdit(e);
                    } catch (Exception exception) {
                    }
                });
            }
        };

        initAction(action, RENAME, keyStroke, MaterialIcon._Editor.MODE_EDIT, false);

        //remove
        keyStroke = null;
        action = new AlmondAction(Dict.REMOVE.toString()) {

            @Override
            public void actionPerformed(ActionEvent e) {
                mProfileListeners.forEach((profileListener) -> {
                    try {
                        profileListener.onRemove(e);
                    } catch (Exception exception) {
                    }
                });
            }
        };

        initAction(action, REMOVE, keyStroke, MaterialIcon._Content.REMOVE, false);

        //remove all
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.SHIFT_DOWN_MASK);
        action = new AlmondAction(Dict.REMOVE_ALL.toString()) {

            @Override
            public void actionPerformed(ActionEvent e) {
                mProfileListeners.forEach((profileListener) -> {
                    try {
                        profileListener.onRemoveAll(e);
                    } catch (Exception exception) {
                    }
                });
            }
        };

        initAction(action, REMOVE_ALL, keyStroke, MaterialIcon._Content.CLEAR, false);

        //help
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0);
        action = new AlmondAction(Dict.DOCUMENTATION.toString()) {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI("https://trixon.se/projects/filebydate/documentation/"));
                } catch (URISyntaxException | IOException ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };

        initAction(action, HELP, keyStroke, null, true);

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

        //quit
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Q, commandMask);
        action = new AlmondAction(Dict.QUIT.toString()) {

            @Override
            public void actionPerformed(ActionEvent e) {
                mAppListeners.forEach((appActionListener) -> {
                    try {
                        appActionListener.onQuit(e);
                    } catch (Exception exception) {
                    }
                });
            }
        };

        initAction(action, QUIT, keyStroke, MaterialIcon._Content.CLEAR, true);

        return this;
    }

    private void initAction(AlmondAction action, String key, KeyStroke keyStroke, Enum iconEnum, boolean baseAction) {
        action.putValue(Action.ACCELERATOR_KEY, keyStroke);
        action.putValue(Action.SHORT_DESCRIPTION, action.getValue(Action.NAME));
        action.putValue("hideActionText", true);
        action.setIconEnum(iconEnum);
        action.updateIcon();

        mInputMap.put(keyStroke, key);
        mActionMap.put(key, action);

        if (baseAction) {
            mBaseActions.add(action);
        }

        mAllActions.add(action);
    }

    public interface AppListener {

        void onCancel(ActionEvent actionEvent);

        void onMenu(ActionEvent actionEvent);

        void onOptions(ActionEvent actionEvent);

        void onQuit(ActionEvent actionEvent);

        void onStart(ActionEvent actionEvent);
    }

    public interface ProfileListener {

        void onAdd(ActionEvent actionEvent);

        void onClone(ActionEvent actionEvent);

        void onEdit(ActionEvent actionEvent);

        void onRemove(ActionEvent actionEvent);

        void onRemoveAll(ActionEvent actionEvent);

    }

    private static class Holder {

        private static final ActionManager INSTANCE = new ActionManager();
    }
}
