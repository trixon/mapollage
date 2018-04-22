/*
 * Copyright 2018 Patrik Karlström.
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

import java.util.ResourceBundle;
import java.util.prefs.PreferenceChangeEvent;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.LogPanel;
import se.trixon.mapollage.Options;

/**
 *
 * @author Patrik Karlström
 */
public class ProgressPanel extends BorderPane {

    private final ResourceBundle mBundle = SystemHelper.getBundle(MainApp.class, "Bundle");
    private final CheckBox mCheckBox = new CheckBox(mBundle.getString("StatusPanel.autoOpenCheckBox.text"));
    private final Tab mErrTab = new Tab(Dict.Dialog.ERROR.toString());
    private final LogPanel mLogErrPanel = new LogPanel();
    private final LogPanel mLogOutPanel = new LogPanel();
    private final Button mOpenButton = new Button();
    private final Options mOptions = Options.getInstance();
    private final Tab mOutTab = new Tab(Dict.OUTPUT.toString());
    private final ProgressBar mProgressBar = new ProgressBar();
    private final TabPane mTabPane = new TabPane();

    public ProgressPanel() {
        mLogOutPanel.setMonospaced();
        mLogErrPanel.setMonospaced();
        mOutTab.setContent(mLogOutPanel);
        mErrTab.setContent(mLogErrPanel);
        mOutTab.setClosable(false);
        mErrTab.setClosable(false);
        mTabPane.getTabs().addAll(mOutTab, mErrTab);

        FxHelper.undecorateButton(mOpenButton);

        Insets insets = new Insets(8);
        mOpenButton.setPadding(insets);
        mProgressBar.setPadding(insets);
        mCheckBox.setSelected(mOptions.isAutoOpen());
        mCheckBox.setOnAction((ActionEvent event) -> {
            mOptions.setAutoOpen(mCheckBox.isSelected());
        });

        HBox box = new HBox(8, mProgressBar, mCheckBox, mOpenButton);
        HBox.setHgrow(mProgressBar, Priority.ALWAYS);
        mProgressBar.setMaxWidth(Double.MAX_VALUE);
        box.setAlignment(Pos.CENTER);
        setCenter(mTabPane);
        setBottom(box);

        mLogOutPanel.setWrapText(mOptions.isWordWrap());
        mLogErrPanel.setWrapText(mOptions.isWordWrap());

        mOptions.getPreferences().addPreferenceChangeListener((PreferenceChangeEvent evt) -> {
            switch (evt.getKey()) {
                case Options.KEY_WORD_WRAP:
                    mLogOutPanel.setWrapText(mOptions.isWordWrap());
                    mLogErrPanel.setWrapText(mOptions.isWordWrap());
                    break;
            }
        });

    }

    public Button getOpenButton() {
        return mOpenButton;
    }

    void clear() {
        mLogOutPanel.clear();
        mLogErrPanel.clear();
    }

    void err(String message) {
        mLogErrPanel.println(message);
    }

    void out(String message) {
        mLogOutPanel.println(message);
    }

    void setProgress(double p) {
        Platform.runLater(() -> {
            mProgressBar.setProgress(p);
        });
    }

}
