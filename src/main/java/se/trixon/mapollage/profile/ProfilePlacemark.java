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

import com.google.gson.annotations.SerializedName;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import org.apache.commons.cli.CommandLine;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class ProfilePlacemark extends ProfileBase {

    private static final String COORDINATE = "coordinate";
    private static final String PLACEMARK_NAME = "placemark-name";

    private transient String[] mCoordinate;
    private transient SimpleDateFormat mDateFormat;
    @SerializedName("date_pattern")
    private String mDatePattern = "yyyy-MM-dd HH.mm";
    @SerializedName("name_by")
    private NameBy mNameBy = NameBy.NONE;
    private transient final Profile mProfile;
    @SerializedName("scale")
    private Double mScale = 3.0;
    @SerializedName("symbol_as")
    private SymbolAs mSymbolAs = SymbolAs.PHOTO;
    @SerializedName("time_stamp")
    private boolean mTimestamp = true;
    @SerializedName("zoom")
    private Double mZoom = 4.0;

    public ProfilePlacemark(Profile profile) {
        mProfile = profile;
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

    public NameBy getNameBy() {
        return mNameBy;
    }

    public Double getScale() {
        return mScale;
    }

    public SymbolAs getSymbolAs() {
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
        return mSymbolAs == SymbolAs.PHOTO;
    }

    public boolean isTimestamp() {
        return mTimestamp;
    }

    @Override
    public boolean isValid() {
        if (mNameBy == NameBy.DATE) {
            try {
                mDateFormat = new SimpleDateFormat(mDatePattern, mOptions.getLocale());
            } catch (IllegalArgumentException e) {
                addValidationError(String.format(BUNDLE.getString("invalid_value"), PLACEMARK_NAME, mDatePattern));
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

    public void setNameBy(NameBy nameBy) {
        mNameBy = nameBy;
    }

    public void setScale(Double scale) {
        mScale = scale;
    }

    public void setSymbolAs(SymbolAs symbolAs) {
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

        String nameBy = BUNDLE_UI.getString("PlacemarkTab.nameByNoRadioButton");

        switch (mNameBy) {
            case DATE:
                nameBy = mDatePattern;
                break;

            case FILE:
                nameBy = Dict.FILENAME.toString();
                break;
        }

        values.put(BUNDLE_UI.getString("PlacemarkTab.nameByLabel"), nameBy);
        values.put(Dict.SYMBOL.toString(), mSymbolAs == SymbolAs.PHOTO ? Dict.PHOTO.toString() : Dict.PIN.toString());
        values.put(Dict.SCALE.toString(), String.valueOf(mScale));
        values.put(Dict.ZOOM.toString(), String.valueOf(mZoom));

        profileInfo.setTitle(getTitle());
        profileInfo.setValues(values);

        return profileInfo;
    }

    public enum NameBy {
        NONE,
        FILE,
        DATE;
    }

    public enum SymbolAs {
        PHOTO,
        PIN;
    }
}
