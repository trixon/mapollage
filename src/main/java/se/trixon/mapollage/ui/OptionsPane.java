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
package se.trixon.mapollage.ui;

import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.control.LocaleComboBox;
import se.trixon.mapollage.Options;

/**
 *
 * @author Patrik Karlström
 */
public class OptionsPane extends GridPane {

    private LocaleComboBox mLocaleComboBox;

    private final Options mOptions = Options.getInstance();
    private CheckBox mWordWrapCheckBox;

    public OptionsPane() {
        createUI();
        load();
    }

    private void createUI() {
        Label label = new Label(Dict.CALENDAR_LANGUAGE.toString());
        mLocaleComboBox = new LocaleComboBox();
        mWordWrapCheckBox = new CheckBox(Dict.DYNAMIC_WORD_WRAP.toString());
        setGridLinesVisible(true);
        addColumn(0, label, mLocaleComboBox, mWordWrapCheckBox);
        GridPane.setMargin(mWordWrapCheckBox, new Insets(16, 0, 0, 0));
    }

    private void load() {
        mLocaleComboBox.setLocale(mOptions.getLocale());
        mWordWrapCheckBox.setSelected(mOptions.isWordWrap());
    }

    void save() {
        mOptions.setLocale(mLocaleComboBox.getLocale());
        mOptions.setWordWrap(mWordWrapCheckBox.isSelected());
    }

}
