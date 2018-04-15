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
package se.trixon.mapollage.ui.config;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import org.controlsfx.glyphfont.FontAwesome;
import se.trixon.almond.util.Dict;
import se.trixon.mapollage.profile.Profile;
import se.trixon.mapollage.profile.ProfilePath;
import se.trixon.mapollage.profile.ProfilePath.SplitBy;

/**
 *
 * @author Patrik Karlström
 */
public class PathTab extends BaseTab {

    private final CheckBox mDrawPathCheckBox = new CheckBox(mBundle.getString("ModulePathPanel.drawPathCheckBox.text"));
    private final CheckBox mDrawPolygonCheckBox = new CheckBox(mBundle.getString("ModulePathPanel.drawPolygonCheckBox.text"));
    private ProfilePath mPath;
    private RadioButton mSplitByDayRadioButton = new RadioButton(Dict.Time.DAY.toString());
    private RadioButton mSplitByHourRadioButton = new RadioButton(Dict.Time.HOUR.toString());
    private RadioButton mSplitByMonthRadioButton = new RadioButton(Dict.Time.MONTH.toString());
    private RadioButton mSplitByNoneRadioButton = new RadioButton(Dict.DO_NOT_SPLIT.toString());
    private RadioButton mSplitByWeekRadioButton = new RadioButton(Dict.Time.WEEK.toString());
    private RadioButton mSplitByYearRadioButton = new RadioButton(Dict.Time.YEAR.toString());
    private ToggleGroup mToggleGroup = new ToggleGroup();
    private Spinner<Double> mWidthSpinner = new Spinner(1.0, 10.0, 1.0, 0.1);

    public PathTab(Profile profile) {
        setText(Dict.PATH_GFX.toString());
        setGraphic(FontAwesome.Glyph.CODE_FORK.getChar());
        mProfile = profile;
        mPath = mProfile.getPath();
        createUI();
        load();
    }

    @Override
    public boolean hasValidSettings() {
        return true;
    }

    @Override
    public void save() {
        mPath.setDrawPolygon(mDrawPolygonCheckBox.isSelected());
        mPath.setDrawPath(mDrawPathCheckBox.isSelected());
        mPath.setWidth(mWidthSpinner.getValue());

        SplitBy splitBy = null;
        Toggle t = mToggleGroup.getSelectedToggle();

        if (t == mSplitByHourRadioButton) {
            splitBy = SplitBy.HOUR;
        } else if (t == mSplitByDayRadioButton) {
            splitBy = SplitBy.DAY;
        } else if (t == mSplitByWeekRadioButton) {
            splitBy = SplitBy.WEEK;
        } else if (t == mSplitByMonthRadioButton) {
            splitBy = SplitBy.MONTH;
        } else if (t == mSplitByYearRadioButton) {
            splitBy = SplitBy.YEAR;
        } else if (t == mSplitByNoneRadioButton) {
            splitBy = SplitBy.NONE;
        }

        mPath.setSplitBy(splitBy);
    }

    private void createUI() {
        VBox vbox = new VBox();
        VBox pathBox = new VBox();

        setContent(vbox);
        Label widthLabel = new Label(Dict.WIDTH.toString());
        Label splitByLabel = new Label(Dict.SPLIT_BY.toString());

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
                mDrawPolygonCheckBox,
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

    private void load() {
        mDrawPolygonCheckBox.setSelected(mPath.isDrawPolygon());
        mDrawPathCheckBox.setSelected(mPath.isDrawPath());
        mWidthSpinner.getValueFactory().setValue(mPath.getWidth());

        RadioButton splitByRadioButton;

        switch (mPath.getSplitBy()) {
            case HOUR:
                splitByRadioButton = mSplitByHourRadioButton;
                break;

            case DAY:
                splitByRadioButton = mSplitByDayRadioButton;
                break;

            case WEEK:
                splitByRadioButton = mSplitByWeekRadioButton;
                break;

            case MONTH:
                splitByRadioButton = mSplitByMonthRadioButton;
                break;

            case YEAR:
                splitByRadioButton = mSplitByYearRadioButton;
                break;

            case NONE:
                splitByRadioButton = mSplitByNoneRadioButton;
                break;

            default:
                throw new AssertionError();
        }

        splitByRadioButton.setSelected(true);
    }
}
