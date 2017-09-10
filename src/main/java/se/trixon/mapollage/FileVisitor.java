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
package se.trixon.mapollage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOCase;
import org.apache.commons.lang3.StringUtils;
import se.trixon.mapollage.profile.ProfileDescription.DescriptionMode;

/**
 *
 * @author Patrik Karlsson
 */
public class FileVisitor extends SimpleFileVisitor<Path> {

    private final Properties mDefaultDescProperties = new Properties();
    private final HashMap<String, Properties> mDirToDesc;
    private final String[] mExcludePatterns;
    private final String mExternalFileValue;
    private List<File> mFiles = new ArrayList<>();
    private boolean mInterrupted;
    private final Operation mOperation;
    private final OperationListener mOperationListener;
    private final PathMatcher mPathMatcher;
    private final File mStartDir;
    private final boolean mUseExternalDescription;

    public FileVisitor(PathMatcher pathMatcher, List<File> paths, File startDir, Operation operation) {
        mStartDir = startDir;
        mOperation = operation;
        mOperationListener = operation.getListener();
        mFiles = paths;
        mPathMatcher = pathMatcher;
        mExcludePatterns = StringUtils.split(operation.getExcludePattern(), "::");
        mDirToDesc = operation.getDirToDesc();

        final DescriptionMode mode = operation.getProfileDescription().getMode();
        mUseExternalDescription = mode == DescriptionMode.EXTERNAL;
        mExternalFileValue = operation.getProfileDescription().getExternalFileValue();
        if (mode == DescriptionMode.EXTERNAL) {
            try {
                File file = new File(startDir, mExternalFileValue);
                if (file.isFile()) {
                    mDefaultDescProperties.load(new InputStreamReader(new FileInputStream(file), Charset.defaultCharset()));
                }
            } catch (IOException ex) {
                // nvm
            }
        }
    }

    public boolean isInterrupted() {
        return mInterrupted;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        if (mExcludePatterns != null) {
            for (String excludePattern : mExcludePatterns) {
                if (IOCase.SYSTEM.isCaseSensitive()) {
                    if (StringUtils.contains(dir.toString(), excludePattern)) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                } else {
                    if (StringUtils.containsIgnoreCase(dir.toString(), excludePattern)) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                }
            }
        }

        String[] filePaths = dir.toFile().list();

        mOperationListener.onOperationLog(dir.toString());
        mOperationListener.onOperationProgress(dir.toString());

        if (filePaths != null && filePaths.length > 0) {
            if (mUseExternalDescription) {
                Properties p = new Properties(mDefaultDescProperties);

                try {
                    File file = new File(dir.toFile(), mExternalFileValue);
                    if (file.isFile()) {
                        p.load(new InputStreamReader(new FileInputStream(file), Charset.defaultCharset()));
                    }
                } catch (IOException ex) {
                    // nvm
                }

                mDirToDesc.put(dir.toFile().getAbsolutePath(), p);
            }

            for (String fileName : filePaths) {
                try {
                    TimeUnit.NANOSECONDS.sleep(1);
                } catch (InterruptedException ex) {
                    mInterrupted = true;
                    return FileVisitResult.TERMINATE;
                }
                File file = new File(dir.toFile(), fileName);
                if (file.isFile() && mPathMatcher.matches(file.toPath().getFileName())) {
                    boolean exclude = false;
                    if (mExcludePatterns != null) {
                        for (String excludePattern : mExcludePatterns) {
                            if (StringUtils.contains(file.getAbsolutePath(), excludePattern)) {
                                exclude = true;
                                break;
                            }
                        }
                    }

                    if (!exclude) {
                        mFiles.add(file);
                    }
                }
            }
        }

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exception) {
        mOperation.logError(String.format("E000 %s", file.toString()));

        return FileVisitResult.CONTINUE;
    }
}
