/*
 * Copyright 2023 Patrik Karlström <patrik@trixon.se>.
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

import java.util.HashMap;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import se.trixon.almond.nbp.dialogs.NbMessage;
import se.trixon.almond.nbp.fx.FxDialogPanel;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class ExecutorManager {

    private final HashMap<String, Executor> mExecutors = new HashMap<>();

    public static ExecutorManager getInstance() {
        return Holder.INSTANCE;
    }

    private ExecutorManager() {
    }

    public HashMap<String, Executor> getExecutors() {
        return mExecutors;
    }

    /*
    TODO
    Request dest file
            if (!Files.isWritable(mDestinationFile.getParentFile().toPath())) {
            mListener.onOperationLog(String.format(mBundle.getString("insufficient_privileges"), mDestinationFile.getAbsolutePath()));
            Thread.currentThread().interrupt();
            mListener.onOperationInterrupted();
            return;
        }

     */
    public void requestStart(Task task) {
        if (mExecutors.containsKey(task.getId())) {
            NbMessage.error(Dict.Dialog.TITLE_TASK_RUNNING.toString(), Dict.Dialog.MESSAGE_TASK_RUNNING.toString());
        } else {
//            var taskSummary = new TaskSummary(task);
            var taskSummary = new Label("asdf");

            var dialogPanel = new FxDialogPanel() {
                @Override
                protected void fxConstructor() {
                    setScene(new Scene(taskSummary));
                }
            };
            dialogPanel.setPreferredSize(SwingHelper.getUIScaledDim(640, 300));

            SwingUtilities.invokeLater(() -> {
                var title = Dict.Dialog.TITLE_TASK_RUN_S.toString().formatted(task.getName());
                var runButton = new JButton(Dict.RUN.toString());
                var d = new DialogDescriptor(
                        dialogPanel,
                        title,
                        true,
                        new Object[]{Dict.CANCEL.toString(), runButton},
                        runButton,
                        0,
                        null,
                        null
                );

                d.setValid(false);
                dialogPanel.setNotifyDescriptor(d);
                dialogPanel.initFx(null);
                SwingHelper.runLaterDelayed(100, () -> runButton.requestFocus());
                var result = DialogDisplayer.getDefault().notify(d);

                if (result == runButton) {
                    start(task);
                }
            });
        }
    }

    public void start(Task task) {
        var executor = new Executor(task);
        mExecutors.put(task.getId(), executor);
        executor.run();
    }

    private static class Holder {

        private static final ExecutorManager INSTANCE = new ExecutorManager();
    }

}
