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
package se.trixon.photokml.profile;

import org.apache.commons.lang3.SystemUtils;
import org.json.simple.JSONObject;

/**
 *
 * @author Patrik Karlsson
 */
public class ProfilePhoto extends ProfileBase {

    public static final String KEY_BASE_URL = "baseUrl";
    public static final String KEY_BASE_URL_VALUE = "baseUrlValue";
    public static final String KEY_FORCE_LOWER_CASE_EXTENSION = "forceLowerCaseExtension";
    public static final String KEY_HEIGHT_LIMIT = "heightLimit";
    public static final String KEY_LIMIT_HEIGHT = "limitHeight";
    public static final String KEY_LIMIT_WIDTH = "limitWidth";
    public static final String KEY_WIDTH_LIMIT = "widthLimit";

    private boolean mBaseUrl = false;
    private String mBaseUrlValue = "http://www.domain.com/img/";
    private boolean mForceLowerCaseExtension = SystemUtils.IS_OS_WINDOWS;
    private int mHeightLimit = 400;
    private boolean mLimitHeight = true;
    private boolean mLimitWidth = true;
    private final Profile mProfile;
    private int mWidthLimit = 400;

    public ProfilePhoto(Profile profile) {
        mProfile = profile;
    }

    public ProfilePhoto(Profile profile, JSONObject json) {
        mProfile = profile;
        mHeightLimit = getInt(json, KEY_HEIGHT_LIMIT);
        mWidthLimit = getInt(json, KEY_WIDTH_LIMIT);
        mLimitHeight = getBoolean(json, KEY_LIMIT_HEIGHT);
        mLimitWidth = getBoolean(json, KEY_LIMIT_WIDTH);
        mBaseUrl = getBoolean(json, KEY_BASE_URL);
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
        json.put(KEY_BASE_URL, mBaseUrl);
        json.put(KEY_BASE_URL_VALUE, mBaseUrlValue);
        json.put(KEY_FORCE_LOWER_CASE_EXTENSION, mForceLowerCaseExtension);
        json.put(KEY_LIMIT_HEIGHT, mLimitHeight);
        json.put(KEY_HEIGHT_LIMIT, mHeightLimit);
        json.put(KEY_LIMIT_WIDTH, mLimitWidth);
        json.put(KEY_WIDTH_LIMIT, mWidthLimit);

        return json;
    }

    public int getWidthLimit() {
        return mWidthLimit;
    }

    public boolean isBaseUrl() {
        return mBaseUrl;
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

    public void setBaseUrl(boolean baseUrl) {
        mBaseUrl = baseUrl;
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

    public void setWidthLimit(int maxWidthValue) {
        mWidthLimit = maxWidthValue;
    }

    @Override
    public String toDebugString() {
        return "ProfilePhoto{" + "mProfile=" + mProfile + ", mLimitHeight=" + mLimitHeight + ", mLimitWidth=" + mLimitWidth + ", mBaseUrl=" + mBaseUrl + ", mForceLowerCaseExtension=" + mForceLowerCaseExtension + ", mBaseUrlValue=" + mBaseUrlValue + ", mHeightLimit=" + mHeightLimit + ", mWidthLimit=" + mWidthLimit + '}';
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
