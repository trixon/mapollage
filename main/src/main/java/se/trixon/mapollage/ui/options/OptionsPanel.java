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
package se.trixon.mapollage.ui.options;

import java.util.ResourceBundle;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.LocaleComboBox;
import se.trixon.mapollage.Options;

/**
 *
 * @author Patrik Karlström
 */
public class OptionsPanel extends GridPane {

    private final ResourceBundle mBundle = NbBundle.getBundle(OptionsPanel.class);
    private final LocaleComboBox mLocaleComboBox = new LocaleComboBox();
    private final Options mOptions = Options.getInstance();
    private final Spinner<Integer> mThumbnailBorderSizeSpinner = new Spinner(0, 20, 2, 1);
    private final Spinner<Integer> mThumbnailSizeSpinner = new Spinner(100, 1200, 250, 10);

    public OptionsPanel() {
        createUI();
    }

    private void createUI() {
        setHgap(32);
        setVgap(2);
        //setGridLinesVisible(true);
        FxHelper.autoSizeColumn(this, 2);

        var calendarLanguageLabel = new Label(Dict.CALENDAR_LANGUAGE.toString());
        var placemarkLabel = new Label(Dict.PLACEMARK.toString());
        var thumbnailLabel = new Label(Dict.THUMBNAIL.toString());
        var borderSizeLabel = new Label(mBundle.getString("OptionsPanel.borderSizeLabel"));

        var fontFamily = Font.getDefault().getFamily();
        var fontSize = FxHelper.getUIScaled(Font.getDefault().getSize());
        var font = Font.font(fontFamily, FontPosture.ITALIC, fontSize * 1.3);

        placemarkLabel.setFont(font);

        int row = 0;
        add(calendarLanguageLabel, 0, row++, REMAINING, 1);
        add(mLocaleComboBox, 0, row++, REMAINING, 1);
        add(placemarkLabel, 0, row++, REMAINING, 1);
        add(thumbnailLabel, 0, row, 1, 1);
        add(borderSizeLabel, 1, row++, 1, 1);
        add(mThumbnailSizeSpinner, 0, row, 1, 1);
        add(mThumbnailBorderSizeSpinner, 1, row++, 1, 1);

        FxHelper.setPadding(new Insets(8, 0, 0, 0),
                placemarkLabel
        );

        for (var columnConstraint : getColumnConstraints()) {
            columnConstraint.setFillWidth(true);
            columnConstraint.setHgrow(Priority.ALWAYS);
        }

        FxHelper.setEditable(true,
                mThumbnailSizeSpinner,
                mThumbnailBorderSizeSpinner
        );

        FxHelper.autoCommitSpinners(
                mThumbnailBorderSizeSpinner,
                mThumbnailSizeSpinner
        );

        mLocaleComboBox.setMaxWidth(Double.MAX_VALUE);
        mThumbnailSizeSpinner.setMaxWidth(Double.MAX_VALUE);
        mThumbnailBorderSizeSpinner.setMaxWidth(Double.MAX_VALUE);

        mLocaleComboBox.valueProperty().bindBidirectional(mOptions.localeProperty());
        mThumbnailBorderSizeSpinner.valueFactoryProperty().getValue().valueProperty().bindBidirectional(mOptions.thumbnailBorderSizeProperty());
        mThumbnailSizeSpinner.valueFactoryProperty().getValue().valueProperty().bindBidirectional(mOptions.thumbnailSizeProperty());
    }

}
