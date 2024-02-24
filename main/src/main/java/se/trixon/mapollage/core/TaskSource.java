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
import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.LinkedHashMap;
import java.util.Locale;
import org.apache.commons.lang3.SystemUtils;
import se.trixon.almond.util.BooleanHelper;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class TaskSource extends TaskBase {

    private final double DEFAULT_LAT = 57.6;
    private final double DEFAULT_LON = 11.3;
    @SerializedName("defLat")
    private double mDefaultLat = DEFAULT_LAT;
    @SerializedName("defLon")
    private double mDefaultLon = DEFAULT_LON;
    @SerializedName("source")
    private File mDir = SystemUtils.getUserHome();
    @SerializedName("exclude_pattern")
    private String mExcludePattern = "";
    @SerializedName("file_pattern")
    private String mFilePattern = "*.jpg";
    @SerializedName("follow_links")
    private boolean mFollowLinks = true;
    @SerializedName("include_null_coordinates")
    private boolean mIncludeNullCoordinate = false;
    private transient PathMatcher mPathMatcher;
    @SerializedName("recursive")
    private boolean mRecursive = true;

    public TaskSource() {
    }

    public double getDefaultLat() {
        return mDefaultLat;
    }

    public double getDefaultLon() {
        return mDefaultLon;
    }

    public File getDir() {
        return mDir;
    }

    public String getExcludePattern() {
        return mExcludePattern;
    }

    public String getFilePattern() {
        return mFilePattern;
    }

    public PathMatcher getPathMatcher() {
        return mPathMatcher;
    }

    @Override
    public String getTitle() {
        return Dict.SOURCE.toString();
    }

    public boolean isFollowLinks() {
        return mFollowLinks;
    }

    public boolean isIncludeNullCoordinate() {
        return mIncludeNullCoordinate;
    }

    public boolean isRecursive() {
        return mRecursive;
    }

    @Override
    public boolean isValid() {
        try {
            mPathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + mFilePattern.toLowerCase(Locale.ROOT));
        } catch (Exception e) {
            addValidationError("invalid file pattern: " + mFilePattern);
        }

        return true;
    }

    public void setDefaultLat(double defaultLat) {
        mDefaultLat = defaultLat;
    }

    public void setDefaultLon(double defaultLon) {
        mDefaultLon = defaultLon;
    }

    public void setDir(File dir) {
        mDir = dir;
    }

    public void setExcludePattern(String excludePattern) {
        mExcludePattern = excludePattern;
    }

    public void setFilePattern(String filePattern) {
        mFilePattern = filePattern;
    }

    public void setFollowLinks(boolean followLinks) {
        mFollowLinks = followLinks;
    }

    public void setIncludeNullCoordinate(boolean includeNullCoordinate) {
        mIncludeNullCoordinate = includeNullCoordinate;
    }

    public void setRecursive(boolean recursive) {
        mRecursive = recursive;
    }

    @Override
    protected TaskInfo getTaskInfo() {
        var taskInfo = new TaskInfo();
        var values = new LinkedHashMap<String, String>();
        values.put(BUNDLE_UI.getString("SourceTab.sourceChooserPanel.header"), mDir.getAbsolutePath());
        values.put(Dict.FILE_PATTERN.toString(), mFilePattern);
        values.put(Dict.SUBDIRECTORIES.toString(), BooleanHelper.asYesNo(mRecursive));
        values.put(Dict.FOLLOW_LINKS.toString(), BooleanHelper.asYesNo(mFollowLinks));
        values.put(BUNDLE_UI.getString("SourceTab.includeNullCoordinateCheckBox"), BooleanHelper.asYesNo(mIncludeNullCoordinate));
        values.put(BUNDLE_UI.getString("SourceTab.excludeLabel"), mExcludePattern);
        values.put(String.format("%s %s", Dict.DEFAULT.toString(), Dict.LATITUDE.toString()), String.valueOf(mDefaultLat));
        values.put(String.format("%s %s", Dict.DEFAULT.toString(), Dict.LONGITUDE.toString()), String.valueOf(mDefaultLon));

        taskInfo.setTitle(getTitle());
        taskInfo.setValues(values);

        return taskInfo;
    }
}
