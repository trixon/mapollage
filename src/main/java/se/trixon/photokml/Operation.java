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
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import se.trixon.almond.util.BundleHelper;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.GraphicsHelper;
import se.trixon.almond.util.Scaler;
import se.trixon.photokml.profile.Profile;
import se.trixon.photokml.profile.ProfilePlacemark;

/**
 *
 * @author Patrik Karlsson
 */
public class Operation {

    private final ResourceBundle mBundle;
    private final DateFormat mDateFormatDate = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
    private File mDestinationFile;
    private final List<File> mFiles = new ArrayList<>();
    private final Map<String, Folder> mFolders = new HashMap<>();
    private boolean mInterrupted = false;
    private Kml mKml = new Kml();
    private final OperationListener mListener;
    private int mNumOfExif;
    private int mNumOfGps;
    private int mNumOfInvalidFormat;
    private int mNumOfPlacemarks;
    private final Profile mProfile;
    private Folder mRootFolder;
    private long mStartTime;
    private final ProfilePlacemark mProfilePlacemark;

    public Operation(OperationListener operationListener, Profile profile) {
        mListener = operationListener;
        mProfile = profile;
        mProfilePlacemark = mProfile.getPlacemark();
        mBundle = BundleHelper.getBundle(Operation.class, "Bundle");
    }

    public void start() {
        mStartTime = System.currentTimeMillis();
        mListener.onOperationStarted();
        String status;
        mDestinationFile = mProfile.getDestFile();
        mRootFolder = mKml.createAndSetFolder().withName(mProfile.getFolder().getRootName());
        mRootFolder.setDescription(mProfile.getFolder().getRootDescription());

        mInterrupted = !generateFileList();

        if (!mInterrupted && !mFiles.isEmpty()) {
            mListener.onOperationProcessingStarted();

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
        } else if (!mFiles.isEmpty()) {
            saveToFile();
        }
//            if (mFiles.isEmpty()) {
//                mListener.onOperationFailed();
//            }
    }

    private void addFileToKml(File file) throws ImageProcessingException, IOException {
        mListener.onOperationLog(file.getAbsolutePath());

        Metadata metadata = ImageMetadataReader.readMetadata(file);
        ExifSubIFDDirectory exifDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
        GpsDescriptor gpsDescriptor = new GpsDescriptor(gpsDirectory);

        if (exifDirectory != null) {
            mNumOfExif++;
            if (gpsDirectory != null) {
                mNumOfGps++;
            }
        } else {
            throw new ImageProcessingException(String.format(mBundle.getString("exifError"), file.getAbsolutePath()));
        }

        Date exifDate = getImageDate(file, exifDirectory);
        String name;

        if (mProfilePlacemark.isByFilename()) {
            name = FilenameUtils.getBaseName(file.getAbsolutePath());
        } else if (mProfilePlacemark.isByDate()) {
            try {
                name = mProfilePlacemark.getDateFormat().format(exifDate);
            } catch (IllegalArgumentException ex) {
                name = "invalid exif date";
            } catch (NullPointerException ex) {
                name = "invalid exif date";
                mListener.onOperationError(" ! Invalid date in " + file.getAbsolutePath());
            }

        } else {
            name = "";
        }

        String desc = mProfilePlacemark.getDesccription();
        desc = StringUtils.replace(desc, Desc.PHOTO, getDescPhoto(file));
        desc = StringUtils.replace(desc, Desc.FILENAME, file.getName());
        desc = StringUtils.replace(desc, Desc.DATE, mDateFormatDate.format(exifDate));

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

            if (exifDate != null) {
                desc = StringUtils.replace(desc, Desc.ALTITUDE, gpsDescriptor.getGpsAltitudeDescription());
                desc = StringUtils.replace(desc, Desc.COORDINATE, gpsDescriptor.getDegreesMinutesSecondsDescription());

                String bearing = gpsDescriptor.getGpsDirectionDescription(GpsDirectory.TAG_DEST_BEARING);
                if (bearing != null) {
                    desc = StringUtils.replace(desc, Desc.BEARING, gpsDescriptor.getGpsAltitudeDescription());
                }
            }
        }

//            descriptionBuilder.append("]]>");
        boolean shouldAppendToKml = gpsDirectory != null || mProfilePlacemark.hasCoordinate();

        if (shouldAppendToKml) {
            double format = 1000000;
            int latInt = (int) (geoLocation.getLatitude() * format);
            int lonInt = (int) (geoLocation.getLongitude() * format);

            getFolder(file, exifDate).createAndAddPlacemark()
                    .withName(name)
                    .withOpen(Boolean.TRUE)
                    .withDescription(desc)
                    .createAndSetPoint()
                    .addToCoordinates(lonInt / format, latInt / format);

            mNumOfPlacemarks++;
        } else {
            mListener.onOperationError(Dict.FAILED.toString());
        }
    }

    private boolean generateFileList() {
        mListener.onOperationLog(Dict.GENERATING_FILELIST.toString());
        PathMatcher pathMatcher = mProfile.getSource().getPathMatcher();

        EnumSet<FileVisitOption> fileVisitOptions = EnumSet.noneOf(FileVisitOption.class);
        if (mProfile.getSource().isFollowLinks()) {
            fileVisitOptions = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
        } else {
            fileVisitOptions = EnumSet.noneOf(FileVisitOption.class);
        }

        File file = mProfile.getSource().getDir();
        if (file.isDirectory()) {
            FileVisitor fileVisitor = new FileVisitor(pathMatcher, mFiles);
            try {
                if (mProfile.getSource().isRecursive()) {
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

        if (mProfile.getMaxWidth() != null) {
            scaler.setWidth(mProfile.getMaxWidth());
        }

        if (mProfile.getMaxHeight() != null) {
            scaler.setHeight(mProfile.getMaxHeight());
        }

        Dimension newDimension = scaler.getDimension();
        String imageTagFormat = "<p><img src='%s' width='%d' height='%d'></p>";
        String imageTag = String.format(imageTagFormat, getImagePath(sourceFile), newDimension.width, newDimension.height);

        return imageTag;
    }

    private Folder getFolder(File file, Date date) {
        String key;
        Folder folder;

        if (mProfile.getFolder().isFolderByDir()) {
            key = file.getParentFile().getName();
            folder = getFolder(key);
        } else if (mProfile.getFolder().isFolderByDate()) {
            key = mProfile.getFolder().getFolderDateFormat().format(date);
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

        if (mProfile.getAbsolutePath() != null) {
            imageSrc = mProfile.getAbsolutePath() + file.getName();
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

        if (mProfile.isLowerCaseExt()) {
            String ext = FilenameUtils.getExtension(imageSrc);
            if (!StringUtils.isBlank(ext) && !StringUtils.isAllLowerCase(ext)) {
                String noExt = FilenameUtils.removeExtension(imageSrc);
                imageSrc = String.format("%s.%s", noExt, ext.toLowerCase());
            }
        }

        return imageSrc;
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
            if (mProfile.getAbsolutePath() == null) {
                mListener.onOperationLog(mBundle.getString("operationNote"));
            }

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

            mListener.onOperationFinished("");
        } catch (FileNotFoundException ex) {
            mListener.onOperationLog(ex.getLocalizedMessage());
        }
    }

}
