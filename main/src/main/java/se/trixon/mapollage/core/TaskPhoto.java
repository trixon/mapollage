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

import com.google.gson.annotations.SerializedName;
import java.util.LinkedHashMap;
import org.apache.commons.lang3.SystemUtils;
import se.trixon.almond.util.BooleanHelper;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class TaskPhoto extends TaskBase {

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
    @SerializedName("reference")
    private Reference mReference = Reference.THUMBNAIL;
    @SerializedName("thumbnail_border_color")
    private String mThumbnailBorderColor = "FFFF00";
    @SerializedName("thumbnail_border_size")
    private int mThumbnailBorderSize = 3;
    @SerializedName("thumbnail_size")
    private int mThumbnailSize = 1000;
    @SerializedName("width_limit")
    private int mWidthLimit = 1000;

    public TaskPhoto() {
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

    public String getThumbnailBorderColor() {
        return mThumbnailBorderColor;
    }

    public int getThumbnailBorderSize() {
        return mThumbnailBorderSize;
    }

    public int getThumbnailSize() {
        return mThumbnailSize;
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

    public void setThumbnailBorderColor(String thumbnailBorderColor) {
        this.mThumbnailBorderColor = thumbnailBorderColor;
    }

    public void setThumbnailBorderSize(int thumbnailBorderSize) {
        mThumbnailBorderSize = thumbnailBorderSize;
    }

    public void setThumbnailSize(int thumbnailSize) {
        mThumbnailSize = thumbnailSize;
    }

    public void setWidthLimit(int maxWidthValue) {
        mWidthLimit = maxWidthValue;
    }

    @Override
    protected TaskInfo getTaskInfo() {
        var taskInfo = new TaskInfo();
        var values = new LinkedHashMap<String, String>();

        values.put(Dict.MAX_WIDTH.toString(), mLimitWidth ? String.valueOf(mWidthLimit) : "-");
        values.put(Dict.MAX_HEIGHT.toString(), mLimitHeight ? String.valueOf(mHeightLimit) : "-");

        String fileReference = null;

        switch (mReference) {
            case ABSOLUTE ->
                fileReference = Dict.ABSOLUTE.toString();

            case ABSOLUTE_PATH ->
                fileReference = mBaseUrlValue;

            case RELATIVE ->
                fileReference = Dict.RELATIVE.toString();

            case THUMBNAIL ->
                fileReference = Dict.THUMBNAIL.toString();
        }

        values.put(Dict.FILE_REFERENCE.toString(), fileReference);
        values.put(BUNDLE_UI.getString("PhotoTab.lowerCaseExtCheckBox"), BooleanHelper.asYesNo(mForceLowerCaseExtension));
        values.put(BUNDLE_UI.getString("PhotoTab.thumbnailSize"), String.valueOf(getTask().getPhoto().getThumbnailSize()));
        values.put(BUNDLE_UI.getString("PhotoTab.thumbnailBorderSize"), String.valueOf(getTask().getPhoto().getThumbnailBorderSize()));
        values.put(BUNDLE_UI.getString("PhotoTab.thumbnailBorderColor"), getTask().getPhoto().getThumbnailBorderColor());

        taskInfo.setTitle(getTitle());
        taskInfo.setValues(values);

        return taskInfo;
    }

    public static enum Reference {
        //NEVER EVER change the order of the elements
        ABSOLUTE,
        ABSOLUTE_PATH,
        RELATIVE,
        THUMBNAIL;
    }
}
