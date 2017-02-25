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
package se.trixon.mapollage.profile;

import java.io.File;
import java.util.Map;
import java.util.ResourceBundle;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import se.trixon.almond.util.BundleHelper;
import se.trixon.mapollage.Options;
import se.trixon.mapollage.ui.config.ConfigPanel;

/**
 *
 * @author Patrik Karlsson
 */
public abstract class ProfileBase {

    protected static StringBuilder sValidationErrorBuilder;
    protected final ResourceBundle mBundle = BundleHelper.getBundle(Profile.class, "Bundle");
    protected final ResourceBundle mBundleUI = BundleHelper.getBundle(ConfigPanel.class, "Bundle");
    protected final Options mOptions = Options.getInstance();

    public abstract JSONObject getJson();

    public abstract String getTitle();

    public abstract boolean isValid();

    public String toDebugString() {
        ProfileInfo profileInfo = getProfileInfo();
        int maxLength = profileInfo.getMaxLength() + 3;

        String separator = " : ";
        StringBuilder builder = new StringBuilder("\n");

        builder.append(profileInfo.getTitle()).append("\n");

        for (Map.Entry<String, String> entry : profileInfo.getValues().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            builder.append(StringUtils.leftPad(key, maxLength)).append(separator).append(value).append("\n");
        }

        return builder.toString();
    }

    protected void addValidationError(String string) {
        sValidationErrorBuilder.append(string).append("\n");
    }

    protected boolean getBoolean(JSONObject object, String key, boolean defaultValue) {
        try {
            return (boolean) object.get(key);
        } catch (NullPointerException e) {
            return defaultValue;
        }
    }

    protected Double getDouble(JSONObject object, String key, Double defaultValue) {
        if (object.containsKey(key)) {
            try {
                Double d = ((Double) object.get(key));
                return d;
            } catch (Exception e) {
            }

        }

        return defaultValue;
    }

    protected File getFileObject(JSONObject object, String key) {
        try {
            return new File((String) object.get(key));
        } catch (Exception e) {
            return null;
        }
    }

    protected int getInt(JSONObject object, String key) {
        try {
            return ((Long) object.get(key)).intValue();

        } catch (ClassCastException e) {
            return (Integer) object.get(key);
        } catch (NullPointerException e) {
            return 0;
        }
    }

    protected abstract ProfileInfo getProfileInfo();
}
