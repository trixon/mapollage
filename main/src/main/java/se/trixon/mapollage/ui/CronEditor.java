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

import java.util.List;
import java.util.Locale;
import javafx.application.Platform;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javax.swing.SwingUtilities;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import se.trixon.almond.nbp.dialogs.NbCronPanel;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.mapollage.Mapollage;
import se.trixon.mapollage.core.Task;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class CronEditor extends BorderPane {

    private List<Action> mActions;
    private final CheckBox mCheckBox = new CheckBox(Dict.SCHEDULE.toString());
    private final ListView<String> mListView = new ListView<>();

    public CronEditor() {
        createUI();
    }

    void load(Task item) {
    }

    private void createUI() {
        final int size = Mapollage.getIconSizeToolBar();

        var addAction = new Action(Dict.ADD.toString(), actionEvent -> {
            edit(null);
        });
        addAction.setGraphic(MaterialIcon._Content.ADD.getImageView(size));

        var remAction = new Action(Dict.REMOVE.toString(), actionEvent -> {
            var baseTitle = Dict.Dialog.TITLE_REMOVE_S.toString().formatted(Dict.TASK.toString().toLowerCase(Locale.ENGLISH));
//            var action = Dict.Dialog.TITLE_REMOVE_S.toString().formatted("'%s'".formatted(getSelected().getName()));
//            var baseHeader = Dict.Dialog.YOU_ARE_ABOUT_TO_S.toString().formatted(action.toLowerCase(Locale.ENGLISH));
//
//            if (confirm(baseTitle + "?",
//                    baseHeader,
//                    Dict.Dialog.ACTION_CANT_BE_UNDONE.toString(),
//                    baseTitle)) {
////                onRemove();
//            }
        });
        remAction.setGraphic(MaterialIcon._Content.REMOVE.getImageView(size));

        var remAllAction = new Action(Dict.REMOVE_ALL.toString(), actionEvent -> {
            var baseTitle = Dict.Dialog.TITLE_REMOVE_ALL_S.toString().formatted(Dict.TASKS.toString().toLowerCase(Locale.ENGLISH));
            var action = Dict.Dialog.TITLE_REMOVE_ALL_S.toString().formatted(Dict.TASKS.toString().toLowerCase(Locale.ENGLISH));
            var baseHeader = Dict.Dialog.YOU_ARE_ABOUT_TO_S.toString().formatted(action.toLowerCase(Locale.ENGLISH));

//            if (confirm(
//                    baseTitle + "?",
//                    baseHeader,
//                    Dict.Dialog.ACTION_CANT_BE_UNDONE.toString(),
//                    baseTitle)) {
//                onRemoveAll();
//            }
        });
        remAllAction.setGraphic(MaterialIcon._Content.CLEAR.getImageView(size));

        var editAction = new Action(Dict.EDIT.toString(), actionEvent -> {
            edit(getSelected());
        });
        editAction.setGraphic(MaterialIcon._Editor.MODE_EDIT.getImageView(size));

        mActions = List.of(
                addAction,
                remAction,
                editAction,
                remAllAction
        );

        var nullSelectionBooleanBinding = mListView.getSelectionModel().selectedItemProperty().isNull();
        editAction.disabledProperty().bind(nullSelectionBooleanBinding);
        remAction.disabledProperty().bind(nullSelectionBooleanBinding);
//        remAllAction.disabledProperty().bind(Bindings.isEmpty(mTaskManager.getItems()));

//        mListView.itemsProperty().bind(mTaskManager.itemsProperty());
        var toolBar = ActionUtils.createToolBar(mActions, ActionUtils.ActionTextBehavior.HIDE);
        FxHelper.undecorateButtons(toolBar.getItems().stream());
        FxHelper.slimToolBar(toolBar);
        toolBar.getItems().add(mCheckBox);
        setTop(toolBar);
        setCenter(mListView);

//        mListView.setCellFactory(listView -> new MainPane.ItemListCellRenderer());
    }

    private void edit(String item) {
        var title = item == null ? Dict.ADD.toString() : Dict.EDIT.toString();

        SwingUtilities.invokeLater(() -> {
            var cronPanel = new NbCronPanel();
            var d = new DialogDescriptor(cronPanel, title);
            d.setValid(false);
            cronPanel.setDialogDescriptor(d);
            cronPanel.setCronString(item);
            if (DialogDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(d)) {
                Platform.runLater(() -> {
                    var cronString = cronPanel.getCronString();
                    if (item != null) {
                        mListView.getItems().remove(item);

                    } else {
                    }
                    mListView.getItems().add(cronString);
                    mListView.getSelectionModel().select(cronString);
                    mListView.requestFocus();
                });
            }
        });

    }

    private String getSelected() {
        return mListView.getSelectionModel().getSelectedItem();
    }

}
