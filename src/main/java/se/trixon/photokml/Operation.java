/*
 * Copyright 2017 Patrik Karlsson.
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
import java.nio.file.PathMatcher;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TimeZone;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import se.trixon.almond.util.BundleHelper;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.GraphicsHelper;
import se.trixon.almond.util.Scaler;
import se.trixon.photokml.profile.Profile;
import se.trixon.photokml.profile.ProfileDescription;
import se.trixon.photokml.profile.ProfileDescription.DescriptionSegment;
import se.trixon.photokml.profile.ProfileFolder;
import se.trixon.photokml.profile.ProfilePhoto;
import se.trixon.photokml.profile.ProfilePlacemark;
import se.trixon.photokml.profile.ProfileSource;

/**
 *
 * @author Patrik Karlsson
 */
public class Operation implements Runnable {

    private final ResourceBundle mBundle;
    private final DateFormat mDateFormatDate = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
    private final File mDestinationFile;
    private final List<File> mFiles = new ArrayList<>();
    private final Map<String, Folder> mFolders = new HashMap<>();
    private boolean mInterrupted = false;
    private final Kml mKml = new Kml();
    private final OperationListener mListener;
    private int mNumOfExif;
    private int mNumOfGps;
    private int mNumOfInvalidFormat;
    private int mNumOfPlacemarks;
    private final Profile mProfile;
    private final ProfileDescription mProfileDescription;
    private final ProfileFolder mProfileFolder;
    private final ProfilePhoto mProfilePhoto;
    private final ProfilePlacemark mProfilePlacemark;
    private final ProfileSource mProfileSource;
    private Folder mRootFolder;
    private long mStartTime;

    public Operation(OperationListener operationListener, Profile profile) {
        mListener = operationListener;
        mProfile = profile;
        mProfileSource = mProfile.getSource();
        mProfileFolder = mProfile.getFolder();
        mProfilePlacemark = mProfile.getPlacemark();
        mProfileDescription = mProfile.getDescription();
        mProfilePhoto = mProfile.getPhoto();
        mDestinationFile = mProfile.getDestinationFile();

        mBundle = BundleHelper.getBundle(Operation.class, "Bundle");
    }

    @Override
    public void run() {
        mStartTime = System.currentTimeMillis();
        mListener.onOperationStarted();
        String status;
        mRootFolder = mKml.createAndSetFolder().withName(mProfileFolder.getRootName());
        mRootFolder.setDescription(mProfileFolder.getRootDescription());

        mListener.onOperationProcessingStarted();
        mInterrupted = !generateFileList();

        if (!mInterrupted && !mFiles.isEmpty()) {
            for (File file : mFiles) {
                try {
                    addFileToKml(file);
                } catch (ImageProcessingException | IOException ex) {
                    if (ex.getMessage().equalsIgnoreCase("File format is not supported")) {
                        mNumOfInvalidFormat++;
                    }
                    mListener.onOperationError(ex.getMessage() + "<<<");
                }

                if (Thread.interrupted()) {
                    mInterrupted = true;
                    break;
                }
            }
        }

        if (mInterrupted) {
            status = Dict.TASK_ABORTED.toString();
            mListener.onOperationLog("\n" + status);
            mListener.onOperationInterrupted();
        } else if (!mFiles.isEmpty()) {
            saveToFile();
        }
    }

    private void addFileToKml(File file) throws ImageProcessingException, IOException {
        mListener.onOperationLog(file.getAbsolutePath());

        Metadata metadata = ImageMetadataReader.readMetadata(file);
        ExifSubIFDDirectory exifDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);

        if (exifDirectory != null) {
            mNumOfExif++;
            if (gpsDirectory != null) {
                mNumOfGps++;
            }
        } else {
            throw new ImageProcessingException(String.format(mBundle.getString("exifError"), file.getAbsolutePath()));
        }

        Date exifDate = getImageDate(file, exifDirectory);
        boolean shouldAppendToKml = gpsDirectory != null || mProfilePlacemark.hasCoordinate();

        if (shouldAppendToKml) {
            GeoLocation geoLocation = getPlacemarkGeoLocation(file, gpsDirectory);
            double format = 1000000;
            int latInt = (int) (geoLocation.getLatitude() * format);
            int lonInt = (int) (geoLocation.getLongitude() * format);

            getFolder(file, exifDate).createAndAddPlacemark()
                    .withName(getPlacemarkName(file, exifDate))
                    .withDescription(getPlacemarkDescription(file, gpsDirectory, geoLocation, exifDate))
                    .withOpen(Boolean.TRUE)
                    .createAndSetPoint()
                    .addToCoordinates(lonInt / format, latInt / format);

            mNumOfPlacemarks++;
        } else {
            mListener.onOperationError(Dict.FAILED.toString());
        }
    }

    private boolean generateFileList() {
        mListener.onOperationLog(Dict.GENERATING_FILELIST.toString());
        PathMatcher pathMatcher = mProfileSource.getPathMatcher();

        EnumSet<FileVisitOption> fileVisitOptions;
        if (mProfileSource.isFollowLinks()) {
            fileVisitOptions = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
        } else {
            fileVisitOptions = EnumSet.noneOf(FileVisitOption.class);
        }

        File file = mProfileSource.getDir();
        if (file.isDirectory()) {
            FileVisitor fileVisitor = new FileVisitor(pathMatcher, mFiles);
            try {
                if (mProfileSource.isRecursive()) {
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
        } else if (file.isFile() && pathMatcher.matches(file.toPath().getFileName())) {
            mFiles.add(file);
        }

        if (mFiles.isEmpty()) {
            mListener.onOperationLog(Dict.FILELIST_EMPTY.toString());
        } else {
            Collections.sort(mFiles);
        }

        return true;
    }

    private String getDescPhoto(File sourceFile) {
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

        if (mProfilePhoto.isLimitWidth()) {
            scaler.setWidth(mProfilePhoto.getWidthLimit());
        }

        if (mProfilePhoto.isLimitHeight()) {
            scaler.setHeight(mProfilePhoto.getHeightLimit());
        }

        Dimension newDimension = scaler.getDimension();
        String imageTagFormat = "<p><img src='%s' width='%d' height='%d'></p>";
        String imageTag = String.format(imageTagFormat, getImagePath(sourceFile), newDimension.width, newDimension.height);

        return imageTag;
    }

    private Folder getFolder(File file, Date date) {
        String key;
        Folder folder = null;

        switch (mProfileFolder.getFoldersBy()) {
            case ProfileFolder.FOLDER_BY_DIR:
                key = file.getParentFile().getName();
                folder = getFolder(key);
                break;
            case ProfileFolder.FOLDER_BY_DATE:
                key = mProfileFolder.getFolderDateFormat().format(date);
                folder = getFolder(key);
                break;

//            case ProfileFolder.:
//                folder = mRootFolder;
//                break;
            case ProfileFolder.FOLDER_BY_NONE:
                folder = mRootFolder;
                break;
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
            date = exifDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL, TimeZone.getDefault());
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

        switch (mProfilePhoto.getReference()) {
            case ABSOLUTE:
                imageSrc = String.format("file://%s", file.getAbsolutePath());
                break;

            case ABSOLUTE_PATH:
                imageSrc = String.format("%s%s", mProfilePhoto.getBaseUrlValue(), file.getName());
                break;

            case RELATIVE:
                Path relativePath = mDestinationFile.toPath().relativize(file.toPath());
                imageSrc = StringUtils.replace(relativePath.toString(), "..", ".", 1);
                break;

            default:
                throw new AssertionError();
        }

        if (SystemUtils.IS_OS_WINDOWS) {
            imageSrc = imageSrc.replace("\\", "/");
        }

        if (mProfilePhoto.isForceLowerCaseExtension()) {
            String ext = FilenameUtils.getExtension(imageSrc);
            if (!StringUtils.isBlank(ext) && !StringUtils.isAllLowerCase(ext)) {
                String noExt = FilenameUtils.removeExtension(imageSrc);
                imageSrc = String.format("%s.%s", noExt, ext.toLowerCase());
            }
        }

        return imageSrc;
    }

    private String getPlacemarkDescription(File file, GpsDirectory gpsDirectory, GeoLocation geoLocation, Date exifDate) throws ImageProcessingException {
        GpsDescriptor gpsDescriptor = new GpsDescriptor(gpsDirectory);
        String desc = mProfileDescription.isCustom() ? mProfileDescription.getCustomValue() : getStaticDescription();

        desc = StringUtils.replace(desc, DescriptionSegment.PHOTO.toString(), getDescPhoto(file));
        desc = StringUtils.replace(desc, DescriptionSegment.FILENAME.toString(), file.getName());
        desc = StringUtils.replace(desc, DescriptionSegment.DATE.toString(), mDateFormatDate.format(exifDate));

        if (exifDate != null) {
            desc = StringUtils.replace(desc, DescriptionSegment.ALTITUDE.toString(), gpsDescriptor.getGpsAltitudeDescription());
            desc = StringUtils.replace(desc, DescriptionSegment.COORDINATE.toString(), gpsDescriptor.getDegreesMinutesSecondsDescription());

            String bearing = gpsDescriptor.getGpsDirectionDescription(GpsDirectory.TAG_DEST_BEARING);
            if (bearing != null) {
                desc = StringUtils.replace(desc, DescriptionSegment.BEARING.toString(), gpsDescriptor.getGpsAltitudeDescription());
            } else {
                desc = StringUtils.replace(desc, DescriptionSegment.BEARING.toString(), "");
            }
        }

        return desc;
    }

    private GeoLocation getPlacemarkGeoLocation(File file, GpsDirectory gpsDirectory) throws ImageProcessingException {
        GeoLocation geoLocation = null;

        if (mProfilePlacemark.hasCoordinate()) {
            geoLocation = new GeoLocation(mProfilePlacemark.getLat(), mProfilePlacemark.getLon());
        }

        if (gpsDirectory != null) {
            geoLocation = gpsDirectory.getGeoLocation();
            if (geoLocation == null) {
                throw new ImageProcessingException(file.getAbsolutePath());
            }

            if (geoLocation.isZero()) {
                geoLocation = new GeoLocation(mProfilePlacemark.getLat(), mProfilePlacemark.getLon());
                gpsDirectory = null;
            }
        }

        return geoLocation;
    }

    private String getPlacemarkName(File file, Date exifDate) {
        String name;

        switch (mProfilePlacemark.getNameBy()) {
            case ProfilePlacemark.NAME_BY_DATE:
                try {
                    name = mProfilePlacemark.getDateFormat().format(exifDate);
                } catch (IllegalArgumentException ex) {
                    name = "invalid exif date";
                } catch (NullPointerException ex) {
                    name = "invalid exif date";
                    mListener.onOperationError(" ! Invalid date in " + file.getAbsolutePath());
                }
                break;

            case ProfilePlacemark.NAME_BY_FILE:
                name = FilenameUtils.getBaseName(file.getAbsolutePath());
                break;

            case ProfilePlacemark.NAME_BY_NONE:
                name = "";
                break;

            default:
                throw new AssertionError();
        }

        return name;
    }

    private String getStaticDescription() {
        StringBuilder builder = new StringBuilder();

        if (mProfileDescription.hasPhoto()) {
            builder.append(DescriptionSegment.PHOTO.toHtml());
        }

        if (mProfileDescription.hasFilename()) {
            builder.append(DescriptionSegment.FILENAME.toHtml());
        }

        if (mProfileDescription.hasDate()) {
            builder.append(DescriptionSegment.DATE.toHtml());
        }

        if (mProfileDescription.hasCoordinate()) {
            builder.append(DescriptionSegment.COORDINATE.toHtml());
        }

        if (mProfileDescription.hasAltitude()) {
            builder.append(DescriptionSegment.ALTITUDE.toHtml());
        }

        if (mProfileDescription.hasBearing()) {
            builder.append(DescriptionSegment.BEARING.toHtml());
        }

        return builder.toString();
    }

    private void saveToFile() {
        List keys = new ArrayList(mFolders.keySet());
        Collections.sort(keys);

        keys.stream().forEach((key) -> {
            mRootFolder.getFeature().add(mFolders.get((String) key));
        });

        mListener.onOperationLog("\n" + String.format(Dict.SAVING.toString(), mDestinationFile.getAbsolutePath()));

        try {
            mKml.marshal(mDestinationFile);

            String files = mBundle.getString("status_files");
            String exif = mBundle.getString("status_exif");
            String coordinate = mBundle.getString("status_coordinate");
            String time = mBundle.getString("status_time");
            String invalidFormat = mBundle.getString("status_invalid_format");
            String placemarks = mBundle.getString("status_placemarks");

            int rightPad = files.length();
            rightPad = Math.max(rightPad, exif.length());
            rightPad = Math.max(rightPad, coordinate.length());
            rightPad = Math.max(rightPad, time.length());
            rightPad = Math.max(rightPad, invalidFormat.length());
            rightPad = Math.max(rightPad, placemarks.length());
            rightPad++;

            int leftPad = 8;
            StringBuilder summaryBuilder = new StringBuilder("\n");

            String filesValue = String.valueOf(mFiles.size());
            summaryBuilder.append(StringUtils.rightPad(files, rightPad)).append(":").append(StringUtils.leftPad(filesValue, leftPad)).append("\n");

            String exifValue = String.valueOf(mNumOfExif);
            summaryBuilder.append(StringUtils.rightPad(exif, rightPad)).append(":").append(StringUtils.leftPad(exifValue, leftPad)).append("\n");

            String coordinateValue = String.valueOf(mNumOfGps);
            summaryBuilder.append(StringUtils.rightPad(coordinate, rightPad)).append(":").append(StringUtils.leftPad(coordinateValue, leftPad)).append("\n");

            String invalidFormatValue = String.valueOf(mNumOfInvalidFormat);
            summaryBuilder.append(StringUtils.rightPad(invalidFormat, rightPad)).append(":").append(StringUtils.leftPad(invalidFormatValue, leftPad)).append("\n");

            String placemarksValue = String.valueOf(mNumOfPlacemarks);
            summaryBuilder.append(StringUtils.rightPad(placemarks, rightPad)).append(":").append(StringUtils.leftPad(placemarksValue, leftPad)).append("\n");

            String timeValue = String.format("%.3f", (System.currentTimeMillis() - mStartTime) / 1000.0).trim();
            summaryBuilder.append(StringUtils.rightPad(time, rightPad)).append(":").append(StringUtils.leftPad(timeValue, leftPad)).append(" ").append(Dict.TIME_SECONDS).append("\n");

            mListener.onOperationLog(summaryBuilder.toString());
            mListener.onOperationFinished(summaryBuilder.toString());
        } catch (FileNotFoundException ex) {
            mListener.onOperationLog(ex.getLocalizedMessage());
        }
    }
}
