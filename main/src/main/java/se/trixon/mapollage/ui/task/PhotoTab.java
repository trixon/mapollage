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

import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.controlsfx.glyphfont.FontAwesome;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.mapollage.core.Task;
import se.trixon.mapollage.core.TaskPhoto;

/**
 *
 * @author Patrik Karlström
 */
public class PhotoTab extends BaseTab {

    private final CheckBox mLowerCaseExtCheckBox = new CheckBox(mBundle.getString("PhotoTab.lowerCaseExtCheckBox"));
    private final CheckBox mMaxHeightCheckBox = new CheckBox(Dict.MAX_HEIGHT.toString());
    private final Spinner<Integer> mMaxHeightSpinner = new Spinner(1, Integer.MAX_VALUE, 400, 10);
    private final CheckBox mMaxWidthCheckBox = new CheckBox(Dict.MAX_WIDTH.toString());
    private final Spinner<Integer> mMaxWidthSpinner = new Spinner(1, Integer.MAX_VALUE, 400, 10);
    private final RadioButton mRefAbsolutePathRadioButton = new RadioButton(mBundle.getString("PhotoTab.absolutePathRadioButton"));
    private final TextField mRefAbsolutePathTextField = new TextField();
    private final RadioButton mRefAbsoluteRadioButton = new RadioButton(Dict.ABSOLUTE.toString());
    private final RadioButton mRefRelativeRadioButton = new RadioButton(Dict.RELATIVE.toString());
    private final RadioButton mRefThumbnailRadioButton = new RadioButton(Dict.THUMBNAIL.toString());
    private final ToggleGroup mToggleGroup = new ToggleGroup();

    public PhotoTab() {
        setText(Dict.PHOTO.toString());
        setGraphic(FontAwesome.Glyph.IMAGE.getChar());
        createUI();
    }

    @Override
    public void load(Task task) {
        mTask = task;

        var taskPhoto = mTask.getPhoto();
        mMaxWidthCheckBox.setSelected(taskPhoto.isLimitWidth());
        mMaxWidthSpinner.getValueFactory().setValue(taskPhoto.getWidthLimit());
        mMaxHeightCheckBox.setSelected(taskPhoto.isLimitHeight());
        mMaxHeightSpinner.getValueFactory().setValue(taskPhoto.getHeightLimit());

        switch (taskPhoto.getReference()) {
            case ABSOLUTE ->
                mRefAbsoluteRadioButton.setSelected(true);

            case ABSOLUTE_PATH ->
                mRefAbsolutePathRadioButton.setSelected(true);

            case RELATIVE ->
                mRefRelativeRadioButton.setSelected(true);

            case THUMBNAIL ->
                mRefThumbnailRadioButton.setSelected(true);

            default ->
                throw new AssertionError();
        }

        mRefAbsolutePathTextField.setText(taskPhoto.getBaseUrlValue());
        mLowerCaseExtCheckBox.setSelected(taskPhoto.isForceLowerCaseExtension());
    }

    @Override
    public void save() {
        var taskPhoto = mTask.getPhoto();
        taskPhoto.setBaseUrlValue(mRefAbsolutePathTextField.getText());
        taskPhoto.setForceLowerCaseExtension(mLowerCaseExtCheckBox.isSelected());
        taskPhoto.setHeightLimit(mMaxHeightSpinner.getValue());
        taskPhoto.setWidthLimit(mMaxWidthSpinner.getValue());
        taskPhoto.setLimitHeight(mMaxHeightCheckBox.isSelected());
        taskPhoto.setLimitWidth(mMaxWidthCheckBox.isSelected());

        if (mRefAbsolutePathRadioButton.isSelected()) {
            taskPhoto.setReference(TaskPhoto.Reference.ABSOLUTE_PATH);
        } else if (mRefAbsoluteRadioButton.isSelected()) {
            taskPhoto.setReference(TaskPhoto.Reference.ABSOLUTE);
        } else if (mRefRelativeRadioButton.isSelected()) {
            taskPhoto.setReference(TaskPhoto.Reference.RELATIVE);
        } else if (mRefThumbnailRadioButton.isSelected()) {
            taskPhoto.setReference(TaskPhoto.Reference.THUMBNAIL);
        }
    }

    private void createUI() {
        var gp = new GridPane();
        setContent(gp);

        mMaxHeightSpinner.setEditable(true);
        mMaxWidthSpinner.setEditable(true);
        FxHelper.autoCommitSpinners(mMaxHeightSpinner, mMaxWidthSpinner);

        mRefAbsolutePathRadioButton.setToggleGroup(mToggleGroup);
        mRefAbsoluteRadioButton.setToggleGroup(mToggleGroup);
        mRefRelativeRadioButton.setToggleGroup(mToggleGroup);
        mRefThumbnailRadioButton.setToggleGroup(mToggleGroup);
        mLowerCaseExtCheckBox.setTooltip(new Tooltip(mBundle.getString("PhotoTab.lowerCaseExtCheckBox.toolTip")));

        var referenceLabel = new Label(Dict.FILE_REFERENCE.toString());

        gp.addColumn(0,
                mMaxWidthCheckBox,
                mMaxWidthSpinner,
                mMaxHeightCheckBox,
                mMaxHeightSpinner,
                referenceLabel,
                mRefThumbnailRadioButton,
                mRefRelativeRadioButton,
                mRefAbsoluteRadioButton,
                mRefAbsolutePathRadioButton,
                mRefAbsolutePathTextField,
                mLowerCaseExtCheckBox
        );

        GridPane.setHgrow(mRefAbsolutePathRadioButton, Priority.ALWAYS);

        addTopMargin(
                mMaxWidthSpinner,
                mMaxHeightSpinner,
                mMaxHeightCheckBox,
                mRefAbsolutePathRadioButton,
                mRefAbsoluteRadioButton,
                mRefRelativeRadioButton,
                mRefThumbnailRadioButton,
                mRefAbsolutePathTextField,
                mLowerCaseExtCheckBox
        );

        addTopPadding(referenceLabel);

        var leftInsets = FxHelper.getUIScaledInsets(0, 0, 0, 24);
        GridPane.setMargin(mRefAbsolutePathTextField, leftInsets);

        mMaxHeightSpinner.disableProperty().bind(mMaxHeightCheckBox.selectedProperty().not());
        mMaxWidthSpinner.disableProperty().bind(mMaxWidthCheckBox.selectedProperty().not());
        mRefAbsolutePathTextField.disableProperty().bind(mRefAbsolutePathRadioButton.selectedProperty().not());
    }
}
