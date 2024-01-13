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

import org.netbeans.api.progress.ProgressHandle;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Cancellable;
import org.openide.windows.FoldHandle;
import org.openide.windows.IOFolding;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import se.trixon.almond.nbp.output.OutputHelper;
import se.trixon.almond.nbp.output.OutputLineMode;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class Executor implements Runnable {

    private final boolean mDryRun;

    private String mDryRunIndicator = "";
    private Thread mExecutorThread;
    private final InputOutput mInputOutput;
    private boolean mInterrupted;
    private long mLastRun;
    private FoldHandle mMainFoldHandle;
    private final Operation mOperation;
    private OutputHelper mOutputHelper;
    private ProgressHandle mProgressHandle;
    private final StatusDisplayer mStatusDisplayer = StatusDisplayer.getDefault();
    private final Task mTask;

    public Executor(Task task, boolean dryRun) {
        mTask = task;
        mDryRun = dryRun;
        mInputOutput = IOProvider.getDefault().getIO(mTask.getName(), false);
        mInputOutput.select();

        if (mDryRun) {
            mDryRunIndicator = String.format(" (%s)", Dict.DRY_RUN.toString());
        }

        mOutputHelper = new OutputHelper(mTask.getName(), mInputOutput, mDryRun);
        mOutputHelper.reset();
        mOperation = new Operation(mTask, mInputOutput, mOutputHelper);
    }

    @Override
    public void run() {
        var allowToCancel = (Cancellable) () -> {
            mExecutorThread.interrupt();
            mInterrupted = true;
            mProgressHandle.finish();
            ExecutorManager.getInstance().getExecutors().remove(mTask.getId());
            jobEnded(OutputLineMode.WARNING, Dict.CANCELED.toString());

            return true;
        };

        mLastRun = System.currentTimeMillis();
        mProgressHandle = ProgressHandle.createHandle(mTask.getName(), allowToCancel);
        mProgressHandle.start();
        mProgressHandle.switchToIndeterminate();

        mExecutorThread = new Thread(() -> {
            mOutputHelper.start();
            mOutputHelper.printSectionHeader(OutputLineMode.INFO, Dict.START.toString(), Dict.TASK.toLower(), mTask.getName());
            mMainFoldHandle = IOFolding.startFold(mInputOutput, true);

            if (!mTask.isValid()) {//and dest dir too
                mInputOutput.getErr().println(mTask.getValidationError());
                jobEnded(OutputLineMode.ERROR, Dict.FAILED.toString());
                mInputOutput.getErr().println(String.format("\n\n%s", Dict.JOB_FAILED.toString()));

                return;
            }

            mInterrupted = mOperation.start();

            if (!mInterrupted) {
                jobEnded(OutputLineMode.OK, Dict.DONE.toString());
            }

            mProgressHandle.finish();
            ExecutorManager.getInstance().getExecutors().remove(mTask.getId());
        }, "Executor");

        mExecutorThread.start();
    }

    private void jobEnded(OutputLineMode outputLineMode, String action) {
        mMainFoldHandle.finish();
        mStatusDisplayer.setStatusText(action);
        mOutputHelper.printSummary(outputLineMode, action, Dict.TASK.toString());
    }

}
