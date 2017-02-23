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

import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import org.apache.commons.cli.CommandLine;
import org.json.simple.JSONObject;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlsson
 */
public class ProfilePlacemark extends ProfileBase {

    public static final String KEY_DATE_PATTERN = "datePattern";
    public static final String KEY_NAME_BY = "nameBy";
    public static final String KEY_SCALE = "scale";
    public static final String KEY_SYMBOL_AS = "symbolAs";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_ZOOM = "zoom";
    public static final int NAME_BY_DATE = 2;
    public static final int NAME_BY_FILE = 1;
    public static final int NAME_BY_NONE = 0;
    public static final int SYMBOL_AS_PHOTO = 0;
    public static final int SYMBOL_AS_PIN = 1;

    private static final String COORDINATE = "coordinate";
    private static final String PLACEMARK_NAME = "placemark-name";

    private String[] mCoordinate;
    private SimpleDateFormat mDateFormat;
    private String mDatePattern = "yyyy-MM-dd HH.mm";
    private int mNameBy;
    private final Profile mProfile;
    private Double mScale = 3.0;
    private int mSymbolAs = 0;
    private boolean mTimestamp = true;
    private Double mZoom = 3.0;

    public ProfilePlacemark(Profile profile) {
        mProfile = profile;
    }

    public ProfilePlacemark(Profile profile, JSONObject json) {
        mProfile = profile;
        mScale = (Double) json.get(KEY_SCALE);
        mZoom = (Double) json.get(KEY_ZOOM);
        mNameBy = getInt(json, KEY_NAME_BY);
        mSymbolAs = getInt(json, KEY_SYMBOL_AS);
        mDatePattern = (String) json.get(KEY_DATE_PATTERN);
        mTimestamp = getBoolean(json, KEY_TIMESTAMP, mTimestamp);
    }

    public ProfilePlacemark(final Profile profile, CommandLine commandLine) {
        mProfile = profile;
        if (commandLine.hasOption(PLACEMARK_NAME)) {
            mDatePattern = commandLine.getOptionValue(PLACEMARK_NAME);
        }

        mCoordinate = commandLine.getOptionValues(COORDINATE);
    }

    public String[] getCoordinate() {
        return mCoordinate;
    }

    public SimpleDateFormat getDateFormat() {
        return mDateFormat;
    }

    public String getDatePattern() {
        return mDatePattern;
    }

    @Override
    public JSONObject getJson() {
        JSONObject json = new JSONObject();
        json.put(KEY_SCALE, mScale);
        json.put(KEY_ZOOM, mZoom);
        json.put(KEY_SYMBOL_AS, mSymbolAs);
        json.put(KEY_NAME_BY, mNameBy);
        json.put(KEY_DATE_PATTERN, mDatePattern);
        json.put(KEY_TIMESTAMP, mTimestamp);

        return json;
    }

    public int getNameBy() {
        return mNameBy;
    }

    public Double getScale() {
        return mScale;
    }

    public int getSymbolAs() {
        return mSymbolAs;
    }

    @Override
    public String getTitle() {
        return Dict.PLACEMARK.toString();
    }

    public Double getZoom() {
        return mZoom;
    }

    public boolean isSymbolAsPhoto() {
        return mSymbolAs == SYMBOL_AS_PHOTO;
    }

    public boolean isTimestamp() {
        return mTimestamp;
    }

    @Override
    public boolean isValid() {
        if (mNameBy == NAME_BY_DATE) {
            try {
                mDateFormat = new SimpleDateFormat(mDatePattern, mOptions.getLocale());
            } catch (Exception e) {
                addValidationError(String.format(mBundle.getString("invalid_value"), PLACEMARK_NAME, mDatePattern));
            }
        }

        return true;
    }

    public void setCoordinate(String[] coordinate) {
        mCoordinate = coordinate;
    }

    public void setDateFormat(SimpleDateFormat dateFormat) {
        mDateFormat = dateFormat;
    }

    public void setDatePattern(String datePattern) {
        mDatePattern = datePattern;
    }

    public void setNameBy(int nameBy) {
        mNameBy = nameBy;
    }

    public void setScale(Double scale) {
        mScale = scale;
    }

    public void setSymbolAs(int symbolAs) {
        mSymbolAs = symbolAs;
    }

    public void setTimestamp(boolean timestamp) {
        mTimestamp = timestamp;
    }

    public void setZoom(Double zoom) {
        mZoom = zoom;
    }

    @Override
    protected ProfileInfo getProfileInfo() {
        ProfileInfo profileInfo = new ProfileInfo();
        LinkedHashMap<String, String> values = new LinkedHashMap<>();

        String nameBy = mBundleUI.getString("ModulePlacemarkPanel.nameByNoRadioButton.text");

        switch (mNameBy) {
            case NAME_BY_DATE:
                nameBy = mDatePattern;
                break;

            case NAME_BY_FILE:
                nameBy = Dict.FILENAME.toString();
                break;

        }

        values.put(mBundleUI.getString("ModulePlacemarkPanel.nameByLabel.text"), nameBy);
        values.put(Dict.SYMBOL.toString(), mSymbolAs == SYMBOL_AS_PHOTO ? Dict.PHOTO.toString() : Dict.PIN.toString());
        values.put(Dict.SCALE.toString(), String.valueOf(mScale));
        values.put(Dict.ZOOM.toString(), String.valueOf(mZoom));

        profileInfo.setTitle(getTitle());
        profileInfo.setValues(values);

        return profileInfo;
    }
}
