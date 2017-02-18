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
import org.json.simple.JSONObject;
import se.trixon.almond.util.Dict;

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

    private static final String FOLDER_NAME = "folder-name";
    private static final String ROOT_NAME = "root-name";

    private String mDatePattern = "yyyy-ww";
    private SimpleDateFormat mFolderDateFormat;
    private int mFoldersBy = FOLDER_BY_DIR;
    private final Profile mProfile;
    private String mRegex = "\\d{8}";
    private String mRegexDefault = "12345678";
    private String mRootDescription = "";
    private String mRootName = "";

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
    public String getTitle() {
        return Dict.FOLDERS.toString();
    }

    @Override
    public boolean isValid() {
        boolean valid = true;
        if (mRootName == null) {
            addValidationError(String.format(mBundle.getString("invalid_value"), ROOT_NAME, mRootName));
            valid = false;
        }

        if (mFoldersBy == FOLDER_BY_DATE) {
            try {
                mFolderDateFormat = new SimpleDateFormat(mDatePattern, mOptions.getLocale());
            } catch (Exception e) {
                addValidationError(String.format(mBundle.getString("invalid_value"), FOLDER_NAME, mDatePattern));
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
    protected ProfileInfo getProfileInfo() {
        ProfileInfo profileInfo = new ProfileInfo();
        LinkedHashMap<String, String> values = new LinkedHashMap<>();
        values.put(mBundleUI.getString("ModuleFoldersPanel.rootNameLabel.text"), mRootName);
        values.put(mBundleUI.getString("ModuleFoldersPanel.rootDescriptionLabel.text"), mRootDescription.replaceAll("\\n", "\\\\n"));
        String foldersBy = mBundleUI.getString("ModuleFoldersPanel.folderByNoneRadioButton.text");

        switch (mFoldersBy) {
            case FOLDER_BY_DATE:
                foldersBy = mDatePattern;
                break;

            case FOLDER_BY_DIR:
                foldersBy = mBundleUI.getString("ModuleFoldersPanel.folderByDirectoryRadioButton.text");
                break;

            case FOLDER_BY_REGEX:
                foldersBy = mRegex;
                break;
        }

        values.put(mBundleUI.getString("ModuleFoldersPanel.folderByLabel.text"), foldersBy);

        profileInfo.setTitle(getTitle());
        profileInfo.setValues(values);

        return profileInfo;
    }
}
