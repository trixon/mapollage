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
package se.trixon.mapollage.core;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class TaskManager {

    private final ObjectProperty<ObservableMap<String, Task>> mIdToItemProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<ObservableList<Task>> mItemsProperty = new SimpleObjectProperty<>();

    public static TaskManager getInstance() {
        return Holder.INSTANCE;
    }

    private TaskManager() {
        mItemsProperty.setValue(FXCollections.observableArrayList());
        mIdToItemProperty.setValue(FXCollections.observableHashMap());

        mIdToItemProperty.get().addListener((MapChangeListener.Change<? extends String, ? extends Task> change) -> {
            var values = new ArrayList<Task>(getIdToItem().values());
            values.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
            getItems().setAll(values);
        });
    }

    public boolean exists(Task item) {
        return getIdToItem().containsValue(item);
    }

    public boolean exists(String name) {
        return getItems().stream()
                .anyMatch(item -> (StringUtils.equalsIgnoreCase(name, item.getName())));
    }

    public Task getById(String id) {
        return getIdToItem().get(id);
    }

    public Task getByName(String name) {
        for (var task : getItems()) {
            if (task.getName().equalsIgnoreCase(name)) {
                return task;
            }
        }

        System.out.println("TASK NOT FOUND: " + name);

        return null;
    }

    public final ObservableMap<String, Task> getIdToItem() {
        return mIdToItemProperty.get();
    }

    public final ObservableList<Task> getItems() {
        return mItemsProperty.get();
    }

    public List<Task> getTasks(ArrayList<String> taskIds) {
        var tasks = new ArrayList<Task>();

        taskIds.forEach(id -> {
            var task = getById(id);
            if (task != null) {
                tasks.add(task);
            }
        });

        return tasks;
    }

    public boolean hasActiveTasks() {
        return false;
    }

    public boolean isValid(String oldName, String newName) {
        if (StringUtils.isBlank(newName)) {
            return false;
        }

        var taskByName = getByName(newName.trim());

        return taskByName == null || taskByName == getByName(oldName);
    }

    public ObjectProperty<ObservableList<Task>> itemsProperty() {
        return mItemsProperty;
    }

    public void log(String message) {
        System.out.println(message);
    }

    public Task save() {
        return null;
    }

    private static class Holder {

        private static final TaskManager INSTANCE = new TaskManager();
    }
}
