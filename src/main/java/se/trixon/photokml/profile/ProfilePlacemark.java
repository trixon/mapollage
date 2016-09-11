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

import java.text.SimpleDateFormat;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang3.math.NumberUtils;
import se.trixon.photokml.PhotoKml;

/**
 *
 * @author Patrik Karlsson
 */
public class ProfilePlacemark extends ProfileBase {

    public boolean isIncludeNullCoordinate() {
        return mIncludeNullCoordinate;
    }

    public void setIncludeNullCoordinate(boolean includeNullCoordinate) {
        this.mIncludeNullCoordinate = includeNullCoordinate;
    }

    private boolean mIncludeNullCoordinate;
    private boolean mByDate;
    private boolean mByFilename;
    private SimpleDateFormat mDateFormat;
    private String mDatePattern = "yyyy-MM-dd HH.mm";
    ;
    private String mDesccription = "";
    private final Profile mProfile;
    private Double mLat = 57.6;
    private Double mLon = 11.3;
    private String[] mCoordinate;
    private int nameBy;

    public int getNameBy() {
        return nameBy;
    }

    public void setNameBy(int nameBy) {
        this.nameBy = nameBy;
    }

    public boolean isByDate() {
        return mByDate;
    }

    public void setByDate(boolean byDate) {
        this.mByDate = byDate;
    }

    public boolean isByFilename() {
        return mByFilename;
    }

    public void setByFilename(boolean byFilename) {
        this.mByFilename = byFilename;
    }

    public SimpleDateFormat getDateFormat() {
        return mDateFormat;
    }

    public void setDateFormat(SimpleDateFormat dateFormat) {
        this.mDateFormat = dateFormat;
    }

    public String getDatePattern() {
        return mDatePattern;
    }

    public void setDatePattern(String datePattern) {
        this.mDatePattern = datePattern;
    }

    public String getDesccription() {
        return mDesccription;
    }

    public void setDesccription(String desccription) {
        this.mDesccription = desccription;
    }

    public Double getLat() {
        return mLat;
    }

    public void setLat(Double lat) {
        this.mLat = lat;
    }

    public Double getLon() {
        return mLon;
    }

    public void setLon(Double lon) {
        this.mLon = lon;
    }

    public String[] getCoordinate() {
        return mCoordinate;
    }

    public void setCoordinate(String[] coordinate) {
        this.mCoordinate = coordinate;
    }

    public ProfilePlacemark(final Profile profile) {
        mProfile = profile;
    }

    public ProfilePlacemark(CommandLine commandLine, final Profile profile) {
        mProfile = profile;
        if (commandLine.hasOption(PhotoKml.PLACEMARK_NAME)) {
            mDatePattern = commandLine.getOptionValue(PhotoKml.PLACEMARK_NAME);
            mByDate = mDatePattern != null;
            mByFilename = !mByDate;
        }
        mCoordinate = commandLine.getOptionValues(PhotoKml.COORDINATE);

        mDesccription = commandLine.getOptionValue(PhotoKml.PLACEMARK_DESC);
    }

    public boolean hasCoordinate() {
        return mLat != null && mLon != null;
    }

    @Override
    public boolean isValid() {
        if (mByDate) {
            try {
                mDateFormat = new SimpleDateFormat(mDatePattern);
            } catch (Exception e) {
                mProfile.addValidationError(String.format(mBundle.getString("invalid_value"), PhotoKml.PLACEMARK_NAME, mDatePattern));

            }
        }

        if (mCoordinate != null) {
            try {
                mLat = NumberUtils.createDouble(mCoordinate[0]);
            } catch (NumberFormatException e) {
                mProfile.addValidationError(String.format(mBundle.getString("invalid_value"), PhotoKml.COORDINATE, mCoordinate[0]));
            }

            try {
                mLon = NumberUtils.createDouble(mCoordinate[1]);
            } catch (NumberFormatException e) {
                mProfile.addValidationError(String.format(mBundle.getString("invalid_value"), PhotoKml.COORDINATE, mCoordinate[1]));
            }
        }

        return true;
    }

    @Override
    public String toDebugString() {
        return "\n PlacemarkByDate=" + mByDate
                + "\n PlacemarkByFilename=" + mByFilename
                + "\n PlacemarkDatePattern=" + mDatePattern
                + "\n PlacemarkDesc=" + mDesccription
                + "\n";
    }
}
