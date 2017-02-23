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

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.LinkedHashMap;
import org.apache.commons.lang3.SystemUtils;
import org.json.simple.JSONObject;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlsson
 */
public class ProfileSource extends ProfileBase {

    public static final String KEY_FOLLOW_LINKS = "followLinks";
    public static final String KEY_INCLUDE_NULL_COORDINATE = "includeNullCoordinate";
    public static final String KEY_PATH = "path";
    public static final String KEY_PATTERN = "pattern";
    public static final String KEY_RECURSIVE = "recursive";
    private File mDir = SystemUtils.getUserHome();
    private String mFilePattern = "{*.jpg,*.JPG}";
    private boolean mFollowLinks = true;
    private boolean mIncludeNullCoordinate = false;
    private PathMatcher mPathMatcher;
    private final Profile mProfile;
    private boolean mRecursive = true;

    public ProfileSource(Profile profile) {
        mProfile = profile;
    }

    public ProfileSource(Profile profile, JSONObject json) {
        mProfile = profile;
        mDir = getFileObject(json, KEY_PATH);
        mFilePattern = (String) json.get(KEY_PATTERN);
        mRecursive = getBoolean(json, KEY_RECURSIVE, mRecursive);
        mFollowLinks = getBoolean(json, KEY_FOLLOW_LINKS, mFollowLinks);
        mIncludeNullCoordinate = getBoolean(json, KEY_INCLUDE_NULL_COORDINATE, mIncludeNullCoordinate);
    }

    public File getDir() {
        return mDir;
    }

    public String getFilePattern() {
        return mFilePattern;
    }

    @Override
    public JSONObject getJson() {
        JSONObject json = new JSONObject();
        json.put(KEY_PATH, mDir.getAbsolutePath());
        json.put(KEY_PATTERN, mFilePattern);
        json.put(KEY_FOLLOW_LINKS, mFollowLinks);
        json.put(KEY_RECURSIVE, mRecursive);
        json.put(KEY_INCLUDE_NULL_COORDINATE, mIncludeNullCoordinate);

        return json;
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
            mPathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + mFilePattern);
        } catch (Exception e) {
            addValidationError("invalid file pattern: " + mFilePattern);
        }

        return true;
    }

    public void setDir(File dir) {
        mDir = dir;
    }

    @Override
    protected ProfileInfo getProfileInfo() {
        ProfileInfo profileInfo = new ProfileInfo();
        LinkedHashMap<String, String> values = new LinkedHashMap<>();
        values.put(mBundleUI.getString("ModuleSourcePanel.sourceChooserPanel.header"), mDir.getAbsolutePath());
        values.put(Dict.FILE_PATTERN.toString(), mFilePattern);
        values.put(Dict.SUBDIRECTORIES.toString(), String.valueOf(mRecursive));
        values.put(Dict.FOLLOW_LINKS.toString(), String.valueOf(mFollowLinks));
        values.put(mBundleUI.getString("ModuleSourcePanel.includeNullCoordinateCheckBox.text"), String.valueOf(mIncludeNullCoordinate));

        profileInfo.setTitle(getTitle());
        profileInfo.setValues(values);

        return profileInfo;
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
}
