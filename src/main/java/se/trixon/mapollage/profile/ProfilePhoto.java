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

import java.util.LinkedHashMap;
import org.apache.commons.lang3.SystemUtils;
import org.json.simple.JSONObject;
import se.trixon.almond.util.BooleanHelper;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlsson
 */
public class ProfilePhoto extends ProfileBase {

    public static final String KEY_BASE_URL_VALUE = "baseUrlValue";
    public static final String KEY_FORCE_LOWER_CASE_EXTENSION = "forceLowerCaseExtension";
    public static final String KEY_HEIGHT_LIMIT = "heightLimit";
    public static final String KEY_LIMIT_HEIGHT = "limitHeight";
    public static final String KEY_LIMIT_WIDTH = "limitWidth";
    public static final String KEY_REFERENCE = "reference";
    public static final String KEY_WIDTH_LIMIT = "widthLimit";

    private String mBaseUrlValue = "https://www.domain.com/img/";
    private boolean mForceLowerCaseExtension = SystemUtils.IS_OS_WINDOWS;
    private int mHeightLimit = 800;
    private boolean mLimitHeight = true;
    private boolean mLimitWidth = true;
    private final Profile mProfile;
    private Reference mReference = Reference.ABSOLUTE;
    private int mWidthLimit = 1000;

    public ProfilePhoto(Profile profile) {
        mProfile = profile;
    }

    public ProfilePhoto(Profile profile, JSONObject json) {
        mProfile = profile;
        mHeightLimit = getInt(json, KEY_HEIGHT_LIMIT, mHeightLimit);
        mWidthLimit = getInt(json, KEY_WIDTH_LIMIT, mWidthLimit);
        mLimitHeight = getBoolean(json, KEY_LIMIT_HEIGHT, mLimitHeight);
        mLimitWidth = getBoolean(json, KEY_LIMIT_WIDTH, mLimitWidth);
        mForceLowerCaseExtension = getBoolean(json, KEY_FORCE_LOWER_CASE_EXTENSION, mForceLowerCaseExtension);
        mReference = Reference.values()[getInt(json, KEY_REFERENCE, Reference.ABSOLUTE.ordinal())];
        mBaseUrlValue = (String) json.get(KEY_BASE_URL_VALUE);
    }

    public String getBaseUrlValue() {
        return mBaseUrlValue;
    }

    public int getHeightLimit() {
        return mHeightLimit;
    }

    @Override
    public JSONObject getJson() {
        JSONObject json = new JSONObject();
        json.put(KEY_REFERENCE, mReference.ordinal());
        json.put(KEY_BASE_URL_VALUE, mBaseUrlValue);
        json.put(KEY_FORCE_LOWER_CASE_EXTENSION, mForceLowerCaseExtension);
        json.put(KEY_LIMIT_HEIGHT, mLimitHeight);
        json.put(KEY_HEIGHT_LIMIT, mHeightLimit);
        json.put(KEY_LIMIT_WIDTH, mLimitWidth);
        json.put(KEY_WIDTH_LIMIT, mWidthLimit);

        return json;
    }

    public Reference getReference() {
        return mReference;
    }

    @Override
    public String getTitle() {
        return Dict.PHOTO.toString();
    }

    public int getWidthLimit() {
        return mWidthLimit;
    }

    public boolean isForceLowerCaseExtension() {
        return mForceLowerCaseExtension;
    }

    public boolean isLimitHeight() {
        return mLimitHeight;
    }

    public boolean isLimitWidth() {
        return mLimitWidth;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    public void setBaseUrlValue(String baseUrlValue) {
        mBaseUrlValue = baseUrlValue;
    }

    public void setForceLowerCaseExtension(boolean forceLowerCaseExtension) {
        mForceLowerCaseExtension = forceLowerCaseExtension;
    }

    public void setHeightLimit(int maxHeightValue) {
        mHeightLimit = maxHeightValue;
    }

    public void setLimitHeight(boolean value) {
        mLimitHeight = value;
    }

    public void setLimitWidth(boolean value) {
        mLimitWidth = value;
    }

    public void setReference(Reference reference) {
        mReference = reference;
    }

    public void setWidthLimit(int maxWidthValue) {
        mWidthLimit = maxWidthValue;
    }

    @Override
    protected ProfileInfo getProfileInfo() {
        ProfileInfo profileInfo = new ProfileInfo();
        LinkedHashMap<String, String> values = new LinkedHashMap<>();

        values.put(mBundleUI.getString("ModulePhotoPanel.widthCheckBox.text"), mLimitWidth ? String.valueOf(mWidthLimit) : "-");
        values.put(mBundleUI.getString("ModulePhotoPanel.heightCheckBox.text"), mLimitHeight ? String.valueOf(mHeightLimit) : "-");

        String fileReference = null;

        switch (mReference) {
            case ABSOLUTE:
                fileReference = Dict.ABSOLUTE.toString();
                break;

            case ABSOLUTE_PATH:
                fileReference = mBaseUrlValue;
                break;

            case RELATIVE:
                fileReference = Dict.RELATIVE.toString();
                break;

            case THUMBNAIL:
                fileReference = Dict.THUMBNAIL.toString();
                break;

        }

        values.put(Dict.FILE_REFERENCE.toString(), fileReference);
        values.put(mBundleUI.getString("ModulePhotoPanel.lowerCaseExtCheckBox.text"), BooleanHelper.asYesNo(mForceLowerCaseExtension));

        profileInfo.setTitle(getTitle());
        profileInfo.setValues(values);

        return profileInfo;
    }

    public static enum Reference {
        //NEVER EVER change the order of the elements
        ABSOLUTE,
        ABSOLUTE_PATH,
        RELATIVE,
        THUMBNAIL;
    }
}
