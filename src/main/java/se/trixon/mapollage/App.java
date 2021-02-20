/*
 * Copyright 2021 Patrik Karlström.
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
package se.trixon.mapollage;

import de.jangassen.MenuToolkit;
import java.util.Arrays;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.lang3.SystemUtils;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.PomInfo;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.SystemHelperFx;
import se.trixon.almond.util.fx.AboutModel;
import se.trixon.almond.util.fx.AlmondFx;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.dialogs.about.AboutPane;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.mapollage.ui.AppForm;
import se.trixon.mapollage.ui.OptionsPanel;

/**
 *
 * @author Patrik Karlström
 */
public class App extends Application {

    public static final String APP_TITLE = "Mapollage";
    public static final int ICON_SIZE_TOOLBAR = 32;
    private static final boolean IS_MAC = SystemUtils.IS_OS_MAC;
    private Action mAboutAction;
    private final AlmondFx mAlmondFX = AlmondFx.getInstance();
    private AppForm mAppForm;
    private Action mHelpAction;
    private final Options mOptions = Options.getInstance();
    private Action mOptionsAction;
    private BorderPane mRoot;
    private final RunManager mRunManager = RunManager.getInstance();
    private Stage mStage;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        mStage = stage;
        stage.getIcons().add(new Image(App.class.getResourceAsStream("about_logo.png")));

        mAlmondFX.addStageWatcher(stage, App.class);
        createUI();

        if (IS_MAC) {
            initMac();
        }

        updateNightMode();

        mStage.setTitle(APP_TITLE);
        FxHelper.removeSceneInitFlicker(mStage);

        mStage.show();
        initAccelerators();
        initListeners();
        mAppForm.initAccelerators();
    }

    private void createUI() {
        mOptionsAction = new Action(Dict.OPTIONS.toString(), actionEvent -> {
            displayOptions();
        });
        FxHelper.setTooltip(mOptionsAction, new KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN));
        mOptionsAction.disabledProperty().bind(mRunManager.runningProperty());

        mHelpAction = new Action(Dict.HELP.toString(), actionEvent -> {
            displayHelp();
        });
        FxHelper.setTooltip(mHelpAction, new KeyCodeCombination(KeyCode.F1, KeyCombination.SHORTCUT_ANY));

        //about
        var pomInfo = new PomInfo(App.class, "se.trixon", "mapollage");
        var aboutModel = new AboutModel(SystemHelper.getBundle(App.class, "about"), SystemHelperFx.getResourceAsImageView(App.class, "about_logo.png"));
        aboutModel.setAppVersion(pomInfo.getVersion());
        mAboutAction = AboutPane.getAction(mStage, aboutModel);

        mRoot = new BorderPane(mAppForm = new AppForm());
        var actions = mAppForm.getToolBarActions();
        actions.addAll(Arrays.asList(
                ActionUtils.ACTION_SPAN,
                ActionUtils.ACTION_SPAN,
                mOptionsAction,
                mAboutAction,
                mHelpAction
        ));

        var toolBar = ActionUtils.createToolBar(actions, ActionUtils.ActionTextBehavior.HIDE);
        FxHelper.undecorateButtons(toolBar.getItems().stream());
        FxHelper.slimToolBar(toolBar);

        mRoot.setTop(toolBar);
        mStage.setScene(new Scene(mRoot));
    }

    private void displayHelp() {
        SystemHelper.desktopBrowse("https://trixon.se/projects/mapollage/documentation/");
    }

    private void displayOptions() {
        var alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(mStage);
        alert.setTitle(Dict.OPTIONS.toString());
        alert.setGraphic(null);
        alert.setHeaderText(null);
        alert.setResizable(true);

        var optionsPanel = new OptionsPanel();
        var dialogPane = alert.getDialogPane();
        dialogPane.setContent(optionsPanel);
        FxHelper.removeSceneInitFlicker(dialogPane);

        var button = (Button) dialogPane.lookupButton(ButtonType.OK);
        button.setText(Dict.CLOSE.toString());
//        dialogPane.setPrefSize(400, 200);

        FxHelper.showAndWait(alert, mStage);
    }

    private void initAccelerators() {
        var accelerators = mStage.getScene().getAccelerators();

        accelerators.put(new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN), () -> {
            mStage.fireEvent(new WindowEvent(mStage, WindowEvent.WINDOW_CLOSE_REQUEST));
        });

        if (!IS_MAC) {
            accelerators.put(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN), () -> {
                displayOptions();
            });

            accelerators.put(new KeyCodeCombination(KeyCode.F1, KeyCombination.SHORTCUT_ANY), () -> {
                displayHelp();
            });
        }
    }

    private void initListeners() {
        mOptions.nightModeProperty().addListener((observable, oldValue, newValue) -> {
            updateNightMode();
        });
    }

    private void initMac() {
        var menuToolkit = MenuToolkit.toolkit();
        var applicationMenu = menuToolkit.createDefaultApplicationMenu(APP_TITLE);
        menuToolkit.setApplicationMenu(applicationMenu);

        applicationMenu.getItems().remove(0);
        MenuItem aboutMenuItem = new MenuItem(String.format(Dict.ABOUT_S.toString(), APP_TITLE));
        aboutMenuItem.setOnAction(mAboutAction);

        var settingsMenuItem = new MenuItem(Dict.PREFERENCES.toString());
        settingsMenuItem.setOnAction(mOptionsAction);
        settingsMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN));

        applicationMenu.getItems().add(0, aboutMenuItem);
        applicationMenu.getItems().add(2, settingsMenuItem);

        int cnt = applicationMenu.getItems().size();
        applicationMenu.getItems().get(cnt - 1).setText(String.format("%s %s", Dict.QUIT.toString(), APP_TITLE));
    }

    private void updateNightMode() {
        MaterialIcon.setDefaultColor(mOptions.isNightMode() ? Color.LIGHTGRAY : Color.BLACK);

        mOptionsAction.setGraphic(MaterialIcon._Action.SETTINGS.getImageView(ICON_SIZE_TOOLBAR));
        mAboutAction.setGraphic(MaterialIcon._Action.INFO_OUTLINE.getImageView(ICON_SIZE_TOOLBAR));
        mHelpAction.setGraphic(MaterialIcon._Action.HELP_OUTLINE.getImageView(ICON_SIZE_TOOLBAR));

        FxHelper.setDarkThemeEnabled(mOptions.isNightMode());
        if (mOptions.isNightMode()) {
            FxHelper.loadDarkTheme(mStage.getScene());
        } else {
            FxHelper.unloadDarkTheme(mStage.getScene());
        }
    }
}
