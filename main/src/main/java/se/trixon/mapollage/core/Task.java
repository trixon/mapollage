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

import com.google.gson.annotations.SerializedName;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.openide.util.Exceptions;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.control.editable_list.EditableListItem;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class Task implements Runnable, EditableListItem {

    public static final DownloadListener DEFAULT_DOWNLOAD_LISTENER = new DownloadListener() {

        @Override
        public void onDownloadFailed(Task task, IOException ex) {
        }

        @Override
        public void onDownloadFinished(Task task, File destFile) {
        }

        @Override
        public void onDownloadStarted(Task task) {
        }
    };
    @SerializedName("cron")
    private String mCron = "0 * * * *";
    @SerializedName("description")
    private String mDescription;
    @SerializedName("destination")
    private String mDestination;
    private transient DownloadListener mDownloadListener = DEFAULT_DOWNLOAD_LISTENER;
    @SerializedName("enabled")
    private boolean mEnabled = true;
    @SerializedName("uuid")
    private String mId = UUID.randomUUID().toString();
    @SerializedName("name")
    private String mName;
    private final transient TaskManager mTaskManager = TaskManager.getInstance();
    @SerializedName("url")
    private String mUrl;

    public Task() {
    }

    public String getCron() {
        return mCron;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getDestination() {
        return mDestination;
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getUrl() {
        return mUrl;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public boolean isValid() {
        return !getName().isEmpty() && !getUrl().isEmpty() && !getDestination().isEmpty();
    }

    @Override
    public void run() {
//        if (!isActive()) {
//            return;
//        }

        new Thread(() -> {
            mDownloadListener.onDownloadStarted(this);
            try {
                var url = new URL(getUrl());
                var destFile = getDestPath();
                FileUtils.copyURLToFile(url, destFile, 15000, 15000);

                String message = String.format("%s: %s", Dict.DOWNLOAD_COMPLETED.toString(), destFile.getAbsolutePath());
                logToFile(message);
                message = String.format("%s (%s)", message, mName);
                mTaskManager.log(message);

                mDownloadListener.onDownloadFinished(this, destFile);
            } catch (IOException ex) {
                String message = String.format("%s: %s", Dict.DOWNLOAD_FAILED.toString(), ex.getLocalizedMessage());
                logToFile(message);
                message = String.format("%s (%s)", message, mName);
                mTaskManager.log(message);

                mDownloadListener.onDownloadFailed(this, ex);
            }
        }).start();
    }

    public void setCron(String cron) {
        mCron = cron;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public void setDestination(String destination) {
        mDestination = destination;
    }

    public void setDownloadListener(DownloadListener downloadListener) {
        mDownloadListener = downloadListener;
    }

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    public void setId(String id) {
        mId = id;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    @Override
    public String toString() {
        String fmt;

        if (mEnabled) {
            fmt = "<html><b>%s</b><br />%s</html>";
        } else {
            fmt = "<html><i>%s<br />%s</i></html>";
        }

        return String.format(fmt, mName, mDescription);
    }

    private File getDestPath() {
        var destPath = new File(getDestination());
        String ext = FilenameUtils.getExtension(destPath.getAbsolutePath());
        String baseName = FilenameUtils.getBaseName(destPath.getAbsolutePath());
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date(System.currentTimeMillis()));

        String filename;
        if (ext.isEmpty()) {
            filename = String.format("%s_%s", baseName, timeStamp);
        } else {
            filename = String.format("%s_%s.%s", baseName, timeStamp, ext);

        }

        return new File(destPath.getParent(), filename);
    }

    private synchronized void logToFile(String message) {
        var dir = new File(mDestination).getParentFile();
        String basename = FilenameUtils.getBaseName(mDestination);
        String ext = FilenameUtils.getExtension(mDestination);
        if (ext.equalsIgnoreCase("log")) {
            ext = "log.log";
        } else {
            ext = "log";
        }

        var logFile = new File(dir, String.format("%s.%s", basename, ext));
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss").format(new Date(System.currentTimeMillis()));

        message = String.format("%s %s%s", timeStamp, message, System.lineSeparator());

        try {
            FileUtils.writeStringToFile(logFile, message, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

}
