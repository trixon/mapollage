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
import se.trixon.photokml.PhotoKml;

/**
 *
 * @author Patrik Karlsson
 */
public class ProfileFolder extends ProfileBase {

    public static final String KEY_CREATE_FOLDERS = "createFolders";
    public static final String KEY_FOLDERS_BY = "foldersBy";
    public static final String KEY_ROOT_DESCRIPTION = "rootDescription";
    public static final String KEY_ROOT_NAME = "rootName";
    public static final String KEY_DATE_PATTERN = "datePattern";
    public static final String KEY_REGEX = "regex";
    public static final String KEY_REGEX_DEFAULT = "regexDefault";

    private boolean mCreateFolders;
    private String mDatePattern;
    private boolean mFolderByDate;
    private boolean mFolderByDir;
    private SimpleDateFormat mFolderDateFormat;
    private String mFolderDatePattern;
    private int mFoldersBy;
    private final Profile mProfile;
    private String mRegex;
    private String mRegexDefault;
    private String mRootDescription;
    private String mRootName;

    public ProfileFolder(final Profile profile) {
        mProfile = profile;
    }

    public ProfileFolder(CommandLine commandLine, final Profile profile) {
        mProfile = profile;
        mRootName = commandLine.getOptionValue(PhotoKml.ROOT_NAME);
        mRootDescription = commandLine.getOptionValue(PhotoKml.ROOT_DESC);
        if (commandLine.hasOption(PhotoKml.FOLDER_NAME)) {
            mFolderDatePattern = commandLine.getOptionValue(PhotoKml.FOLDER_NAME);
            mFolderByDate = mFolderDatePattern != null;
            mFolderByDir = !mFolderByDate;
        }
    }

    public String getDatePattern() {
        return mDatePattern;
    }

    public SimpleDateFormat getFolderDateFormat() {
        return mFolderDateFormat;
    }

    public String getFolderDatePattern() {
        return mFolderDatePattern;
    }

    public int getFoldersBy() {
        return mFoldersBy;
    }

    public String getRegex() {
        return mRegex;
    }

    public String getRegexDefault() {
        return mRegexDefault;
    }

    public String getRootDescription() {
        return mRootDescription;
    }

    public String getRootName() {
        return mRootName;
    }

    public boolean isCreateFolders() {
        return mCreateFolders;
    }

    public boolean isFolderByDate() {
        return mFolderByDate;
    }

    public boolean isFolderByDir() {
        return mFolderByDir;
    }

    public boolean isValid() {
        if (mRootName == null) {
            mProfile.addValidationError(String.format(mBundle.getString("invalid_value"), PhotoKml.ROOT_NAME, mRootName));
        }

        if (mFolderByDate) {
            try {
                mFolderDateFormat = new SimpleDateFormat(mFolderDatePattern);
            } catch (Exception e) {
                mProfile.addValidationError(String.format(mBundle.getString("invalid_value"), PhotoKml.FOLDER_NAME, mFolderDatePattern));
            }
        }
        return true;
    }

    public void setCreateFolders(boolean createFolders) {
        mCreateFolders = createFolders;
    }

    public void setDatePattern(String datePattern) {
        mDatePattern = datePattern;
    }

    public void setFolderDateFormat(SimpleDateFormat folderDateFormat) {
        mFolderDateFormat = folderDateFormat;
    }

    public void setFoldersBy(int foldersBy) {
        mFoldersBy = foldersBy;
    }

    public void setRegex(String regex) {
        mRegex = regex;
    }

    public void setRegexDefault(String regexDefault) {
        mRegexDefault = regexDefault;
    }

    public void setRootDescription(String rootDescription) {
        mRootDescription = rootDescription;
    }

    public void setRootName(String rootName) {
        mRootName = rootName;
    }

    @Override
    public String toDebugString() {
        return "\n RootName=" + mRootName
                + "\n RootDesc=" + mRootDescription
                + "\n FolderByDate=" + mFolderByDate
                + "\n FolderByDir=" + mFolderByDir
                + "\n FolderDatePattern=" + mFolderDatePattern
                + "\n Regex=" + mRegex
                + "\n RegexDefault=" + mRegexDefault
                //                + "\n FolderDesc=" + mFolderDesc
                + "\n";
    }

    @Override
    public String toString() {
        return "Folder{" + "mRootDescription=" + mRootDescription + ", mRootName=" + mRootName + '}';
    }

}
