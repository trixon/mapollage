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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.NbBundle;
import se.trixon.almond.util.BooleanHelper;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.control.editable_list.EditableListItem;
import se.trixon.mapollage.ui.options.OptionsPanel;

/**
 *
 * @author Patrik Karlström
 */
public class Task extends TaskBase implements EditableListItem {

    @SerializedName("description")
    private TaskDescription mDescription = new TaskDescription(this);
    @SerializedName("descriptionString")
    private String mDescriptionString;
    @SerializedName("destinationFile")
    private File mDestinationFile;
    @SerializedName("folder")
    private TaskFolder mFolder = new TaskFolder(this);
    @SerializedName("uuid")
    private String mId = UUID.randomUUID().toString();
    @SerializedName("last_run")
    private long mLastRun;
    @SerializedName("name")
    private String mName;
    @SerializedName("path")
    private TaskPath mPath = new TaskPath(this);
    @SerializedName("photo")
    private TaskPhoto mPhoto = new TaskPhoto(this);
    @SerializedName("placemark")
    private TaskPlacemark mPlacemark = new TaskPlacemark(this);
    @SerializedName("source")
    private TaskSource mSource = new TaskSource(this);

    public Task() {
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

    public long getLastRun() {
        return mLastRun;
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
        ArrayList<TaskInfo> profileInfos = new ArrayList<>();
        profileInfos.add(mSource.getProfileInfo());
        profileInfos.add(mFolder.getProfileInfo());
        profileInfos.add(mPath.getProfileInfo());
        profileInfos.add(mPlacemark.getProfileInfo());
        profileInfos.add(mDescription.getProfileInfo());
        profileInfos.add(mPhoto.getProfileInfo());
        profileInfos.add(getProfileInfo());

        int maxLength = Integer.MIN_VALUE;
        for (TaskInfo profileInfo : profileInfos) {
            maxLength = Math.max(maxLength, profileInfo.getMaxLength());
        }
        maxLength = maxLength + 3;

        String separator = " : ";
        StringBuilder builder = new StringBuilder("\n");
        builder.append(StringUtils.leftPad(Dict.PROFILE.toString(), maxLength)).append(separator).append(mName).append("\n");
        builder.append(StringUtils.leftPad("", maxLength)).append(separator).append(mDescriptionString).append("\n");

        for (TaskInfo profileInfo : profileInfos) {
            builder.append(profileInfo.getTitle()).append("\n");

            for (Map.Entry<String, String> entry : profileInfo.getValues().entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                builder.append(StringUtils.leftPad(key, maxLength)).append(separator).append(value).append("\n");
            }

            builder.append("\n");
        }

        return builder.toString();
    }

    public String toInfoString() {
        ArrayList<TaskInfo> profileInfos = new ArrayList<>();
        profileInfos.add(mSource.getProfileInfo());
        profileInfos.add(mFolder.getProfileInfo());
        profileInfos.add(mPath.getProfileInfo());
        profileInfos.add(mPlacemark.getProfileInfo());
        profileInfos.add(mDescription.getProfileInfo());
        profileInfos.add(mPhoto.getProfileInfo());
        profileInfos.add(getProfileInfo());

        int maxLength = Integer.MIN_VALUE;
        for (TaskInfo profileInfo : profileInfos) {
            maxLength = Math.max(maxLength, profileInfo.getMaxLength());
        }
        maxLength = maxLength + 3;

        String separator = " : ";
        StringBuilder builder = new StringBuilder();
        for (TaskInfo profileInfo : profileInfos) {
            builder.append(profileInfo.getTitle()).append("\n");

            for (Map.Entry<String, String> entry : profileInfo.getValues().entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
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
    protected TaskInfo getProfileInfo() {
        ResourceBundle bundle = NbBundle.getBundle(OptionsPanel.class);
        TaskInfo profileInfo = new TaskInfo();
        LinkedHashMap<String, String> values = new LinkedHashMap<>();

        values.put(Dict.CALENDAR_LANGUAGE.toString(), mOptions.getLocale().getDisplayName());
        values.put(Dict.THUMBNAIL.toString(), String.valueOf(mOptions.getThumbnailSize()));
        values.put(Dict.BORDER_SIZE.toString(), String.valueOf(mOptions.getThumbnailBorderSize()));
        values.put(String.format("%s %s", Dict.DEFAULT.toString(), Dict.LATITUDE.toString()), String.valueOf(mOptions.getDefaultLat()));
        values.put(String.format("%s %s", Dict.DEFAULT.toString(), Dict.LONGITUDE.toString()), String.valueOf(mOptions.getDefaultLon()));
        values.put(bundle.getString("ProgressPanel.autoOpenCheckBox"), BooleanHelper.asYesNo(mOptions.isAutoOpen()));

        profileInfo.setTitle(Dict.OPTIONS.toString());
        profileInfo.setValues(values);

        return profileInfo;
    }
}
