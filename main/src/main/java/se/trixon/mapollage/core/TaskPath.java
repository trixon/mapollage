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
import se.trixon.almond.util.BooleanHelper;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class TaskPath extends TaskBase {

    @SerializedName("draw_path")
    private boolean mDrawPath = true;
    @SerializedName("draw_polygon")
    private boolean mDrawPolygon = false;
    @SerializedName("path_color")
    private String mPathColor = "FF0000";
    @SerializedName("path_gap_color")
    private String mPathGapColor = "FFFF00";
    @SerializedName("split_by")
    private SplitBy mSplitBy = SplitBy.MONTH;
    @SerializedName("width")
    private Double mWidth = 2.0;

    public TaskPath() {
    }

    public String getPathColor() {
        return mPathColor;
    }

    public String getPathGapColor() {
        return mPathGapColor;
    }

    public SplitBy getSplitBy() {
        return mSplitBy;
    }

    @Override
    public String getTitle() {
        return Dict.Geometry.PATH.toString();
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

    public void setPathColor(String pathColor) {
        mPathColor = pathColor;
    }

    public void setPathGapColor(String pathGapColor) {
        mPathGapColor = pathGapColor;
    }

    public void setSplitBy(SplitBy splitBy) {
        mSplitBy = splitBy;
    }

    public void setWidth(Double width) {
        mWidth = width;
    }

    @Override
    protected TaskInfo getTaskInfo() {
        var taskInfo = new TaskInfo();
        var values = new LinkedHashMap<String, String>();
        values.put(BUNDLE_UI.getString("PathTab.drawPolygonCheckBox"), BooleanHelper.asYesNo(mDrawPolygon));
        values.put(BUNDLE_UI.getString("PathTab.drawPathCheckBox"), BooleanHelper.asYesNo(mDrawPath));
        values.put(Dict.Geometry.WIDTH.toString(), String.valueOf(mWidth));

        values.put(Dict.SPLIT_BY.toString(), getLabel(mSplitBy));

        taskInfo.setTitle(getTitle());
        taskInfo.setValues(values);

        return taskInfo;
    }

    private String getLabel(SplitBy splitBy) {
        return switch (splitBy) {
            case NONE ->
                Dict.DO_NOT_SPLIT.toString();
            case HOUR ->
                Dict.Time.HOUR.toString();
            case DAY ->
                Dict.Time.DAY.toString();
            case WEEK ->
                Dict.Time.WEEK.toString();
            case MONTH ->
                Dict.Time.MONTH.toString();
            case YEAR ->
                Dict.Time.YEAR.toString();
            default ->
                null;
        };
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
