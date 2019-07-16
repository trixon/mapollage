/* 
 * Copyright 2019 Patrik Karlström.
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

import com.google.gson.annotations.SerializedName;
import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.LinkedHashMap;
import org.apache.commons.lang3.SystemUtils;
import se.trixon.almond.util.BooleanHelper;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class ProfileSource extends ProfileBase {

    @SerializedName("source")
    private File mDir = SystemUtils.getUserHome();
    @SerializedName("exclude_pattern")
    private String mExcludePattern = "";
    @SerializedName("file_pattern")
    private String mFilePattern = "{*.jpg,*.JPG}";
    @SerializedName("follow_links")
    private boolean mFollowLinks = true;
    @SerializedName("include_null_coordinates")
    private boolean mIncludeNullCoordinate = false;
    private transient PathMatcher mPathMatcher;
    private transient final Profile mProfile;
    @SerializedName("recursive")
    private boolean mRecursive = true;

    public ProfileSource(Profile profile) {
        mProfile = profile;
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
            mPathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + mFilePattern);
        } catch (Exception e) {
            addValidationError("invalid file pattern: " + mFilePattern);
        }

        return true;
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
    protected ProfileInfo getProfileInfo() {
        ProfileInfo profileInfo = new ProfileInfo();
        LinkedHashMap<String, String> values = new LinkedHashMap<>();
        values.put(BUNDLE_UI.getString("SourceTab.sourceChooserPanel.header"), mDir.getAbsolutePath());
        values.put(Dict.FILE_PATTERN.toString(), mFilePattern);
        values.put(Dict.SUBDIRECTORIES.toString(), BooleanHelper.asYesNo(mRecursive));
        values.put(Dict.FOLLOW_LINKS.toString(), BooleanHelper.asYesNo(mFollowLinks));
        values.put(BUNDLE_UI.getString("SourceTab.includeNullCoordinateCheckBox"), BooleanHelper.asYesNo(mIncludeNullCoordinate));
        values.put(BUNDLE_UI.getString("SourceTab.excludeLabel"), mExcludePattern);

        profileInfo.setTitle(getTitle());
        profileInfo.setValues(values);

        return profileInfo;
    }
}
