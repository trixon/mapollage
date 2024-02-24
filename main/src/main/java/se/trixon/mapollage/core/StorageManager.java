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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import javafx.collections.ObservableMap;
import org.apache.commons.io.FileUtils;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.gson_adapter.FileAdapter;

/**
 *
 * @author Patrik Karlström
 */
public class StorageManager {

    public static final Gson GSON = new GsonBuilder()
            .setVersion(1.0)
            .serializeNulls()
            .setPrettyPrinting()
            .registerTypeAdapter(File.class, new FileAdapter())
            .create();

    private final File mHistoryFile;
    private final File mLogFile;
    private Storage mStorage = new Storage();
    private final TaskManager mTaskManager = TaskManager.getInstance();
    private final File mTasksBackupFile;
    private final File mTasksFile;
    private final File mUserDirectory;

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
        mUserDirectory = Places.getUserDirectory();

        mTasksFile = new File(mUserDirectory, "tasks.json");
        mTasksBackupFile = new File(mUserDirectory, "tasks.bak");
        mHistoryFile = new File(mUserDirectory, "var/history");
        mLogFile = new File(mUserDirectory, "var/mapollage.log");
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

    public File getUserDirectory() {
        return mUserDirectory;
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
        mStorage.setTasks(mTaskManager.getIdToItem());
        String json = mStorage.save(mTasksFile);
        String tag = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        FileUtils.writeStringToFile(mTasksBackupFile, String.format("%s=%s\n", tag, json), Charset.defaultCharset(), true);

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

    public class Storage {

        private static final int FILE_FORMAT_VERSION = 1;
        @SerializedName("fileFormatVersion")
        private int mFileFormatVersion;
        @SerializedName("tasks")
        private final HashMap<String, Task> mTasks = new HashMap<>();

        public static Storage open(File file) throws IOException, JsonSyntaxException {
            String json = FileUtils.readFileToString(file, Charset.defaultCharset());

            var storage = GSON.fromJson(json, Storage.class);

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

        public String save(File file) throws IOException {
            mFileFormatVersion = FILE_FORMAT_VERSION;
            var json = GSON.toJson(this);
            FileUtils.writeStringToFile(file, json, Charset.defaultCharset());

            return json;
        }

        void setTasks(ObservableMap<String, Task> tasks) {
            mTasks.clear();
            mTasks.putAll(tasks);
        }
    }
}
