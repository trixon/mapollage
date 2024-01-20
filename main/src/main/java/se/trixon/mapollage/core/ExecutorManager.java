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
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.io.FileUtils;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.nbp.dialogs.NbMessage;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.swing.dialogs.SimpleDialog;

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

    public void requestStart(Task task) {
        if (task.isValid()) {
            requestKmlFileObject(task);
        } else {
            NbMessage.error(Dict.ABORTING.toString(), task.getValidationError());
        }
    }

    private void requestKmlFileObject(Task task) {
        var filter = new FileNameExtensionFilter("Keyhole Markup Language (*.kml)", "kml");
        SimpleDialog.clearFilters();
        SimpleDialog.addFilter(filter);
        SimpleDialog.setFilter(filter);
        SimpleDialog.setParent(Almond.getFrame());
        SimpleDialog.setTitle(String.format("%s %s", Dict.SAVE.toString(), task.getName()));

        var destination = task.getDestinationFile();
        if (destination == null) {
            SimpleDialog.setPath(FileUtils.getUserDirectory());
        } else {
            SimpleDialog.setPath(destination.getParentFile());
            SimpleDialog.setSelectedFile(destination);
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
        if (SimpleDialog.saveFile()) {
            task.setDestinationFile(SimpleDialog.getPath());
            task.isValid();

            if (task.hasValidRelativeSourceDest()) {
                start(task);
            } else {
                System.out.println("invalid");
//                mStatusPanel.out(mBundle.getString("invalid_relative_source_dest"));
//                mStatusPanel.out(Dict.ABORTING.toString());
            }
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
