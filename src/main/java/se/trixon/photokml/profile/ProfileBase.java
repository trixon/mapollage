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
import java.util.ResourceBundle;
import org.json.simple.JSONObject;
import se.trixon.almond.util.BundleHelper;

/**
 *
 * @author Patrik Karlsson
 */
public abstract class ProfileBase {

    protected final ResourceBundle mBundle = BundleHelper.getBundle(Profile.class, "Bundle");

    public abstract boolean isValid();

    public abstract String toDebugString();

    protected boolean getBoolean(JSONObject object, String key) {
        return (boolean) object.get(key);
    }

    protected File getFileObject(JSONObject object, String key) {
        try {
            return new File((String) object.get(key));
        } catch (Exception e) {
            return null;
        }
    }

    protected int getInt(JSONObject object, String key) {
        return ((Long) object.get(key)).intValue();
    }
}
