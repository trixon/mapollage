/*
 * Copyright 2017 Patrik Karlsson.
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

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlsson
 */
public class Profile extends ProfileBase implements Comparable<Profile> {

    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_FOLDER = "folder";
    private static final String KEY_NAME = "name";
    private static final String KEY_PHOTO = "photo";
    private static final String KEY_PLACEMARK = "placemark";
    private static final String KEY_SOURCE = "source";

    private ProfileDescription mDescription = new ProfileDescription(this);
    private File mDestinationFile;
    private ProfileFolder mFolder = new ProfileFolder(this);
    private String mName;
    private ProfilePhoto mPhoto = new ProfilePhoto(this);
    private ProfilePlacemark mPlacemark = new ProfilePlacemark(this);
    private ProfileSource mSource = new ProfileSource(this);

    public Profile() {
    }

    public Profile(JSONObject json) {
        setName((String) json.get(KEY_NAME));

        setSource(new ProfileSource(this, (JSONObject) json.get(KEY_SOURCE)));
        setFolder(new ProfileFolder(this, (JSONObject) json.get(KEY_FOLDER)));
        setPlacemark(new ProfilePlacemark(this, (JSONObject) json.get(KEY_PLACEMARK)));
        setDescription(new ProfileDescription(this, (JSONObject) json.get(KEY_DESCRIPTION)));
        setPhoto(new ProfilePhoto(this, (JSONObject) json.get(KEY_PHOTO)));
    }

    @Override
    public int compareTo(Profile o) {
        return mName.compareTo(o.getName());
    }

    public ProfileDescription getDescription() {
        return mDescription;
    }

    public File getDestinationFile() {
        return mDestinationFile;
    }

    public ProfileFolder getFolder() {
        return mFolder;
    }

    @Override
    public JSONObject getJson() {
        JSONObject json = new JSONObject();
        json.put(KEY_NAME, getName());

        json.put(KEY_SOURCE, getSource().getJson());
        json.put(KEY_FOLDER, getFolder().getJson());
        json.put(KEY_PLACEMARK, getPlacemark().getJson());
        json.put(KEY_DESCRIPTION, getDescription().getJson());
        json.put(KEY_PHOTO, getPhoto().getJson());

        return json;
    }

    public String getName() {
        return mName;
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

    @Override
    public boolean isValid() {
        sValidationErrorBuilder = new StringBuilder();

        mSource.isValid();
        mFolder.isValid();
        mPlacemark.isValid();
        mPhoto.isValid();

        return sValidationErrorBuilder.length() == 0;
    }

    public void setDescription(ProfileDescription description) {
        mDescription = description;
    }

    public void setDestinationFile(File destinationFile) {
        mDestinationFile = destinationFile;
    }

    public void setFolder(ProfileFolder folder) {
        mFolder = folder;
    }

    public void setName(String name) {
        mName = name;
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

    @Override
    public String toString() {
        return mName;
    }

    @Override
    protected ProfileInfo getProfileInfo() {
        ProfileInfo profileInfo = new ProfileInfo();
        LinkedHashMap<String, String> values = new LinkedHashMap<>();

        values.put(Dict.CALENDAR_LANGUAGE.toString(), mOptions.getLocale().getDisplayName());
        values.put(Dict.IMAGE_SIZE.toString(), String.valueOf(mOptions.getThumbnailSize()));
        values.put(Dict.BORDER_SIZE.toString(), String.valueOf(mOptions.getThumbnailBorderSize()));
        values.put(String.format("%s %s", Dict.DEFAULT.toString(), Dict.LATITUDE.toString()), String.valueOf(mOptions.getDefaultLat()));
        values.put(String.format("%s %s", Dict.DEFAULT.toString(), Dict.LONGITUDE.toString()), String.valueOf(mOptions.getDefaultLon()));

        profileInfo.setTitle(Dict.GLOBAL_OPTIONS.toString());
        profileInfo.setValues(values);

        return profileInfo;
    }
}
