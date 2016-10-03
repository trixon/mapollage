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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;

/**
 *
 * @author Patrik Karlsson
 */
public class Profile extends ProfileBase implements Comparable<Profile>, Cloneable {

    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_FOLDER = "folder";
    private static final String KEY_NAME = "name";
    private static final String KEY_PHOTO = "photo";
    private static final String KEY_PLACEMARK = "placemark";
    private static final String KEY_SOURCE = "source";

    private ProfileDescription mDescription = new ProfileDescription(this);
    private File mDestinationFile;
    private ProfileFolder mFolder = new ProfileFolder(this);
    private String mName;
    private ProfilePhoto mPhoto = new ProfilePhoto(this);
    private ProfilePlacemark mPlacemark = new ProfilePlacemark(this);
    private ProfileSource mSource = new ProfileSource(this);

    public Profile() {

    }

    public Profile(JSONObject json) {
        setName((String) json.get(KEY_NAME));

        setSource(new ProfileSource(this, (JSONObject) json.get(KEY_SOURCE)));
        setFolder(new ProfileFolder(this, (JSONObject) json.get(KEY_FOLDER)));
        setPlacemark(new ProfilePlacemark(this, (JSONObject) json.get(KEY_PLACEMARK)));
        setDescription(new ProfileDescription(this, (JSONObject) json.get(KEY_DESCRIPTION)));
        setPhoto(new ProfilePhoto(this, (JSONObject) json.get(KEY_PHOTO)));
    }

    @Override
    public Profile clone() {
        try {
            return (Profile) super.clone();
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(Profile.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public int compareTo(Profile o) {
        return mName.compareTo(o.getName());
    }

    public ProfileDescription getDescription() {
        return mDescription;
    }

    public File getDestinationFile() {
        return mDestinationFile;
    }

    public ProfileFolder getFolder() {
        return mFolder;
    }

    @Override
    public JSONObject getJson() {
        JSONObject json = new JSONObject();
        json.put(KEY_NAME, getName());

        json.put(KEY_SOURCE, getSource().getJson());
        json.put(KEY_FOLDER, getFolder().getJson());
        json.put(KEY_PLACEMARK, getPlacemark().getJson());
        json.put(KEY_DESCRIPTION, getDescription().getJson());
        json.put(KEY_PHOTO, getPhoto().getJson());

        return json;
    }

    public String getName() {
        return mName;
    }

    public ProfilePhoto getPhoto() {
        return mPhoto;
    }

    public ProfilePlacemark getPlacemark() {
        return mPlacemark;
    }

    public ProfileSource getSource() {
        return mSource;
    }

    public String getValidationError() {
        return sValidationErrorBuilder.toString();
    }

    @Override
    public boolean isValid() {
        sValidationErrorBuilder = new StringBuilder();

        mSource.isValid();
        mFolder.isValid();
        mPlacemark.isValid();
        mPhoto.isValid();

        return sValidationErrorBuilder.length() == 0;
    }

    public void setDescription(ProfileDescription description) {
        mDescription = description;
    }

    public void setDestinationFile(File destinationFile) {
        mDestinationFile = destinationFile;
    }

    public void setFolder(ProfileFolder folder) {
        mFolder = folder;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setPhoto(ProfilePhoto photo) {
        mPhoto = photo;
    }

    public void setPlacemark(ProfilePlacemark placemark) {
        mPlacemark = placemark;
    }

    public void setSource(ProfileSource source) {
        mSource = source;
    }

//    public void setSourceAndDest(String[] args) {
//        if (args.length == 2) {
//            String source = args[0];
//            File sourceFile = new File(source);
//
//            if (sourceFile.isDirectory()) {
//                mSource.setDir(sourceFile);
//                mSource.setFilePattern("*");
//            } else {
//                String sourceDir = FilenameUtils.getFullPathNoEndSeparator(source);
//                mSource.setDir(new File(sourceDir));
//                mSource.setFilePattern(FilenameUtils.getName(source));
//            }
//
//            setDestFile(new File(args[1]));
//        } else {
//            addValidationError(mBundle.getString("invalid_arg_count"));
//        }
//    }
    @Override
    public String toDebugString() {
        StringBuilder builder = new StringBuilder("Profile summary { ").append(mName).append("\n")
                .append(mSource.toDebugString()).append("\n")
                .append(mFolder.toDebugString()).append("\n")
                .append(mPlacemark.toDebugString()).append("\n")
                .append(mDescription.toDebugString()).append("\n")
                .append(mPhoto.toDebugString()).append("\n")
                .append("}");

        return builder.toString();
    }

    @Override
    public String toString() {
        return mName;
    }
}
