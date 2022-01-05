/*
 * Copyright 2022 Patrik Karlström.
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
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.LogPanel;
import static se.trixon.mapollage.App.ICON_SIZE_TOOLBAR;
import se.trixon.mapollage.Options;
import se.trixon.mapollage.RunManager;
import se.trixon.mapollage.RunStatus;

/**
 *
 * @author Patrik Karlström
 */
public class StatusPanel extends BorderPane {

    private final ResourceBundle mBundle = SystemHelper.getBundle(AppForm.class, "Bundle");
    private final CheckBox mCheckBox = new CheckBox(mBundle.getString("ProgressPanel.autoOpenCheckBox"));
    private final Label mDescLabel = new Label();
    private final GlyphFont mFontAwesome = GlyphFontRegistry.font("FontAwesome");
    private final LogPanel mLogErrPanel = new LogPanel();
    private final LogPanel mLogInfoPanel = new LogPanel();
    private final LogPanel mLogOutPanel = new LogPanel();
    private final Label mNameLabel = new Label();
    private final Button mOpenButton = new Button();
    private final Options mOptions = Options.getInstance();
    private final ProgressBar mProgressBar = new ProgressBar();
    private final RunManager mRunManager = RunManager.getInstance();
    private final TabPane mTabPane = new TabPane();

    public StatusPanel() {
        createUI();
        initListeners();
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

    private void createUI() {
        String fontFamily = Font.getDefault().getFamily();
        double fontSize = Font.getDefault().getSize();

        mNameLabel.setFont(Font.font(fontFamily, FontWeight.BOLD, fontSize * 1.6));
        mDescLabel.setFont(Font.font(fontFamily, FontWeight.NORMAL, fontSize * 1.3));

        mProgressBar.setMaxWidth(Double.MAX_VALUE);
        mProgressBar.setProgress(0);

        mCheckBox.selectedProperty().bindBidirectional(mOptions.autoOpenProperty());

        FxHelper.undecorateButton(mOpenButton);
        mOpenButton.setTooltip(new Tooltip(Dict.OPEN.toString()));
        mOpenButton.setGraphic(mFontAwesome.create(FontAwesome.Glyph.GLOBE).size(ICON_SIZE_TOOLBAR / 2));
        mOpenButton.disableProperty().bind(mRunManager.runningProperty().or(mRunManager.runStatusProperty().isNotEqualTo(RunStatus.FINISHED)));
        mOpenButton.setOnAction(actionEvent -> {
            mRunManager.openDestination();
        });

        HBox progressBox = new HBox(8, mProgressBar, mCheckBox, mOpenButton);
        HBox.setHgrow(mProgressBar, Priority.ALWAYS);
        progressBox.setAlignment(Pos.CENTER);

        var topBox = new VBox(
                mNameLabel,
                mDescLabel,
                progressBox
        );
        topBox.setPadding(FxHelper.getUIScaledInsets(8));
        topBox.setAlignment(Pos.CENTER_LEFT);

        setTop(topBox);

        mLogOutPanel.setMonospaced();
        mLogErrPanel.setMonospaced();
        mLogInfoPanel.setMonospaced();

        var outTab = new Tab(Dict.OUTPUT.toString(), mLogOutPanel);
        var errTab = new Tab(Dict.Dialog.ERROR.toString(), mLogErrPanel);
        var infoTab = new Tab(Dict.INFORMATION.toString(), mLogInfoPanel);

        mTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        mTabPane.setSide(Side.BOTTOM);
        mTabPane.getTabs().setAll(outTab, errTab, infoTab);
        setCenter(mTabPane);

        mLogOutPanel.setWrapText(mOptions.isWordWrap());
    }

    private void initListeners() {
        mOptions.wordWrapProperty().addListener((observable, oldValue, newValue) -> {
            mLogOutPanel.setWrapText(newValue);
            mLogErrPanel.setWrapText(newValue);
            mLogInfoPanel.setWrapText(newValue);
        });

        mRunManager.profileProperty().addListener((observable, oldValue, newValue) -> {
            mLogInfoPanel.clear();

            if (newValue != null) {
                mNameLabel.setText(newValue.getName());
                mDescLabel.setText(newValue.getDescriptionString());
                mLogInfoPanel.println(newValue.toInfoString());
            } else {
                mNameLabel.setText("");
                mDescLabel.setText("");
            }
        });
    }
}
