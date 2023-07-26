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
package se.trixon.mapollage.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import se.trixon.mapollage.profile.Profile;

/**
 *
 * @author Patrik Karlström
 */
public class ProfileIndicator extends VBox {

    private final Label mDescLabel = new Label();
    private final Label mNameLabel = new Label();

    public ProfileIndicator() {
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(0, 0, 0, 16));
        getChildren().addAll(mNameLabel, mDescLabel);
        String fontFamily = Font.getDefault().getFamily();
        double fontSize = Font.getDefault().getSize();

        mNameLabel.setFont(Font.font(fontFamily, FontWeight.BOLD, fontSize * 1.7));
        mDescLabel.setFont(Font.font(fontFamily, FontWeight.NORMAL, fontSize * 1.2));
    }

    public void setProfile(Profile profile) {
        mNameLabel.setText(profile.getName());
        mDescLabel.setText(profile.getDescriptionString());
    }
}
