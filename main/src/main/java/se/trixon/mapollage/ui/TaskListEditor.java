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

import java.time.LocalDate;
import java.util.UUID;
import javafx.application.Platform;
import javafx.scene.Scene;
import javax.swing.SwingUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import se.trixon.almond.nbp.fx.FxDialogPanel;
import se.trixon.almond.nbp.fx.NbEditableList;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.editable_list.EditableList;
import se.trixon.almond.util.swing.SwingHelper;
import se.trixon.mapollage.core.StorageManager;
import static se.trixon.mapollage.core.StorageManager.GSON;
import se.trixon.mapollage.core.Task;
import se.trixon.mapollage.core.TaskManager;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class TaskListEditor {

    private EditableList<Task> mEditableList;
    private final TaskManager mTaskManager = TaskManager.getInstance();

    public TaskListEditor() {
        init();
    }

    public EditableList<Task> getEditableList() {
        return mEditableList;
    }

    private void editTask(String title, Task task) {
        var editor = new TaskEditor();
        editor.setPadding(FxHelper.getUIScaledInsets(8, 8, 0, 8));
        var dialogPanel = new FxDialogPanel() {
            @Override
            protected void fxConstructor() {
                setScene(new Scene(editor));
            }
        };
        dialogPanel.setPreferredSize(SwingHelper.getUIScaledDim(600, 480));

        SwingUtilities.invokeLater(() -> {
            editor.setPrefSize(FxHelper.getUIScaled(600), FxHelper.getUIScaled(660));
            var d = new DialogDescriptor(dialogPanel, title);
            d.setValid(false);
            dialogPanel.setNotifyDescriptor(d);
            dialogPanel.initFx(() -> {
                editor.load(task, d);
            });

            if (DialogDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(d)) {
                Platform.runLater(() -> {
                    var editedItem = editor.save();
                    postEdit(mTaskManager.getById(editedItem.getId()));
                });
            }
        });
    }

    private void init() {
        mEditableList = new NbEditableList.Builder<Task>()
                .setItemSingular(Dict.TASK.toString())
                .setItemPlural(Dict.TASKS.toString())
                .setItemsProperty(mTaskManager.itemsProperty())
                .setOnEdit((title, task) -> {
                    editTask(title, task);
                })
                .setOnRemoveAll(() -> {
                    mTaskManager.getIdToItem().clear();
                    StorageManager.save();
                })
                .setOnRemove(t -> {
                    mTaskManager.getIdToItem().remove(t.getId());
                    StorageManager.save();
                })
                .setOnClone(t -> {
                    var original = t;
                    var json = GSON.toJson(original);
                    var clone = GSON.fromJson(json, original.getClass());
                    var uuid = UUID.randomUUID().toString();
                    clone.setId(uuid);
                    clone.setLastRun(0);
                    clone.setName("%s %s".formatted(clone.getName(), LocalDate.now().toString()));
                    mTaskManager.getIdToItem().put(clone.getId(), clone);

                    StorageManager.save();

                    return mTaskManager.getById(uuid);
                })
                .setOnStart(task -> {
                    //mExecutorManager.requestStart(task);
                })
                .build();

        mEditableList.getListView().setCellFactory(listView -> new TaskListCell());
    }

    private void postEdit(Task task) {
        mEditableList.postEdit(task);
    }

}
