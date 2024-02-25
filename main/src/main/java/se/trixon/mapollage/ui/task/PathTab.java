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
package se.trixon.mapollage.ui.task;

import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.controlsfx.glyphfont.FontAwesome;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.mapollage.core.Task;
import se.trixon.mapollage.core.TaskPath.SplitBy;

/**
 *
 * @author Patrik Karlström
 */
public class PathTab extends BaseTab {

    private final CheckBox mDrawPathCheckBox = new CheckBox(mBundle.getString("PathTab.drawPathCheckBox"));
    private final CheckBox mDrawPolygonCheckBox = new CheckBox(mBundle.getString("PathTab.drawPolygonCheckBox"));
    private final RadioButton mSplitByDayRadioButton = new RadioButton(Dict.Time.DAY.toString());
    private final RadioButton mSplitByHourRadioButton = new RadioButton(Dict.Time.HOUR.toString());
    private final RadioButton mSplitByMonthRadioButton = new RadioButton(Dict.Time.MONTH.toString());
    private final RadioButton mSplitByNoneRadioButton = new RadioButton(Dict.DO_NOT_SPLIT.toString());
    private final RadioButton mSplitByWeekRadioButton = new RadioButton(Dict.Time.WEEK.toString());
    private final RadioButton mSplitByYearRadioButton = new RadioButton(Dict.Time.YEAR.toString());
    private final ToggleGroup mToggleGroup = new ToggleGroup();
    private final ColorPicker mTrackColorPicker = new ColorPicker();
    private final ColorPicker mTrackGapColorPicker = new ColorPicker();
    private final Spinner<Double> mWidthSpinner = new Spinner(1.0, 10.0, 1.0, 0.1);

    public PathTab() {
        setText(Dict.TRACKS.toString());
        setGraphic(FontAwesome.Glyph.CODE_FORK.getChar());
        createUI();
    }

    @Override
    public void load(Task task) {
        mTask = task;

        var taskPath = mTask.getPath();
        mDrawPolygonCheckBox.setSelected(taskPath.isDrawPolygon());
        mDrawPathCheckBox.setSelected(taskPath.isDrawPath());
        mWidthSpinner.getValueFactory().setValue(taskPath.getWidth());

        RadioButton splitByRadioButton;

        switch (taskPath.getSplitBy()) {
            case HOUR ->
                splitByRadioButton = mSplitByHourRadioButton;

            case DAY ->
                splitByRadioButton = mSplitByDayRadioButton;

            case WEEK ->
                splitByRadioButton = mSplitByWeekRadioButton;

            case MONTH ->
                splitByRadioButton = mSplitByMonthRadioButton;

            case YEAR ->
                splitByRadioButton = mSplitByYearRadioButton;

            case NONE ->
                splitByRadioButton = mSplitByNoneRadioButton;

            default ->
                throw new AssertionError();
        }

        splitByRadioButton.setSelected(true);

        loadColor(taskPath.getPathColor(), mTrackColorPicker);
        loadColor(taskPath.getPathGapColor(), mTrackGapColorPicker);
    }

    @Override
    public void save() {
        var taskPath = mTask.getPath();
        taskPath.setDrawPolygon(mDrawPolygonCheckBox.isSelected());
        taskPath.setDrawPath(mDrawPathCheckBox.isSelected());
        taskPath.setWidth(mWidthSpinner.getValue());
        taskPath.setPathColor(FxHelper.colorToHexRGB(mTrackColorPicker.getValue()));
        taskPath.setPathGapColor(FxHelper.colorToHexRGB(mTrackGapColorPicker.getValue()));

        SplitBy splitBy = null;
        var toggle = mToggleGroup.getSelectedToggle();

        if (toggle == mSplitByHourRadioButton) {
            splitBy = SplitBy.HOUR;
        } else if (toggle == mSplitByDayRadioButton) {
            splitBy = SplitBy.DAY;
        } else if (toggle == mSplitByWeekRadioButton) {
            splitBy = SplitBy.WEEK;
        } else if (toggle == mSplitByMonthRadioButton) {
            splitBy = SplitBy.MONTH;
        } else if (toggle == mSplitByYearRadioButton) {
            splitBy = SplitBy.YEAR;
        } else if (toggle == mSplitByNoneRadioButton) {
            splitBy = SplitBy.NONE;
        }

        taskPath.setSplitBy(splitBy);
    }

    private void createUI() {
        var contentPane = new VBox(FxHelper.getUIScaled(4));
        var gp = new GridPane(FxHelper.getUIScaled(8), 0);
        var subSectionInsets = FxHelper.getUIScaledInsets(0, 0, 0, 16);
        FxHelper.setPadding(subSectionInsets, gp);

        setContent(contentPane);

        mWidthSpinner.setEditable(true);
        FxHelper.autoCommitSpinners(mWidthSpinner);

        mSplitByHourRadioButton.setToggleGroup(mToggleGroup);
        mSplitByDayRadioButton.setToggleGroup(mToggleGroup);
        mSplitByWeekRadioButton.setToggleGroup(mToggleGroup);
        mSplitByMonthRadioButton.setToggleGroup(mToggleGroup);
        mSplitByYearRadioButton.setToggleGroup(mToggleGroup);
        mSplitByNoneRadioButton.setToggleGroup(mToggleGroup);

        var widthLabel = new Label(Dict.Geometry.WIDTH.toString());
        var splitByLabel = new Label(Dict.SPLIT_BY.toString());
        var trackColorLabel = new Label(mBundle.getString("PathTab.pathColor"));
        var trackGapColorLabel = new Label(mBundle.getString("PathTab.pathGapColor"));

        gp.addColumn(0,
                widthLabel,
                mWidthSpinner,
                splitByLabel,
                mSplitByHourRadioButton,
                mSplitByDayRadioButton,
                mSplitByWeekRadioButton,
                mSplitByMonthRadioButton,
                mSplitByYearRadioButton,
                mSplitByNoneRadioButton
        );

        gp.addColumn(1,
                trackColorLabel,
                mTrackColorPicker
        );

        gp.addColumn(2,
                trackGapColorLabel,
                mTrackGapColorPicker
        );

        gp.disableProperty().bind(mDrawPathCheckBox.selectedProperty().not());
        mTrackGapColorPicker.disableProperty().bind(mSplitByNoneRadioButton.selectedProperty().or(mDrawPathCheckBox.selectedProperty().not()));

        contentPane.getChildren().addAll(
                mDrawPathCheckBox,
                gp,
                mDrawPolygonCheckBox
        );

        addTopPadding(
                splitByLabel,
                mSplitByHourRadioButton,
                mSplitByDayRadioButton,
                mSplitByWeekRadioButton,
                mSplitByMonthRadioButton,
                mSplitByYearRadioButton,
                mSplitByNoneRadioButton,
                mDrawPolygonCheckBox
        );
    }
}
