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

import org.json.simple.JSONObject;

/**
 *
 * @author Patrik Karlsson
 */
public class ProfileDescription extends ProfileBase {

    public static final String KEY_ALTITUDE = "altitude";
    public static final String KEY_BEARING = "bearing";
    public static final String KEY_COORDINATE = "coordinate";
    public static final String KEY_CUSTOM = "custom";
    public static final String KEY_CUSTOM_VALUE = "customValue";
    public static final String KEY_DATE = "date";
    public static final String KEY_EXTERNAL_FILE = "externalFile";
    public static final String KEY_EXTERNAL_FILE_VALUE = "externalFileValue";
    public static final String KEY_FILENAME = "filename";
    public static final String KEY_PHOTO = "photo";

    private boolean mAltitude;
    private boolean mBearing;
    private boolean mCoordinate = true;
    private boolean mCustom;
    private String mCustomValue;
    private boolean mDate = true;
    private boolean mExternalFile;
    private String mExternalFileValue = "description.csv";
    private boolean mFilename = true;
    private boolean mPhoto = true;
    private final Profile mProfile;

    public ProfileDescription(Profile profile, JSONObject json) {
        mProfile = profile;
        mAltitude = getBoolean(json, KEY_ALTITUDE);
        mBearing = getBoolean(json, KEY_BEARING);
        mCoordinate = getBoolean(json, KEY_COORDINATE);
        mCustom = getBoolean(json, KEY_CUSTOM);
        mDate = getBoolean(json, KEY_DATE);
        mExternalFile = getBoolean(json, KEY_EXTERNAL_FILE);
        mFilename = getBoolean(json, KEY_FILENAME);
        mPhoto = getBoolean(json, KEY_PHOTO);

        mCustomValue = (String) json.get(KEY_CUSTOM_VALUE);
        mExternalFileValue = (String) json.get(KEY_EXTERNAL_FILE_VALUE);
    }

    public ProfileDescription(final Profile profile) {
        mProfile = profile;
    }

    public String getCustomValue() {
        return mCustomValue;
    }

    public String getExternalFileValue() {
        return mExternalFileValue;
    }

    public JSONObject getJson() {
        JSONObject json = new JSONObject();
        json.put(KEY_ALTITUDE, mAltitude);
        json.put(KEY_BEARING, mBearing);
        json.put(KEY_COORDINATE, mCoordinate);
        json.put(KEY_CUSTOM, mCustom);
        json.put(KEY_CUSTOM_VALUE, mCustomValue);
        json.put(KEY_DATE, mDate);
        json.put(KEY_EXTERNAL_FILE, mExternalFile);
        json.put(KEY_EXTERNAL_FILE_VALUE, mExternalFileValue);
        json.put(KEY_FILENAME, mFilename);
        json.put(KEY_PHOTO, mPhoto);

        return json;
    }

    public boolean isAltitude() {
        return mAltitude;
    }

    public boolean isBearing() {
        return mBearing;
    }

    public boolean isCoordinate() {
        return mCoordinate;
    }

    public boolean isCustom() {
        return mCustom;
    }

    public boolean isDate() {
        return mDate;
    }

    public boolean isExternalFile() {
        return mExternalFile;
    }

    public boolean isFilename() {
        return mFilename;
    }

    public boolean isPhoto() {
        return mPhoto;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    public void setAltitude(boolean altitude) {
        mAltitude = altitude;
    }

    public void setBearing(boolean bearing) {
        mBearing = bearing;
    }

    public void setCoordinate(boolean coordinate) {
        mCoordinate = coordinate;
    }

    public void setCustom(boolean custom) {
        mCustom = custom;
    }

    public void setCustomValue(String customValue) {
        mCustomValue = customValue;
    }

    public void setDate(boolean date) {
        mDate = date;
    }

    public void setExternalFile(boolean externalFile) {
        mExternalFile = externalFile;
    }

    public void setExternalFileValue(String externalFileValue) {
        mExternalFileValue = externalFileValue;
    }

    public void setFilename(boolean filename) {
        mFilename = filename;
    }

    public void setPhoto(boolean photo) {
        mPhoto = photo;
    }

    @Override
    public String toDebugString() {
        return "ProfileDescription{" + "mProfile=" + mProfile + ", mAltitude=" + mAltitude + ", mBearing=" + mBearing + ", mCoordinate=" + mCoordinate + ", mCustom=" + mCustom + ", mDate=" + mDate + ", mExternalFile=" + mExternalFile + ", mExternalFileValue=" + mExternalFileValue + ", mCustomValue=" + mCustomValue + ", mFilename=" + mFilename + ", mPhoto=" + mPhoto + '}';
    }
}
