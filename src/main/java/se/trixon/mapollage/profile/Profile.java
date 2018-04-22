/*
 * Copyright 2018 Patrik Karlström.
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
package se.trixon.mapollage.profile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import se.trixon.almond.util.BooleanHelper;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.mapollage.ui.OptionsPanel;

/**
 *
 * @author Patrik Karlström
 */
public class Profile extends ProfileBase implements Comparable<Profile>, Cloneable {

    private static final Gson GSON = new GsonBuilder()
            .setVersion(1.0)
            .serializeNulls()
            .setPrettyPrinting()
            .create();

    @SerializedName("description")
    private ProfileDescription mDescription = new ProfileDescription(this);
    @SerializedName("descriptionString")
    private String mDescriptionString;
    private transient File mDestinationFile;
    @SerializedName("folder")
    private ProfileFolder mFolder = new ProfileFolder(this);
    @SerializedName("last_run")
    private long mLastRun;
    @SerializedName("name")
    private String mName;
    @SerializedName("path")
    private ProfilePath mPath = new ProfilePath(this);
    @SerializedName("photo")
    private ProfilePhoto mPhoto = new ProfilePhoto(this);
    @SerializedName("placemark")
    private ProfilePlacemark mPlacemark = new ProfilePlacemark(this);
    @SerializedName("source")
    private ProfileSource mSource = new ProfileSource(this);

    public Profile() {
    }

    @Override
    public Profile clone() {
        try {
            super.clone();
            String json = GSON.toJson(this);
            return GSON.fromJson(json, Profile.class);
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(Profile.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public int compareTo(Profile o) {
        return mName.compareTo(o.getName());
    }

    public ProfileDescription getDescription() {
        return mDescription;
    }

    public String getDescriptionString() {
        return mDescriptionString;
    }

    public File getDestinationFile() {
        return mDestinationFile;
    }

    public ProfileFolder getFolder() {
        return mFolder;
    }

    public long getLastRun() {
        return mLastRun;
    }

    public String getName() {
        return mName;
    }

    public ProfilePath getPath() {
        return mPath;
    }

    public ProfilePhoto getPhoto() {
        return mPhoto;
    }

    public ProfilePlacemark getPlacemark() {
        return mPlacemark;
    }

    public ProfileSource getSource() {
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

        if (mDescription.hasPhotoStaticOrDynamic() && mPhoto.getReference() == ProfilePhoto.Reference.RELATIVE) {
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

    public void setDescription(ProfileDescription description) {
        mDescription = description;
    }

    public void setDescriptionString(String descriptionString) {
        this.mDescriptionString = descriptionString;
    }

    public void setDestinationFile(File destinationFile) {
        mDestinationFile = destinationFile;
    }

    public void setFolder(ProfileFolder folder) {
        mFolder = folder;
    }

    public void setLastRun(long lastRun) {
        mLastRun = lastRun;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setPath(ProfilePath path) {
        mPath = path;
    }

    public void setPhoto(ProfilePhoto photo) {
        mPhoto = photo;
    }

    public void setPlacemark(ProfilePlacemark placemark) {
        mPlacemark = placemark;
    }

    public void setSource(ProfileSource source) {
        mSource = source;
    }

    @Override
    public String toDebugString() {
        ArrayList<ProfileInfo> profileInfos = new ArrayList<>();
        profileInfos.add(mSource.getProfileInfo());
        profileInfos.add(mFolder.getProfileInfo());
        profileInfos.add(mPath.getProfileInfo());
        profileInfos.add(mPlacemark.getProfileInfo());
        profileInfos.add(mDescription.getProfileInfo());
        profileInfos.add(mPhoto.getProfileInfo());
        profileInfos.add(getProfileInfo());

        int maxLength = Integer.MIN_VALUE;
        for (ProfileInfo profileInfo : profileInfos) {
            maxLength = Math.max(maxLength, profileInfo.getMaxLength());
        }
        maxLength = maxLength + 3;

        String separator = " : ";
        StringBuilder builder = new StringBuilder("\n");
        builder.append(StringUtils.leftPad(Dict.PROFILE.toString(), maxLength)).append(separator).append(mName).append("\n");
        builder.append(StringUtils.leftPad("", maxLength)).append(separator).append(mDescriptionString).append("\n");

        for (ProfileInfo profileInfo : profileInfos) {
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
        ArrayList<ProfileInfo> profileInfos = new ArrayList<>();
        profileInfos.add(mSource.getProfileInfo());
        profileInfos.add(mFolder.getProfileInfo());
        profileInfos.add(mPath.getProfileInfo());
        profileInfos.add(mPlacemark.getProfileInfo());
        profileInfos.add(mDescription.getProfileInfo());
        profileInfos.add(mPhoto.getProfileInfo());
        profileInfos.add(getProfileInfo());

        int maxLength = Integer.MIN_VALUE;
        for (ProfileInfo profileInfo : profileInfos) {
            maxLength = Math.max(maxLength, profileInfo.getMaxLength());
        }
        maxLength = maxLength + 3;

        String separator = " : ";
        StringBuilder builder = new StringBuilder();
        for (ProfileInfo profileInfo : profileInfos) {
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
    protected ProfileInfo getProfileInfo() {
        ResourceBundle bundle = SystemHelper.getBundle(OptionsPanel.class, "Bundle");
        ProfileInfo profileInfo = new ProfileInfo();
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
