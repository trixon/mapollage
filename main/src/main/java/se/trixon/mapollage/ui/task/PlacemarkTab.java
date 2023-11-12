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
package se.trixon.mapollage.ui.task;

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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.controlsfx.glyphfont.FontAwesome;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.UriLabel;
import se.trixon.mapollage.core.Task;
import se.trixon.mapollage.core.TaskPlacemark;
import se.trixon.mapollage.core.TaskPlacemark.NameBy;
import se.trixon.mapollage.core.TaskPlacemark.SymbolAs;

/**
 *
 * @author Patrik Karlström
 */
public class PlacemarkTab extends BaseTab {

    private final UriLabel mDateFormatUriLabel = new UriLabel(Dict.PATTERNS.toString());
    private final ComboBox<String> mDatePatternComboBox = new ComboBox<>();
    private final RadioButton mNameByDateRadioButton = new RadioButton(Dict.DATE_PATTERN.toString());
    private final RadioButton mNameByFileRadioButton = new RadioButton(Dict.FILENAME.toString());
    private final RadioButton mNameByNoRadioButton = new RadioButton(mBundle.getString("PlacemarkTab.nameByNoRadioButton"));
    private final ToggleGroup mNameByToggleGroup = new ToggleGroup();
    private final Spinner<Double> mScaleSpinner = new Spinner(0.5, 10.0, 1.0, 0.1);
    private final RadioButton mSymbolAsPhotoRadioButton = new RadioButton(Dict.PHOTO.toString());
    private final RadioButton mSymbolAsPinRadioButton = new RadioButton(Dict.PIN.toString());
    private final ToggleGroup mSymbolToggleGroup = new ToggleGroup();
    private final CheckBox mTimestampCheckBox = new CheckBox(mBundle.getString("PlacemarkTab.timestampCheckBox"));
    private final Spinner<Double> mZoomSpinner = new Spinner(1.0, 10.0, 1.0, 0.1);

    public PlacemarkTab() {
        setText(Dict.PLACEMARK.toString());
        setGraphic(FontAwesome.Glyph.MAP_MARKER.getChar());
        createUI();
    }

    @Override
    public void load(Task task) {
        mTask = task;
        TaskPlacemark p = mTask.getPlacemark();

        mDatePatternComboBox.setValue(p.getDatePattern());
        mScaleSpinner.getValueFactory().setValue(p.getScale());
        mZoomSpinner.getValueFactory().setValue(p.getZoom());
        mTimestampCheckBox.setSelected(p.isTimestamp());

        RadioButton nameByRadioButton;
        switch (p.getNameBy()) {
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
        switch (p.getSymbolAs()) {
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

    @Override
    public void save() {
        TaskPlacemark p = mTask.getPlacemark();

        p.setDatePattern(mDatePatternComboBox.getValue());
        p.setScale(mScaleSpinner.getValue());
        p.setZoom(mZoomSpinner.getValue());
        p.setTimestamp(mTimestampCheckBox.isSelected());

        NameBy nameBy = null;
        Toggle nameToggle = mNameByToggleGroup.getSelectedToggle();

        if (nameToggle == mNameByFileRadioButton) {
            nameBy = NameBy.FILE;
        } else if (nameToggle == mNameByDateRadioButton) {
            nameBy = NameBy.DATE;
        } else if (nameToggle == mNameByNoRadioButton) {
            nameBy = NameBy.NONE;
        }

        p.setNameBy(nameBy);

        SymbolAs symbolAs = null;
        Toggle symbolToggle = mSymbolToggleGroup.getSelectedToggle();

        if (symbolToggle == mSymbolAsPhotoRadioButton) {
            symbolAs = SymbolAs.PHOTO;
        } else if (symbolToggle == mSymbolAsPinRadioButton) {
            symbolAs = SymbolAs.PIN;
        }

        p.setSymbolAs(symbolAs);
    }

    private void createUI() {
        mDateFormatUriLabel.setUri("https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/text/SimpleDateFormat.html");
        VBox vBox = new VBox();
        VBox leftBox = new VBox();
        VBox rightBox = new VBox();
        vBox.getChildren().addAll(leftBox, rightBox);
        setContent(vBox);

        mScaleSpinner.setEditable(true);
        mZoomSpinner.setEditable(true);
        FxHelper.autoCommitSpinners(mScaleSpinner, mZoomSpinner);

        mNameByFileRadioButton.setToggleGroup(mNameByToggleGroup);
        mNameByDateRadioButton.setToggleGroup(mNameByToggleGroup);
        mNameByNoRadioButton.setToggleGroup(mNameByToggleGroup);

        mDatePatternComboBox.setMaxWidth(Double.MAX_VALUE);
        mDatePatternComboBox.setEditable(true);
        mDatePatternComboBox.setItems(FXCollections.observableList(Arrays.asList(mBundle.getString("dateFormats").split(";"))));

        Insets leftInsets = new Insets(0, 0, 0, 24);
        VBox.setMargin(mDatePatternComboBox, leftInsets);

        leftBox.getChildren().addAll(
                new Label(mBundle.getString("PlacemarkTab.nameByLabel")),
                mNameByFileRadioButton,
                new HBox(8, mNameByDateRadioButton, mDateFormatUriLabel),
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
                mDateFormatUriLabel,
                mNameByNoRadioButton,
                rightBox,
                mSymbolAsPhotoRadioButton,
                mSymbolAsPinRadioButton,
                scaleLabel,
                zoomLabel
        );

        mDatePatternComboBox.disableProperty().bind(mNameByDateRadioButton.selectedProperty().not());

    }

}
