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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class StorageManager {

    public final static JsonMapper JSON = JsonMapper.builder()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .visibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            .visibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .build();

    private final File mHistoryFile;
    private final File mLogFile;
    private Storage mStorage = new Storage();
    private final TaskManager mTaskManager = TaskManager.getInstance();
    private final File mTasksBackupFile;
    private final File mTasksFile;

    public static StorageManager getInstance() {
        return Holder.INSTANCE;
    }

    public static void save() {
        try {
            StorageManager.getInstance().saveToFile();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private StorageManager() {
        var userDirectory = Places.getUserDirectory();

        mTasksFile = new File(userDirectory, "tasks.json");
        mTasksBackupFile = new File(userDirectory, "tasks.bak");
        mHistoryFile = new File(userDirectory, "var/history");
        mLogFile = new File(userDirectory, "var/mapollage.log");
    }

    public int getFileFormatVersion() {
        return mStorage.getFileFormatVersion();
    }

    public File getHistoryFile() {
        return mHistoryFile;
    }

    public File getLogFile() {
        return mLogFile;
    }

    public TaskManager getTaskManager() {
        return mTaskManager;
    }

    public File getTasksFile() {
        return mTasksFile;
    }

    public void load() throws IOException {
        if (mTasksFile.exists()) {
            mStorage = Storage.open(mTasksFile);

            var taskItems = mTaskManager.getIdToItem();
            taskItems.clear();
            taskItems.putAll(mStorage.getTasks());

            for (var task : taskItems.values()) {
//                task.getSource().setTask(task);
            }
        } else {
            mStorage = new Storage();
        }
    }

    private void saveToFile() throws IOException {
        mStorage.setTasks(new HashMap<>(mTaskManager.getIdToItem()));
        mStorage.save(mTasksFile);

        try {
            FxHelper.runLater(() -> {
                try {
                    load();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            });
        } catch (IllegalStateException e) {
            //nvm - probably started from console w/o fx
        }
    }

    private static class Holder {

        private static final StorageManager INSTANCE = new StorageManager();
    }

    public static class Storage {

        private static final int FILE_FORMAT_VERSION = 1;
        @JsonProperty("fileFormatVersion")
        private int mFileFormatVersion;
        @JsonProperty("tasks")
        private final HashMap<String, Task> mTasks = new HashMap<>();

        public static Storage open(File file) throws IOException {
            var storage = JSON.readValue(file, Storage.class);
            if (storage.mFileFormatVersion != FILE_FORMAT_VERSION) {
                //TODO Handle file format version change
            }

            return storage;
        }

        public int getFileFormatVersion() {
            return mFileFormatVersion;
        }

        public HashMap<String, Task> getTasks() {
            for (var task : mTasks.values()) {
                task.postLoad();
            }

            return mTasks;
        }

        public void save(File file) throws IOException {
            mFileFormatVersion = FILE_FORMAT_VERSION;
            JSON.writeValue(file, this);
        }

        void setTasks(HashMap<String, Task> tasks) {
            mTasks.clear();
            mTasks.putAll(tasks);
        }
    }
}
