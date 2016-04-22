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
package se.trixon.photokml;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.FilenameUtils;
import se.trixon.util.BundleHelper;

/**
 *
 * @author Patrik Karlsson
 */
public class OptionsHolder {

    private final ResourceBundle mBundle = BundleHelper.getBundle(OptionsHolder.class, "Bundle");
    private File mDestDir;
    private String mFilePattern;
    private boolean mFollowLinks;
    private PathMatcher mPathMatcher;
    private boolean mRecursive;
    private String mRootDesc;
    private String mRootName;
    private File mSourceDir;
    private final StringBuilder mValidationErrorBuilder = new StringBuilder();
    private SimpleDateFormat mFolderDateFormat;
    private String mFolderDatePattern;

    public OptionsHolder(CommandLine commandLine) {
        mRootName = commandLine.getOptionValue("root-name");
        mRootDesc = commandLine.getOptionValue("root-desc");

        mFollowLinks = commandLine.hasOption("links");
        mRecursive = commandLine.hasOption("recursive");

        setSourceAndDest(commandLine.getArgs());
    }

    public File getDestDir() {
        return mDestDir;
    }

    public String getFilePattern() {
        return mFilePattern;
    }

    public PathMatcher getPathMatcher() {
        return mPathMatcher;
    }

    public String getRootDesc() {
        return mRootDesc;
    }

    public String getRootName() {
        return mRootName;
    }

    public File getSourceDir() {
        return mSourceDir;
    }

    public String getValidationError() {
        return mValidationErrorBuilder.toString();
    }

    public boolean isFollowLinks() {
        return mFollowLinks;
    }

    public boolean isRecursive() {
        return mRecursive;
    }

    public boolean isValid() {
        try {
            mPathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + mFilePattern);
        } catch (Exception e) {
            addValidationError("invalid file pattern: " + mFilePattern);
        }

        try {
            mFolderDateFormat = new SimpleDateFormat(mFolderDatePattern);
        } catch (Exception e) {
            addValidationError(String.format(mBundle.getString("invalid_date_pattern"), mFolderDatePattern));
        }

        return mValidationErrorBuilder.length() == 0;
    }

    public void setDestDir(File dest) {
        mDestDir = dest;
    }

    public void setFilePattern(String filePattern) {
        mFilePattern = filePattern;
    }

    public void setFollowLinks(boolean followLinks) {
        mFollowLinks = followLinks;
    }

    public void setPathMatcher(PathMatcher pathMatcher) {
        mPathMatcher = pathMatcher;
    }

    public void setRecursive(boolean recursive) {
        mRecursive = recursive;
    }

    public void setRootDesc(String rootDesc) {
        mRootDesc = rootDesc;
    }

    public void setRootName(String rootName) {
        mRootName = rootName;
    }

    public void setSourceAndDest(String[] args) {
        if (args.length == 2) {
            String source = args[0];
            File sourceFile = new File(source);

            if (sourceFile.isDirectory()) {
                mSourceDir = sourceFile;
                mFilePattern = "*";
            } else {
                String sourceDir = FilenameUtils.getFullPathNoEndSeparator(source);
                mSourceDir = new File(sourceDir);
                mFilePattern = FilenameUtils.getName(source);
            }

            setDestDir(new File(args[1]));
        } else {
            addValidationError(mBundle.getString("invalid_arg_count"));
        }
    }

    private void addValidationError(String string) {
        mValidationErrorBuilder.append(string).append("\n");
    }

}
