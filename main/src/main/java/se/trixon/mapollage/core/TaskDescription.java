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
import java.util.LinkedHashMap;
import org.apache.commons.lang3.StringUtils;
import se.trixon.almond.util.BooleanHelper;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class TaskDescription extends TaskBase {

    private static final String DEFAULT_CUSTOM_VALUE;
    private static final String DEFAULT_EXTERNAL_FILE = "mapollage_descriptions.txt";

    @SerializedName("altitude")
    private boolean mAltitude;
    @SerializedName("bearing")
    private boolean mBearing;
    @SerializedName("coordinate")
    private boolean mCoordinate = true;
    @SerializedName("custom_value")
    private String mCustomValue;
    @SerializedName("date")
    private boolean mDate = true;
    @SerializedName("defaultMode")
    private DescriptionMode mDefaultMode = DescriptionMode.STATIC;
    @SerializedName("defaultTo")
    private boolean mDefaultTo = true;
    @SerializedName("externalFileValue")
    private String mExternalFileValue;
    @SerializedName("filename")
    private boolean mFilename = true;
    @SerializedName("mode")
    private DescriptionMode mMode = DescriptionMode.STATIC;
    @SerializedName("photo")
    private boolean mPhoto = true;
    private transient final Task mProfile;

    static {
        StringBuilder builder = new StringBuilder();
        builder.append(DescriptionSegment.PHOTO.toHtml())
                .append(DescriptionSegment.FILENAME.toHtml())
                .append(DescriptionSegment.DATE.toHtml())
                .append(DescriptionSegment.COORDINATE.toHtml())
                .append(DescriptionSegment.ALTITUDE.toHtml())
                .append(DescriptionSegment.BEARING.toHtml());

        DEFAULT_CUSTOM_VALUE = builder.toString();
    }

    public static String getDefaultCustomValue() {
        return DEFAULT_CUSTOM_VALUE;
    }

    public TaskDescription(final Task profile) {
        mProfile = profile;
    }

    public String getCustomValue() {
        if (StringUtils.isBlank(mCustomValue)) {
            return DEFAULT_CUSTOM_VALUE;
        } else {
            return mCustomValue;
        }
    }

    public DescriptionMode getDefaultMode() {
        return mDefaultMode;
    }

    public String getExternalFileValue() {
        if (mExternalFileValue == null) {
            mExternalFileValue = DEFAULT_EXTERNAL_FILE;
        }
        return mExternalFileValue;
    }

    public DescriptionMode getMode() {
        if (mMode == null) {
            mMode = DescriptionMode.STATIC;
        }
        return mMode;
    }

    @Override
    public String getTitle() {
        return Dict.DESCRIPTION.toString();
    }

    public boolean hasAltitude() {
        return mAltitude;
    }

    public boolean hasBearing() {
        return mBearing;
    }

    public boolean hasCoordinate() {
        return mCoordinate;
    }

    public boolean hasDate() {
        return mDate;
    }

    public boolean hasFilename() {
        return mFilename;
    }

    public boolean hasPhoto() {
        return mPhoto;
    }

    public boolean hasPhotoStaticOrDynamic() {
        boolean hasPhoto = (mMode == DescriptionMode.CUSTOM && StringUtils.containsIgnoreCase(mCustomValue, "+photo"))
                || (mMode != DescriptionMode.STATIC && hasPhoto())
                || mMode == DescriptionMode.EXTERNAL;

        return hasPhoto;
    }

    public boolean isDefaultTo() {
        return mDefaultTo;
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

    public void setCustomValue(String customValue) {
        mCustomValue = customValue;
    }

    public void setDate(boolean date) {
        mDate = date;
    }

    public void setDefaultMode(DescriptionMode defaultMode) {
        mDefaultMode = defaultMode;
    }

    public void setDefaultTo(boolean defaultTo) {
        mDefaultTo = defaultTo;
    }

    public void setExternalFileValue(String externalFileValue) {
        mExternalFileValue = externalFileValue;
    }

    public void setFilename(boolean filename) {
        mFilename = filename;
    }

    public void setMode(DescriptionMode mode) {
        mMode = mode;
    }

    public void setPhoto(boolean photo) {
        mPhoto = photo;
    }

    @Override
    protected TaskInfo getProfileInfo() {
        TaskInfo profileInfo = new TaskInfo();
        LinkedHashMap<String, String> values = new LinkedHashMap<>();
        String mode = null;
        if (null != mMode) {
            switch (mMode) {
                case CUSTOM:
                    mode = Dict.CUSTOMIZED.toString();
                    break;

                case EXTERNAL:
                    mode = Dict.EXTERNAL_FILE.toString();
                    break;

                case NONE:
                    mode = Dict.NONE.toString();
                    break;

                case STATIC:
                    mode = Dict.STATIC.toString();
                    break;
            }
        }
        values.put(Dict.TYPE.toString(), mode);

        if (null != mMode) {
            switch (mMode) {
                case CUSTOM:
                    String value = mCustomValue != null ? mCustomValue.replaceAll("\\n", "\\\\n") : "";
                    values.put(Dict.VALUE.toString(), value);
                    break;

                case EXTERNAL:
                    values.put(Dict.FILE.toString(), mExternalFileValue);
                    if (mDefaultTo) {
                        String defaultValue = Dict.CUSTOMIZED.toString();
                        if (mMode == DescriptionMode.STATIC) {
                            defaultValue = Dict.STATIC.toString();
                        }
                        values.put(Dict.DEFAULT.toString(), defaultValue);
                    }
                    break;

                case STATIC:
                    values.put(Dict.PHOTO.toString(), BooleanHelper.asYesNo(mPhoto));
                    values.put(Dict.FILENAME.toString(), BooleanHelper.asYesNo(mFilename));
                    values.put(Dict.DATE.toString(), BooleanHelper.asYesNo(mDate));
                    values.put(Dict.COORDINATE.toString(), BooleanHelper.asYesNo(mCoordinate));
                    values.put(Dict.ALTITUDE.toString(), BooleanHelper.asYesNo(mAltitude));
                    values.put(Dict.BEARING.toString(), BooleanHelper.asYesNo(mBearing));
                    break;
            }
        }

        profileInfo.setTitle(getTitle());
        profileInfo.setValues(values);

        return profileInfo;
    }

    public enum DescriptionMode {
        CUSTOM, EXTERNAL, NONE, STATIC,
    }

    public enum DescriptionSegment {
        ALTITUDE, BEARING, COORDINATE, DATE, FILENAME, PHOTO;

        @Override
        public String toString() {
            return String.format("+%s", name().toLowerCase());
        }

        public String toHtml() {
            String begTag = "<p>";
            String endTag = "</p>";
            if (this == FILENAME) {
                begTag = "<h2>";
                endTag = "</h2>";

            }
            return String.format("%s%s%s\n", begTag, toString(), endTag);
        }
    }
}
