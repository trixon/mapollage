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

import java.util.function.Predicate;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javax.swing.JFileChooser;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.validation.Validator;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.FileChooserPaneSwingFx;
import se.trixon.mapollage.core.Task;

/**
 *
 * @author Patrik Karlström
 */
public class SourceTab extends BaseTab {

    private final TextArea mDescTextArea = new TextArea();

    private final TextField mExcludeTextField = new TextField();
    private final TextField mFilePatternField = new TextField();
    private final CheckBox mIncludeCheckBox = new CheckBox(mBundle.getString("SourceTab.includeNullCoordinateCheckBox"));
    private final CheckBox mLinksCheckBox = new CheckBox(Dict.FOLLOW_LINKS.toString());
    private final TextField mNameTextField = new TextField();
    private final CheckBox mRecursiveCheckBox = new CheckBox(Dict.SUBDIRECTORIES.toString());
    private final FileChooserPaneSwingFx mSourceChooser = new FileChooserPaneSwingFx(Dict.SELECT.toString(), Dict.IMAGE_DIRECTORY.toString(), Almond.getFrame(), JFileChooser.DIRECTORIES_ONLY);

    public SourceTab() {
        setText(Dict.SOURCE.toString());
        setGraphic(FontAwesome.Glyph.FILE_IMAGE_ALT.getChar());

        createUI();
    }

    @Override
    public void load(Task task) {
        mTask = task;
        initValidation();
        var taskSource = mTask.getSource();

        mNameTextField.setText(mTask.getName());
        mDescTextArea.setText(mTask.getDescriptionString());

        mSourceChooser.setPath(taskSource.getDir());
        mExcludeTextField.setText(taskSource.getExcludePattern());
        mFilePatternField.setText(taskSource.getFilePattern());

        mRecursiveCheckBox.setSelected(taskSource.isRecursive());
        mLinksCheckBox.setSelected(taskSource.isFollowLinks());
        mIncludeCheckBox.setSelected(taskSource.isIncludeNullCoordinate());

        mNameTextField.requestFocus();
    }

    @Override
    public void save() {
        mTask.setName(mNameTextField.getText());
        mTask.setDescriptionString(mDescTextArea.getText());

        var taskSource = mTask.getSource();
        taskSource.setDir(mSourceChooser.getPath());
        taskSource.setExcludePattern(mExcludeTextField.getText());
        taskSource.setFilePattern(mFilePatternField.getText());

        taskSource.setRecursive(mRecursiveCheckBox.isSelected());
        taskSource.setFollowLinks(mLinksCheckBox.isSelected());
        taskSource.setIncludeNullCoordinate(mIncludeCheckBox.isSelected());
    }

    private void createUI() {
        var nameLabel = new Label(Dict.NAME.toString());
        var descLabel = new Label(Dict.DESCRIPTION.toString());
        var filePatternLabel = new Label(Dict.FILE_PATTERN.toString());
        var excludeLabel = new Label(mBundle.getString("SourceTab.excludeLabel"));

        mExcludeTextField.setTooltip(new Tooltip(mBundle.getString("SourceTab.excludeTextField.toolTip")));

        var hBox = new HBox(FxHelper.getUIScaled(8), mRecursiveCheckBox, mLinksCheckBox, mIncludeCheckBox);

        var gp = new GridPane(FxHelper.getUIScaled(8), FxHelper.getUIScaled(2));
        addTopPadding(
                descLabel,
                mSourceChooser,
                gp
        );

        FxHelper.setPadding(FxHelper.getUIScaledInsets(16, 0, 0, 0), hBox);

        gp.addRow(0, filePatternLabel, excludeLabel);
        gp.addRow(1, mFilePatternField, mExcludeTextField);

        var vBox = new VBox(
                nameLabel,
                mNameTextField,
                descLabel,
                mDescTextArea,
                mSourceChooser,
                gp,
                hBox
        );

        FxHelper.autoSizeColumn(gp, 2);
        VBox.setVgrow(mDescTextArea, Priority.ALWAYS);

        setContent(vBox);
    }

    private void initValidation() {
        final String message = "Text is required";
        boolean indicateRequired = false;

        var namePredicate = (Predicate<String>) s -> {
            return mTaskManager.isValid(mTask.getName(), s);
        };

        sValidationSupport.registerValidator(mNameTextField, indicateRequired, Validator.createEmptyValidator(message));
        sValidationSupport.registerValidator(mNameTextField, indicateRequired, Validator.createPredicateValidator(namePredicate, message));
        sValidationSupport.registerValidator(mDescTextArea, indicateRequired, Validator.createEmptyValidator(message));
        sValidationSupport.registerValidator(mSourceChooser.getTextField(), indicateRequired, Validator.createEmptyValidator(message));
        sValidationSupport.registerValidator(mFilePatternField, indicateRequired, Validator.createEmptyValidator(message));
    }

}
