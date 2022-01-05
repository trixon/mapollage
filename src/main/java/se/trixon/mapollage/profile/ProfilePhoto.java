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
package se.trixon.mapollage.profile;

import com.google.gson.annotations.SerializedName;
import java.util.LinkedHashMap;
import org.apache.commons.lang3.SystemUtils;
import se.trixon.almond.util.BooleanHelper;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class ProfilePhoto extends ProfileBase {

    @SerializedName("base_url_value")
    private String mBaseUrlValue = "https://www.domain.com/img/";
    @SerializedName("force_lower_case_extension")
    private boolean mForceLowerCaseExtension = SystemUtils.IS_OS_WINDOWS;
    @SerializedName("height_limit")
    private int mHeightLimit = 800;
    @SerializedName("limit_height")
    private boolean mLimitHeight = true;
    @SerializedName("limit_width")
    private boolean mLimitWidth = true;
    private transient final Profile mProfile;
    @SerializedName("reference")
    private Reference mReference = Reference.ABSOLUTE;
    @SerializedName("width_limit")
    private int mWidthLimit = 1000;

    public ProfilePhoto(Profile profile) {
        mProfile = profile;
    }

    public String getBaseUrlValue() {
        return mBaseUrlValue;
    }

    public int getHeightLimit() {
        return mHeightLimit;
    }

    public Reference getReference() {
        return mReference;
    }

    @Override
    public String getTitle() {
        return Dict.PHOTO.toString();
    }

    public int getWidthLimit() {
        return mWidthLimit;
    }

    public boolean isForceLowerCaseExtension() {
        return mForceLowerCaseExtension;
    }

    public boolean isLimitHeight() {
        return mLimitHeight;
    }

    public boolean isLimitWidth() {
        return mLimitWidth;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    public void setBaseUrlValue(String baseUrlValue) {
        mBaseUrlValue = baseUrlValue;
    }

    public void setForceLowerCaseExtension(boolean forceLowerCaseExtension) {
        mForceLowerCaseExtension = forceLowerCaseExtension;
    }

    public void setHeightLimit(int maxHeightValue) {
        mHeightLimit = maxHeightValue;
    }

    public void setLimitHeight(boolean value) {
        mLimitHeight = value;
    }

    public void setLimitWidth(boolean value) {
        mLimitWidth = value;
    }

    public void setReference(Reference reference) {
        mReference = reference;
    }

    public void setWidthLimit(int maxWidthValue) {
        mWidthLimit = maxWidthValue;
    }

    @Override
    protected ProfileInfo getProfileInfo() {
        ProfileInfo profileInfo = new ProfileInfo();
        LinkedHashMap<String, String> values = new LinkedHashMap<>();

        values.put(Dict.MAX_WIDTH.toString(), mLimitWidth ? String.valueOf(mWidthLimit) : "-");
        values.put(Dict.MAX_HEIGHT.toString(), mLimitHeight ? String.valueOf(mHeightLimit) : "-");

        String fileReference = null;

        switch (mReference) {
            case ABSOLUTE:
                fileReference = Dict.ABSOLUTE.toString();
                break;

            case ABSOLUTE_PATH:
                fileReference = mBaseUrlValue;
                break;

            case RELATIVE:
                fileReference = Dict.RELATIVE.toString();
                break;

            case THUMBNAIL:
                fileReference = Dict.THUMBNAIL.toString();
                break;
        }

        values.put(Dict.FILE_REFERENCE.toString(), fileReference);
        values.put(BUNDLE_UI.getString("PhotoTab.lowerCaseExtCheckBox"), BooleanHelper.asYesNo(mForceLowerCaseExtension));

        profileInfo.setTitle(getTitle());
        profileInfo.setValues(values);

        return profileInfo;
    }

    public static enum Reference {
        //NEVER EVER change the order of the elements
        ABSOLUTE,
        ABSOLUTE_PATH,
        RELATIVE,
        THUMBNAIL;
    }
}
