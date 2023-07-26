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
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javax.swing.SwingUtilities;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import se.trixon.almond.nbp.fx.FxDialogPanel;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.almond.util.swing.SwingHelper;
import se.trixon.mapollage.Mapollage;
import se.trixon.mapollage.Options;
import se.trixon.mapollage.core.Storage;
import se.trixon.mapollage.core.StorageManager;
import se.trixon.mapollage.core.Task;
import se.trixon.mapollage.core.TaskManager;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class MainPane extends BorderPane {

    private List<Action> mActions;
    private final ListView<Task> mListView = new ListView<>();
    private final Options mOptions = Options.getInstance();
    private final TaskManager mTaskManager = TaskManager.getInstance();

    public MainPane() {
        createUI();
    }

    private boolean confirm(String title, String header, String content, String buttonText) {
        var d = new DialogDescriptor(
                "%s\n%s".formatted(header, content),
                title,
                true,
                new Object[]{Dict.CANCEL.toString(), buttonText},
                buttonText,
                0,
                null,
                null
        );

        return buttonText == DialogDisplayer.getDefault().notify(d);
    }

    private void createUI() {
        final int size = Mapollage.getIconSizeToolBar();

        var addAction = new Action(Dict.ADD.toString(), actionEvent -> {
            edit(null);
        });
        addAction.setGraphic(MaterialIcon._Content.ADD.getImageView(size));

        var remAction = new Action(Dict.REMOVE.toString(), actionEvent -> {
            var baseTitle = Dict.Dialog.TITLE_REMOVE_S.toString().formatted(Dict.TASK.toString().toLowerCase(Locale.ENGLISH));
            var action = Dict.Dialog.TITLE_REMOVE_S.toString().formatted("'%s'".formatted(getSelected().getName()));
            var baseHeader = Dict.Dialog.YOU_ARE_ABOUT_TO_S.toString().formatted(action.toLowerCase(Locale.ENGLISH));

            if (confirm(baseTitle + "?",
                    baseHeader,
                    Dict.Dialog.ACTION_CANT_BE_UNDONE.toString(),
                    baseTitle)) {
                onRemove();
            }
        });
        remAction.setGraphic(MaterialIcon._Content.REMOVE.getImageView(size));

        var remAllAction = new Action(Dict.REMOVE_ALL.toString(), actionEvent -> {
            var baseTitle = Dict.Dialog.TITLE_REMOVE_ALL_S.toString().formatted(Dict.TASKS.toString().toLowerCase(Locale.ENGLISH));
            var action = Dict.Dialog.TITLE_REMOVE_ALL_S.toString().formatted(Dict.TASKS.toString().toLowerCase(Locale.ENGLISH));
            var baseHeader = Dict.Dialog.YOU_ARE_ABOUT_TO_S.toString().formatted(action.toLowerCase(Locale.ENGLISH));

            if (confirm(
                    baseTitle + "?",
                    baseHeader,
                    Dict.Dialog.ACTION_CANT_BE_UNDONE.toString(),
                    baseTitle)) {
                onRemoveAll();
            }
        });
        remAllAction.setGraphic(MaterialIcon._Content.CLEAR.getImageView(size));

        var editAction = new Action(Dict.EDIT.toString(), actionEvent -> {
            edit(getSelected());
        });
        editAction.setGraphic(MaterialIcon._Editor.MODE_EDIT.getImageView(size));

        var cloneAction = new Action(Dict.CLONE.toString(), actionEvent -> {
            onClone();
        });
        cloneAction.setGraphic(MaterialIcon._Content.CONTENT_COPY.getImageView(size));

        mActions = List.of(
                addAction,
                remAction,
                editAction,
                cloneAction,
                remAllAction
        );

        var nullSelectionBooleanBinding = mListView.getSelectionModel().selectedItemProperty().isNull();
        editAction.disabledProperty().bind(nullSelectionBooleanBinding);
        cloneAction.disabledProperty().bind(nullSelectionBooleanBinding);
        remAction.disabledProperty().bind(nullSelectionBooleanBinding);
        remAllAction.disabledProperty().bind(Bindings.isEmpty(mTaskManager.getItems()));

        mListView.itemsProperty().bind(mTaskManager.itemsProperty());
        var toolBar = ActionUtils.createToolBar(mActions, ActionUtils.ActionTextBehavior.HIDE);
        FxHelper.undecorateButtons(toolBar.getItems().stream());
        FxHelper.slimToolBar(toolBar);

        setTop(toolBar);
        setCenter(mListView);

        mListView.setCellFactory(listView -> new ItemListCellRenderer());
    }

    private void edit(Task item) {
        var title = item == null ? Dict.ADD.toString() : Dict.EDIT.toString();
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
                editor.load(item, d);
            });

            if (DialogDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(d)) {
                Platform.runLater(() -> {
                    var editedItem = editor.save();
                    mListView.getSelectionModel().select(mTaskManager.getById(editedItem.getId()));
                    mListView.requestFocus();
                });
            }
        });
    }

    private Task getSelected() {
        return mListView.getSelectionModel().getSelectedItem();
    }

    private void onClone() {
        var original = getSelected();
        var json = Storage.GSON.toJson(original);
        var clone = Storage.GSON.fromJson(json, original.getClass());
        var uuid = UUID.randomUUID().toString();
        clone.setId(uuid);
        clone.setName("%s %s".formatted(clone.getName(), LocalDate.now().toString()));
        mTaskManager.getIdToItem().put(clone.getId(), clone);

        StorageManager.save();

        mListView.getSelectionModel().select(mTaskManager.getById(uuid));
        mListView.requestFocus();
        edit(getSelected());
    }

    private void onRemove() {
        mTaskManager.getIdToItem().remove(getSelected().getId());
        StorageManager.save();
    }

    private void onRemoveAll() {
        mTaskManager.getIdToItem().clear();

        StorageManager.save();
    }

    public class ItemListCellRenderer extends ListCell<Task> {

        private final Font mDefaultFont = Font.getDefault();
        private final Label mDescLabel = new Label();
        private final Label mNameLabel = new Label();
        private final VBox mRoot = new VBox();

        public ItemListCellRenderer() {
            createUI();
        }

        @Override
        protected void updateItem(Task item, boolean empty) {
            super.updateItem(item, empty);

            if (item == null || empty) {
                clearContent();
            } else {
                addContent(item);
            }
        }

        private void addContent(Task item) {
            setText(null);

            mNameLabel.setText(item.getName());
            mDescLabel.setText(item.getDescription());
            mRoot.getChildren().setAll(mNameLabel, mDescLabel);
            mRoot.setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2) {
                }
            });
            setGraphic(mRoot);
        }

        private void clearContent() {
            setText(null);
            setGraphic(null);
        }

        private void createUI() {
            String fontFamily = mDefaultFont.getFamily();
            double fontSize = mDefaultFont.getSize();

            mNameLabel.setFont(Font.font(fontFamily, FontWeight.BOLD, fontSize * 1.4));
            mDescLabel.setFont(Font.font(fontFamily, FontWeight.NORMAL, fontSize * 1.1));
        }
    }
}
