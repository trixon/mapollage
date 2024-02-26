/*
 * Copyright 2023 Patrik Karlström <patrik@trixon.se>.
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
package se.trixon.mapollage.ui;

import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.editable_list.EditableListCell;
import se.trixon.mapollage.core.ExecutorManager;
import se.trixon.mapollage.core.Task;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class TaskListCell extends EditableListCell<Task> {

    private final Label mDescLabel = new Label();
    private final TaskListEditor mEditor;
    private final Label mNameLabel = new Label();
    private final VBox mRoot = new VBox();

    public TaskListCell(TaskListEditor editor) {
        mEditor = editor;
        createUI();
    }

    @Override
    protected void updateItem(Task task, boolean empty) {
        super.updateItem(task, empty);
        if (task == null || empty) {
            clearContent();
        } else {
            addContent(task);
        }
    }

    private void addContent(Task task) {
        setText(null);
        mNameLabel.setText(task.getName());
        mDescLabel.setText(task.getDescriptionString());
        mRoot.getChildren().setAll(mNameLabel, mDescLabel);
        mRoot.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2) {
                if (mouseEvent.isControlDown()) {
                    mEditor.editTask(null, task);
                } else if (mouseEvent.isShiftDown()) {
                    try {
                        SystemHelper.desktopOpenOrElseParent(task.getDestinationFile());
                    } catch (Exception e) {
                        // nvm
                    }
                } else {
                    ExecutorManager.getInstance().requestStart(task);
                }
            }
        });
        setGraphic(mRoot);
    }

    private void clearContent() {
        setText(null);
        setGraphic(null);
    }

    private void createUI() {
        var fontSize = FxHelper.getScaledFontSize();
        var fontStyle = "-fx-font-size: %.0fpx; -fx-font-weight: %s;";

        mNameLabel.setStyle(fontStyle.formatted(fontSize * 1.4, "bold"));
        mDescLabel.setStyle(fontStyle.formatted(fontSize * 1.1, "normal"));
    }

}
