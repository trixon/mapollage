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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.math.NumberUtils;
import se.trixon.photokml.PhotoKml;

/**
 *
 * @author Patrik Karlsson
 */
public class Profile extends ProfileBase implements Comparable<Profile>, Cloneable {

    private String mAbsolutePath;
    private File mDestFile;
    private ProfileFolder mFolder = new ProfileFolder(this);
    private String mFolderDesc;
    private boolean mLowerCaseExt;
    private Integer mMaxHeight;
    private String mMaxHeightString;
    private Integer mMaxWidth;
    private String mMaxWidthString;
    private String mName;
    private ProfileSource mSource = new ProfileSource(this);
    private StringBuilder mValidationErrorBuilder;
    private ProfilePlacemark mPlacemark = new ProfilePlacemark(this);

    public Profile() {

    }

    public ProfilePlacemark getPlacemark() {
        return mPlacemark;
    }

    public Profile(CommandLine commandLine) {
        mFolder = new ProfileFolder(commandLine, this);
        mFolderDesc = commandLine.getOptionValue(PhotoKml.FOLDER_DESC);

        mPlacemark = new ProfilePlacemark(commandLine, this);

        mMaxHeightString = commandLine.getOptionValue(PhotoKml.MAX_HEIGHT);
        mMaxWidthString = commandLine.getOptionValue(PhotoKml.MAX_WIDTH);

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

    public Integer getMaxHeight() {
        return mMaxHeight;
    }

    public Integer getMaxWidth() {
        return mMaxWidth;
    }

    public String getName() {
        return mName;
    }

    public ProfileSource getSource() {
        return mSource;
    }

    public String getValidationError() {
        return mValidationErrorBuilder.toString();
    }

    public boolean isLowerCaseExt() {
        return mLowerCaseExt;
    }

    @Override
    public boolean isValid() {
        mValidationErrorBuilder = new StringBuilder();
        mFolder.isValid();
        mPlacemark.isValid();

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
                .append(mPlacemark.toDebugString())
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
