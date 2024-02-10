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

import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Region;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.openide.DialogDescriptor;
import se.trixon.almond.util.fx.FxHelper;
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
    private DialogDescriptor mDialogDescriptor;
    private FoldersTab mFoldersTab;
    private PathTab mPathTab;
    private PhotoTab mPhotoTab;
    private PlacemarkTab mPlacemarkTab;
    private SourceTab mSourceTab;
    private Task mTask;
    private final TaskManager mTaskManager = TaskManager.getInstance();
    private final ValidationSupport mValidationSupport = new ValidationSupport();

    public TaskEditor() {
        createUI();

        Platform.runLater(() -> {
            initValidation();
        });
    }

    public void load(Task task, DialogDescriptor dialogDescriptor) {
        if (task == null) {
            task = new Task();
        }

        mDialogDescriptor = dialogDescriptor;
        mTask = task;

        getBaseTabs().forEachOrdered(tab -> {
            tab.load(mTask);
        });
        getSelectionModel().selectFirst();
    }

    public Task save() {
        mTaskManager.getIdToItem().put(mTask.getId(), mTask);

        getBaseTabs().forEachOrdered(tab -> {
            tab.save();
        });

        StorageManager.save();

        return mTask;
    }

    private void createUI() {
        var tabHeight = BaseTab.ICON_SIZE * 1.5;
        setTabMaxHeight(tabHeight);
        setTabMinHeight(tabHeight);
        setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

        BaseTab.setValidationSupport(mValidationSupport);

        mSourceTab = new SourceTab();
        mFoldersTab = new FoldersTab();
        mPathTab = new PathTab();
        mPlacemarkTab = new PlacemarkTab();
        mDescriptionTab = new DescriptionTab();
        mPhotoTab = new PhotoTab();

        getTabs().setAll(
                mSourceTab,
                mFoldersTab,
                mPathTab,
                mPlacemarkTab,
                mDescriptionTab,
                mPhotoTab
        );

        var insets = FxHelper.getUIScaledInsets(8);
        getBaseTabs()
                .filter(tab -> tab.getContent() instanceof Region)
                .map(tab -> (Region) tab.getContent())
                .forEachOrdered(region -> {
                    region.setPadding(insets);
                });

    }

    private Stream<BaseTab> getBaseTabs() {
        return getTabs().stream()
                .filter(tab -> tab instanceof BaseTab)
                .map(tab -> (BaseTab) tab);
    }

    private void initValidation() {
        mValidationSupport.validationResultProperty().addListener((ObservableValue<? extends ValidationResult> observable, ValidationResult oldValue, ValidationResult newValue) -> {
            mDialogDescriptor.setValid(!mValidationSupport.isInvalid());
        });

        mValidationSupport.initInitialDecoration();
    }
}
