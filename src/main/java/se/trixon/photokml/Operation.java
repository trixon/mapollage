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

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDescriptor;
import com.drew.metadata.exif.GpsDirectory;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import se.trixon.util.BundleHelper;
import se.trixon.util.GraphicsHelper;
import se.trixon.util.Scaler;
import se.trixon.util.dictionary.Dict;

/**
 *
 * @author Patrik Karlsson
 */
class Operation {

    private final ResourceBundle mBundle;
//    private final File mDestinationFile;
    private final List<Exception> mExceptions = new ArrayList<>();
    private final List<File> mFiles = new ArrayList<>();
    private boolean mInterrupted = false;
    private final OperationListener mListener;
    private final OptionsHolder mOptionsHolder;
    private final ArrayList<File> mFilesWithErrors = new ArrayList<>();
    private final Map<String, Folder> mFolders = new HashMap<>();

    private Kml mKml = new Kml();
    private int mNumOfExif;
    private int mNumOfFiles;
    private int mNumOfGps;
    private Folder mRootFolder;
    private final long mStartTime = System.currentTimeMillis();
    private File mDestinationFile;
    private final DateFormat mDateFormatDate = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);

    public Operation(OperationListener operationListener, OptionsHolder optionsHolder) {
        mListener = operationListener;
        mOptionsHolder = optionsHolder;
        mBundle = BundleHelper.getBundle(Operation.class, "Bundle");
    }

    public void start() {
        mListener.onOperationStarted();
        long startTime = System.currentTimeMillis();
        String status;
        mDestinationFile = mOptionsHolder.getDestFile();
        mRootFolder = mKml.createAndSetFolder().withName(mOptionsHolder.getRootName());
        mRootFolder.setDescription(mOptionsHolder.getRootDesc());

        mInterrupted = !generateFileList();

        if (!mInterrupted && !mFiles.isEmpty()) {
            mListener.onOperationProcessingStarted();
            status = String.format("\n%s\n", Dict.PROCESSING.toString());
            mListener.onOperationLog(status);

            for (File sourceFile : mFiles) {
                try {
                    process(sourceFile);
                } catch (ImageProcessingException | IOException ex) {
                    mFilesWithErrors.add(sourceFile);
                }

                if (Thread.interrupted()) {
                    mInterrupted = true;
                    break;
                }
            }
        }

        if (mInterrupted) {
            status = Dict.TASK_ABORTED.getString();
            mListener.onOperationLog("\n" + status);
        } else {
            for (Exception exception : mExceptions) {
                mListener.onOperationLog(String.format("#%s", exception.getLocalizedMessage()));
            }

            if (!mFilesWithErrors.isEmpty()) {
                mListener.onOperationLog("\n" + mBundle.getString("ignoredFiles") + "\n");

                for (File fileWithError : mFilesWithErrors) {
                    logSuccess(false, fileWithError);
                }
            }

            if (!mFiles.isEmpty()) {
                saveToFile();
            }
        }
//            if (mFiles.isEmpty()) {
//                mListener.onOperationFailed();
//            }
    }

    private String getDescPhoto(File sourceFile, ExifSubIFDDirectory exifDirectory) {

        Dimension originalDimension = null;

        try {
            originalDimension = GraphicsHelper.getImgageDimension(sourceFile);
        } catch (IOException ex) {
            System.err.println(ex.getLocalizedMessage());
        }

        if (originalDimension == null) {
            originalDimension = new Dimension(200, 200);
        }

        Scaler scaler = new Scaler(new Dimension(originalDimension));

        if (mOptionsHolder.getMaxWidth() != null) {
            scaler.setWidth(mOptionsHolder.getMaxWidth());
        }

        if (mOptionsHolder.getMaxHeight() != null) {
            scaler.setHeight(mOptionsHolder.getMaxHeight());
        }

        Dimension newDimension = scaler.getDimension();
        String imageTagFormat = "<p><img src='%s' width='%d' height='%d'></p>";
        String imageTag = String.format(imageTagFormat, getImagePath(sourceFile), newDimension.width, newDimension.height);

        return imageTag;
    }

    private boolean generateFileList() {
        mListener.onOperationLog(String.format("* %s", Dict.GENERATING_FILELIST.getString()));
        boolean invalidPath = false;

        if (mFiles != null) {
            for (File file : mFiles) {
                String path = file.getAbsolutePath();

                if (path.contains(SystemUtils.PATH_SEPARATOR)) {
                    invalidPath = true;
                    String message = String.format(Dict.INVALID_PATH.getString(), file.getAbsolutePath());
                    mListener.onOperationLog(message);
                }
            }

            if (invalidPath) {
                String message = String.format(Dict.ERROR_PATH_SEPARATOR.getString(), SystemUtils.PATH_SEPARATOR);
                System.err.println(Dict.IO_ERROR_TITLE.getString() + ": " + message);
            }
        }

        File file = mOptionsHolder.getSourceDir();
        if (file.isDirectory()) {
            FileVisitor fileVisitor = new FileVisitor(mOptionsHolder.getPathMatcher(), mFiles);
            try {
                EnumSet<FileVisitOption> fileVisitOptions;
                if (mOptionsHolder.isFollowLinks()) {
                    fileVisitOptions = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
                } else {
                    fileVisitOptions = EnumSet.noneOf(FileVisitOption.class);
                }

                if (mOptionsHolder.isRecursive()) {
                    Files.walkFileTree(file.toPath(), fileVisitOptions, Integer.MAX_VALUE, fileVisitor);
                } else {
                    Files.walkFileTree(file.toPath(), fileVisitOptions, 1, fileVisitor);
                }

                if (fileVisitor.isInterrupted()) {
                    return false;
                }
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
            }
        } else if (file.isFile() && mOptionsHolder.getPathMatcher().matches(file.toPath().getFileName())) {
            mFiles.add(file);
        }

        if (mFiles.isEmpty()) {
            mListener.onOperationLog("* " + Dict.FILELIST_EMPTY.getString());
            mListener.onOperationFailed(Dict.FILELIST_EMPTY.getString());
        } else {
            Collections.sort(mFiles);
        }

        return true;
    }

    private Folder getFolder(File file, Date date) {
        String key;
        Folder folder;

        if (mOptionsHolder.isFolderByDir()) {
            key = file.getParentFile().getName();
            folder = getFolder(key);
        } else if (mOptionsHolder.isFolderByDate()) {
            key = mOptionsHolder.getFolderDateFormat().format(date);
            folder = getFolder(key);
        } else {
            folder = mRootFolder;
        }

        return folder;
    }

    private Folder getFolder(String key) {
        Folder folder = mFolders.get(key);

        if (folder == null) {
            folder = new Folder().withName(key);
            mFolders.put(key, folder);
        }

        return folder;
    }

    private Date getImageDate(File file, ExifSubIFDDirectory exifDirectory) {
        Date date;

        if (exifDirectory.containsTag(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL)) {
            date = exifDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
        } else {
            long millis = 0;
            try {
                BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                millis = attr.creationTime().toMillis();
            } catch (IOException ex) {
                millis = file.lastModified();
            } finally {
                date = new Date(millis);
            }
        }

        return date;
    }

    private String getImagePath(File file) {
        String imageSrc;

        if (mOptionsHolder.getAbsolutePath() != null) {
            imageSrc = mOptionsHolder.getAbsolutePath() + file.getName();
        } else {
            Path relativePath = mDestinationFile.toPath().relativize(file.toPath());

//            try {
//                relativeSourcePath = URLEncoder.encode(relativePath.toString(), "UTF-8");
//            } catch (UnsupportedEncodingException ex) {
//                Exceptions.printStackTrace(ex);
//            }
            imageSrc = StringUtils.replace(relativePath.toString(), "..", ".", 1);

            if (SystemUtils.IS_OS_WINDOWS) {
                imageSrc = imageSrc.replace("\\", "/");
            }
        }

        if (mOptionsHolder.isLowerCaseExt()) {
            String ext = FilenameUtils.getExtension(imageSrc);
            if (!StringUtils.isBlank(ext) && !StringUtils.isAllLowerCase(ext)) {
                String noExt = FilenameUtils.removeExtension(imageSrc);
                imageSrc = String.format("%s.%s", noExt, ext.toLowerCase());
            }
        }

        return imageSrc;
    }

    private void logSuccess(boolean b, File sourceFile) {
        String prefix = b ? "+" : "-";
        mListener.onOperationLog(String.format(" %s %s", prefix, sourceFile.getAbsolutePath()));
    }

    private void process(File sourceFile) throws ImageProcessingException, IOException {
        Metadata metadata = ImageMetadataReader.readMetadata(sourceFile);

        ExifSubIFDDirectory exifDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
//            ExifSubIFDDescriptor exifSubIFDDescriptor = new ExifSubIFDDescriptor(exifDirectory);

        GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
        GpsDescriptor gpsDescriptor = new GpsDescriptor(gpsDirectory);

        if (exifDirectory != null) {
            mNumOfExif++;
            if (gpsDirectory != null || mOptionsHolder.getLat() != null) {
                mNumOfGps++;
            }
        } else {
            throw new ImageProcessingException(String.format(mBundle.getString("exifError"), sourceFile.getAbsolutePath()));
        }

        Date exifDate = getImageDate(sourceFile, exifDirectory);
        String name;

        if (mOptionsHolder.isPlacemarkByFilename()) {
            name = FilenameUtils.getBaseName(sourceFile.getAbsolutePath());
        } else if (mOptionsHolder.isPlacemarkByDate()) {
            try {
                name = mOptionsHolder.getPlacemarkDateFormat().format(exifDate);
            } catch (IllegalArgumentException ex) {
                name = "invalid exif date";
            } catch (NullPointerException ex) {
                name = "invalid exif date";
                System.err.println(" ! Invalid date in " + sourceFile.getAbsolutePath());
            }

        } else {
            name = "";
        }

        String desc = mOptionsHolder.getPlacemarkDesc();
        desc = StringUtils.replace(desc, Desc.PHOTO, getDescPhoto(sourceFile, exifDirectory));
        desc = StringUtils.replace(desc, Desc.FILENAME, sourceFile.getName());
        desc = StringUtils.replace(desc, Desc.DATE, mDateFormatDate.format(exifDate));

//        StringBuilder descriptionBuilder = new StringBuilder(mOptionsHolder.getPlacemarkDesc());
////            descriptionBuilder.append("<![CDATA[");
//        if (mOptions.isDescriptionPhoto()) {
//            addPhoto(descriptionBuilder, sourceFile, exifDirectory);
//        }
//
//        if (mOptions.isDescriptionFilename()) {
//            descriptionBuilder.append(sourceFile.getName()).append("<br />");
//        }
//
//        if (mOptions.isDescriptionDate()) {
//            descriptionBuilder.append(Toolbox.getDefaultDateFormat().format(exifDate)).append("<br />");
//        }
        GeoLocation geoLocation = new GeoLocation(mOptionsHolder.getLat(), mOptionsHolder.getLon());
        if (gpsDirectory != null) {
            geoLocation = gpsDirectory.getGeoLocation();
            if (geoLocation == null) {
                throw new ImageProcessingException(sourceFile.getAbsolutePath());
            }

            if (geoLocation.getLatitude() == 0 && geoLocation.getLongitude() == 0) {
                geoLocation = new GeoLocation(mOptionsHolder.getLat(), mOptionsHolder.getLon());
                gpsDirectory = null;
            }

            if (exifDate != null) {
                desc = StringUtils.replace(desc, Desc.ALTITUDE, gpsDescriptor.getGpsAltitudeDescription());
                desc = StringUtils.replace(desc, Desc.COORDINATE, gpsDescriptor.getDegreesMinutesSecondsDescription());

                String bearing = gpsDescriptor.getGpsDirectionDescription(GpsDirectory.TAG_DEST_BEARING);
                if (bearing != null) {
                    desc = StringUtils.replace(desc, Desc.BEARING, gpsDescriptor.getGpsAltitudeDescription());
                }

//                if (mOptions.isDescriptionCoordinate()) {
//                    descriptionBuilder.append(gpsDescriptor.getDegreesMinutesSecondsDescription()).append("<br />");
//                }
//
//                if (mOptions.isDescriptionAltitude()) {
//                    descriptionBuilder.append(gpsDescriptor.getGpsAltitudeDescription()).append("<br />");
//                }
//
//                if (mOptions.isDescriptionBearing()) {
//                    String bearing = gpsDescriptor.getGpsDirectionDescription(GpsDirectory.TAG_DEST_BEARING);
//                    if (bearing != null) {
//                        descriptionBuilder.append(bearing).append("<br />");
//                    }
//                }
            }
        }

//            descriptionBuilder.append("]]>");
        boolean shouldAppendToKml = gpsDirectory != null || mOptionsHolder.hasCoordinate();

        if (shouldAppendToKml) {
            double format = 1000000;
            int latInt = (int) (geoLocation.getLatitude() * format);
            int lonInt = (int) (geoLocation.getLongitude() * format);

            getFolder(sourceFile, exifDate).createAndAddPlacemark()
                    .withName(name)
                    .withOpen(Boolean.TRUE)
                    .withDescription(desc)
                    .createAndSetPoint()
                    .addToCoordinates(lonInt / format, latInt / format);
            logSuccess(shouldAppendToKml, sourceFile);
        } else {
            mFilesWithErrors.add(sourceFile);
        }
    }

    private void saveToFile() {
        List keys = new ArrayList(mFolders.keySet());
        Collections.sort(keys);

        keys.stream().forEach((key) -> {
            mRootFolder.getFeature().add(mFolders.get((String) key));
        });

        mListener.onOperationLog("\n" + String.format(Dict.SAVING.getString(), mDestinationFile.getAbsolutePath()));

        try {
            mKml.marshal(mDestinationFile);
            mKml = null;

            long millis = System.currentTimeMillis() - mStartTime;
            long min = TimeUnit.MILLISECONDS.toMinutes(millis);
            long sec = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis));

            String status = String.format(mBundle.getString("operationSummary"), mNumOfGps, min, Dict.TIME_MIN.getString(), sec, Dict.TIME_SEC.getString());

            if (mOptionsHolder.getAbsolutePath() == null) {
                status = status + "\n\n" + mBundle.getString("operationNote");
            }

            mListener.onOperationLog("\n" + status);
            mListener.onOperationFinished(status);
        } catch (FileNotFoundException ex) {
            mListener.onOperationLog(ex.getLocalizedMessage());
        }
    }

}
