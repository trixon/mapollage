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
import javafx.geometry.Side;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.control.LogPanel;
import se.trixon.mapollage.Options;
import se.trixon.mapollage.RunState;
import se.trixon.mapollage.RunStateManager;

/**
 *
 * @author Patrik Karlström
 */
public class StatusPanel extends BorderPane {

    private final LogPanel mLogErrPanel = new LogPanel();
    private final LogPanel mLogInfoPanel = new LogPanel();
    private final LogPanel mLogOutPanel = new LogPanel();
    private final Options mOptions = Options.getInstance();
    private final ProgressBar mProgressBar = new ProgressBar();
    private final RunStateManager mRunStateManager = RunStateManager.getInstance();
    private final SummaryHeader mSummaryHeader = new SummaryHeader();
    private final TabPane mTabPane = new TabPane();

    public StatusPanel() {
        createUI();
        initListeners();
    }

    void clear() {
        mLogOutPanel.clear();
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

    private void createUI() {
        mLogOutPanel.setMonospaced();
        mLogErrPanel.setMonospaced();
        mLogInfoPanel.setMonospaced();

        var outTab = new Tab(Dict.OUTPUT.toString(), mLogOutPanel);
        var errTab = new Tab(Dict.Dialog.ERROR.toString(), mLogErrPanel);
        var infoTab = new Tab(Dict.INFORMATION.toString(), mLogInfoPanel);

        var box = new VBox(
                mSummaryHeader,
                mProgressBar
        );

        mProgressBar.setMaxWidth(Double.MAX_VALUE);
        mProgressBar.setProgress(0);
        box.setAlignment(Pos.CENTER);
        setTop(box);
        mTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        mTabPane.setSide(Side.BOTTOM);
        mTabPane.getTabs().setAll(outTab, errTab, infoTab);
        setCenter(mTabPane);

        mLogOutPanel.setWrapText(mOptions.isWordWrap());
    }

    private void initListeners() {
        mOptions.wordWrapProperty().addListener((observable, oldValue, newValue) -> {
            mLogOutPanel.setWrapText(mOptions.isWordWrap());
            mLogErrPanel.setWrapText(mOptions.isWordWrap());
            mLogInfoPanel.setWrapText(mOptions.isWordWrap());
        });

        mOptions.nightModeProperty().addListener((observable, oldValue, newValue) -> {
            mSummaryHeader.load(mRunStateManager.getProfile());
        });

        mRunStateManager.profileProperty().addListener((observable, oldValue, newValue) -> {
            mSummaryHeader.load(newValue);
            mLogInfoPanel.clear();
            if (newValue != null) {
                mLogInfoPanel.println(newValue.toInfoString());
            }
        });

        mRunStateManager.runStateProperty().addListener((observable, oldValue, newValue) -> {
            setProgress(newValue == RunState.CANCELABLE ? -1 : 1);
        });
    }
}
