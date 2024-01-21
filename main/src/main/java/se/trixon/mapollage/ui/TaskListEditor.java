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
import se.trixon.mapollage.core.ExecutorManager;
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
    private final ExecutorManager mExecutorManager = ExecutorManager.getInstance();
    private final TaskEditor mTaskEditor;
    private final FxDialogPanel mDialogPanel;

    public TaskListEditor() {
        init();

        mTaskEditor = new TaskEditor();
        mTaskEditor.setPadding(FxHelper.getUIScaledInsets(8, 8, 0, 8));

        mDialogPanel = new FxDialogPanel() {
            @Override
            protected void fxConstructor() {
                setScene(new Scene(mTaskEditor));
            }
        };

        mDialogPanel.setPreferredSize(SwingHelper.getUIScaledDim(640, 480));
        mDialogPanel.initFx();
    }

    public EditableList<Task> getEditableList() {
        return mEditableList;
    }

    private void editTask(String title, Task task) {
        var d = new DialogDescriptor(mDialogPanel, title);
        d.setValid(false);
        mDialogPanel.setNotifyDescriptor(d);
        mTaskEditor.load(task, d);

        SwingUtilities.invokeLater(() -> {
            if (DialogDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(d)) {
                Platform.runLater(() -> {
                    var editedItem = mTaskEditor.save();
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
                    mExecutorManager.requestStart(task);
                })
                //                .setOnSelect((Task o, Task n) -> {
                //                    var io = IOProvider.getDefault().getIO(Dict.INFORMATION.toString(), false);
                //                    try {
                //                        io.getOut().reset();
                //                    } catch (IOException ex) {
                //                        Exceptions.printStackTrace(ex);
                //                    }
                //                    if (n != null) {
                //                        io.getOut().println(n.toInfoString());
                //                    }
                //                    io.getOut().close();
                //                })
                .build();

        mEditableList.getListView().setCellFactory(listView -> new TaskListCell());
    }

    private void postEdit(Task task) {
        mEditableList.postEdit(task);
    }

}
