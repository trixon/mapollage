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
import org.json.simple.JSONObject;
import se.trixon.photokml.PhotoKml;

/**
 *
 * @author Patrik Karlsson
 */
public class Profile extends ProfileBase implements Comparable<Profile>, Cloneable {

    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_FOLDER = "folder";
    private static final String KEY_NAME = "name";
    private static final String KEY_PLACEMARK = "placemark";
    private static final String KEY_PHOTO = "photo";
    private static final String KEY_SOURCE = "source";

    private String mAbsolutePath;
    private ProfileDescription mDescription = new ProfileDescription(this);
    private File mDestFile;
    private ProfileFolder mFolder = new ProfileFolder(this);
    private String mFolderDesc;
    private boolean mLowerCaseExt;
    private Integer mMaxHeight;
    private String mMaxHeightString;
    private Integer mMaxWidth;
    private String mMaxWidthString;
    private String mName;
    private ProfilePlacemark mPlacemark = new ProfilePlacemark(this);
    private ProfileSource mSource = new ProfileSource(this);
    private StringBuilder mValidationErrorBuilder;
    private ProfilePhoto mPhoto = new ProfilePhoto(this);

    public Profile() {

    }

    public ProfilePhoto getPhoto() {
        return mPhoto;
    }

    public void setPhoto(ProfilePhoto photo) {
        mPhoto = photo;
    }

    public Profile(JSONObject json) {
        setName((String) json.get(KEY_NAME));

        setSource(new ProfileSource(this, (JSONObject) json.get(KEY_SOURCE)));
        setFolder(new ProfileFolder(this, (JSONObject) json.get(KEY_FOLDER)));
        setPlacemark(new ProfilePlacemark(this, (JSONObject) json.get(KEY_PLACEMARK)));
        setDescription(new ProfileDescription(this, (JSONObject) json.get(KEY_DESCRIPTION)));
        setPhoto(new ProfilePhoto(this, (JSONObject) json.get(KEY_PHOTO)));
    }

    public Profile(CommandLine commandLine) {
        mFolder = new ProfileFolder(this, commandLine);
        mFolderDesc = commandLine.getOptionValue(PhotoKml.FOLDER_DESC);

        mPlacemark = new ProfilePlacemark(this, commandLine);

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

    public ProfileDescription getDescription() {
        return mDescription;
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

    @Override
    public JSONObject getJson() {
        JSONObject json = new JSONObject();
        json.put(KEY_NAME, getName());

        json.put(KEY_SOURCE, getSource().getJson());
        json.put(KEY_FOLDER, getFolder().getJson());
        json.put(KEY_PLACEMARK, getPlacemark().getJson());
        json.put(KEY_DESCRIPTION, getDescription().getJson());
        json.put(KEY_PHOTO, getPhoto().getJson());

        return json;
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

    public ProfilePlacemark getPlacemark() {
        return mPlacemark;
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

    public void setDescription(ProfileDescription description) {
        mDescription = description;
    }

    public void setDestFile(File dest) {
        mDestFile = dest;
    }

    public void setFolder(ProfileFolder folder) {
        mFolder = folder;
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

    public void setPlacemark(ProfilePlacemark placemark) {
        mPlacemark = placemark;
    }

    public void setSource(ProfileSource source) {
        mSource = source;
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
                .append(mDescription.toDebugString())
                .append(mPhoto.toDebugString())
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
