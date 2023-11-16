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
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToggleGroup;
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
    private final Spinner<Double> mWidthSpinner = new Spinner(1.0, 10.0, 1.0, 0.1);

    public PathTab() {
        setText(Dict.Geometry.PATH.toString());
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
    }

    @Override
    public void save() {
        var taskPath = mTask.getPath();
        taskPath.setDrawPolygon(mDrawPolygonCheckBox.isSelected());
        taskPath.setDrawPath(mDrawPathCheckBox.isSelected());
        taskPath.setWidth(mWidthSpinner.getValue());

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
        var vbox = new VBox();
        var pathBox = new VBox();

        setContent(vbox);
        var widthLabel = new Label(Dict.Geometry.WIDTH.toString());
        var splitByLabel = new Label(Dict.SPLIT_BY.toString());

        mWidthSpinner.setEditable(true);
        FxHelper.autoCommitSpinners(mWidthSpinner);

        mSplitByHourRadioButton.setToggleGroup(mToggleGroup);
        mSplitByDayRadioButton.setToggleGroup(mToggleGroup);
        mSplitByWeekRadioButton.setToggleGroup(mToggleGroup);
        mSplitByMonthRadioButton.setToggleGroup(mToggleGroup);
        mSplitByYearRadioButton.setToggleGroup(mToggleGroup);
        mSplitByNoneRadioButton.setToggleGroup(mToggleGroup);

        pathBox.getChildren().addAll(
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
        pathBox.disableProperty().bind(mDrawPathCheckBox.selectedProperty().not());

        vbox.getChildren().addAll(
                mDrawPolygonCheckBox,
                mDrawPathCheckBox,
                pathBox
        );

        addTopPadding(
                mDrawPathCheckBox,
                widthLabel,
                splitByLabel,
                mSplitByHourRadioButton,
                mSplitByDayRadioButton,
                mSplitByWeekRadioButton,
                mSplitByMonthRadioButton,
                mSplitByYearRadioButton,
                mSplitByNoneRadioButton
        );
    }
}
