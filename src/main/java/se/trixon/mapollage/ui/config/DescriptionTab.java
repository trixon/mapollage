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

import javafx.beans.property.BooleanProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.controlsfx.glyphfont.FontAwesome;
import se.trixon.almond.util.Dict;
import se.trixon.mapollage.profile.Profile;
import se.trixon.mapollage.profile.ProfileDescription;
import se.trixon.mapollage.profile.ProfileDescription.DescriptionMode;

/**
 *
 * @author Patrik Karlström
 */
public class DescriptionTab extends BaseTab {

    private RadioButton mCustomRadioButton = new RadioButton(Dict.CUSTOMIZED.toString());
    private Button mCustomResetButton = new Button(Dict.RESET.toString());
    private TextArea mCustomTextArea = new TextArea();
    private ToggleGroup mDefaultToggleGroup = new ToggleGroup();
    private RadioButton mExternalCustomRadioButton = new RadioButton(Dict.CUSTOMIZED.toString());
    private CheckBox mExternalDefaultCheckBox = new CheckBox(mBundle.getString("ModuleDescriptionPanel.defaultToCheckBox.text"));
    private RadioButton mExternalRadioButton = new RadioButton(Dict.EXTERNAL_FILE.toString());
    private RadioButton mExternalStaticRadioButton = new RadioButton(Dict.STATIC.toString());
    private TextField mExternalTextField = new TextField("descriptions.txt");
    private ToggleGroup mSourceToggleGroup = new ToggleGroup();
    private CheckBox mStaticAltitudeCheckBox = new CheckBox(Dict.ALTITUDE.toString());
    private CheckBox mStaticBearingCheckBox = new CheckBox(Dict.BEARING.toString());
    private CheckBox mStaticCoordinateCheckBox = new CheckBox(Dict.COORDINATE.toString());
    private CheckBox mStaticDateCheckBox = new CheckBox(Dict.DATE.toString());
    private CheckBox mStaticFilenameCheckBox = new CheckBox(Dict.FILENAME.toString());
    private CheckBox mStaticPhotoCheckBox = new CheckBox(Dict.PHOTO.toString());
    private RadioButton mStaticRadioButton = new RadioButton(Dict.STATIC.toString());

    public DescriptionTab(Profile profile) {
        setText(Dict.DESCRIPTION.toString());
        setGraphic(FontAwesome.Glyph.COMMENT_ALT.getChar());
        mProfile = profile;
        createUI();
        initListeners();
        load();
    }

    @Override
    public boolean hasValidSettings() {
        return true;
    }

    @Override
    public void save() {
        ProfileDescription p = mProfile.getDescription();

        p.setAltitude(mStaticAltitudeCheckBox.isSelected());
        p.setBearing(mStaticBearingCheckBox.isSelected());
        p.setCoordinate(mStaticCoordinateCheckBox.isSelected());
        p.setFilename(mStaticFilenameCheckBox.isSelected());
        p.setPhoto(mStaticPhotoCheckBox.isSelected());
        p.setDate(mStaticDateCheckBox.isSelected());

        p.setDefaultTo(mExternalDefaultCheckBox.isSelected());

        if (mStaticRadioButton.isSelected()) {
            p.setMode(DescriptionMode.STATIC);
        } else if (mCustomRadioButton.isSelected()) {
            p.setMode(DescriptionMode.CUSTOM);
        } else if (mExternalRadioButton.isSelected()) {
            p.setMode(DescriptionMode.EXTERNAL);
        }

        if (mExternalCustomRadioButton.isSelected()) {
            p.setDefaultMode(DescriptionMode.CUSTOM);
        } else {
            p.setDefaultMode(DescriptionMode.STATIC);
        }
    }

    private void createUI() {
        VBox staticBox = new VBox(8);
        GridPane gp = new GridPane();
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

        BooleanProperty staticSelect = mStaticRadioButton.selectedProperty();
        staticBox.disableProperty().bind(staticSelect.not());

        BooleanProperty customSelect = mCustomRadioButton.selectedProperty();
        mCustomResetButton.disableProperty().bind(customSelect.not());
        mCustomTextArea.disableProperty().bind(customSelect.not());

        BooleanProperty externalSelect = mExternalRadioButton.selectedProperty();
        mExternalDefaultCheckBox.disableProperty().bind(externalSelect.not());
        mExternalTextField.disableProperty().bind(externalSelect.not());
        BooleanProperty externalDefault = mExternalDefaultCheckBox.selectedProperty();
        mExternalCustomRadioButton.disableProperty().bind(externalSelect.not().or(externalDefault.not()));
        mExternalStaticRadioButton.disableProperty().bind(externalSelect.not().or(externalDefault.not()));

        int col = 0;
        int row = 0;

        gp.add(mStaticRadioButton, col, row, GridPane.REMAINING, 1);
        gp.add(staticBox, col, ++row, GridPane.REMAINING, 1);
        gp.addRow(++row, new HBox(8, mCustomRadioButton, mCustomResetButton));
        gp.add(mCustomTextArea, col, ++row, GridPane.REMAINING, 1);
        gp.addRow(++row, mExternalRadioButton, mExternalDefaultCheckBox, mExternalStaticRadioButton, mExternalCustomRadioButton);
        gp.add(mExternalTextField, col, ++row, GridPane.REMAINING, 1);

        //gp.setBackground(FxHelper.createBackground(Color.BISQUE));
        GridPane.setHgrow(mExternalRadioButton, Priority.ALWAYS);

        Insets topInsets = new Insets(8, 0, 0, 0);
        GridPane.setMargin(mCustomTextArea, topInsets);
        GridPane.setMargin(mExternalTextField, topInsets);
        GridPane.setMargin(mExternalDefaultCheckBox, topInsets);
        GridPane.setMargin(mCustomResetButton, topInsets);
        GridPane.setMargin(mExternalStaticRadioButton, new Insets(0, 8, 0, 8));

        addTopPadding(
                staticBox,
                mCustomRadioButton,
                mExternalRadioButton,
                mExternalCustomRadioButton,
                mExternalStaticRadioButton
        );
    }

    private void initListeners() {
        mCustomResetButton.setOnAction((ActionEvent event) -> {
            mCustomTextArea.setText(ProfileDescription.getDefaultCustomValue());
        });
    }

    private void load() {
        ProfileDescription p = mProfile.getDescription();

        switch (p.getMode()) {
            case STATIC:
                mStaticRadioButton.setSelected(true);
                break;

            case CUSTOM:
                mCustomRadioButton.setSelected(true);
                break;

            case EXTERNAL:
                mExternalRadioButton.setSelected(true);
                break;

            default:
                throw new AssertionError();
        }

        if (p.getDefaultMode() == DescriptionMode.CUSTOM) {
            mExternalCustomRadioButton.setSelected(true);
        } else {
            mExternalStaticRadioButton.setSelected(true);
        }

        mStaticAltitudeCheckBox.setSelected(p.hasAltitude());
        mStaticBearingCheckBox.setSelected(p.hasBearing());
        mStaticCoordinateCheckBox.setSelected(p.hasCoordinate());
        mStaticDateCheckBox.setSelected(p.hasDate());
        mStaticFilenameCheckBox.setSelected(p.hasFilename());
        mStaticPhotoCheckBox.setSelected(p.hasPhoto());

        mCustomTextArea.setText(p.getCustomValue());
        mExternalTextField.setText(p.getExternalFileValue());
        mExternalDefaultCheckBox.setSelected(p.isDefaultTo());
    }
}
