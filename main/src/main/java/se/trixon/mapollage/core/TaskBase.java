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

import java.util.ResourceBundle;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.NbBundle;
import se.trixon.mapollage.Mapollage;
import se.trixon.mapollage.Options;
import se.trixon.mapollage.ui.task.BaseTab;

/**
 *
 * @author Patrik Karlström
 */
public abstract class TaskBase {

    protected static final ResourceBundle BUNDLE = NbBundle.getBundle(Mapollage.class);
    protected static final ResourceBundle BUNDLE_UI = NbBundle.getBundle(BaseTab.class);
    protected static final Options mOptions = Options.getInstance();
    protected static StringBuilder sValidationErrorBuilder;
    private transient Task mTask;

    public TaskBase() {
    }

    public Task getTask() {
        return mTask;
    }

    public abstract String getTitle();

    public abstract boolean isValid();

    public void setTask(Task task) {
        mTask = task;
    }

    public String toDebugString() {
        var taskInfo = getTaskInfo();
        int maxLength = taskInfo.getMaxLength() + 3;

        String separator = " : ";
        StringBuilder builder = new StringBuilder("\n");

        builder.append(taskInfo.getTitle()).append("\n");

        taskInfo.getValues().entrySet().forEach((entry) -> {
            String key = entry.getKey();
            String value = entry.getValue();
            builder.append(StringUtils.leftPad(key, maxLength)).append(separator).append(value).append("\n");
        });

        return builder.toString();
    }

    protected void addValidationError(String string) {
        sValidationErrorBuilder.append(string).append("\n");
    }

    protected abstract TaskInfo getTaskInfo();
}
