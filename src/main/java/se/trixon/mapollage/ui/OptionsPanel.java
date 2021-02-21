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

import java.util.ResourceBundle;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import org.controlsfx.control.ToggleSwitch;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.LocaleComboBox;
import se.trixon.mapollage.Options;

/**
 *
 * @author Patrik Karlström
 */
public class OptionsPanel extends GridPane {

    private final ResourceBundle mBundle = SystemHelper.getBundle(OptionsPanel.class, "Bundle");
    private final ToggleSwitch mCleanNs2ToggleSwitch = new ToggleSwitch(mBundle.getString("OptionsPanel.cleanNs2ToggleSwitch"));
    private final ToggleSwitch mCleanSpaceToggleSwitch = new ToggleSwitch(mBundle.getString("OptionsPanel.cleanSpaceToggleSwitch"));
    private final Spinner<Double> mDefaultLatitudeSpinner = new Spinner(-90, 90, 0, 0.01);
    private final Spinner<Double> mDefaultLongitudeSpinner = new Spinner(-180, 180, 0, 0.01);
    private final LocaleComboBox mLocaleComboBox = new LocaleComboBox();
    private final ToggleSwitch mLogKmlToggleSwitch = new ToggleSwitch(mBundle.getString("OptionsPanel.logKmlToggleSwitch"));
    private final ToggleSwitch mNightModeToggleSwitch = new ToggleSwitch(Dict.NIGHT_MODE.toString());
    private final Options mOptions = Options.getInstance();
    private final Spinner<Integer> mThumbnailBorderSizeSpinner = new Spinner(0, 20, 2, 1);
    private final Spinner<Integer> mThumbnailSizeSpinner = new Spinner(100, 1200, 250, 10);
    private final ToggleSwitch mWordWrapToggleSwitch = new ToggleSwitch(Dict.DYNAMIC_WORD_WRAP.toString());

    public OptionsPanel() {
        createUI();
    }

    private void createUI() {
        setHgap(32);
        setVgap(2);
        //setGridLinesVisible(true);
        FxHelper.autoSizeColumn(this, 2);

        var calendarLanguageLabel = new Label(Dict.CALENDAR_LANGUAGE.toString());
        var placemarkLabel = new Label(Dict.PLACEMARK.toString());
        var logLabel = new Label(Dict.LOG.toString());
        var thumbnailLabel = new Label(Dict.THUMBNAIL.toString());
        var latitudeLabel = new Label(Dict.LATITUDE.toString());
        var longitudeLabel = new Label(Dict.LONGITUDE.toString());
        var borderSizeLabel = new Label(mBundle.getString("OptionsPanel.borderSizeLabel"));
        var cleanLabel = new Label(mBundle.getString("OptionsPanel.cleanLabel"));
        var defaultCoordinateLabel = new Label(mBundle.getString("OptionsPanel.coordinateLabel"));

        var defaultFont = Font.getDefault();
        var fontFamily = defaultFont.getFamily();
        var fontSize = defaultFont.getSize();
        var font = Font.font(fontFamily, FontPosture.ITALIC, fontSize * 1.3);

        placemarkLabel.setFont(font);
        defaultCoordinateLabel.setFont(font);
        cleanLabel.setFont(font);
        logLabel.setFont(font);

        int row = 0;
        add(calendarLanguageLabel, 0, row++, REMAINING, 1);
        add(mLocaleComboBox, 0, row++, REMAINING, 1);
        add(placemarkLabel, 0, row++, REMAINING, 1);
        add(thumbnailLabel, 0, row, 1, 1);
        add(borderSizeLabel, 1, row++, 1, 1);
        add(mThumbnailSizeSpinner, 0, row, 1, 1);
        add(mThumbnailBorderSizeSpinner, 1, row++, 1, 1);
        add(defaultCoordinateLabel, 0, row++, REMAINING, 1);
        add(latitudeLabel, 0, row, 1, 1);
        add(longitudeLabel, 1, row++, 1, 1);
        add(mDefaultLatitudeSpinner, 0, row, 1, 1);
        add(mDefaultLongitudeSpinner, 1, row++, 1, 1);
        add(cleanLabel, 0, row++, REMAINING, 1);
        add(mCleanNs2ToggleSwitch, 0, row, 1, 1);
        add(mCleanSpaceToggleSwitch, 1, row++, 1, 1);
        add(logLabel, 0, row++, REMAINING, 1);
        add(mWordWrapToggleSwitch, 0, row, 1, 1);
        add(mLogKmlToggleSwitch, 1, row++, 1, 1);
        add(mNightModeToggleSwitch, 0, row++, 1, 1);

        FxHelper.setPadding(new Insets(8, 0, 0, 0),
                placemarkLabel,
                defaultCoordinateLabel,
                cleanLabel,
                logLabel
        );

        FxHelper.setPadding(new Insets(28, 0, 0, 0),
                mNightModeToggleSwitch
        );

        for (var columnConstraint : getColumnConstraints()) {
            columnConstraint.setFillWidth(true);
            columnConstraint.setHgrow(Priority.ALWAYS);
        }

        FxHelper.setEditable(true,
                mDefaultLongitudeSpinner,
                mDefaultLatitudeSpinner,
                mThumbnailSizeSpinner,
                mThumbnailBorderSizeSpinner
        );

        FxHelper.autoCommitSpinners(
                mDefaultLatitudeSpinner,
                mDefaultLongitudeSpinner,
                mThumbnailBorderSizeSpinner,
                mThumbnailSizeSpinner
        );

        mLocaleComboBox.setMaxWidth(Double.MAX_VALUE);
        mDefaultLatitudeSpinner.setMaxWidth(Double.MAX_VALUE);
        mDefaultLongitudeSpinner.setMaxWidth(Double.MAX_VALUE);
        mThumbnailSizeSpinner.setMaxWidth(Double.MAX_VALUE);
        mThumbnailBorderSizeSpinner.setMaxWidth(Double.MAX_VALUE);
        mCleanNs2ToggleSwitch.setMaxWidth(Double.MAX_VALUE);
        mCleanSpaceToggleSwitch.setMaxWidth(Double.MAX_VALUE);
        mWordWrapToggleSwitch.setMaxWidth(Double.MAX_VALUE);
        mLogKmlToggleSwitch.setMaxWidth(Double.MAX_VALUE);
        mNightModeToggleSwitch.setMaxWidth(Double.MAX_VALUE);

        mLocaleComboBox.valueProperty().bindBidirectional(mOptions.localeProperty());
        mDefaultLatitudeSpinner.valueFactoryProperty().getValue().valueProperty().bindBidirectional(mOptions.defaultLatProperty());
        mDefaultLongitudeSpinner.valueFactoryProperty().getValue().valueProperty().bindBidirectional(mOptions.defaultLonProperty());
        mThumbnailBorderSizeSpinner.valueFactoryProperty().getValue().valueProperty().bindBidirectional(mOptions.thumbnailBorderSizeProperty());
        mThumbnailSizeSpinner.valueFactoryProperty().getValue().valueProperty().bindBidirectional(mOptions.thumbnailSizeProperty());
        mCleanNs2ToggleSwitch.selectedProperty().bindBidirectional(mOptions.cleanNS2Property());
        mCleanSpaceToggleSwitch.selectedProperty().bindBidirectional(mOptions.cleanSpaceProperty());
        mWordWrapToggleSwitch.selectedProperty().bindBidirectional(mOptions.wordWrapProperty());
        mLogKmlToggleSwitch.selectedProperty().bindBidirectional(mOptions.logKmlProperty());
        mNightModeToggleSwitch.selectedProperty().bindBidirectional(mOptions.nightModeProperty());
    }

}
