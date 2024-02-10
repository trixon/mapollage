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

import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.controlsfx.glyphfont.FontAwesome;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.Spacer;
import se.trixon.mapollage.core.Task;
import se.trixon.mapollage.core.TaskDescription;
import se.trixon.mapollage.core.TaskDescription.DescriptionMode;

/**
 *
 * @author Patrik Karlström
 */
public class DescriptionTab extends BaseTab {

    private final RadioButton mCustomRadioButton = new RadioButton(Dict.CUSTOMIZED.toString());
    private final Button mCustomResetButton = new Button(Dict.RESET.toString());
    private final TextArea mCustomTextArea = new TextArea();
    private final ToggleGroup mDefaultToggleGroup = new ToggleGroup();
    private final RadioButton mExternalCustomRadioButton = new RadioButton(Dict.CUSTOMIZED.toString());
    private final CheckBox mExternalDefaultCheckBox = new CheckBox(mBundle.getString("DescriptionTab.defaultToCheckBox"));
    private final RadioButton mExternalRadioButton = new RadioButton(Dict.EXTERNAL_FILE.toString());
    private final RadioButton mExternalStaticRadioButton = new RadioButton(Dict.STATIC.toString());
    private final TextField mExternalTextField = new TextField();
    private final ToggleGroup mSourceToggleGroup = new ToggleGroup();
    private final CheckBox mStaticAltitudeCheckBox = new CheckBox(Dict.ALTITUDE.toString());
    private final CheckBox mStaticBearingCheckBox = new CheckBox(Dict.BEARING.toString());
    private final CheckBox mStaticCoordinateCheckBox = new CheckBox(Dict.COORDINATE.toString());
    private final CheckBox mStaticDateCheckBox = new CheckBox(Dict.DATE.toString());
    private final CheckBox mStaticFilenameCheckBox = new CheckBox(Dict.FILENAME.toString());
    private final CheckBox mStaticPhotoCheckBox = new CheckBox(Dict.PHOTO.toString());
    private final RadioButton mStaticRadioButton = new RadioButton(Dict.STATIC.toString());

    public DescriptionTab() {
        setText(Dict.DESCRIPTION.toString());
        setGraphic(FontAwesome.Glyph.COMMENT_ALT.getChar());
        createUI();
        initListeners();
    }

    @Override
    public void load(Task task) {
        mTask = task;
        var taskDescription = mTask.getDescription();

        switch (taskDescription.getMode()) {
            case STATIC ->
                mStaticRadioButton.setSelected(true);

            case CUSTOM ->
                mCustomRadioButton.setSelected(true);

            case EXTERNAL ->
                mExternalRadioButton.setSelected(true);

            default ->
                throw new AssertionError();
        }

        if (taskDescription.getDefaultMode() == DescriptionMode.CUSTOM) {
            mExternalCustomRadioButton.setSelected(true);
        } else {
            mExternalStaticRadioButton.setSelected(true);
        }

        mStaticAltitudeCheckBox.setSelected(taskDescription.hasAltitude());
        mStaticBearingCheckBox.setSelected(taskDescription.hasBearing());
        mStaticCoordinateCheckBox.setSelected(taskDescription.hasCoordinate());
        mStaticDateCheckBox.setSelected(taskDescription.hasDate());
        mStaticFilenameCheckBox.setSelected(taskDescription.hasFilename());
        mStaticPhotoCheckBox.setSelected(taskDescription.hasPhoto());

        mCustomTextArea.setText(taskDescription.getCustomValue());
        mExternalTextField.setText(taskDescription.getExternalFileValue());
        mExternalDefaultCheckBox.setSelected(taskDescription.isDefaultTo());
    }

    @Override
    public void save() {
        var taskDescription = mTask.getDescription();
        taskDescription.setAltitude(mStaticAltitudeCheckBox.isSelected());
        taskDescription.setBearing(mStaticBearingCheckBox.isSelected());
        taskDescription.setCoordinate(mStaticCoordinateCheckBox.isSelected());
        taskDescription.setFilename(mStaticFilenameCheckBox.isSelected());
        taskDescription.setPhoto(mStaticPhotoCheckBox.isSelected());
        taskDescription.setDate(mStaticDateCheckBox.isSelected());
        taskDescription.setCustomValue(mCustomTextArea.getText());
        taskDescription.setDefaultTo(mExternalDefaultCheckBox.isSelected());

        if (mStaticRadioButton.isSelected()) {
            taskDescription.setMode(DescriptionMode.STATIC);
        } else if (mCustomRadioButton.isSelected()) {
            taskDescription.setMode(DescriptionMode.CUSTOM);
        } else if (mExternalRadioButton.isSelected()) {
            taskDescription.setMode(DescriptionMode.EXTERNAL);
        }

        if (mExternalCustomRadioButton.isSelected()) {
            taskDescription.setDefaultMode(DescriptionMode.CUSTOM);
        } else {
            taskDescription.setDefaultMode(DescriptionMode.STATIC);
        }
    }

    private void createUI() {
        var staticBox = new HBox(FxHelper.getUIScaled(24));
        var gp = new GridPane();
        setContent(gp);

        mStaticRadioButton.setToggleGroup(mSourceToggleGroup);
        mCustomRadioButton.setToggleGroup(mSourceToggleGroup);
        mExternalRadioButton.setToggleGroup(mSourceToggleGroup);

        mExternalCustomRadioButton.setToggleGroup(mDefaultToggleGroup);
        mExternalStaticRadioButton.setToggleGroup(mDefaultToggleGroup);

        staticBox.getChildren().addAll(
                mStaticAltitudeCheckBox,
                mStaticBearingCheckBox,
                mStaticCoordinateCheckBox,
                mStaticDateCheckBox,
                mStaticFilenameCheckBox,
                mStaticPhotoCheckBox
        );

        staticBox.disableProperty().bind(mStaticRadioButton.selectedProperty().not());
        mCustomResetButton.disableProperty().bind(mCustomRadioButton.selectedProperty().not());
        mCustomTextArea.disableProperty().bind(mCustomRadioButton.selectedProperty().not());

        var externalSelectedProperty = mExternalRadioButton.selectedProperty();
        mExternalDefaultCheckBox.disableProperty().bind(externalSelectedProperty.not());
        mExternalTextField.disableProperty().bind(externalSelectedProperty.not());

        var externalDefaultProperty = mExternalDefaultCheckBox.selectedProperty();
        mExternalCustomRadioButton.disableProperty().bind(externalSelectedProperty.not().or(externalDefaultProperty.not()));
        mExternalStaticRadioButton.disableProperty().bind(externalSelectedProperty.not().or(externalDefaultProperty.not()));

        int col = 0;
        int row = 0;

        gp.add(mStaticRadioButton, col, row++, GridPane.REMAINING, 1);
        gp.add(staticBox, col, row++, GridPane.REMAINING, 1);
        gp.addRow(row++, new Spacer(Orientation.VERTICAL, FxHelper.getUIScaled(16)));
        var customHBox = new HBox(FxHelper.getUIScaled(16), mCustomRadioButton, mCustomResetButton);
        customHBox.setAlignment(Pos.CENTER_LEFT);
        gp.addRow(row++, customHBox);
        gp.add(mCustomTextArea, col, row++, GridPane.REMAINING, 1);
//        gp.addRow(++row, mExternalRadioButton, mExternalDefaultCheckBox, mExternalStaticRadioButton, mExternalCustomRadioButton);
//        gp.add(mExternalTextField, col, ++row, GridPane.REMAINING, 1);
//        mExternalRadioButton.setDisable(true);
        //gp.setBackground(FxHelper.createBackground(Color.BISQUE));
        mCustomTextArea.setPrefColumnCount(999);
        mCustomTextArea.setPrefRowCount(2);
        FxHelper.autoSizeRegionHorizontal(mCustomTextArea);
        FxHelper.autoSizeRegionVertical(mCustomTextArea);

        var subSectionInsets = FxHelper.getUIScaledInsets(8, 0, 0, 12);
        FxHelper.setPadding(subSectionInsets, staticBox);
        FxHelper.setMargin(subSectionInsets, mCustomTextArea);
    }

    private void initListeners() {
        mCustomResetButton.setOnAction(actionEvent -> {
            mCustomTextArea.setText(TaskDescription.getDefaultCustomValue());
        });
    }

}
