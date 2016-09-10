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

/**
 *
 * @author Patrik Karlsson
 */
public class ProfileManager {

    private static final String KEY_CASE_BASE = "case_base";
    private static final String KEY_CASE_SUFFIX = "case_suffix";
    private static final String KEY_DATE_PATTERN = "date_pattern";
    private static final String KEY_DATE_SOURCE = "date_source";
    private static final String KEY_DEST = "dest";
    private static final String KEY_FILE_PATTERN = "file_pattern";
    private static final String KEY_FOLLOW_LINKS = "follow_links";
    private static final String KEY_NAME = "name";
    private static final String KEY_OPERATION = "operation";
    private static final String KEY_OVERWRITE = "overwrite";
    private static final String KEY_PROFILES = "profiles";
    private static final String KEY_RECURSIVE = "recursive";
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
            JSONObject object = new JSONObject();
            object.put(KEY_NAME, profile.getName());
//            object.put(KEY_SOURCE, profile.getSourceDirAsString());
//            object.put(KEY_DEST, profile.getDestDirAsString());
//            object.put(KEY_FILE_PATTERN, profile.getFilePattern());
//            object.put(KEY_DATE_SOURCE, profile.getDateSource().name());
//            object.put(KEY_DATE_PATTERN, profile.getDatePattern());
//            object.put(KEY_OPERATION, profile.getOperation());
//            object.put(KEY_FOLLOW_LINKS, profile.isFollowLinks());
//            object.put(KEY_RECURSIVE, profile.isRecursive());
//            object.put(KEY_OVERWRITE, profile.isReplaceExisting());
//            object.put(KEY_CASE_BASE, profile.getBaseNameCase().name());
//            object.put(KEY_CASE_SUFFIX, profile.getExtNameCase().name());

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
            profile.setName((String) object.get(KEY_NAME));
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
