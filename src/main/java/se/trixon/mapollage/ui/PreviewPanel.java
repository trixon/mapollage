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

import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.geometry.Insets;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.filebydate.Profile;

/**
 *
 * @author Patrik Karlström
 */
public class PreviewPanel extends BorderPane {

    private Text mBasedOn = new Text();
    private final ResourceBundle mBundle = SystemHelper.getBundle(PreviewPanel.class, "Bundle");
    private Text mCase = new Text();
    private Text mDest = new Text("\n");
    private Text mFilesFrom = new Text(mBundle.getString("files_from"));
    private Text mOperation = new Text();
    private Text mOptions = new Text();
    private Text mSource = new Text("\n");
    private Text mTo = new Text(String.format(" %s\n", Dict.TO.toString().toLowerCase(Locale.getDefault())));

    public PreviewPanel() {
        mOperation.setFill(Color.RED);
        mSource.setFill(Color.RED);
        mDest.setFill(Color.RED);

        TextFlow textFlow = new TextFlow(
                mOperation,
                mFilesFrom,
                mSource,
                mTo,
                mDest,
                mOptions,
                mBasedOn,
                mCase
        );

        final int fontSize = 18;
        textFlow.getChildren().stream().filter((node) -> (node instanceof Text)).forEachOrdered((node) -> {
            ((Text) node).setFont(Font.font(fontSize));
        });
        textFlow.setVisible(false);

        Font defaultFont = Font.getDefault();
        mOperation.setFont(Font.font(defaultFont.getName(), FontWeight.EXTRA_BOLD, fontSize));

        setCenter(textFlow);
        setMargin(textFlow, new Insets(16));
        setVisible(false);
    }

    public void load(Profile p) {
        setVisible(true);
        getCenter().setVisible(true);

        mOperation.setText(p.getCommand().toString());
        mSource.setText(String.format("%s%s%s",
                p.getSourceDirAsString(),
                File.separator,
                p.getFilePattern())
        );
        mDest.setText(String.format("%s%s%s\n",
                p.getDestDirAsString(),
                File.separator,
                p.getDatePattern())
        );
        mBasedOn.setText(String.format("%s = '%s'\n",
                Dict.DATE_SOURCE.toString(),
                p.getDateSource().toString()
        ));

        StringBuilder sb = new StringBuilder();
        sb.append(getBallotBox(p.isFollowLinks())).append(Dict.FOLLOW_LINKS.toString()).append(", ");
        sb.append(getBallotBox(p.isRecursive())).append(Dict.RECURSIVE.toString()).append(", ");
        sb.append(getBallotBox(p.isReplaceExisting())).append(Dict.REPLACE.toString()).append(". ");
        mOptions.setText(sb.toString());

        String caseText = String.format("%s %s, %s %s",
                Dict.BASENAME.toString(),
                p.getCaseBase(),
                Dict.EXTENSION.toString(),
                p.getCaseExt()
        );

        mCase.setText(caseText);
    }

    private String getBallotBox(boolean value) {
        return value ? "☑ " : "☐ ";
    }
}
