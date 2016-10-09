/*
 * Copyright 2016 Patrik Karlsson.
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
package se.trixon.photokml;

/**
 *
 * @author Patrik Karlsson
 */
public class DescriptionManager {

    private DescriptionManager() {
    }

    public static DescriptionManager getInstance() {
        return Holder.INSTANCE;
    }

    public String getSegmentString(DescriptionSegment segment) {
        switch (segment) {
            case ALTITUDE:
                return String.format("%s", DescriptionSegment.ALTITUDE.toString());

            case BEARING:
                return String.format("%s", DescriptionSegment.BEARING.toString());

            case COORDINATE:
                return String.format("%s", DescriptionSegment.COORDINATE.toString());

            case DATE:
                return String.format("%s", DescriptionSegment.DATE.toString());

            case FILENAME:
                return String.format("%s", DescriptionSegment.FILENAME.toString());

            case PHOTO:
                return String.format("%s", DescriptionSegment.PHOTO.toString());

            default:
                throw new AssertionError();
        }
    }

    private static class Holder {

        private static final DescriptionManager INSTANCE = new DescriptionManager();
    }

    public static enum DescriptionSegment {
        ALTITUDE, BEARING, COORDINATE, DATE, FILENAME, PHOTO;

        @Override
        public String toString() {
            return String.format("+%s", name().toLowerCase());
        }
    }
}
