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
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import org.apache.commons.lang3.SystemUtils;

/**
 *
 * @author Patrik Karlsson
 */
public class ProfileSource extends ProfileBase {

    public static final String KEY_FOLLOW_LINKS = "followLinks";
    public static final String KEY_PATH = "path";
    public static final String KEY_PATTERN = "pattern";
    public static final String KEY_RECURSIVE = "recursive";
    private File mDir = SystemUtils.getUserHome();
    private String mFilePattern = "{*.jpg,*.JPG}";
    private boolean mFollowLinks;
    private boolean mRecursive;
    private final Profile mProfile;
    private PathMatcher mPathMatcher;

    public ProfileSource(final Profile profile) {
        mProfile = profile;
    }

    public File getDir() {
        return mDir;
    }

    public String getFilePattern() {
        return mFilePattern;
    }

    public boolean isFollowLinks() {
        return mFollowLinks;
    }

    public boolean isValid() {

        try {
            mPathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + mFilePattern);
        } catch (Exception e) {
            mProfile.addValidationError("invalid file pattern: " + mFilePattern);
        }
        return true;
    }

    public PathMatcher getPathMatcher() {
        return mPathMatcher;
    }

    public boolean isRecursive() {
        return mRecursive;
    }

    public void setDir(File dir) {
        mDir = dir;
    }

    public void setFilePattern(String filePattern) {
        mFilePattern = filePattern;
    }

    public void setFollowLinks(boolean followLinks) {
        mFollowLinks = followLinks;
    }

    public void setRecursive(boolean recursive) {
        mRecursive = recursive;
    }

    @Override
    public String toDebugString() {
        return "\n Source=" + mDir
                + "\n FilePattern=" + mFilePattern
                + "\n Links=" + mFollowLinks
                + "\n Recursive=" + mRecursive
                + "\n";
    }
}
