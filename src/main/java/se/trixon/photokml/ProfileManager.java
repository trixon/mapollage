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
import se.trixon.photokml.Profile.Source;

/**
 *
 * @author Patrik Karlsson
 */
public class ProfileManager {

    private static final String KEY_NAME = "name";
    private static final String KEY_PROFILES = "profiles";
    private static final String KEY_SOURCE = "source";
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

    public JSONArray getJsonArray() {
        JSONArray array = new JSONArray();

        for (Profile profile : mProfiles) {
            Source source = profile.getSource();
            JSONObject object = new JSONObject();
            object.put(KEY_NAME, profile.getName());

            JSONObject sourceObject = new JSONObject();
            sourceObject.put(Source.KEY_PATH, source.getDir().getAbsolutePath());
            sourceObject.put(Source.KEY_PATTERN, source.getFilePattern());
            sourceObject.put(Source.KEY_FOLLOW_LINKS, source.isFollowLinks());
            sourceObject.put(Source.KEY_RECURSIVE, source.isRecursive());

            object.put(KEY_SOURCE, sourceObject);
            array.add(object);
        }

        return array;
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
            JSONArray jobsArray = (JSONArray) jsonObject.get(KEY_PROFILES);

            setProfiles(jobsArray);
        }
    }

    public void save() throws IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(KEY_PROFILES, getJsonArray());
        jsonObject.put(KEY_VERSION, sVersion);

        String jsonString = jsonObject.toJSONString();
        FileUtils.writeStringToFile(mProfileFile, jsonString, Charset.defaultCharset());
    }

    private boolean getBoolean(JSONObject object, String key) {
        return (boolean) object.get(key);
    }

    private File getFileObject(JSONObject object, String key) {
        try {
            return new File((String) object.get(key));
        } catch (Exception e) {
            return null;
        }
    }

    private int getInt(JSONObject object, String key) {
        return ((Long) object.get(key)).intValue();
    }

    void setProfiles(JSONArray array) {
        mProfiles.clear();

        for (Object arrayItem : array) {
            JSONObject object = (JSONObject) arrayItem;
            Profile profile = new Profile();
            Source source = profile.getSource();

            profile.setName((String) object.get(KEY_NAME));
            JSONObject sourceObject = (JSONObject) object.get(KEY_SOURCE);

            source.setDir(getFileObject(sourceObject, Source.KEY_PATH));
            source.setFilePattern((String) sourceObject.get(Source.KEY_PATTERN));
            source.setRecursive(getBoolean(sourceObject, Source.KEY_RECURSIVE));
            source.setFollowLinks(getBoolean(sourceObject, Source.KEY_FOLLOW_LINKS));

//            profile.setSourceDir(getFileObject(object, KEY_SOURCE));
//            profile.setDestDir(getFileObject(object, KEY_DEST));
//            profile.setFilePattern((String) object.get(KEY_FILE_PATTERN));
//            DateSource dateSource = DateSource.valueOf((String) object.get(KEY_DATE_SOURCE));
//            profile.setDateSource(dateSource);
//            profile.setDatePattern((String) object.get(KEY_DATE_PATTERN));
//            profile.setOperation(getInt(object, KEY_OPERATION));
//            profile.setFollowLinks(getBoolean(object, KEY_FOLLOW_LINKS));
//            profile.setRecursive(getBoolean(object, KEY_RECURSIVE));
//            profile.setReplaceExisting(getBoolean(object, KEY_OVERWRITE));
//            NameCase nameCase = NameCase.valueOf((String) object.get(KEY_CASE_BASE));
//            profile.setBaseNameCase(nameCase);
//            nameCase = NameCase.valueOf((String) object.get(KEY_CASE_SUFFIX));
//            profile.setExtNameCase(nameCase);
            mProfiles.add(profile);
        }

        Collections.sort(mProfiles);
    }

    private static class Holder {

        private static final ProfileManager INSTANCE = new ProfileManager();
    }
}
