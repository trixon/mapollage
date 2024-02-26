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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.controlsfx.glyphfont.FontAwesome;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.UriLabel;
import se.trixon.mapollage.core.Task;
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

        var taskPlacemark = mTask.getPlacemark();
        mDatePatternComboBox.setValue(taskPlacemark.getDatePattern());
        mScaleSpinner.getValueFactory().setValue(taskPlacemark.getScale());
        mZoomSpinner.getValueFactory().setValue(taskPlacemark.getZoom());
        mTimestampCheckBox.setSelected(taskPlacemark.isTimestamp());

        RadioButton nameByRadioButton;
        switch (taskPlacemark.getNameBy()) {
            case FILE ->
                nameByRadioButton = mNameByFileRadioButton;

            case DATE ->
                nameByRadioButton = mNameByDateRadioButton;

            case NONE ->
                nameByRadioButton = mNameByNoRadioButton;

            default ->
                throw new AssertionError();
        }

        nameByRadioButton.setSelected(true);

        RadioButton symboAsRadioButton;
        switch (taskPlacemark.getSymbolAs()) {
            case PHOTO ->
                symboAsRadioButton = mSymbolAsPhotoRadioButton;

            case PIN ->
                symboAsRadioButton = mSymbolAsPinRadioButton;

            default ->
                throw new AssertionError();
        }

        symboAsRadioButton.setSelected(true);
    }

    @Override
    public void save() {
        var taskPlacemark = mTask.getPlacemark();
        taskPlacemark.setDatePattern(mDatePatternComboBox.getValue());
        taskPlacemark.setScale(mScaleSpinner.getValue());
        taskPlacemark.setZoom(mZoomSpinner.getValue());
        taskPlacemark.setTimestamp(mTimestampCheckBox.isSelected());

        NameBy nameBy = null;
        var nameToggle = mNameByToggleGroup.getSelectedToggle();

        if (nameToggle == mNameByFileRadioButton) {
            nameBy = NameBy.FILE;
        } else if (nameToggle == mNameByDateRadioButton) {
            nameBy = NameBy.DATE;
        } else if (nameToggle == mNameByNoRadioButton) {
            nameBy = NameBy.NONE;
        }

        taskPlacemark.setNameBy(nameBy);

        SymbolAs symbolAs = null;
        var symbolToggle = mSymbolToggleGroup.getSelectedToggle();

        if (symbolToggle == mSymbolAsPhotoRadioButton) {
            symbolAs = SymbolAs.PHOTO;
        } else if (symbolToggle == mSymbolAsPinRadioButton) {
            symbolAs = SymbolAs.PIN;
        }

        taskPlacemark.setSymbolAs(symbolAs);
    }

    private void createUI() {
        mDateFormatUriLabel.setUri(DATE_FORMAT_URL);
        mScaleSpinner.setEditable(true);
        mZoomSpinner.setEditable(true);
        FxHelper.autoCommitSpinners(mScaleSpinner, mZoomSpinner);

        mNameByFileRadioButton.setToggleGroup(mNameByToggleGroup);
        mNameByDateRadioButton.setToggleGroup(mNameByToggleGroup);
        mNameByNoRadioButton.setToggleGroup(mNameByToggleGroup);

        mDatePatternComboBox.setMaxWidth(Double.MAX_VALUE);
        mDatePatternComboBox.setEditable(true);
        mDatePatternComboBox.setItems(FXCollections.observableList(Arrays.asList(mBundle.getString("dateFormats").split(";"))));

        var leftInsets = FxHelper.getUIScaledInsets(0, 0, 0, 24);
        VBox.setMargin(mDatePatternComboBox, leftInsets);
        var symbolLabel = new Label(Dict.SYMBOL.toString());
        var scaleLabel = new Label(Dict.SCALE.toString());
        var zoomLabel = new Label(Dict.ZOOM.toString());

        var vBox = new VBox(
                new Label(mBundle.getString("PlacemarkTab.nameByLabel")),
                mNameByFileRadioButton,
                new HBox(FxHelper.getUIScaled(8), mNameByDateRadioButton, mDateFormatUriLabel),
                mDatePatternComboBox,
                mNameByNoRadioButton,
                symbolLabel,
                mSymbolAsPhotoRadioButton,
                mSymbolAsPinRadioButton,
                scaleLabel,
                mScaleSpinner,
                zoomLabel,
                mZoomSpinner,
                mTimestampCheckBox
        );

        mSymbolAsPhotoRadioButton.setTooltip(new Tooltip(mBundle.getString("PlacemarkTab.symbolPhotoRadioButton.toolTip")));
        mSymbolAsPhotoRadioButton.setToggleGroup(mSymbolToggleGroup);
        mSymbolAsPinRadioButton.setToggleGroup(mSymbolToggleGroup);

        var topInsets = FxHelper.getUIScaledInsets(8, 0, 0, 0);
        VBox.setMargin(mTimestampCheckBox, topInsets);

        addTopPadding(
                mNameByFileRadioButton,
                mNameByDateRadioButton,
                mDateFormatUriLabel,
                mNameByNoRadioButton,
                mSymbolAsPhotoRadioButton,
                mSymbolAsPinRadioButton
        );

        FxHelper.setPadding(FxHelper.getUIScaledInsets(16, 0, 0, 0),
                symbolLabel,
                scaleLabel,
                zoomLabel,
                mTimestampCheckBox
        );

        mDatePatternComboBox.disableProperty().bind(mNameByDateRadioButton.selectedProperty().not());

        setContent(vBox);
    }
}
