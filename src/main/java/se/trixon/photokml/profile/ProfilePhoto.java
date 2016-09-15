/*
 * Copyright 2016 Patrik Karlsson.
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

    public static final String KEY_MAX_HEIGHT = "maxHeight";
    public static final String KEY_MAX_HEIGHT_VALUE = "maxHeighValuet";
    public static final String KEY_MAX_WIDTH = "maxWidth";
    public static final String KEY_MAX_WIDTH_VALUE = "maxWidthValue";
    private boolean mBaseUrl = true;
    private String mBaseUrlValue = "http://www.domain.com/img/";
    private boolean mForceLowerCaseExtension = SystemUtils.IS_OS_WINDOWS;
    private boolean mMaxHeight = true;
    private int mMaxHeightValue = 400;
    private boolean mMaxWidth = true;
    private int mMaxWidthValue = 400;
    private final Profile mProfile;

    public ProfilePhoto(Profile profile) {
        mProfile = profile;
    }

    public ProfilePhoto(Profile profile, JSONObject json) {
        mProfile = profile;
        mMaxHeightValue = getInt(json, KEY_MAX_HEIGHT_VALUE);
        mMaxWidthValue = getInt(json, KEY_MAX_WIDTH_VALUE);
        mMaxHeight = getBoolean(json, KEY_MAX_HEIGHT);
        mMaxWidth = getBoolean(json, KEY_MAX_WIDTH);
        mBaseUrl = getBoolean(json, KEY_BASE_URL);
        mBaseUrlValue = (String) json.get(KEY_BASE_URL_VALUE);
    }

    public String getBaseUrlValue() {
        return mBaseUrlValue;
    }

    @Override
    public JSONObject getJson() {
        JSONObject json = new JSONObject();
        json.put(KEY_BASE_URL, mBaseUrl);
        json.put(KEY_BASE_URL_VALUE, mBaseUrlValue);
        json.put(KEY_FORCE_LOWER_CASE_EXTENSION, mForceLowerCaseExtension);
        json.put(KEY_MAX_HEIGHT, mMaxHeight);
        json.put(KEY_MAX_HEIGHT_VALUE, mMaxHeightValue);
        json.put(KEY_MAX_WIDTH, mMaxWidth);
        json.put(KEY_MAX_WIDTH_VALUE, mMaxWidthValue);

        return json;
    }

    public int getMaxHeightValue() {
        return mMaxHeightValue;
    }

    public int getMaxWidthValue() {
        return mMaxWidthValue;
    }

    public boolean isBaseUrl() {
        return mBaseUrl;
    }

    public boolean isForceLowerCaseExtension() {
        return mForceLowerCaseExtension;
    }

    public boolean isMaxHeight() {
        return mMaxHeight;
    }

    public boolean isMaxWidth() {
        return mMaxWidth;
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

    public void setMaxHeight(boolean maxHeight) {
        mMaxHeight = maxHeight;
    }

    public void setMaxHeightValue(int maxHeightValue) {
        mMaxHeightValue = maxHeightValue;
    }

    public void setMaxWidth(boolean maxWidth) {
        mMaxWidth = maxWidth;
    }

    public void setMaxWidthValue(int maxWidthValue) {
        mMaxWidthValue = maxWidthValue;
    }

    @Override
    public String toDebugString() {
        return "ProfilePhoto{" + "mProfile=" + mProfile + ", mMaxHeight=" + mMaxHeight + ", mMaxWidth=" + mMaxWidth + ", mBaseUrl=" + mBaseUrl + ", mForceLowerCaseExtension=" + mForceLowerCaseExtension + ", mBaseUrlValue=" + mBaseUrlValue + ", mMaxHeightValue=" + mMaxHeightValue + ", mMaxWidthValue=" + mMaxWidthValue + '}';
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
