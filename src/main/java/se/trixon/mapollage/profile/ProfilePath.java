/*
 * Copyright 2018 Patrik Karlström.
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
import se.trixon.almond.util.BooleanHelper;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class ProfilePath extends ProfileBase {

    @SerializedName("draw_polygon")
    private boolean mDrawPolygon = true;
    @SerializedName("draw_path")
    private boolean mDrawPath = true;
    private transient final Profile mProfile;
    @SerializedName("split_by")
    private SplitBy mSplitBy = SplitBy.MONTH;
    @SerializedName("width")
    private Double mWidth = 2.0;

    public ProfilePath(Profile profile) {
        mProfile = profile;
    }

    public SplitBy getSplitBy() {
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

    public boolean isDrawPolygon() {
        return mDrawPolygon;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    public void setDrawPath(boolean drawPath) {
        mDrawPath = drawPath;
    }

    public void setDrawPolygon(boolean drawPolygon) {
        mDrawPolygon = drawPolygon;
    }

    public void setSplitBy(SplitBy splitBy) {
        mSplitBy = splitBy;
    }

    public void setWidth(Double width) {
        mWidth = width;
    }

    private String getLabel(SplitBy splitBy) {
        switch (splitBy) {
            case NONE:
                return Dict.DO_NOT_SPLIT.toString();
            case HOUR:
                return Dict.Time.HOUR.toString();
            case DAY:
                return Dict.Time.DAY.toString();
            case WEEK:
                return Dict.Time.WEEK.toString();
            case MONTH:
                return Dict.Time.MONTH.toString();
            case YEAR:
                return Dict.Time.YEAR.toString();
            default:
                return null;
        }
    }

    @Override
    protected ProfileInfo getProfileInfo() {
        ProfileInfo profileInfo = new ProfileInfo();
        LinkedHashMap<String, String> values = new LinkedHashMap<>();
        values.put(BUNDLE_UI.getString("ModulePathPanel.drawPathCheckBox.text"), BooleanHelper.asYesNo(mDrawPath));
        values.put(BUNDLE_UI.getString("ModulePathPanel.drawPolygonCheckBox.text"), BooleanHelper.asYesNo(mDrawPolygon));
        values.put(Dict.WIDTH.toString(), String.valueOf(mWidth));

        values.put(Dict.SPLIT_BY.toString(), getLabel(mSplitBy));

        profileInfo.setTitle(getTitle());
        profileInfo.setValues(values);

        return profileInfo;
    }

    public enum SplitBy {
        NONE,
        HOUR,
        DAY,
        WEEK,
        MONTH,
        YEAR;
    }
}
