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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.validation.Validator;
import se.trixon.almond.util.Dict;
import se.trixon.mapollage.profile.Profile;
import se.trixon.mapollage.profile.ProfileFolder;
import se.trixon.mapollage.profile.ProfileFolder.FolderBy;
import static se.trixon.mapollage.ui.config.BaseTab.sValidationSupport;

/**
 *
 * @author Patrik Karlström
 */
public class FoldersTab extends BaseTab {

    private ComboBox<String> mDatePatternComboBox = new ComboBox<>();
    private ProfileFolder mFolder;
    private RadioButton mFolderByDateRadioButton = new RadioButton(Dict.DATE_PATTERN.toString());
    private RadioButton mFolderByDirectoryRadioButton = new RadioButton(mBundle.getString("ModuleFoldersPanel.folderByDirectoryRadioButton.text"));
    private RadioButton mFolderByNoneRadioButton = new RadioButton(mBundle.getString("ModuleFoldersPanel.folderByNoneRadioButton.text"));
    private RadioButton mFolderByRegexRadioButton = new RadioButton(mBundle.getString("ModuleFoldersPanel.folderByRegexRadioButton.text"));
    private TextField mRegexDefaultTextField = new TextField();
    private TextField mRegexTextField = new TextField();
    private TextArea mRootDescTextArea = new TextArea();
    private TextField mRootNameTextField = new TextField();
    private ToggleGroup mToggleGroup = new ToggleGroup();

    public FoldersTab(Profile profile) {
        setText(Dict.FOLDERS.toString());
        setGraphic(FontAwesome.Glyph.FOLDER_ALT.getChar());
        mProfile = profile;
        mFolder = mProfile.getFolder();
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
        mFolder.setRootName(mRootNameTextField.getText());
        mFolder.setRootDescription(mRootDescTextArea.getText());
        mFolder.setDatePattern(mDatePatternComboBox.getValue());
        mFolder.setRegex(mRegexTextField.getText());
        mFolder.setRegexDefault(mRegexDefaultTextField.getText());

        FolderBy folderBy = null;
        Toggle t = mToggleGroup.getSelectedToggle();
        if (t == mFolderByDirectoryRadioButton) {
            folderBy = FolderBy.DIR;
        } else if (t == mFolderByDateRadioButton) {
            folderBy = FolderBy.DATE;
        } else if (t == mFolderByRegexRadioButton) {
            folderBy = FolderBy.REGEX;
        } else if (t == mFolderByNoneRadioButton) {
            folderBy = FolderBy.NONE;
        }

        mFolder.setFoldersBy(folderBy);
    }

    private void createUI() {
        VBox leftBox = new VBox();
        VBox rightBox = new VBox();

        GridPane gridPane = new GridPane();
        gridPane.addRow(0, leftBox, rightBox);
        double width = 50;
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(width);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(width);
        gridPane.getColumnConstraints().addAll(col1, col2);
        GridPane.setMargin(leftBox, new Insets(0, 8, 0, 0));
        GridPane.setMargin(rightBox, new Insets(0, 0, 0, 8));
        GridPane.setFillHeight(leftBox, Boolean.TRUE);
        setContent(gridPane);

        //Left Pane
        Label rootNameLabel = new Label(mBundle.getString("ModuleFoldersPanel.rootNameLabel.text"));
        Label rootDescLabel = new Label(mBundle.getString("ModuleFoldersPanel.rootDescriptionLabel.text"));
        Label regexLabel = new Label(Dict.DEFAULT_VALUE.toString());

        leftBox.getChildren().addAll(
                rootNameLabel,
                mRootNameTextField,
                rootDescLabel,
                mRootDescTextArea
        );

        VBox.setVgrow(mRootDescTextArea, Priority.ALWAYS);

        //Right Pane
        mFolderByDirectoryRadioButton.setToggleGroup(mToggleGroup);
        mFolderByDateRadioButton.setToggleGroup(mToggleGroup);
        mFolderByRegexRadioButton.setToggleGroup(mToggleGroup);
        mFolderByNoneRadioButton.setToggleGroup(mToggleGroup);

        mDatePatternComboBox.setMaxWidth(Double.MAX_VALUE);
        mDatePatternComboBox.setEditable(true);
        mDatePatternComboBox.setItems(FXCollections.observableList(Arrays.asList(mBundle.getString("dateFormats").split(";"))));

        Label label = new Label(mBundle.getString("ModuleFoldersPanel.folderByLabel.text"));
        rightBox.getChildren().addAll(
                label,
                mFolderByDirectoryRadioButton,
                mFolderByDateRadioButton,
                mDatePatternComboBox,
                mFolderByRegexRadioButton,
                mRegexTextField,
                regexLabel,
                mRegexDefaultTextField,
                mFolderByNoneRadioButton
        );

        addTopPadding(
                rootDescLabel,
                mFolderByDirectoryRadioButton,
                mFolderByDateRadioButton,
                mFolderByRegexRadioButton,
                regexLabel,
                mFolderByNoneRadioButton
        );

        Insets leftInsets = new Insets(0, 0, 0, 24);
        VBox.setMargin(mDatePatternComboBox, leftInsets);
        VBox.setMargin(mRegexTextField, leftInsets);
        VBox.setMargin(mRegexDefaultTextField, leftInsets);
        VBox.setMargin(regexLabel, leftInsets);

        mDatePatternComboBox.disableProperty().bind(mFolderByDateRadioButton.selectedProperty().not());
        mRegexTextField.disableProperty().bind(mFolderByRegexRadioButton.selectedProperty().not());
        mRegexDefaultTextField.disableProperty().bind(mFolderByRegexRadioButton.selectedProperty().not());
    }

    private void initValidation() {
        final String message = "Text is required";
        boolean indicateRequired = false;

        Predicate datePredicate = (Predicate) (Object o) -> {
            if (!mFolderByDateRadioButton.isSelected()) {
                return true;
            } else {
                return !StringUtils.isBlank((String) o) && previewDateFormat();
            }
        };

        Predicate regexPredicate = (Predicate) (Object o) -> {
            if (!mFolderByRegexRadioButton.isSelected()) {
                return true;
            } else {
                try {
                    final String s = (String) o;
                    Pattern p = Pattern.compile(s);
                    return !StringUtils.isBlank(s);
                } catch (PatternSyntaxException e) {
                }
                return false;
            }
        };

        Predicate regexDefaultPredicate = (Predicate) (Object o) -> {
            if (!mFolderByRegexRadioButton.isSelected()) {
                return true;
            } else {
                final String s = (String) o;
                return !StringUtils.isBlank(s);
            }
        };

        sValidationSupport.registerValidator(mRootNameTextField, indicateRequired, Validator.createEmptyValidator(message));
        sValidationSupport.registerValidator(mDatePatternComboBox, indicateRequired, Validator.createPredicateValidator(datePredicate, message));
        sValidationSupport.registerValidator(mRegexTextField, indicateRequired, Validator.createPredicateValidator(regexPredicate, message));
        sValidationSupport.registerValidator(mRegexDefaultTextField, indicateRequired, Validator.createPredicateValidator(regexDefaultPredicate, message));
    }

    private void load() {
        mRootNameTextField.setText(mFolder.getRootName());
        mRootDescTextArea.setText(mFolder.getRootDescription());
        mDatePatternComboBox.setValue(mFolder.getDatePattern());
        mRegexTextField.setText(mFolder.getRegex());
        mRegexDefaultTextField.setText(mFolder.getRegexDefault());

        RadioButton folderByRadioButton;

        switch (mFolder.getFoldersBy()) {
            case DIR:
                folderByRadioButton = mFolderByDirectoryRadioButton;
                break;

            case DATE:
                folderByRadioButton = mFolderByDateRadioButton;
                break;

            case REGEX:
                folderByRadioButton = mFolderByRegexRadioButton;
                break;

            default:
                folderByRadioButton = mFolderByNoneRadioButton;
                break;
        }

        mToggleGroup.selectToggle(folderByRadioButton);
    }

    private boolean previewDateFormat() {
        boolean validFormat = true;
        String datePreview;

        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(mDatePatternComboBox.getValue(), mOptions.getLocale());
            datePreview = simpleDateFormat.format(new Date(System.currentTimeMillis()));
        } catch (IllegalArgumentException ex) {
            datePreview = Dict.Dialog.ERROR.toString();
            validFormat = false;
        }

        String dateLabel = String.format("%s (%s)", Dict.DATE_PATTERN.toString(), datePreview);
        mFolderByDateRadioButton.setText(dateLabel);
        mFolderByDateRadioButton.setTooltip(new Tooltip(datePreview));

        return validFormat;
    }
}
