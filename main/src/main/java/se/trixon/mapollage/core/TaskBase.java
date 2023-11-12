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
import se.trixon.mapollage.ui.task.Dummy;

/**
 *
 * @author Patrik Karlström
 */
public abstract class TaskBase {

    protected static StringBuilder sValidationErrorBuilder;
    protected static final ResourceBundle BUNDLE = NbBundle.getBundle(Mapollage.class);
    protected static final ResourceBundle BUNDLE_UI = NbBundle.getBundle(Dummy.class);
    protected static final Options mOptions = Options.getInstance();

    public TaskBase() {
    }

    public abstract String getTitle();

    public abstract boolean isValid();

    public String toDebugString() {
        TaskInfo profileInfo = getProfileInfo();
        int maxLength = profileInfo.getMaxLength() + 3;

        String separator = " : ";
        StringBuilder builder = new StringBuilder("\n");

        builder.append(profileInfo.getTitle()).append("\n");

        profileInfo.getValues().entrySet().forEach((entry) -> {
            String key = entry.getKey();
            String value = entry.getValue();
            builder.append(StringUtils.leftPad(key, maxLength)).append(separator).append(value).append("\n");
        });

        return builder.toString();
    }

    protected void addValidationError(String string) {
        sValidationErrorBuilder.append(string).append("\n");
    }

    protected abstract TaskInfo getProfileInfo();
}
