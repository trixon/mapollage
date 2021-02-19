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
package se.trixon.mapollage.ui;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.control.LogPanel;
import se.trixon.mapollage.Options;
import se.trixon.mapollage.RunState;
import se.trixon.mapollage.RunStateManager;
import se.trixon.mapollage.profile.Profile;

/**
 *
 * @author Patrik Karlström
 */
public class StatusPanel extends BorderPane {

    private final Tab mErrTab = new Tab(Dict.Dialog.ERROR.toString());
    private final LogPanel mLogOutPanel = new LogPanel();
    private final Options mOptions = Options.getInstance();
    private final Tab mOutTab = new Tab(Dict.OUTPUT.toString());
    private Profile mProfile;
    private final ProgressBar mProgressBar = new ProgressBar();
    private final RunStateManager mRunStateManager = RunStateManager.getInstance();
    private final SummaryHeader mSummaryHeader = new SummaryHeader();

    public StatusPanel() {
        createUI();
        initListeners();
    }

    void clear() {
        mLogOutPanel.clear();
    }

    void err(String message) {
        mLogOutPanel.println(message);
    }

    void out(String message) {
        mLogOutPanel.println(message);
    }

    void setProgress(double p) {
        Platform.runLater(() -> {
            mProgressBar.setProgress(p);
        });
    }

    private void createUI() {
        mLogOutPanel.setMonospaced();
        mOutTab.setContent(mLogOutPanel);
        var box = new VBox(
                mSummaryHeader,
                mProgressBar
        );

        mProgressBar.setMaxWidth(Double.MAX_VALUE);
        mProgressBar.setProgress(0);
        box.setAlignment(Pos.CENTER);
        setTop(box);
        setCenter(mLogOutPanel);

        mLogOutPanel.setWrapText(mOptions.isWordWrap());
    }

    private void initListeners() {
        mOptions.wordWrapProperty().addListener((observable, oldValue, newValue) -> {
            mLogOutPanel.setWrapText(mOptions.isWordWrap());
        });

        mOptions.nightModeProperty().addListener((observable, oldValue, newValue) -> {
            mSummaryHeader.load(mProfile);
        });

        mRunStateManager.profileProperty().addListener((observable, oldValue, newValue) -> {
            mProfile = newValue;
            mSummaryHeader.load(newValue);
        });

        mRunStateManager.runStateProperty().addListener((observable, oldValue, newValue) -> {
            setProgress(newValue == RunState.CANCELABLE ? -1 : 1);
        });
    }
}
