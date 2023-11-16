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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.validation.Validator;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.UriLabel;
import se.trixon.mapollage.core.Task;
import se.trixon.mapollage.core.TaskFolder.FolderBy;

/**
 *
 * @author Patrik Karlström
 */
public class FoldersTab extends BaseTab {

    private final UriLabel mDateFormatUriLabel = new UriLabel(Dict.PATTERNS.toString());
    private final ComboBox<String> mDatePatternComboBox = new ComboBox<>();
    private final RadioButton mFolderByDateRadioButton = new RadioButton(Dict.DATE_PATTERN.toString());
    private final RadioButton mFolderByDirectoryRadioButton = new RadioButton(mBundle.getString("FoldersTab.folderByDirectoryRadioButton"));
    private final RadioButton mFolderByNoneRadioButton = new RadioButton(mBundle.getString("FoldersTab.folderByNoneRadioButton"));
    private final RadioButton mFolderByRegexRadioButton = new RadioButton(mBundle.getString("FoldersTab.folderByRegexRadioButton"));
    private final TextField mRegexDefaultTextField = new TextField();
    private final TextField mRegexTextField = new TextField();
    private final TextArea mRootDescTextArea = new TextArea();
    private final TextField mRootNameTextField = new TextField();
    private final ToggleGroup mToggleGroup = new ToggleGroup();

    public FoldersTab() {
        setText(Dict.FOLDERS.toString());
        setGraphic(FontAwesome.Glyph.FOLDER_ALT.getChar());
        createUI();
        initValidation();
    }

    @Override
    public void load(Task task) {
        mTask = task;

        var taskFolder = mTask.getFolder();
        mRootNameTextField.setText(taskFolder.getRootName());
        mRootDescTextArea.setText(taskFolder.getRootDescription());
        mDatePatternComboBox.setValue(taskFolder.getDatePattern());
        mRegexTextField.setText(taskFolder.getRegex());
        mRegexDefaultTextField.setText(taskFolder.getRegexDefault());

        RadioButton folderByRadioButton;

        folderByRadioButton = switch (taskFolder.getFoldersBy()) {
            case DIR ->
                mFolderByDirectoryRadioButton;
            case DATE ->
                mFolderByDateRadioButton;
            case REGEX ->
                mFolderByRegexRadioButton;
            default ->
                mFolderByNoneRadioButton;
        };

        mToggleGroup.selectToggle(folderByRadioButton);
    }

    @Override
    public void save() {
        var taskFolder = mTask.getFolder();
        taskFolder.setRootName(mRootNameTextField.getText());
        taskFolder.setRootDescription(mRootDescTextArea.getText());
        taskFolder.setDatePattern(mDatePatternComboBox.getValue());
        taskFolder.setRegex(mRegexTextField.getText());
        taskFolder.setRegexDefault(mRegexDefaultTextField.getText());

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

        taskFolder.setFoldersBy(folderBy);
    }

    private void createUI() {
        mDateFormatUriLabel.setUri("https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/text/SimpleDateFormat.html");
        var leftBox = new VBox();
        var rightBox = new VBox();

        setContent(leftBox);

        //Left Pane
        var rootNameLabel = new Label(mBundle.getString("FoldersTab.rootNameLabel"));
        var rootDescLabel = new Label(mBundle.getString("FoldersTab.rootDescriptionLabel"));
        var regexLabel = new Label(Dict.DEFAULT_VALUE.toString());

        leftBox.getChildren().addAll(
                rootNameLabel,
                mRootNameTextField,
                rootDescLabel,
                mRootDescTextArea,
                rightBox
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

        var label = new Label(mBundle.getString("FoldersTab.folderByLabel"));
        rightBox.getChildren().addAll(
                label,
                mFolderByDirectoryRadioButton,
                new HBox(FxHelper.getUIScaled(8), mFolderByDateRadioButton, mDateFormatUriLabel),
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
                mDateFormatUriLabel,
                mFolderByRegexRadioButton,
                regexLabel,
                mFolderByNoneRadioButton,
                rightBox
        );

        var leftInsets = FxHelper.getUIScaledInsets(0, 0, 0, 24);
        VBox.setMargin(mDatePatternComboBox, leftInsets);
        VBox.setMargin(mRegexTextField, leftInsets);
        VBox.setMargin(mRegexDefaultTextField, leftInsets);
        VBox.setMargin(regexLabel, leftInsets);

        mDatePatternComboBox.disableProperty().bind(mFolderByDateRadioButton.selectedProperty().not());
        mRegexTextField.disableProperty().bind(mFolderByRegexRadioButton.selectedProperty().not());
        mRegexDefaultTextField.disableProperty().bind(mFolderByRegexRadioButton.selectedProperty().not());

//        EventHandler eventHandler = (EventHandler) (Event event) -> {
////            sValidationSupport.initInitialDecoration();
//            sValidationSupport.getValidationDecorator().removeDecorations(mDatePatternComboBox);
//
//            sValidationSupport.redecorate();
//            System.out.println(event);
//        };
//
//        mFolderByDateRadioButton.setOnAction(eventHandler);
//        mFolderByDirectoryRadioButton.setOnAction(eventHandler);
//        mFolderByNoneRadioButton.setOnAction(eventHandler);
//        mFolderByRegexRadioButton.setOnAction(eventHandler);
    }

    private void initValidation() {
        final String message = "Text is required";
        boolean indicateRequired = false;

        var datePredicate = (Predicate<String>) s -> {
            if (!mFolderByDateRadioButton.isSelected()) {
                return true;
            } else {
                return !StringUtils.isBlank(s) && previewDateFormat();
            }
        };

        var regexPredicate = (Predicate<String>) s -> {
            if (!mFolderByRegexRadioButton.isSelected()) {
                return true;
            } else {
                try {
                    Pattern.compile(s);
                    return !StringUtils.isBlank(s);
                } catch (PatternSyntaxException e) {
                    return false;
                }
            }
        };

        var regexDefaultPredicate = (Predicate<String>) s -> {
            if (!mFolderByRegexRadioButton.isSelected()) {
                return true;
            } else {
                return !StringUtils.isBlank(s);
            }
        };

        sValidationSupport.registerValidator(mRootNameTextField, indicateRequired, Validator.createEmptyValidator(message));
        sValidationSupport.registerValidator(mDatePatternComboBox, indicateRequired, Validator.createPredicateValidator(datePredicate, message));
        sValidationSupport.registerValidator(mRegexTextField, indicateRequired, Validator.createPredicateValidator(regexPredicate, message));
        sValidationSupport.registerValidator(mRegexDefaultTextField, indicateRequired, Validator.createPredicateValidator(regexDefaultPredicate, message));
    }

    private boolean previewDateFormat() {
        boolean validFormat = true;
        String datePreview;

        try {
            var simpleDateFormat = new SimpleDateFormat(mDatePatternComboBox.getValue(), mOptions.getLocale());
            datePreview = simpleDateFormat.format(new Date(System.currentTimeMillis()));
        } catch (IllegalArgumentException ex) {
            datePreview = Dict.Dialog.ERROR.toString();
            validFormat = false;
        }

        var dateLabel = String.format("%s (%s)", Dict.DATE_PATTERN.toString(), datePreview);
        mFolderByDateRadioButton.setText(dateLabel);
        mFolderByDateRadioButton.setTooltip(new Tooltip(datePreview));

        return validFormat;
    }
}
