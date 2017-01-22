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
import org.json.simple.JSONObject;
import se.trixon.photokml.PhotoKml;

/**
 *
 * @author Patrik Karlsson
 */
public class ProfileFolder extends ProfileBase {

    public static final int FOLDER_BY_DATE = 2;
    public static final int FOLDER_BY_DIR = 1;
    public static final int FOLDER_BY_NONE = 0;
    public static final int FOLDER_BY_REGEX = 3;

    public static final String KEY_CREATE_FOLDERS = "createFolders";
    public static final String KEY_DATE_PATTERN = "datePattern";
    public static final String KEY_FOLDERS_BY = "foldersBy";
    public static final String KEY_REGEX = "regex";
    public static final String KEY_REGEX_DEFAULT = "regexDefault";
    public static final String KEY_ROOT_DESCRIPTION = "rootDescription";
    public static final String KEY_ROOT_NAME = "rootName";

    private String mDatePattern = "yyyy-ww";
    private SimpleDateFormat mFolderDateFormat;
    private int mFoldersBy = FOLDER_BY_DIR;
    private final Profile mProfile;
    private String mRegex = "\\d{8}";
    private String mRegexDefault = "12345678";
    private String mRootDescription;
    private String mRootName;

    public ProfileFolder(Profile profile) {
        mProfile = profile;
    }

    public ProfileFolder(Profile profile, JSONObject json) {
        mProfile = profile;
        mRootName = (String) json.get(KEY_ROOT_NAME);
        mRootDescription = (String) json.get(KEY_ROOT_DESCRIPTION);
        mFoldersBy = getInt(json, KEY_FOLDERS_BY);
        mDatePattern = (String) json.get(KEY_DATE_PATTERN);
        mRegex = (String) json.get(KEY_REGEX);
        mRegexDefault = (String) json.get(KEY_REGEX_DEFAULT);
    }

//    public ProfileFolder(final Profile profile, CommandLine commandLine) {
//        mProfile = profile;
//        mRootName = commandLine.getOptionValue(PhotoKml.ROOT_NAME);
//        mRootDescription = commandLine.getOptionValue(PhotoKml.ROOT_DESC);
//        if (commandLine.hasOption(PhotoKml.FOLDER_NAME)) {
//            mFolderDatePattern = commandLine.getOptionValue(PhotoKml.FOLDER_NAME);
//            mFolderByDate = mFolderDatePattern != null;
//            mFolderByDir = !mFolderByDate;
//        }
//    }
    public String getDatePattern() {
        return mDatePattern;
    }

    public SimpleDateFormat getFolderDateFormat() {
        return mFolderDateFormat;
    }

    public int getFoldersBy() {
        return mFoldersBy;
    }

    @Override
    public JSONObject getJson() {
        JSONObject json = new JSONObject();

        json.put(KEY_ROOT_NAME, mRootName);
        json.put(KEY_ROOT_DESCRIPTION, mRootDescription);
        json.put(KEY_FOLDERS_BY, mFoldersBy);
        json.put(KEY_DATE_PATTERN, mDatePattern);
        json.put(KEY_REGEX, mRegex);
        json.put(KEY_REGEX_DEFAULT, mRegexDefault);

        return json;
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

    @Override
    public boolean isValid() {
        boolean valid = true;
        if (mRootName == null) {
            addValidationError(String.format(mBundle.getString("invalid_value"), PhotoKml.ROOT_NAME, mRootName));
            valid = false;
        }

        if (mFoldersBy == FOLDER_BY_DATE) {
            try {
                mFolderDateFormat = new SimpleDateFormat(mDatePattern);
            } catch (Exception e) {
                addValidationError(String.format(mBundle.getString("invalid_value"), PhotoKml.FOLDER_NAME, mDatePattern));
                valid = false;
            }
        }

        return valid;
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
        return "ProfileFolder{" + "mDatePattern=" + mDatePattern + ", mFolderDateFormat=" + mFolderDateFormat + ", mFoldersBy=" + mFoldersBy + ", mProfile=" + mProfile + ", mRegex=" + mRegex + ", mRegexDefault=" + mRegexDefault + ", mRootDescription=" + mRootDescription + ", mRootName=" + mRootName + '}';
    }

    @Override
    public String toString() {
        return "Folder{" + "mRootDescription=" + mRootDescription + ", mRootName=" + mRootName + '}';
    }
}
