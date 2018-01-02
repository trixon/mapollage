/*
 * Copyright 2018 Patrik Karlsson.
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
package se.trixon.mapollage;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import se.trixon.almond.util.Xlog;
import se.trixon.mapollage.profile.Profile;

/**
 *
 * @author Patrik Karlsson
 */
public class ProfileManager {

    private final File mDirectory;
    private final File mProfileFile;
    private ProfilesJson mProfilesHolder;

    public static ProfileManager getInstance() {
        return Holder.INSTANCE;
    }

    private ProfileManager() {
        mDirectory = new File(System.getProperty("user.home"), ".config/mapollage");
        mProfileFile = new File(mDirectory, "mapollage2.profiles");

        try {
            FileUtils.forceMkdir(mDirectory);
        } catch (IOException ex) {
            Xlog.timedErr(ex.getLocalizedMessage());
        }
    }

    public Profile getProfile(String name) {
        for (Profile profile : mProfilesHolder.getProfiles()) {
            if (profile.getName().equalsIgnoreCase(name)) {
                return profile;
            }
        }

        return null;
    }

    public LinkedList<Profile> getProfiles() {
        if (mProfilesHolder == null) {
            try {
                load();
            } catch (IOException ex) {
                mProfilesHolder = new ProfilesJson();
                Logger.getLogger(ProfileManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return mProfilesHolder.getProfiles();
    }

    public int getVersion() {
        return mProfilesHolder.getFileFormatVersion();
    }

    public boolean hasProfiles() {
        return !mProfilesHolder.getProfiles().isEmpty();
    }

    public void load() throws IOException {
        if (mProfileFile.exists()) {
            mProfilesHolder = ProfilesJson.open(mProfileFile);
        } else {
            mProfilesHolder = new ProfilesJson();
        }
    }

    public void save() throws IOException {
        mProfilesHolder.save(mProfileFile);
    }

    private static class Holder {

        private static final ProfileManager INSTANCE = new ProfileManager();
    }
}
