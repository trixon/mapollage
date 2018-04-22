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
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.control.LocaleComboBox;
import se.trixon.mapollage.Options;

/**
 *
 * @author Patrik Karlström
 */
public class OptionsPanel extends GridPane {

    private final ResourceBundle mBundle = SystemHelper.getBundle(OptionsPanel.class, "Bundle");
    private final CheckBox mCleanNs2CheckBox = new CheckBox(mBundle.getString("OptionsPanel.cleanNs2CheckBox"));
    private final CheckBox mCleanSpaceCheckBox = new CheckBox(mBundle.getString("OptionsPanel.cleanSpaceCheckBox"));
    private final Font mDefaultFont = Font.getDefault();
    private final Spinner<Double> mDefaultLatitudeSpinner = new Spinner(-180, 180, 0, 0.01);
    private final Spinner<Double> mDefaultLongitudeSpinner = new Spinner(-90, 90, 0, 0.01);
    private final LocaleComboBox mLocaleComboBox = new LocaleComboBox();
    private final CheckBox mLogKmlCheckBox = new CheckBox(mBundle.getString("OptionsPanel.logKmlCheckBox"));
    private final Options mOptions = Options.getInstance();
    private final Spinner<Integer> mThumbnailBorderSizeSpinner = new Spinner(0, 20, 2, 1);
    private final Spinner<Integer> mThumbnailSizeSpinner = new Spinner(100, 1200, 250, 10);
    private final Insets mTopInsets = new Insets(8, 0, 0, 0);
    private final CheckBox mWordWrapCheckBox = new CheckBox(Dict.DYNAMIC_WORD_WRAP.toString());

    public OptionsPanel() {
        createUI();
        load();
    }

    private void addTopMargin(Region... regions) {
        for (Region region : regions) {
            GridPane.setMargin(region, mTopInsets);
        }
    }

    private void addTopPadding(Region... regions) {
        for (Region region : regions) {
            region.setPadding(mTopInsets);
        }
    }

    private void createUI() {
        //setGridLinesVisible(true);
        String fontFamily = mDefaultFont.getFamily();
        double fontSize = mDefaultFont.getSize();

        Font font = Font.font(fontFamily, FontPosture.ITALIC, fontSize * 1.3);

        Label calendarLanguageLabel = new Label(Dict.CALENDAR_LANGUAGE.toString());
        Label placemarkLabel = new Label(Dict.PLACEMARK.toString());
        Label logLabel = new Label(Dict.LOG.toString());
        Label thumbnailLabel = new Label(Dict.THUMBNAIL.toString());
        Label latitudeLabel = new Label(Dict.LATITUDE.toString());
        Label longitudeLabel = new Label(Dict.LONGITUDE.toString());
        Label borderSizeLabel = new Label(mBundle.getString("OptionsPanel.borderSizeLabel"));
        Label cleanLabel = new Label(mBundle.getString("OptionsPanel.cleanLabel"));
        Label defaultCoordinateLabel = new Label(mBundle.getString("OptionsPanel.coordinateLabel"));

        placemarkLabel.setFont(font);
        defaultCoordinateLabel.setFont(font);
        cleanLabel.setFont(font);
        logLabel.setFont(font);

        mDefaultLongitudeSpinner.setEditable(true);
        mDefaultLatitudeSpinner.setEditable(true);
        mThumbnailSizeSpinner.setEditable(true);
        mThumbnailBorderSizeSpinner.setEditable(true);

        addColumn(0,
                calendarLanguageLabel,
                mLocaleComboBox,
                placemarkLabel,
                thumbnailLabel,
                mThumbnailSizeSpinner,
                borderSizeLabel,
                mThumbnailBorderSizeSpinner,
                defaultCoordinateLabel,
                latitudeLabel,
                mDefaultLatitudeSpinner,
                longitudeLabel,
                mDefaultLongitudeSpinner,
                cleanLabel,
                mCleanNs2CheckBox,
                mCleanSpaceCheckBox,
                logLabel,
                mWordWrapCheckBox,
                mLogKmlCheckBox
        );

        addTopPadding(
                placemarkLabel,
                defaultCoordinateLabel,
                borderSizeLabel,
                longitudeLabel,
                cleanLabel,
                logLabel
        );

        addTopMargin(
                mCleanSpaceCheckBox,
                mLogKmlCheckBox
        );
    }

    private void load() {
        mLocaleComboBox.setLocale(mOptions.getLocale());
        mWordWrapCheckBox.setSelected(mOptions.isWordWrap());
        mCleanNs2CheckBox.setSelected(mOptions.isCleanNs2());
        mCleanSpaceCheckBox.setSelected(mOptions.isCleanSpace());
        mDefaultLatitudeSpinner.getValueFactory().setValue(mOptions.getDefaultLat());
        mDefaultLongitudeSpinner.getValueFactory().setValue(mOptions.getDefaultLon());
        mLogKmlCheckBox.setSelected(mOptions.isLogKml());
        mThumbnailSizeSpinner.getValueFactory().setValue(mOptions.getThumbnailSize());
        mThumbnailBorderSizeSpinner.getValueFactory().setValue(mOptions.getThumbnailBorderSize());
    }

    void save() {
        mOptions.setLocale(mLocaleComboBox.getLocale());
        mOptions.setWordWrap(mWordWrapCheckBox.isSelected());
        mOptions.setCleanNs2(mCleanNs2CheckBox.isSelected());
        mOptions.setCleanSpace(mCleanSpaceCheckBox.isSelected());
        mOptions.setDefaultLat(mDefaultLatitudeSpinner.getValue());
        mOptions.setDefaultLon(mDefaultLongitudeSpinner.getValue());
        mOptions.setLogKml(mLogKmlCheckBox.isSelected());
        mOptions.setThumbnailSize(mThumbnailSizeSpinner.getValue());
        mOptions.setThumbnailBorderSize(mThumbnailBorderSizeSpinner.getValue());
    }
}
