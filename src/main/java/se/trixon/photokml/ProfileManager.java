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
package se.trixon.photokml;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.LinkedList;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import se.trixon.almond.util.Xlog;
import se.trixon.photokml.profile.Profile;

/**
 *
 * @author Patrik Karlsson
 */
public class ProfileManager {

    private static final String KEY_PROFILES = "profiles";
    private static final String KEY_VERSION = "version";

    private static final int sVersion = 1;
    private final File mDirectory;
    private final File mProfileFile;
    private final LinkedList<Profile> mProfiles = new LinkedList<>();
    private int mVersion;

    public static ProfileManager getInstance() {
        return Holder.INSTANCE;
    }

    private ProfileManager() {
        mDirectory = new File(System.getProperty("user.home"), ".config/trixon");
        mProfileFile = new File(mDirectory, "photokml.profiles");

        try {
            FileUtils.forceMkdir(mDirectory);
        } catch (IOException ex) {
            Xlog.timedErr(ex.getLocalizedMessage());
        }
    }

    public Profile getProfile(String name) {
        for (Profile profile : mProfiles) {
            if (profile.getName().equalsIgnoreCase(name)) {
                return profile;
            }
        }

        return null;
    }

    public LinkedList<Profile> getProfiles() {
        return mProfiles;
    }

    public int getVersion() {
        return mVersion;
    }

    public boolean hasProfiles() {
        return !mProfiles.isEmpty();
    }

    public void load() throws IOException {
        if (mProfileFile.exists()) {
            JSONObject jsonObject = (JSONObject) JSONValue.parse(FileUtils.readFileToString(mProfileFile, Charset.defaultCharset()));
            mVersion = getInt(jsonObject, KEY_VERSION);
            JSONArray array = (JSONArray) jsonObject.get(KEY_PROFILES);

            mProfiles.clear();

            for (Object arrayItem : array) {
                JSONObject object = (JSONObject) arrayItem;
                mProfiles.add(new Profile(object));
            }

            Collections.sort(mProfiles);
        }
    }

    public void save() throws IOException {
        JSONArray array = new JSONArray();

        for (Profile profile : mProfiles) {
            array.add(profile.getJson());
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(KEY_PROFILES, array);
        jsonObject.put(KEY_VERSION, sVersion);

        String jsonString = jsonObject.toJSONString();
        FileUtils.writeStringToFile(mProfileFile, jsonString, Charset.defaultCharset());
    }

    private int getInt(JSONObject object, String key) {
        return ((Long) object.get(key)).intValue();
    }

    private static class Holder {

        private static final ProfileManager INSTANCE = new ProfileManager();
    }
}
