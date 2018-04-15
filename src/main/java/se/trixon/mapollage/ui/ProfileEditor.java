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

import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import se.trixon.mapollage.profile.Profile;
import se.trixon.mapollage.ui.config.BaseTab;
import se.trixon.mapollage.ui.config.FoldersTab;
import se.trixon.mapollage.ui.config.InformationTab;
import se.trixon.mapollage.ui.config.PathTab;
import se.trixon.mapollage.ui.config.PhotoTab;
import se.trixon.mapollage.ui.config.PlacemarkTab;
import se.trixon.mapollage.ui.config.SourceTab;

/**
 *
 * @author Patrik Karlström
 */
public class ProfileEditor extends TabPane {

    private FoldersTab mFoldersTab;
    private InformationTab mInformationTab;
    private Button mOkButton;
    private PathTab mPathTab;
    private PhotoTab mPhotoTab;
    private PlacemarkTab mPlacemarkTab;

    private Profile mProfile;
    private SourceTab mSourceTab;

    public ProfileEditor() {
        init();
    }

    public ProfileEditor(Tab... tabs) {
        super(tabs);
        init();
    }

    public ProfileEditor(Profile profile) {
        mProfile = profile;
        init();
    }

    public void save() {

    }

    private void init() {
        final double TAB_SIZE = BaseTab.ICON_SIZE * 1.5;
        setTabMaxHeight(TAB_SIZE);
        setTabMinHeight(TAB_SIZE);
        setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

        mSourceTab = new SourceTab(mProfile);
        mFoldersTab = new FoldersTab(mProfile);
        mPathTab = new PathTab(mProfile);
        mPlacemarkTab = new PlacemarkTab(mProfile);
        mPhotoTab = new PhotoTab(mProfile);
        mInformationTab = new InformationTab(mProfile);

        getTabs().add(mSourceTab);
        getTabs().add(mFoldersTab);
        getTabs().add(mPathTab);
        getTabs().add(mPlacemarkTab);
        getTabs().add(mPhotoTab);
        getTabs().add(mInformationTab);
    }

    void setOkButton(Button button) {
        mOkButton = button;
    }
}
