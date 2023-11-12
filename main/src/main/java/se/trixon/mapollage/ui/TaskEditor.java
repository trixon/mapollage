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
package se.trixon.mapollage.ui;

import java.util.ArrayList;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Region;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.openide.DialogDescriptor;
import se.trixon.mapollage.core.StorageManager;
import se.trixon.mapollage.core.Task;
import se.trixon.mapollage.core.TaskManager;
import se.trixon.mapollage.ui.task.BaseTab;
import se.trixon.mapollage.ui.task.DescriptionTab;
import se.trixon.mapollage.ui.task.FoldersTab;
import se.trixon.mapollage.ui.task.PathTab;
import se.trixon.mapollage.ui.task.PhotoTab;
import se.trixon.mapollage.ui.task.PlacemarkTab;
import se.trixon.mapollage.ui.task.SourceTab;

/**
 *
 * @author Patrik Karlström
 */
public class TaskEditor extends TabPane {

    private DescriptionTab mDescriptionTab;
    private final TaskManager mTaskManager = TaskManager.getInstance();
    private DialogDescriptor mDialogDescriptor;

    private FoldersTab mFoldersTab;
    private Button mOkButton;
    private PathTab mPathTab;
    private PhotoTab mPhotoTab;
    private PlacemarkTab mPlacemarkTab;
    private Task mTask;
    private SourceTab mSourceTab;
    private final ArrayList<BaseTab> mTabs = new ArrayList<>();
    private final ValidationSupport mValidationSupport = new ValidationSupport();

    public TaskEditor() {
        createUI();
    }

    public TaskEditor(Tab... tabs) {
        super(tabs);
        createUI();
    }

//    public TaskEditor(Task profile) {
//        mTask = profile;
//        createUI();
//    }
    public Task save() {
        mTaskManager.getIdToItem().put(mTask.getId(), mTask);
        mTabs.forEach((tab) -> {
            tab.save();
        });

        StorageManager.save();

        return mTask;
    }

    public void setOkButton(Button button) {
        mOkButton = button;
    }

    void load(Task task, DialogDescriptor dialogDescriptor) {
        if (task == null) {
            task = new Task();
        }

        mDialogDescriptor = dialogDescriptor;
        mTask = task;
    }

    private void createUI() {
        final double TAB_SIZE = BaseTab.ICON_SIZE * 1.5;
        setTabMaxHeight(TAB_SIZE);
        setTabMinHeight(TAB_SIZE);
        setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

        BaseTab.setValidationSupport(mValidationSupport);

        mSourceTab = new SourceTab();
        mFoldersTab = new FoldersTab();
        mPathTab = new PathTab();
        mPlacemarkTab = new PlacemarkTab();
        mDescriptionTab = new DescriptionTab();
        mPhotoTab = new PhotoTab();

        final ObservableList<Tab> tabs = getTabs();
        tabs.add(mSourceTab);
        tabs.add(mFoldersTab);
        tabs.add(mPathTab);
        tabs.add(mPlacemarkTab);
        tabs.add(mDescriptionTab);
        tabs.add(mPhotoTab);

        tabs.forEach((tab) -> {
            mTabs.add((BaseTab) tab);
        });

        final int size = 8;
        Insets insets = new Insets(size, size, size, size);
        mTabs.forEach((tab) -> {
            try {
                Region region = (Region) tab.getContent();
                region.setPadding(insets);
            } catch (Exception e) {
            }
        });

        mValidationSupport.validationResultProperty().addListener((ObservableValue<? extends ValidationResult> observable, ValidationResult oldValue, ValidationResult newValue) -> {
//            mOkButton.setDisable(mValidationSupport.isInvalid());
        });

        mValidationSupport.initInitialDecoration();
    }
}
