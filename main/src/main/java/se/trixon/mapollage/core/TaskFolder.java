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
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class TaskFolder extends TaskBase {

    private static final String FOLDER_NAME = "folder-name";
    private static final String ROOT_NAME = "root-name";

    @SerializedName("date_pattern")
    private String mDatePattern = "yyyy-ww";
    private transient SimpleDateFormat mFolderDateFormat;
    @SerializedName("folders_by")
    private FolderBy mFoldersBy = FolderBy.DIR;
    @SerializedName("regex")
    private String mRegex = "\\d{8}";
    @SerializedName("regex_default")
    private String mRegexDefault = "12345678";

    public TaskFolder() {
    }

    public String getDatePattern() {
        return mDatePattern;
    }

    public SimpleDateFormat getFolderDateFormat() {
        return mFolderDateFormat;
    }

    public FolderBy getFoldersBy() {
        return mFoldersBy;
    }

    public String getRegex() {
        return mRegex;
    }

    public String getRegexDefault() {
        return mRegexDefault;
    }

    @Override
    public String getTitle() {
        return Dict.FOLDERS.toString();
    }

    @Override
    public boolean isValid() {
        boolean valid = true;
        if (getTask().getName() == null) {
            addValidationError(String.format(BUNDLE.getString("invalid_value"), ROOT_NAME, getTask().getName()));
            valid = false;
        }

        if (mFoldersBy == FolderBy.DATE) {
            try {
                mFolderDateFormat = new SimpleDateFormat(mDatePattern, mOptions.getLocale());
            } catch (IllegalArgumentException e) {
                addValidationError(String.format(BUNDLE.getString("invalid_value"), FOLDER_NAME, mDatePattern));
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

    public void setFoldersBy(FolderBy foldersBy) {
        mFoldersBy = foldersBy;
    }

    public void setRegex(String regex) {
        mRegex = regex;
    }

    public void setRegexDefault(String regexDefault) {
        mRegexDefault = regexDefault;
    }

    @Override
    protected TaskInfo getTaskInfo() {
        var taskInfo = new TaskInfo();
        var values = new LinkedHashMap<String, String>();
        values.put(BUNDLE_UI.getString("FoldersTab.rootNameLabel"), getTask().getName());
        values.put(BUNDLE_UI.getString("FoldersTab.rootDescriptionLabel"), getTask().getDescriptionString().replaceAll("\\n", "\\\\n"));
        String foldersBy = BUNDLE_UI.getString("FoldersTab.folderByNoneRadioButton");

        switch (mFoldersBy) {
            case DATE ->
                foldersBy = mDatePattern;

            case DIR ->
                foldersBy = BUNDLE_UI.getString("FoldersTab.folderByDirectoryRadioButton");

            case REGEX ->
                foldersBy = mRegex;
        }

        values.put(BUNDLE_UI.getString("FoldersTab.folderByLabel"), foldersBy);

        taskInfo.setTitle(getTitle());
        taskInfo.setValues(values);

        return taskInfo;
    }

    public enum FolderBy {
        NONE,
        DIR,
        DATE,
        REGEX
    }
}
