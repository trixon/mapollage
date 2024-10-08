/*
 * Copyright 2024 Patrik Karlström <patrik@trixon.se>.
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

import com.drew.imaging.ImageProcessingException;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.io.IOCase;
import org.apache.commons.lang3.StringUtils;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.FoldHandle;
import org.openide.windows.IOColorPrint;
import org.openide.windows.IOFolding;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import se.trixon.almond.nbp.output.OutputAdapter;
import se.trixon.almond.nbp.output.OutputHelper;
import se.trixon.almond.nbp.output.OutputLineMode;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.mapollage.ui.TaskListEditor;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class Executor implements Runnable {

    private final ResourceBundle mBundle = NbBundle.getBundle(DocumentGenerator.class);
    private DocumentGenerator mDocumentGenerator;
    private final ArrayList<String> mErrorsIO = new ArrayList<>();
    private final ArrayList<String> mErrorsImageProcessing = new ArrayList<>();
    private Thread mExecutorThread;
    private final List<File> mFiles = new ArrayList<>();
    private final InputOutput mInputOutput;
    private FoldHandle mMainFoldHandle;
    private final OutputHelper mOutputHelper;
    private ProgressHandle mProgressHandle;
    private final AtomicBoolean mRunning = new AtomicBoolean(false);
    private final StatusDisplayer mStatusDisplayer = StatusDisplayer.getDefault();
    private final Task mTask;

    public Executor(Task task) {
        mTask = task;
        mInputOutput = IOProvider.getDefault().getIO(mTask.getName(), false);
        mInputOutput.select();

        mOutputHelper = new OutputHelper(mTask.getName(), mInputOutput, false);
        mOutputHelper.reset();
    }

    @Override
    public void run() {
        mRunning.set(true);
        var allowToCancel = (Cancellable) () -> {
            mRunning.set(false);
            mExecutorThread.interrupt();
            mProgressHandle.finish();
            ExecutorManager.getInstance().getExecutors().remove(mTask.getId());
            jobEnded(OutputLineMode.WARNING, Dict.CANCELED.toString());

            return true;
        };

        mProgressHandle = ProgressHandle.createHandle(mTask.getName(), allowToCancel);
        mProgressHandle.start();
        mProgressHandle.switchToIndeterminate();

        mDocumentGenerator = new DocumentGenerator(mTask, mInputOutput, mOutputHelper);

        mExecutorThread = new Thread(() -> {
            mOutputHelper.start();
            var album = StringUtils.toRootLowerCase(NbBundle.getMessage(TaskListEditor.class, "album"));
            mOutputHelper.printSectionHeader(OutputLineMode.INFO, Dict.START.toString(), album, mTask.getName());
            mMainFoldHandle = IOFolding.startFold(mInputOutput, true);

            if (!mTask.isValid()) {//TODO and dest dir too
                mInputOutput.getErr().println(mTask.getValidationError());
                jobEnded(OutputLineMode.ERROR, Dict.FAILED.toString());
                mInputOutput.getErr().println(String.format("\n\n%s", Dict.JOB_FAILED.toString()));

                return;
            }

            generateFileList();

            if (mRunning.get() && !mFiles.isEmpty()) {
                mOutputHelper.println(OutputLineMode.INFO, mBundle.getString("found_count").formatted(mFiles.size()));
                var foldHandle = mMainFoldHandle.startFold(false);
                mOutputHelper.println(OutputLineMode.STANDARD, String.join("\n", mFiles.stream().map(f -> f.getAbsolutePath()).toList()));
                foldHandle.finish();

                mInputOutput.getOut().println("");
                mOutputHelper.printSectionHeader(OutputLineMode.INFO, Dict.PROCESSING.toString(), null, null);

                mProgressHandle.switchToDeterminate(mFiles.size());
                int progress = 0;

                mDocumentGenerator.start();

                for (var file : mFiles) {
                    mProgressHandle.progress(file.getName());
                    try {
                        TimeUnit.NANOSECONDS.sleep(1);
                    } catch (InterruptedException ex) {
                        break;
                    }

                    try {
                        mDocumentGenerator.addPhoto(file);
                    } catch (ImageProcessingException ex) {
                        mErrorsImageProcessing.add(ex.getMessage());
                    } catch (IOException ex) {
                        mErrorsIO.add(file.getAbsolutePath());
                    }

                    if (Thread.interrupted()) {
                        break;
                    }

                    mProgressHandle.progress(++progress);
                }

                if (mTask.getPath().isDrawPolygon()) {
                    mDocumentGenerator.addPolygons();
                }
            }

            logErrors(mBundle.getString("title_error_io"), mErrorsIO);
            logErrors(mBundle.getString("title_error_image"), mErrorsImageProcessing);

            if (!mErrorsIO.isEmpty() || !mErrorsImageProcessing.isEmpty()) {
                mInputOutput.getOut().println(mBundle.getString("error_description"));
            }
            mMainFoldHandle.silentFinish();

            if (mRunning.get() && !mFiles.isEmpty()) {
                mDocumentGenerator.saveToFile(mFiles.size());
                mTask.setLastRun(System.currentTimeMillis());
                StorageManager.save();
            }

            if (mRunning.get()) {
                jobEnded(OutputLineMode.OK, Dict.DONE.toString());

                try {
                    mInputOutput.getOut().println();
                    mInputOutput.getOut().println(mBundle.getString("displayKml"), null, true);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                var foldHandle = IOFolding.startFold(mInputOutput, false);
                var emptyKml = StringUtils.isBlank(mDocumentGenerator.getKmlString());
                if (emptyKml) {
                    mOutputHelper.println(OutputLineMode.INFO, Dict.EMPTY.toString());
                } else {
                    mOutputHelper.println(OutputLineMode.INFO, mDocumentGenerator.getKmlString());
                }
                foldHandle.finish();

                if (!emptyKml) {
                    mInputOutput.getOut().print("\n%s ".formatted(Dict.OPEN.toString()));
                    try {
                        IOColorPrint.print(mInputOutput, mTask.getDestinationFile().getAbsolutePath(), new OutputAdapter() {
                            @Override
                            public void outputLineAction(OutputEvent ev) {
                                SystemHelper.desktopOpenOrElseParent(mTask.getDestinationFile());
                            }
                        }, false, Color.MAGENTA);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    mInputOutput.getOut().println(".");
                }
            }

            mProgressHandle.finish();
            ExecutorManager.getInstance().getExecutors().remove(mTask.getId());
        }, "Executor");

        mExecutorThread.start();
    }

    private void generateFileList() {
        var source = mTask.getSource();
        mInputOutput.getOut().println();
        mOutputHelper.printSectionHeader(OutputLineMode.INFO, Dict.GENERATING_FILELIST.toString(), "", source.getDir().getAbsolutePath());

        var pathMatcher = source.getPathMatcher();
        var fileVisitOptions = source.isFollowLinks() ? EnumSet.of(FileVisitOption.FOLLOW_LINKS) : EnumSet.noneOf(FileVisitOption.class);
        var sourceDir = source.getDir();

        if (sourceDir.isDirectory()) {
            var fileVisitor = new FileVisitor();
            try {
                if (source.isRecursive()) {
                    Files.walkFileTree(sourceDir.toPath(), fileVisitOptions, Integer.MAX_VALUE, fileVisitor);
                } else {
                    Files.walkFileTree(sourceDir.toPath(), fileVisitOptions, 1, fileVisitor);
                }
            } catch (IOException ex) {
                mInputOutput.getErr().println(ex.getMessage());
            }
        } else if (sourceDir.isFile() && pathMatcher.matches(sourceDir.toPath().getFileName())) {
            mFiles.add(sourceDir);
        }

        if (mFiles.isEmpty()) {
            mInputOutput.getOut().println(Dict.FILELIST_EMPTY.toString());
        } else {
            Collections.sort(mFiles);
        }
    }

    private void jobEnded(OutputLineMode outputLineMode, String action) {
        mMainFoldHandle.silentFinish();
        mStatusDisplayer.setStatusText(action);
        mOutputHelper.printSummary(outputLineMode, action, NbBundle.getMessage(TaskListEditor.class, "album"));
    }

    private void logErrors(String title, ArrayList<String> list) {
        if (list.isEmpty()) {
            return;
        }

        mOutputHelper.println(OutputLineMode.ERROR, title);
        var foldHandle = mMainFoldHandle.startFold(false);
        mOutputHelper.println(OutputLineMode.STANDARD, String.join("\n", list));
        foldHandle.finish();
    }

    public class FileVisitor extends SimpleFileVisitor<Path> {

        private final Properties mDefaultDescProperties = new Properties();
        private final HashMap<String, Properties> mDirToDesc;
        private final String[] mExcludePatterns;
        private final String mExternalFileValue;
        private final PathMatcher mPathMatcher;
        private final boolean mUseExternalDescription;

        public FileVisitor() {
            mPathMatcher = mTask.getSource().getPathMatcher();
            mExcludePatterns = StringUtils.split(mTask.getSource().getExcludePattern(), "::");
            mDirToDesc = mDocumentGenerator.getDirToDesc();

            var mode = mTask.getDescription().getMode();
            mUseExternalDescription = mode == TaskDescription.DescriptionMode.EXTERNAL;
            mExternalFileValue = mTask.getDescription().getExternalFileValue();

            if (mode == TaskDescription.DescriptionMode.EXTERNAL) {
                try {
                    var file = new File(mTask.getSource().getDir(), mExternalFileValue);
                    if (file.isFile()) {
                        mDefaultDescProperties.load(new InputStreamReader(new FileInputStream(file), Charset.defaultCharset()));
                    }
                } catch (IOException ex) {
                    // nvm
                }
            }
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            if (mExcludePatterns != null) {
                for (var excludePattern : mExcludePatterns) {
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
            mInputOutput.getOut().println(dir.toString());
            if (filePaths != null && filePaths.length > 0) {
                if (mUseExternalDescription) {
                    var p = new Properties(mDefaultDescProperties);

                    try {
                        var file = new File(dir.toFile(), mExternalFileValue);
                        if (file.isFile()) {
                            p.load(new InputStreamReader(new FileInputStream(file), Charset.defaultCharset()));
                        }
                    } catch (IOException ex) {
                        // nvm
                    }

                    mDirToDesc.put(dir.toFile().getAbsolutePath(), p);
                }

                for (var fileName : filePaths) {
                    try {
                        TimeUnit.NANOSECONDS.sleep(1);
                    } catch (InterruptedException ex) {
                        return FileVisitResult.TERMINATE;
                    }

                    var file = new File(dir.toFile(), fileName);
                    if (file.isFile() && mPathMatcher.matches(new File(file.getAbsolutePath().toLowerCase(Locale.ROOT)).toPath().getFileName())) {
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
            mInputOutput.getErr().println(file.toString());

            return FileVisitResult.CONTINUE;
        }
    }

}
