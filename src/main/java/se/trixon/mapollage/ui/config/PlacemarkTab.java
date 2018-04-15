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
package se.trixon.mapollage.ui.config;

import java.util.Arrays;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import org.controlsfx.glyphfont.FontAwesome;
import se.trixon.almond.util.Dict;
import se.trixon.mapollage.profile.Profile;
import se.trixon.mapollage.profile.ProfilePlacemark;
import se.trixon.mapollage.profile.ProfilePlacemark.NameBy;
import se.trixon.mapollage.profile.ProfilePlacemark.SymbolAs;

/**
 *
 * @author Patrik Karlström
 */
public class PlacemarkTab extends BaseTab {

    private ComboBox<String> mDatePatternComboBox = new ComboBox<>();
    private RadioButton mNameByDateRadioButton = new RadioButton(Dict.DATE_PATTERN.toString());
    private RadioButton mNameByFileRadioButton = new RadioButton(Dict.FILENAME.toString());
    private RadioButton mNameByNoRadioButton = new RadioButton(mBundle.getString("ModulePlacemarkPanel.nameByNoRadioButton.text"));
    private ToggleGroup mNameByToggleGroup = new ToggleGroup();
    private ProfilePlacemark mPlacemark;
    private Spinner<Double> mScaleSpinner = new Spinner(0.5, 10.0, 1.0, 0.1);
    private RadioButton mSymbolAsPhotoRadioButton = new RadioButton(Dict.PHOTO.toString());
    private RadioButton mSymbolAsPinRadioButton = new RadioButton(Dict.PIN.toString());
    private ToggleGroup mSymbolToggleGroup = new ToggleGroup();
    private CheckBox mTimestampCheckBox = new CheckBox(mBundle.getString("ModulePlacemarkPanel.timestampCheckBox.text"));
    private Spinner<Double> mZoomSpinner = new Spinner(1.0, 10.0, 1.0, 0.1);

    public PlacemarkTab(Profile profile) {
        setText(Dict.PLACEMARK.toString());
        setGraphic(FontAwesome.Glyph.MAP_MARKER.getChar());
        mProfile = profile;
        mPlacemark = mProfile.getPlacemark();
        createUI();
        load();
    }

    @Override
    public boolean hasValidSettings() {
        return true;
    }

    @Override
    public void save() {
        mPlacemark.setDatePattern(mDatePatternComboBox.getValue());
        mPlacemark.setScale(mScaleSpinner.getValue());
        mPlacemark.setZoom(mZoomSpinner.getValue());
        mPlacemark.setTimestamp(mTimestampCheckBox.isSelected());

        NameBy nameBy = null;
        Toggle nameToggle = mNameByToggleGroup.getSelectedToggle();

        if (nameToggle == mNameByFileRadioButton) {
            nameBy = NameBy.FILE;
        } else if (nameToggle == mNameByDateRadioButton) {
            nameBy = NameBy.DATE;
        } else if (nameToggle == mNameByNoRadioButton) {
            nameBy = NameBy.NONE;
        }

        mPlacemark.setNameBy(nameBy);

        SymbolAs symbolAs = null;
        Toggle symbolToggle = mSymbolToggleGroup.getSelectedToggle();

        if (symbolToggle == mSymbolAsPhotoRadioButton) {
            symbolAs = SymbolAs.PHOTO;
        } else if (symbolToggle == mSymbolAsPinRadioButton) {
            symbolAs = SymbolAs.PIN;
        }

        mPlacemark.setSymbolAs(symbolAs);
    }

    private void createUI() {
        VBox vBox = new VBox();
        VBox leftBox = new VBox();
        VBox rightBox = new VBox();
        vBox.getChildren().addAll(leftBox, rightBox);
        setContent(vBox);

        mNameByFileRadioButton.setToggleGroup(mNameByToggleGroup);
        mNameByDateRadioButton.setToggleGroup(mNameByToggleGroup);
        mNameByNoRadioButton.setToggleGroup(mNameByToggleGroup);

        mDatePatternComboBox.setMaxWidth(Double.MAX_VALUE);
        mDatePatternComboBox.setEditable(true);
        mDatePatternComboBox.setItems(FXCollections.observableList(Arrays.asList(mBundle.getString("dateFormats").split(";"))));

        Insets leftInsets = new Insets(0, 0, 0, 24);
        VBox.setMargin(mDatePatternComboBox, leftInsets);

        leftBox.getChildren().addAll(
                new Label(mBundle.getString("ModulePlacemarkPanel.nameByLabel.text")),
                mNameByFileRadioButton,
                mNameByDateRadioButton,
                mDatePatternComboBox,
                mNameByNoRadioButton
        );

        mSymbolAsPhotoRadioButton.setToggleGroup(mSymbolToggleGroup);
        mSymbolAsPinRadioButton.setToggleGroup(mSymbolToggleGroup);
        Label scaleLabel = new Label(Dict.SCALE.toString());
        Label zoomLabel = new Label(Dict.ZOOM.toString());

        Insets topInsets = new Insets(8, 0, 0, 0);
        VBox.setMargin(mTimestampCheckBox, topInsets);

        rightBox.getChildren().addAll(
                new Label(Dict.SYMBOL.toString()),
                mSymbolAsPhotoRadioButton,
                mSymbolAsPinRadioButton,
                scaleLabel,
                mScaleSpinner,
                zoomLabel,
                mZoomSpinner,
                mTimestampCheckBox
        );

        addTopPadding(
                mNameByFileRadioButton,
                mNameByDateRadioButton,
                mNameByNoRadioButton,
                rightBox,
                mSymbolAsPhotoRadioButton,
                mSymbolAsPinRadioButton,
                scaleLabel,
                zoomLabel
        );

        mDatePatternComboBox.disableProperty().bind(mNameByDateRadioButton.selectedProperty().not());

    }

    private void load() {
        mDatePatternComboBox.setValue(mPlacemark.getDatePattern());
        mScaleSpinner.getValueFactory().setValue(mPlacemark.getScale());
        mZoomSpinner.getValueFactory().setValue(mPlacemark.getZoom());
        mTimestampCheckBox.setSelected(mPlacemark.isTimestamp());

        RadioButton nameByRadioButton;
        switch (mPlacemark.getNameBy()) {
            case FILE:
                nameByRadioButton = mNameByFileRadioButton;
                break;

            case DATE:
                nameByRadioButton = mNameByDateRadioButton;
                break;

            case NONE:
                nameByRadioButton = mNameByNoRadioButton;
                break;

            default:
                throw new AssertionError();
        }

        nameByRadioButton.setSelected(true);

        RadioButton symboAsRadioButton;
        switch (mPlacemark.getSymbolAs()) {
            case PHOTO:
                symboAsRadioButton = mSymbolAsPhotoRadioButton;
                break;

            case PIN:
                symboAsRadioButton = mSymbolAsPinRadioButton;
                break;

            default:
                throw new AssertionError();
        }

        symboAsRadioButton.setSelected(true);
    }
}
