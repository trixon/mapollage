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

import javafx.geometry.Insets;
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
import se.trixon.mapollage.profile.Profile;
import se.trixon.mapollage.profile.ProfilePhoto;

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

    public PhotoTab(Profile profile) {
        setText(Dict.PHOTO.toString());
        setGraphic(FontAwesome.Glyph.IMAGE.getChar());
        mProfile = profile;
        createUI();
        load();
    }

    @Override
    public void load() {
        ProfilePhoto p = mProfile.getPhoto();

        mMaxWidthCheckBox.setSelected(p.isLimitWidth());
        mMaxWidthSpinner.getValueFactory().setValue(p.getWidthLimit());
        mMaxHeightCheckBox.setSelected(p.isLimitHeight());
        mMaxHeightSpinner.getValueFactory().setValue(p.getHeightLimit());

        switch (p.getReference()) {
            case ABSOLUTE:
                mRefAbsoluteRadioButton.setSelected(true);
                break;

            case ABSOLUTE_PATH:
                mRefAbsolutePathRadioButton.setSelected(true);
                break;

            case RELATIVE:
                mRefRelativeRadioButton.setSelected(true);
                break;

            case THUMBNAIL:
                mRefThumbnailRadioButton.setSelected(true);
                break;

            default:
                throw new AssertionError();
        }

        mRefAbsolutePathTextField.setText(p.getBaseUrlValue());
        mLowerCaseExtCheckBox.setSelected(p.isForceLowerCaseExtension());
    }

    @Override
    public void save() {
        ProfilePhoto p = mProfile.getPhoto();

        p.setBaseUrlValue(mRefAbsolutePathTextField.getText());
        p.setForceLowerCaseExtension(mLowerCaseExtCheckBox.isSelected());
        p.setHeightLimit(mMaxHeightSpinner.getValue());
        p.setWidthLimit(mMaxWidthSpinner.getValue());
        p.setLimitHeight(mMaxHeightCheckBox.isSelected());
        p.setLimitWidth(mMaxWidthCheckBox.isSelected());

        if (mRefAbsolutePathRadioButton.isSelected()) {
            p.setReference(ProfilePhoto.Reference.ABSOLUTE_PATH);
        } else if (mRefAbsoluteRadioButton.isSelected()) {
            p.setReference(ProfilePhoto.Reference.ABSOLUTE);
        } else if (mRefRelativeRadioButton.isSelected()) {
            p.setReference(ProfilePhoto.Reference.RELATIVE);
        } else if (mRefThumbnailRadioButton.isSelected()) {
            p.setReference(ProfilePhoto.Reference.THUMBNAIL);
        }
    }

    private void createUI() {
        GridPane gp = new GridPane();
        setContent(gp);

        mMaxHeightSpinner.setEditable(true);
        mMaxWidthSpinner.setEditable(true);

        mRefAbsolutePathRadioButton.setToggleGroup(mToggleGroup);
        mRefAbsoluteRadioButton.setToggleGroup(mToggleGroup);
        mRefRelativeRadioButton.setToggleGroup(mToggleGroup);
        mRefThumbnailRadioButton.setToggleGroup(mToggleGroup);
        mLowerCaseExtCheckBox.setTooltip(new Tooltip(mBundle.getString("PhotoTab.lowerCaseExtCheckBox.toolTip")));

        Label referenceLabel = new Label(Dict.FILE_REFERENCE.toString());

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

        Insets leftInsets = new Insets(0, 0, 0, 24);
        GridPane.setMargin(mRefAbsolutePathTextField, leftInsets);

        mMaxHeightSpinner.disableProperty().bind(mMaxHeightCheckBox.selectedProperty().not());
        mMaxWidthSpinner.disableProperty().bind(mMaxWidthCheckBox.selectedProperty().not());
        mRefAbsolutePathTextField.disableProperty().bind(mRefAbsolutePathRadioButton.selectedProperty().not());
    }

}
