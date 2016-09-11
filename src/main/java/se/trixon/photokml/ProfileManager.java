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
import se.trixon.photokml.profile.Profile;
import se.trixon.photokml.profile.ProfileFolder;
import se.trixon.photokml.profile.ProfilePlacemark;
import se.trixon.photokml.profile.ProfileSource;

/**
 *
 * @author Patrik Karlsson
 */
public class ProfileManager {

    private static final String KEY_FOLDER = "folder";
    private static final String KEY_NAME = "name";
    private static final String KEY_PLACEMARK = "placemark";
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
            JSONObject object = new JSONObject();
            object.put(KEY_NAME, profile.getName());

            ProfileSource source = profile.getSource();
            JSONObject sourceObject = new JSONObject();
            sourceObject.put(ProfileSource.KEY_PATH, source.getDir().getAbsolutePath());
            sourceObject.put(ProfileSource.KEY_PATTERN, source.getFilePattern());
            sourceObject.put(ProfileSource.KEY_FOLLOW_LINKS, source.isFollowLinks());
            sourceObject.put(ProfileSource.KEY_RECURSIVE, source.isRecursive());
            object.put(KEY_SOURCE, sourceObject);

            ProfileFolder profileFolder = profile.getFolder();
            JSONObject folderObject = new JSONObject();
            folderObject.put(ProfileFolder.KEY_ROOT_NAME, profileFolder.getRootName());
            folderObject.put(ProfileFolder.KEY_ROOT_DESCRIPTION, profileFolder.getRootDescription());
            folderObject.put(ProfileFolder.KEY_FOLDERS_BY, profileFolder.getFoldersBy());
            folderObject.put(ProfileFolder.KEY_CREATE_FOLDERS, profileFolder.isCreateFolders());
            folderObject.put(ProfileFolder.KEY_DATE_PATTERN, profileFolder.getDatePattern());
            folderObject.put(ProfileFolder.KEY_REGEX, profileFolder.getRegex());
            folderObject.put(ProfileFolder.KEY_REGEX_DEFAULT, profileFolder.getRegexDefault());
            object.put(KEY_FOLDER, folderObject);

            ProfilePlacemark profilePlacemark = profile.getPlacemark();
            JSONObject placemarkObject = new JSONObject();
            placemarkObject.put(ProfilePlacemark.KEY_LAT, profilePlacemark.getLat());
            placemarkObject.put(ProfilePlacemark.KEY_LON, profilePlacemark.getLon());
            placemarkObject.put(ProfilePlacemark.KEY_NAME_BY, profilePlacemark.getNameBy());
            placemarkObject.put(ProfilePlacemark.KEY_INCLUDE_NULL_COORDINATE, profilePlacemark.isIncludeNullCoordinate());
            placemarkObject.put(ProfilePlacemark.KEY_DATE_PATTERN, profilePlacemark.getDatePattern());
            object.put(KEY_PLACEMARK, placemarkObject);

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

            ProfileSource source = profile.getSource();
            JSONObject sourceObject = (JSONObject) object.get(KEY_SOURCE);

            source.setDir(getFileObject(sourceObject, ProfileSource.KEY_PATH));
            source.setFilePattern((String) sourceObject.get(ProfileSource.KEY_PATTERN));
            source.setRecursive(getBoolean(sourceObject, ProfileSource.KEY_RECURSIVE));
            source.setFollowLinks(getBoolean(sourceObject, ProfileSource.KEY_FOLLOW_LINKS));

            ProfileFolder profileFolder = profile.getFolder();
            JSONObject folderObject = (JSONObject) object.get(KEY_FOLDER);
            profileFolder.setRootName((String) folderObject.get(ProfileFolder.KEY_ROOT_NAME));
            profileFolder.setRootDescription((String) folderObject.get(ProfileFolder.KEY_ROOT_DESCRIPTION));
            profileFolder.setCreateFolders(getBoolean(folderObject, ProfileFolder.KEY_CREATE_FOLDERS));
            profileFolder.setFoldersBy(getInt(folderObject, ProfileFolder.KEY_FOLDERS_BY));
            profileFolder.setDatePattern((String) folderObject.get(ProfileFolder.KEY_DATE_PATTERN));
            profileFolder.setRegex((String) folderObject.get(ProfileFolder.KEY_REGEX));
            profileFolder.setRegexDefault((String) folderObject.get(ProfileFolder.KEY_REGEX_DEFAULT));

            ProfilePlacemark profilePlacemark = profile.getPlacemark();
            JSONObject placemarkObject = (JSONObject) object.get(KEY_PLACEMARK);
            profilePlacemark.setLat((Double) placemarkObject.get(ProfilePlacemark.KEY_LAT));
            profilePlacemark.setLon((Double) placemarkObject.get(ProfilePlacemark.KEY_LON));
            profilePlacemark.setNameBy(getInt(placemarkObject, ProfilePlacemark.KEY_NAME_BY));
            profilePlacemark.setIncludeNullCoordinate(getBoolean(placemarkObject, ProfilePlacemark.KEY_INCLUDE_NULL_COORDINATE));
            profilePlacemark.setDatePattern((String) placemarkObject.get(ProfilePlacemark.KEY_DATE_PATTERN));

            mProfiles.add(profile);
        }

        Collections.sort(mProfiles);
    }

    private static class Holder {

        private static final ProfileManager INSTANCE = new ProfileManager();
    }
}
