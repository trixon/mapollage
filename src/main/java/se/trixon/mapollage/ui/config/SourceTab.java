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

import java.util.function.Predicate;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.validation.Validator;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.control.FileChooserPane;
import se.trixon.mapollage.profile.Profile;
import se.trixon.mapollage.profile.ProfileSource;

/**
 *
 * @author Patrik Karlström
 */
public class SourceTab extends BaseTab {

    private TextField mDescTextField;
    private TextField mExcludeTextField;
    private TextField mFilePatternField;
    private CheckBox mIncludeCheckBox;
    private CheckBox mLinksCheckBox;
    private TextField mNameTextField;
    private CheckBox mRecursiveCheckBox;
    private ProfileSource mSource;
    private FileChooserPane mSourceChooser;
    private final HBox mhBox = new HBox(8);
    private final VBox mvBox = new VBox();

    public SourceTab(Profile profile) {
        setText(Dict.SOURCE.toString());
        setGraphic(FontAwesome.Glyph.FILE_IMAGE_ALT.getChar());
        mProfile = profile;
        mSource = mProfile.getSource();

        createUI();
        initValidation();
        load();
    }

    @Override
    public boolean hasValidSettings() {
        return true;
    }

    @Override
    public void save() {
        mProfile.setName(mNameTextField.getText());
        mProfile.setDescriptionString(mDescTextField.getText());

        mSource.setDir(mSourceChooser.getPath());
        mSource.setExcludePattern(mExcludeTextField.getText());
        mSource.setFilePattern(mFilePatternField.getText());

        mSource.setRecursive(mRecursiveCheckBox.isSelected());
        mSource.setFollowLinks(mLinksCheckBox.isSelected());
        mSource.setIncludeNullCoordinate(mIncludeCheckBox.isSelected());
    }

    private void createUI() {
        mSourceChooser = new FileChooserPane(Dict.SELECT.toString(), Dict.IMAGE_DIRECTORY.toString(), FileChooserPane.ObjectMode.DIRECTORY, SelectionMode.SINGLE);
        Label nameLabel = new Label(Dict.NAME.toString());
        Label descLabel = new Label(Dict.DESCRIPTION.toString());
        Label filePatternLabel = new Label(Dict.FILE_PATTERN.toString());
        Label excludeLabel = new Label(mBundle.getString("ModuleSourcePanel.excludeLabel.text"));

        mNameTextField = new TextField();
        mDescTextField = new TextField();
        mFilePatternField = new TextField();
        mExcludeTextField = new TextField();
        mExcludeTextField.setTooltip(new Tooltip(mBundle.getString("ModuleSourcePanel.excludeTextField.toolTipText")));

        mRecursiveCheckBox = new CheckBox(Dict.SUBDIRECTORIES.toString());
        mLinksCheckBox = new CheckBox(Dict.FOLLOW_LINKS.toString());
        mIncludeCheckBox = new CheckBox(mBundle.getString("ModuleSourcePanel.includeNullCoordinateCheckBox.text"));

        mhBox.getChildren().addAll(mRecursiveCheckBox, mLinksCheckBox, mIncludeCheckBox);

        addTopPadding(
                descLabel,
                mSourceChooser,
                filePatternLabel,
                excludeLabel,
                mhBox
        );

        mvBox.getChildren().addAll(
                nameLabel,
                mNameTextField,
                descLabel,
                mDescTextField,
                mSourceChooser,
                filePatternLabel,
                mFilePatternField,
                excludeLabel,
                mExcludeTextField,
                mhBox
        );

        setContent(mvBox);
    }

    private void initValidation() {
        final String message = "Text is required";
        boolean indicateRequired = false;

        Predicate namePredicate = (Predicate) (Object o) -> {
            return mProfileManager.isValid(mProfile.getName(), (String) o);
        };

        sValidationSupport.registerValidator(mNameTextField, indicateRequired, Validator.createEmptyValidator(message));
        sValidationSupport.registerValidator(mNameTextField, indicateRequired, Validator.createPredicateValidator(namePredicate, message));
        sValidationSupport.registerValidator(mDescTextField, indicateRequired, Validator.createEmptyValidator(message));
        sValidationSupport.registerValidator(mSourceChooser.getTextField(), indicateRequired, Validator.createEmptyValidator(message));
        sValidationSupport.registerValidator(mFilePatternField, indicateRequired, Validator.createEmptyValidator(message));
    }

    private void load() {
        mNameTextField.setText(mProfile.getName());
        mDescTextField.setText(mProfile.getDescriptionString());

        mSourceChooser.setPath(mSource.getDir());
        mExcludeTextField.setText(mSource.getExcludePattern());
        mFilePatternField.setText(mSource.getFilePattern());

        mRecursiveCheckBox.setSelected(mSource.isRecursive());
        mLinksCheckBox.setSelected(mSource.isFollowLinks());
        mIncludeCheckBox.setSelected(mSource.isIncludeNullCoordinate());
    }
}
