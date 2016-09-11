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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.math.NumberUtils;
import se.trixon.almond.util.BundleHelper;
import se.trixon.photokml.PhotoKml;

/**
 *
 * @author Patrik Karlsson
 */
public class Profile extends ProfileBase implements Comparable<Profile>, Cloneable {

    private String mAbsolutePath;
    private final ResourceBundle mBundle = BundleHelper.getBundle(Profile.class, "Bundle");
    private String[] mCoordinate;
    private File mDestFile;
    private ProfileFolder mFolder = new ProfileFolder(this);
    private String mFolderDesc;
    private Double mLat;
    private Double mLon;
    private boolean mLowerCaseExt;
    private Integer mMaxHeight;
    private String mMaxHeightString;
    private Integer mMaxWidth;
    private String mMaxWidthString;
    private String mName;
    private boolean mPlacemarkByDate;
    private boolean mPlacemarkByFilename;
    private SimpleDateFormat mPlacemarkDateFormat;
    private String mPlacemarkDatePattern;
    private String mPlacemarkDesc = "";
    private ProfileSource mSource = new ProfileSource(this);
    private StringBuilder mValidationErrorBuilder;

    public Profile() {

    }

    public Profile(CommandLine commandLine) {
        mFolder = new ProfileFolder(commandLine, this);
        mFolderDesc = commandLine.getOptionValue(PhotoKml.FOLDER_DESC);

        if (commandLine.hasOption(PhotoKml.PLACEMARK_NAME)) {
            mPlacemarkDatePattern = commandLine.getOptionValue(PhotoKml.PLACEMARK_NAME);
            mPlacemarkByDate = mPlacemarkDatePattern != null;
            mPlacemarkByFilename = !mPlacemarkByDate;
        }

        mPlacemarkDesc = commandLine.getOptionValue(PhotoKml.PLACEMARK_DESC);

        mMaxHeightString = commandLine.getOptionValue(PhotoKml.MAX_HEIGHT);
        mMaxWidthString = commandLine.getOptionValue(PhotoKml.MAX_WIDTH);

        mCoordinate = commandLine.getOptionValues(PhotoKml.COORDINATE);

        mLowerCaseExt = commandLine.hasOption(PhotoKml.LOWER_CASE_EXT);
        mAbsolutePath = commandLine.getOptionValue(PhotoKml.ABSOLUTE_PATH);

        mSource.setFollowLinks(commandLine.hasOption(PhotoKml.LINKS));
        mSource.setRecursive(commandLine.hasOption(PhotoKml.RECURSIVE));
        setSourceAndDest(commandLine.getArgs());
    }

    @Override
    public Profile clone() {
        try {
            return (Profile) super.clone();
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(Profile.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public int compareTo(Profile o) {
        return mName.compareTo(o.getName());
    }

    public String getAbsolutePath() {
        return mAbsolutePath;
    }

    public File getDestFile() {
        return mDestFile;
    }

    public ProfileFolder getFolder() {
        return mFolder;
    }

    public String getFolderDesc() {
        return mFolderDesc;
    }

    public Double getLat() {
        return mLat;
    }

    public Double getLon() {
        return mLon;
    }

    public Integer getMaxHeight() {
        return mMaxHeight;
    }

    public Integer getMaxWidth() {
        return mMaxWidth;
    }

    public String getName() {
        return mName;
    }

    public SimpleDateFormat getPlacemarkDateFormat() {
        return mPlacemarkDateFormat;
    }

    public String getPlacemarkDatePattern() {
        return mPlacemarkDatePattern;
    }

    public String getPlacemarkDesc() {
        return mPlacemarkDesc;
    }

    public ProfileSource getSource() {
        return mSource;
    }

    public String getValidationError() {
        return mValidationErrorBuilder.toString();
    }

    public boolean hasCoordinate() {
        return mLat != null && mLon != null;
    }

    public boolean isLowerCaseExt() {
        return mLowerCaseExt;
    }

    public boolean isPlacemarkByDate() {
        return mPlacemarkByDate;
    }

    public boolean isPlacemarkByFilename() {
        return mPlacemarkByFilename;
    }

    public boolean isValid() {
        mValidationErrorBuilder = new StringBuilder();
        mFolder.isValid();

        if (mPlacemarkByDate) {
            try {
                mPlacemarkDateFormat = new SimpleDateFormat(mPlacemarkDatePattern);
            } catch (Exception e) {
                addValidationError(String.format(mBundle.getString("invalid_value"), PhotoKml.PLACEMARK_NAME, mPlacemarkDatePattern));

            }
        }

        if (mMaxHeightString != null) {
            try {
                mMaxHeight = NumberUtils.createInteger(mMaxHeightString);
            } catch (NumberFormatException e) {
                addValidationError(String.format(mBundle.getString("invalid_value"), PhotoKml.MAX_HEIGHT, mMaxHeightString));
            }
        }

        if (mMaxWidthString != null) {
            try {
                mMaxWidth = NumberUtils.createInteger(mMaxWidthString);
            } catch (NumberFormatException e) {
                addValidationError(String.format(mBundle.getString("invalid_value"), PhotoKml.MAX_WIDTH, mMaxWidthString));
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

        mSource.isValid();

        return mValidationErrorBuilder.length() == 0;
    }

    public void setAbsolutePath(String absolutePath) {
        mAbsolutePath = absolutePath;
    }

    public void setDestFile(File dest) {
        mDestFile = dest;
    }

    public void setFolderDesc(String folderDesc) {
        mFolderDesc = folderDesc;
    }

    public void setLat(Double lat) {
        mLat = lat;
    }

    public void setLon(Double lon) {
        mLon = lon;
    }

    public void setLowerCaseExt(boolean lowerCaseExt) {
        mLowerCaseExt = lowerCaseExt;
    }

    public void setMaxHeight(Integer height) {
        mMaxHeight = height;
    }

    public void setMaxWidth(Integer width) {
        mMaxWidth = width;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setPlacemarkByDate(boolean placemarkByDate) {
        mPlacemarkByDate = placemarkByDate;
    }

    public void setPlacemarkByFilename(boolean placemarkByFilename) {
        mPlacemarkByFilename = placemarkByFilename;
    }

    public void setPlacemarkDateFormat(SimpleDateFormat placemarkDateFormat) {
        mPlacemarkDateFormat = placemarkDateFormat;
    }

    public void setPlacemarkDatePattern(String placemarkDatePattern) {
        mPlacemarkDatePattern = placemarkDatePattern;
    }

    public void setPlacemarkDesc(String placemarkDesc) {
        mPlacemarkDesc = placemarkDesc;
    }

    public void setSourceAndDest(String[] args) {
        if (args.length == 2) {
            String source = args[0];
            File sourceFile = new File(source);

            if (sourceFile.isDirectory()) {
                mSource.setDir(sourceFile);
                mSource.setFilePattern("*");
            } else {
                String sourceDir = FilenameUtils.getFullPathNoEndSeparator(source);
                mSource.setDir(new File(sourceDir));
                mSource.setFilePattern(FilenameUtils.getName(source));
            }

            setDestFile(new File(args[1]));
        } else {
            addValidationError(mBundle.getString("invalid_arg_count"));
        }
    }

    @Override
    public String toDebugString() {
        StringBuilder builder = new StringBuilder("Profile summary { ").append(mName)
                .append(mSource.toDebugString())
                .append(mFolder.toDebugString())
                .append("}");

        return builder.toString();

//        return "Profile summary { " + mName
//                + "\n Source=" + mSource.getDir()
//                + "\n FilePattern=" + mSource.getFilePattern()
//                + "\n Links=" + mSource.isFollowLinks()
//                + "\n Recursive=" + mSource.isRecursive()
//                + "\n"
//                + "\n RootName=" + mFolder.getRootName()
//                + "\n RootDesc=" + mFolder.getRootDescription()
//                + "\n"
//                //                + "\n FolderByDate=" + mFolderByDate
//                //                + "\n FolderByDir=" + mFolderByDir
//                //                + "\n FolderDatePattern=" + mFolderDatePattern
//                //                + "\n FolderDesc=" + mFolderDesc
//                + "\n"
//                + "\n PlacemarkByDate=" + mPlacemarkByDate
//                + "\n PlacemarkByFilename=" + mPlacemarkByFilename
//                + "\n PlacemarkDatePattern=" + mPlacemarkDatePattern
//                + "\n PlacemarkDesc=" + mPlacemarkDesc
//                + "\n"
//                + "\n MaxHeight=" + mMaxHeight
//                + "\n MaxWidth=" + mMaxWidth
//                + "\n"
//                + "\n Lat=" + mLat
//                + "\n Lon=" + mLon
//                + "\n"
//                + "\n LowerCaseExt=" + mLowerCaseExt
//                + "\n AbsolutePath=" + mAbsolutePath
//                + "\n"
//                + "\n Dest=" + mDestFile
//                + "\n}";
    }

    @Override
    public String toString() {
        return mName;
    }

    void addValidationError(String string) {
        mValidationErrorBuilder.append(string).append("\n");
    }

}