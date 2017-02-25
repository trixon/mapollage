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

import java.util.LinkedHashMap;
import org.json.simple.JSONObject;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlsson
 */
public class ProfilePath extends ProfileBase {

    public static final String KEY_DRAW_PATH = "drawPath";
    public static final String KEY_SPLIT_BY = "splitBy";
    public static final String KEY_WIDTH = "width";
    public static final int SPLIT_BY_DAY = 2;
    public static final int SPLIT_BY_HOUR = 1;
    public static final int SPLIT_BY_MONTH = 4;
    public static final int SPLIT_BY_NONE = 0;
    public static final int SPLIT_BY_WEEK = 3;
    public static final int SPLIT_BY_YEAR = 5;

    private boolean mDrawPath = true;
    private final Profile mProfile;
    private int mSplitBy = SPLIT_BY_MONTH;
    private Double mWidth = 2.0;

    public ProfilePath(Profile profile) {
        mProfile = profile;
    }

    public ProfilePath(Profile profile, JSONObject json) {
        mProfile = profile;
        mDrawPath = getBoolean(json, KEY_DRAW_PATH, mDrawPath);
        mWidth = getDouble(json, KEY_WIDTH, mWidth);
        mSplitBy = getInt(json, KEY_SPLIT_BY, mSplitBy);

    }

    @Override
    public JSONObject getJson() {
        JSONObject json = new JSONObject();
        json.put(KEY_DRAW_PATH, mDrawPath);
        json.put(KEY_WIDTH, mWidth);
        json.put(KEY_SPLIT_BY, mSplitBy);

        return json;
    }

    public int getSplitBy() {
        return mSplitBy;
    }

    @Override
    public String getTitle() {
        return Dict.PATH_GFX.toString();
    }

    public Double getWidth() {
        return mWidth;
    }

    public boolean isDrawPath() {
        return mDrawPath;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    public void setDrawPath(boolean drawPath) {
        mDrawPath = drawPath;
    }

    public void setSplitBy(int splitBy) {
        mSplitBy = splitBy;
    }

    public void setWidth(Double width) {
        mWidth = width;
    }

    @Override
    protected ProfileInfo getProfileInfo() {
        ProfileInfo profileInfo = new ProfileInfo();
        LinkedHashMap<String, String> values = new LinkedHashMap<>();
        values.put(mBundleUI.getString("ModulePathPanel.drawPathCheckBox.text"), String.valueOf(mDrawPath));
        values.put(Dict.WIDTH.toString(), String.valueOf(mWidth));

        profileInfo.setTitle(getTitle());
        profileInfo.setValues(values);

        return profileInfo;
    }
}
