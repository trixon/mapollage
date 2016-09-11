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
package se.trixon.photokml;

import java.util.prefs.Preferences;
import org.apache.commons.lang3.SystemUtils;

/**
 *
 * @author Patrik Karlsson <patrik@trixon.se>
 */
public enum Options {

    INSTANCE;
    public static final String KEY_DESCRIPTION_ALTITUDE = "descriptionAltitude";
    public static final String KEY_DESCRIPTION_BEARING = "descriptionBearing";
    public static final String KEY_DESCRIPTION_COORDINATE = "descriptionCoordinate";
    public static final String KEY_DESCRIPTION_CUSTOM = "descriptionCustom";
    public static final String KEY_DESCRIPTION_CUSTOM_TEXT = "descriptionCustomText";
    public static final String KEY_DESCRIPTION_DATE = "descriptionDate";
    public static final String KEY_DESCRIPTION_EXTERNAL_FILE = "descriptionExternalFile";
    public static final String KEY_DESCRIPTION_EXTERNAL_FILE_VALUE = "descriptionExternalFileValue";
    public static final String KEY_DESCRIPTION_FILENAME = "descriptionFilename";
    public static final String KEY_DESCRIPTION_PHOTO = "descriptionPhoto";

    public static final String KEY_PHOTO_BALLOON_MAX_HEIGHT = "photoBalloonMaxHeight";
    public static final String KEY_PHOTO_BALLOON_MAX_HEIGHT_VALUE = "photoBalloonMaxHeighValuet";
    public static final String KEY_PHOTO_BALLOON_MAX_WIDTH = "photoBalloonMaxWidth";
    public static final String KEY_PHOTO_BALLOON_MAX_WIDTH_VALUE = "photoBalloonMaxWidthValue";
    public static final String KEY_PHOTO_BASE_URL = "photoBaseUrl";
    public static final String KEY_PHOTO_BASE_URL_VALUE = "photoBaseUrlValue";
    public static final String KEY_PHOTO_FORCE_LOWER_CASE_EXTENSION = "photoForceLowerCaseExtension";

    public static final String KEY_PLACEMARK_DATE_PATTERN = "placemarkDatePattern";
    public static final String KEY_PLACEMARK_INCLUDE_NULL_COORDINATE = "placemarkIncludeNullCoordinate";
    public static final String KEY_PLACEMARK_LAT = "placemarkLat";
    public static final String KEY_PLACEMARK_LON = "placemarkLon";
    public static final String KEY_PLACEMARK_NAME_BY = "placemarkNameBy";

    private static final boolean DEFAULT_DESCRIPTION_ALTITUDE = false;
    private static final boolean DEFAULT_DESCRIPTION_BEARING = false;
    private static final boolean DEFAULT_DESCRIPTION_COORDINATE = true;
    private static final boolean DEFAULT_DESCRIPTION_CUSTOM = false;
    private static final String DEFAULT_DESCRIPTION_CUSTOM_TEXT = "";
    private static final boolean DEFAULT_DESCRIPTION_DATE = true;
    private static final boolean DEFAULT_DESCRIPTION_EXTERNAL_FILE = true;
    private static final String DEFAULT_DESCRIPTION_EXTERNAL_FILE_VALUE = "description.csv";
    private static final boolean DEFAULT_DESCRIPTION_FILENAME = true;
    private static final boolean DEFAULT_DESCRIPTION_PHOTO = true;

    private static final String DEFAULT_FOLDERS_DATE_PATTERN = "yyyy-ww";
    private static final String DEFAULT_FOLDERS_REGEX = "\\d{8}";
    private static final String DEFAULT_FOLDERS_REGEX_DEFAULT = "12345678";

    private static final boolean DEFAULT_PHOTO_BALLOON_MAX_HEIGHT = true;
    private static final int DEFAULT_PHOTO_BALLOON_MAX_HEIGHT_VALUE = 400;
    private static final boolean DEFAULT_PHOTO_BALLOON_MAX_WIDTH = true;
    private static final int DEFAULT_PHOTO_BALLOON_MAX_WIDTH_VALUE = 400;
    private static final boolean DEFAULT_PHOTO_BASE_URL = false;
    private static final String DEFAULT_PHOTO_BASE_URL_VALUE = "http://www.domain.com/img/";
    private static final boolean DEFAULT_PHOTO_FORCE_LOWER_CASE_EXTENSION = SystemUtils.IS_OS_WINDOWS;

    private static final String DEFAULT_PLACEMARK_DATE_PATTERN = "yyyy-MM-dd HH.mm";
    private static final boolean DEFAULT_PLACEMARK_INCLUDE_NULL_COORDINATE = false;
    private static final double DEFAULT_PLACEMARK_LAT = 57.6;
    private static final double DEFAULT_PLACEMARK_LON = 11.3;
    private static final int DEFAULT_PLACEMARK_NAME_BY = 0;

    private static final Preferences mPreferences = Preferences.userNodeForPackage(Options.class);

    public static Preferences getPreferences() {
        return mPreferences;
    }

    private Options() {
        init();
    }

    public String getDescriptionCustomText() {
        return mPreferences.get(KEY_DESCRIPTION_CUSTOM_TEXT, DEFAULT_DESCRIPTION_CUSTOM_TEXT);
    }

    public String getDescriptionExternalFileValue() {
        return mPreferences.get(KEY_DESCRIPTION_EXTERNAL_FILE_VALUE, DEFAULT_DESCRIPTION_EXTERNAL_FILE_VALUE);
    }

    public int getPhotoBalloonMaxHeightValue() {
        return mPreferences.getInt(KEY_PHOTO_BALLOON_MAX_HEIGHT_VALUE, DEFAULT_PHOTO_BALLOON_MAX_HEIGHT_VALUE);
    }

    public int getPhotoBalloonMaxWidthValue() {
        return mPreferences.getInt(KEY_PHOTO_BALLOON_MAX_WIDTH_VALUE, DEFAULT_PHOTO_BALLOON_MAX_WIDTH_VALUE);
    }

    public String getPhotoBaseUrlValue() {
        return mPreferences.get(KEY_PHOTO_BASE_URL_VALUE, DEFAULT_PHOTO_BASE_URL_VALUE);
    }

    public String getPlacemarkDatePattern() {
        return mPreferences.get(KEY_PLACEMARK_DATE_PATTERN, DEFAULT_PLACEMARK_DATE_PATTERN);
    }

    public double getPlacemarkLat() {
        return mPreferences.getDouble(KEY_PLACEMARK_LAT, DEFAULT_PLACEMARK_LAT);
    }

    public double getPlacemarkLon() {
        return mPreferences.getDouble(KEY_PLACEMARK_LON, DEFAULT_PLACEMARK_LON);
    }

    public int getPlacemarkNameBy() {
        return mPreferences.getInt(KEY_PLACEMARK_NAME_BY, DEFAULT_PLACEMARK_NAME_BY);
    }

    public boolean isDescriptionAltitude() {
        return mPreferences.getBoolean(KEY_DESCRIPTION_ALTITUDE, DEFAULT_DESCRIPTION_ALTITUDE);
    }

    public boolean isDescriptionBearing() {
        return mPreferences.getBoolean(KEY_DESCRIPTION_BEARING, DEFAULT_DESCRIPTION_BEARING);
    }

    public boolean isDescriptionCoordinate() {
        return mPreferences.getBoolean(KEY_DESCRIPTION_COORDINATE, DEFAULT_DESCRIPTION_COORDINATE);
    }

    public boolean isDescriptionCustom() {
        return mPreferences.getBoolean(KEY_DESCRIPTION_CUSTOM, DEFAULT_DESCRIPTION_CUSTOM);
    }

    public boolean isDescriptionDate() {
        return mPreferences.getBoolean(KEY_DESCRIPTION_DATE, DEFAULT_DESCRIPTION_DATE);
    }

    public boolean isDescriptionExternalFile() {
        return mPreferences.getBoolean(KEY_DESCRIPTION_EXTERNAL_FILE, DEFAULT_DESCRIPTION_EXTERNAL_FILE);
    }

    public boolean isDescriptionFilename() {
        return mPreferences.getBoolean(KEY_DESCRIPTION_FILENAME, DEFAULT_DESCRIPTION_FILENAME);
    }

    public boolean isDescriptionPhoto() {
        return mPreferences.getBoolean(KEY_DESCRIPTION_PHOTO, DEFAULT_DESCRIPTION_PHOTO);
    }

    public boolean isPhotoBalloonMaxHeight() {
        return mPreferences.getBoolean(KEY_PHOTO_BALLOON_MAX_HEIGHT, DEFAULT_PHOTO_BALLOON_MAX_HEIGHT);
    }

    public boolean isPhotoBalloonMaxWidth() {
        return mPreferences.getBoolean(KEY_PHOTO_BALLOON_MAX_WIDTH, DEFAULT_PHOTO_BALLOON_MAX_WIDTH);
    }

    public boolean isPhotoBaseUrl() {
        return mPreferences.getBoolean(KEY_PHOTO_BASE_URL, DEFAULT_PHOTO_BASE_URL);
    }

    public boolean isPhotoForceLowerCaseExtension() {
        return mPreferences.getBoolean(KEY_PHOTO_FORCE_LOWER_CASE_EXTENSION, DEFAULT_PHOTO_FORCE_LOWER_CASE_EXTENSION);
    }

    public boolean isPlacemarkIncludeNullCoordinate() {
        return mPreferences.getBoolean(KEY_PLACEMARK_INCLUDE_NULL_COORDINATE, DEFAULT_PLACEMARK_INCLUDE_NULL_COORDINATE);
    }

    public void setDescriptionAltitude(boolean value) {
        mPreferences.putBoolean(KEY_DESCRIPTION_ALTITUDE, value);
    }

    public void setDescriptionBearing(boolean value) {
        mPreferences.putBoolean(KEY_DESCRIPTION_BEARING, value);
    }

    public void setDescriptionCoordinate(boolean value) {
        mPreferences.putBoolean(KEY_DESCRIPTION_COORDINATE, value);
    }

    public void setDescriptionCustom(boolean value) {
        mPreferences.putBoolean(KEY_DESCRIPTION_CUSTOM, value);
    }

    public void setDescriptionCustomText(String value) {
        mPreferences.put(KEY_DESCRIPTION_CUSTOM_TEXT, value);
    }

    public void setDescriptionDate(boolean value) {
        mPreferences.putBoolean(KEY_DESCRIPTION_DATE, value);
    }

    public void setDescriptionExternalFile(boolean value) {
        mPreferences.putBoolean(KEY_DESCRIPTION_EXTERNAL_FILE, value);
    }

    public void setDescriptionExternalFileValue(String value) {
        mPreferences.put(KEY_DESCRIPTION_EXTERNAL_FILE_VALUE, value);
    }

    public void setDescriptionFilename(boolean value) {
        mPreferences.putBoolean(KEY_DESCRIPTION_FILENAME, value);
    }

    public void setDescriptionPhoto(boolean value) {
        mPreferences.putBoolean(KEY_DESCRIPTION_PHOTO, value);
    }

    public void setPhotoBalloonMaxHeight(boolean value) {
        mPreferences.putBoolean(KEY_PHOTO_BALLOON_MAX_HEIGHT, value);
    }

    public void setPhotoBalloonMaxHeightValue(int value) {
        mPreferences.putInt(KEY_PHOTO_BALLOON_MAX_HEIGHT_VALUE, value);
    }

    public void setPhotoBalloonMaxWidth(boolean value) {
        mPreferences.putBoolean(KEY_PHOTO_BALLOON_MAX_WIDTH, value);
    }

    public void setPhotoBalloonMaxWidthValue(int value) {
        mPreferences.putInt(KEY_PHOTO_BALLOON_MAX_WIDTH_VALUE, value);
    }

    public void setPhotoBaseUrl(boolean value) {
        mPreferences.putBoolean(KEY_PHOTO_BASE_URL, value);
    }

    public void setPhotoBaseUrlValue(String value) {
        mPreferences.put(KEY_PHOTO_BASE_URL_VALUE, value);
    }

    public void setPhotoForceLowerCaseExtension(boolean value) {
        mPreferences.putBoolean(KEY_PHOTO_FORCE_LOWER_CASE_EXTENSION, value);
    }

    public void setPlacemarkDatePattern(String value) {
        mPreferences.put(KEY_PLACEMARK_DATE_PATTERN, value);
    }

    public void setPlacemarkIncludeNullCoordinate(boolean value) {
        mPreferences.putBoolean(KEY_PLACEMARK_INCLUDE_NULL_COORDINATE, value);
    }

    public void setPlacemarkLat(double value) {
        mPreferences.putDouble(KEY_PLACEMARK_LAT, value);
    }

    public void setPlacemarkLon(double value) {
        mPreferences.putDouble(KEY_PLACEMARK_LON, value);
    }

    public void setPlacemarkNameBy(int value) {
        mPreferences.putInt(KEY_PLACEMARK_NAME_BY, value);
    }

    private void init() {
    }
}
