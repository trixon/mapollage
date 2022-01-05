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
package se.trixon.mapollage;

import java.io.File;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.mapollage.profile.Profile;

/**
 *
 * @author Patrik Karlström
 */
public class RunManager {

    private final ObjectProperty<File> mDestinationProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<Profile> mProfileProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<RunState> mRunStateProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<RunStatus> mRunStatusProperty = new SimpleObjectProperty<>();
    private final BooleanProperty mRunningProperty = new SimpleBooleanProperty(false);

    public static RunManager getInstance() {
        return Holder.INSTANCE;
    }

    private RunManager() {
    }

    public ObjectProperty<File> destinationProperty() {
        return mDestinationProperty;
    }

    public File getDestination() {
        return mDestinationProperty.get();
    }

    public Profile getProfile() {
        return mProfileProperty.get();
    }

    public RunState getRunState() {
        return mRunStateProperty.get();
    }

    public RunStatus getRunStatus() {
        return mRunStatusProperty.get();
    }

    public boolean isRunning() {
        return mRunningProperty.get();
    }

    public void openDestination() {
        SystemHelper.desktopOpen(getDestination());
    }

    public ObjectProperty<Profile> profileProperty() {
        return mProfileProperty;
    }

    public ObjectProperty<RunState> runStateProperty() {
        return mRunStateProperty;
    }

    public ObjectProperty<RunStatus> runStatusProperty() {
        return mRunStatusProperty;
    }

    public BooleanProperty runningProperty() {
        return mRunningProperty;
    }

    public void setDestination(File destination) {
        mDestinationProperty.set(destination);
    }

    public void setProfile(Profile profile) {
        mProfileProperty.set(profile);
    }

    public void setRunState(RunState runState) {
        FxHelper.runLater(() -> {
            mRunStateProperty.set(runState);
            mRunningProperty.set(runState == RunState.CANCELABLE);
        });
    }

    public void setRunStatus(RunStatus runStatus) {
        mRunStatusProperty.set(runStatus);
    }

    private static class Holder {

        private static final RunManager INSTANCE = new RunManager();
    }
}
