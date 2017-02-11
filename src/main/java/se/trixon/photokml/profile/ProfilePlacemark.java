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

import java.text.SimpleDateFormat;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.simple.JSONObject;
import se.trixon.photokml.PhotoKml;

/**
 *
 * @author Patrik Karlsson
 */
public class ProfilePlacemark extends ProfileBase {

    public static final String KEY_DATE_PATTERN = "datePattern";
    public static final String KEY_INCLUDE_NULL_COORDINATE = "includeNullCoordinate";
    public static final String KEY_LAT = "lat";
    public static final String KEY_LON = "lon";
    public static final String KEY_NAME_BY = "nameBy";
    public static final int NAME_BY_DATE = 2;
    public static final int NAME_BY_FILE = 1;
    public static final int NAME_BY_NONE = 0;

    private String[] mCoordinate;
    private SimpleDateFormat mDateFormat;
    private String mDatePattern = "yyyy-MM-dd HH.mm";
    private boolean mIncludeNullCoordinate;
    private Double mLat = 57.6;
    private Double mLon = 11.3;
    private int mNameBy;
    private final Profile mProfile;

    public ProfilePlacemark(Profile profile) {
        mProfile = profile;
    }

    public ProfilePlacemark(Profile profile, JSONObject json) {
        mProfile = profile;
        mLat = (Double) json.get(KEY_LAT);
        mLon = (Double) json.get(KEY_LON);
        mNameBy = getInt(json, KEY_NAME_BY);
        mIncludeNullCoordinate = getBoolean(json, KEY_INCLUDE_NULL_COORDINATE);
        mDatePattern = (String) json.get(KEY_DATE_PATTERN);
    }

    public ProfilePlacemark(final Profile profile, CommandLine commandLine) {
        mProfile = profile;
        if (commandLine.hasOption(PhotoKml.PLACEMARK_NAME)) {
            mDatePattern = commandLine.getOptionValue(PhotoKml.PLACEMARK_NAME);
        }

        mCoordinate = commandLine.getOptionValues(PhotoKml.COORDINATE);
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
        json.put(KEY_LAT, mLat);
        json.put(KEY_LON, mLon);
        json.put(KEY_NAME_BY, mNameBy);
        json.put(KEY_INCLUDE_NULL_COORDINATE, mIncludeNullCoordinate);
        json.put(KEY_DATE_PATTERN, mDatePattern);

        return json;
    }

    public Double getLat() {
        return mLat;
    }

    public Double getLon() {
        return mLon;
    }

    public int getNameBy() {
        return mNameBy;
    }

    public boolean hasCoordinate() {
        return mLat != null && mLon != null;
    }

    public boolean isIncludeNullCoordinate() {
        return mIncludeNullCoordinate;
    }

    @Override
    public boolean isValid() {
        if (mNameBy == NAME_BY_DATE) {
            try {
                mDateFormat = new SimpleDateFormat(mDatePattern, mOptions.getLocale());
            } catch (Exception e) {
                addValidationError(String.format(mBundle.getString("invalid_value"), PhotoKml.PLACEMARK_NAME, mDatePattern));
            }
        }

        if (mCoordinate != null) {
            try {
                mLat = NumberUtils.createDouble(mCoordinate[0]);
            } catch (NumberFormatException e) {
                addValidationError(String.format(mBundle.getString("invalid_value"), PhotoKml.COORDINATE, mCoordinate[0]));
            }

            try {
                mLon = NumberUtils.createDouble(mCoordinate[1]);
            } catch (NumberFormatException e) {
                addValidationError(String.format(mBundle.getString("invalid_value"), PhotoKml.COORDINATE, mCoordinate[1]));
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

    public void setIncludeNullCoordinate(boolean includeNullCoordinate) {
        mIncludeNullCoordinate = includeNullCoordinate;
    }

    public void setLat(Double lat) {
        mLat = lat;
    }

    public void setLon(Double lon) {
        mLon = lon;
    }

    public void setNameBy(int nameBy) {
        mNameBy = nameBy;
    }

    @Override
    public String toDebugString() {
        return "ProfilePlacemark{" + ", mCoordinate=" + mCoordinate + ", mDateFormat=" + mDateFormat + ", mDatePattern=" + mDatePattern + ", mIncludeNullCoordinate=" + mIncludeNullCoordinate + ", mLat=" + mLat + ", mLon=" + mLon + ", mNameBy=" + mNameBy + ", mProfile=" + mProfile + '}';
    }

    @Override
    public String toString() {
        return "ProfilePlacemark{" + "mIncludeNullCoordinate=" + mIncludeNullCoordinate + ", mDateFormat=" + mDateFormat + ", mDatePattern=" + mDatePattern + ", mProfile=" + mProfile + ", mLat=" + mLat + ", mLon=" + mLon + ", mCoordinate=" + mCoordinate + ", nameBy=" + mNameBy + '}';
    }
}
