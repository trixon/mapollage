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
package se.trixon.mapollage.core;

import com.google.gson.annotations.SerializedName;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.control.editable_list.EditableListItem;

/**
 *
 * @author Patrik Karlström
 */
public class Task extends TaskBase implements EditableListItem {

    @SerializedName("description")
    private TaskDescription mDescription = new TaskDescription();
    @SerializedName("descriptionString")
    private String mDescriptionString;
    @SerializedName("destinationFile")
    private File mDestinationFile;
    @SerializedName("folder")
    private TaskFolder mFolder = new TaskFolder();
    @SerializedName("uuid")
    private String mId = UUID.randomUUID().toString();
    @SerializedName("language")
    private String mLanguage = Locale.getDefault().toLanguageTag();
    @SerializedName("last_run")
    private long mLastRun;
    @SerializedName("name")
    private String mName;
    @SerializedName("path")
    private TaskPath mPath = new TaskPath();
    @SerializedName("photo")
    private TaskPhoto mPhoto = new TaskPhoto();
    @SerializedName("placemark")
    private TaskPlacemark mPlacemark = new TaskPlacemark();
    @SerializedName("source")
    private TaskSource mSource = new TaskSource();

    public Task() {
        postLoad();
    }

    public TaskDescription getDescription() {
        return mDescription;
    }

    public String getDescriptionString() {
        return mDescriptionString;
    }

    public File getDestinationFile() {
        return mDestinationFile;
    }

    public TaskFolder getFolder() {
        return mFolder;
    }

    public String getId() {
        return mId;
    }

    public String getLanguage() {
        return mLanguage;
    }

    public long getLastRun() {
        return mLastRun;
    }

    public Locale getLocale() {
        return Locale.forLanguageTag(getLanguage());
    }

    @Override
    public String getName() {
        return mName;
    }

    public TaskPath getPath() {
        return mPath;
    }

    public TaskPhoto getPhoto() {
        return mPhoto;
    }

    public TaskPlacemark getPlacemark() {
        return mPlacemark;
    }

    public TaskSource getSource() {
        return mSource;
    }

    @Override
    public String getTitle() {
        return Dict.PROFILE.toString();
    }

    public String getValidationError() {
        return sValidationErrorBuilder.toString();
    }

    public boolean hasValidRelativeSourceDest() {
        boolean valid = true;

        if (mDescription.hasPhotoStaticOrDynamic() && mPhoto.getReference() == TaskPhoto.Reference.RELATIVE) {
            try {
                Path path = mDestinationFile.toPath().relativize(mSource.getDir().toPath());
            } catch (IllegalArgumentException e) {
                valid = false;
            }
        }

        return valid;
    }

    @Override
    public boolean isValid() {
        sValidationErrorBuilder = new StringBuilder();

        mSource.isValid();
        mFolder.isValid();
        mPlacemark.isValid();
        mPhoto.isValid();
        mPath.isValid();

        return sValidationErrorBuilder.length() == 0;
    }

    public void postLoad() {
        mDescription.setTask(this);
        mFolder.setTask(this);
        mPath.setTask(this);
        mPhoto.setTask(this);
        mPlacemark.setTask(this);
        mSource.setTask(this);
    }

    public void setDescription(TaskDescription description) {
        mDescription = description;
    }

    public void setDescriptionString(String descriptionString) {
        this.mDescriptionString = descriptionString;
    }

    public void setDestinationFile(File destinationFile) {
        mDestinationFile = destinationFile;
    }

    public void setFolder(TaskFolder folder) {
        mFolder = folder;
    }

    public void setId(String id) {
        this.mId = id;
    }

    public void setLanguage(String language) {
        mLanguage = language;
    }

    public void setLastRun(long lastRun) {
        mLastRun = lastRun;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setPath(TaskPath path) {
        mPath = path;
    }

    public void setPhoto(TaskPhoto photo) {
        mPhoto = photo;
    }

    public void setPlacemark(TaskPlacemark placemark) {
        mPlacemark = placemark;
    }

    public void setSource(TaskSource source) {
        mSource = source;
    }

    @Override
    public String toDebugString() {
        var taskInfos = List.of(
                mSource.getTaskInfo(),
                mFolder.getTaskInfo(),
                mPath.getTaskInfo(),
                mPlacemark.getTaskInfo(),
                mDescription.getTaskInfo(),
                mPhoto.getTaskInfo(),
                getTaskInfo()
        );

        int maxLength = Integer.MIN_VALUE;
        for (var taskInfo : taskInfos) {
            maxLength = Math.max(maxLength, taskInfo.getMaxLength());
        }
        maxLength = maxLength + 3;

        String separator = " : ";
        var builder = new StringBuilder("\n");
        builder.append(StringUtils.leftPad(Dict.PROFILE.toString(), maxLength)).append(separator).append(mName).append("\n");
        builder.append(StringUtils.leftPad("", maxLength)).append(separator).append(mDescriptionString).append("\n");

        for (var taskInfo : taskInfos) {
            builder.append(taskInfo.getTitle()).append("\n");

            for (var entry : taskInfo.getValues().entrySet()) {
                var key = entry.getKey();
                var value = entry.getValue();
                builder.append(StringUtils.leftPad(key, maxLength)).append(separator).append(value).append("\n");
            }

            builder.append("\n");
        }

        return builder.toString();
    }

    public String toInfoString() {
        var taskInfos = List.of(
                mSource.getTaskInfo(),
                mFolder.getTaskInfo(),
                mPath.getTaskInfo(),
                mPlacemark.getTaskInfo(),
                mDescription.getTaskInfo(),
                mPhoto.getTaskInfo()
        );

        int maxLength = Integer.MIN_VALUE;
        for (var taskInfo : taskInfos) {
            maxLength = Math.max(maxLength, taskInfo.getMaxLength());
        }
        maxLength = maxLength + 3;

        String separator = " : ";
        var builder = new StringBuilder();
        for (var taskInfo : taskInfos) {
            builder.append(taskInfo.getTitle().toUpperCase(Locale.ROOT)).append("\n");

            for (var entry : taskInfo.getValues().entrySet()) {
                var key = entry.getKey();
                var value = entry.getValue();
                builder.append(StringUtils.leftPad(key, maxLength)).append(separator).append(value).append("\n");
            }

            builder.append("\n");
        }

        return builder.toString().trim();
    }

    @Override
    public String toString() {
        return mName;
    }

    @Override
    protected TaskInfo getTaskInfo() {
        return null;
    }
}
