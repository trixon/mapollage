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
package se.trixon.mapollage;

import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.exif.GpsDescriptor;
import com.drew.metadata.exif.GpsDirectory;
import de.micromata.opengis.kml.v_2_2_0.AltitudeMode;
import de.micromata.opengis.kml.v_2_2_0.BalloonStyle;
import de.micromata.opengis.kml.v_2_2_0.DisplayMode;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Icon;
import de.micromata.opengis.kml.v_2_2_0.IconStyle;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.LineString;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Style;
import de.micromata.opengis.kml.v_2_2_0.StyleState;
import de.micromata.opengis.kml.v_2_2_0.TimeStamp;
import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
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
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import se.trixon.almond.util.BundleHelper;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.Scaler;
import se.trixon.mapollage.profile.Profile;
import se.trixon.mapollage.profile.ProfileDescription;
import se.trixon.mapollage.profile.ProfileDescription.DescriptionSegment;
import se.trixon.mapollage.profile.ProfileFolder;
import se.trixon.mapollage.profile.ProfilePath;
import se.trixon.mapollage.profile.ProfilePhoto;
import se.trixon.mapollage.profile.ProfilePlacemark;
import se.trixon.mapollage.profile.ProfileSource;

/**
 *
 * @author Patrik Karlsson
 */
public class Operation implements Runnable {

    private final BalloonStyle mBalloonStyle;
    private final ResourceBundle mBundle;
    private final DateFormat mDateFormatDate = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
    private final File mDestinationFile;
    private final Document mDocument;
    private final HashMap<File, File> mFileThumbMap = new HashMap<>();
    private final List<File> mFiles = new ArrayList<>();
    private final Pattern mFolderByRegexPattern;
    private final Map<String, Folder> mFolders = new HashMap<>();
    private boolean mInterrupted = false;
    private final Kml mKml = new Kml();
    private final ArrayList<LineNode> mLineNodes = new ArrayList<>();
    private final OperationListener mListener;
    private int mNumOfErrors = 0;
    private int mNumOfExif;
    private int mNumOfGps;
    private int mNumOfPlacemarks;
    private Folder mPathFolder;
    private PhotoInfo mPhotoInfo;
    private final Profile mProfile;
    private final ProfileDescription mProfileDescription;
    private final ProfileFolder mProfileFolder;
    private final ProfilePath mProfilePath;
    private final ProfilePhoto mProfilePhoto;
    private final ProfilePlacemark mProfilePlacemark;
    private final ProfileSource mProfileSource;
    private Folder mRootFolder;
    private final Map<String, Folder> mRootFolders = new HashMap<>();
    private long mStartTime;
    private File mThumbsDir;
    private final SimpleDateFormat mTimeStampDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");

    public Operation(OperationListener operationListener, Profile profile) {
        mListener = operationListener;
        mProfile = profile;
        mProfileSource = mProfile.getSource();
        mProfileFolder = mProfile.getFolder();
        mProfilePath = mProfile.getPath();
        mProfilePlacemark = mProfile.getPlacemark();
        mProfileDescription = mProfile.getDescription();
        mProfilePhoto = mProfile.getPhoto();
        mDestinationFile = mProfile.getDestinationFile();

        mFolderByRegexPattern = Pattern.compile(mProfileFolder.getRegex());

        mBundle = BundleHelper.getBundle(Operation.class, "Bundle");
        mDocument = mKml.createAndSetDocument().withOpen(true);
        mBalloonStyle = KmlFactory.createBalloonStyle()
                .withId("BalloonStyleId")
                //aabbggrr
                .withBgColor("FF272420")
                .withTextColor("FFEEEEEE")
                .withText("$[description]")
                .withDisplayMode(DisplayMode.DEFAULT);
    }

    @Override
    public void run() {
        mStartTime = System.currentTimeMillis();
        Date date = new Date(mStartTime);
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        mListener.onOperationStarted();
        mListener.onOperationLog(new SimpleDateFormat().format(new Date()));

        if (mProfilePlacemark.isSymbolAsPhoto()) {
            mThumbsDir = new File(mDestinationFile.getParent() + String.format("/%s-thumbnails", FilenameUtils.getBaseName(mDestinationFile.getAbsolutePath())));
            try {
                FileUtils.forceMkdir(mThumbsDir);
            } catch (IOException ex) {
                logError(String.format("E000 %s", ex.getMessage()));
            }
        }

        String status;
        mRootFolder = mDocument.createAndAddFolder().withName(mProfileFolder.getRootName()).withOpen(true);

        String href = "<a href=\"https://trixon.se/mapollage/\">Mapollage</a>";
        String description = String.format("<p>%s %s, %s</p>%s",
                Dict.MADE_WITH.toString(),
                href,
                dateFormat.format(date),
                mProfileFolder.getRootDescription().replaceAll("\\n", "<br />"));
        mRootFolder.setDescription(description);

        mListener.onOperationProcessingStarted();
        try {
            mInterrupted = !generateFileList();
        } catch (IOException ex) {
            logError(ex.getMessage());
        }

        if (!mInterrupted && !mFiles.isEmpty()) {
            mListener.onOperationLog(String.format(mBundle.getString("found_count"), mFiles.size()));
            mListener.onOperationLog("");
            mListener.onOperationProgressInit(mFiles.size());

            for (File file : mFiles) {
                mListener.onOperationProgress(file.getAbsolutePath());

                try {
                    addPhoto(file);
                } catch (ImageProcessingException ex) {
                    logError(String.format(ex.getMessage()));
                } catch (IOException ex) {
                    logError(String.format("E000 %s", file.getAbsolutePath()));
                }

                if (Thread.interrupted()) {
                    mInterrupted = true;
                    break;
                }
            }

            if (mProfilePath.isDrawPath() && mLineNodes.size() > 1) {
                addPath();
            }
        }

        if (mInterrupted) {
            status = Dict.TASK_ABORTED.toString();
            mListener.onOperationLog("\n" + status);
            mListener.onOperationInterrupted();
        } else if (!mFiles.isEmpty()) {
            saveToFile();
        }

        if (mNumOfErrors > 0) {
            logError(mBundle.getString("error_description"));
        }
    }

    private void addPath() {
        Collections.sort(mLineNodes, (LineNode o1, LineNode o2) -> o1.getDate().compareTo(o2.getDate()));
        SimpleDateFormat nameDateFormat = new SimpleDateFormat("yyyyMMdd HHmmss");

        mPathFolder = KmlFactory.createFolder()
                .withName(Dict.PATH_GFX.toString())
                .withOpen(true);

        String[] patterns = new String[]{"'NO_SPLIT'", "yyyyMMddHH", "yyyyMMdd", "yyyyww", "yyyyMM", "yyyy"};
        String pattern = patterns[mProfilePath.getSplitBy()];
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);

        TreeMap<String, ArrayList<LineNode>> map = new TreeMap<>();

        mLineNodes.forEach((node) -> {
            String key = dateFormat.format(node.getDate());
            if (!map.containsKey(key)) {
                map.put(key, new ArrayList<>());
            }
            map.get(key).add(node);
        });

        boolean colorState = true;

        for (ArrayList<LineNode> nodes : map.values()) {
            if (nodes.size() > 1) {
                String name = String.format("%s_%s",
                        nameDateFormat.format(nodes.get(0).getDate()),
                        nameDateFormat.format(nodes.get(nodes.size() - 1).getDate()));

                Placemark path = mPathFolder.createAndAddPlacemark()
                        .withName(name);

                Style pathStyle = path.createAndAddStyle();
                pathStyle.createAndSetLineStyle()
                        .withColor(colorState ? "ff0000ff" : "ff00ffff")
                        .withWidth(mProfilePath.getWidth());

                LineString line = path
                        .createAndSetLineString()
                        .withExtrude(false)
                        .withTessellate(true);

                nodes.forEach((node) -> {
                    line.addToCoordinates(node.getLon(), node.getLat());
                });

                colorState = !colorState;
            }
        }
    }

    private void addPhoto(File file) throws ImageProcessingException, IOException {
        mPhotoInfo = new PhotoInfo(file, mProfileSource.isIncludeNullCoordinate());
        try {
            mPhotoInfo.init();
        } catch (ImageProcessingException | IOException e) {
            if (mPhotoInfo.hasExif()) {
                mNumOfExif++;
            }

            throw e;
        }

        boolean hasLocation = false;
        if (mPhotoInfo.hasExif()) {
            mNumOfExif++;
            hasLocation = mPhotoInfo.hasGps() && !mPhotoInfo.isZeroCoordinate();
            if (hasLocation) {
                mNumOfGps++;
            }
        } else {
            throw new ImageProcessingException(String.format("E010 %s", file.getAbsolutePath()));
        }

        Date exifDate = mPhotoInfo.getDate();
        if (hasLocation && mProfilePath.isDrawPath()) {
            mLineNodes.add(new LineNode(exifDate, mPhotoInfo.getLat(), mPhotoInfo.getLon()));
        }

        if (hasLocation || mProfileSource.isIncludeNullCoordinate()) {
            Folder folder = getFolder(file, exifDate);

            String imageId = String.format("%08x", FileUtils.checksumCRC32(file));
            String styleNormalId = String.format("s_%s-pushpin", imageId);
            String styleHighlightId = String.format("s_%s-pushpin_hl", imageId);
            String styleMapId = String.format("m_%s-pushpin", imageId);
            double highlightZoom = mProfilePlacemark.getZoom() * mProfilePlacemark.getScale();

            IconStyle normalIconStyle = folder.createAndAddStyle().withId(styleNormalId).createAndSetIconStyle().withScale(mProfilePlacemark.getScale());
            IconStyle highlightIconStyle = folder.createAndAddStyle().withId(styleHighlightId).createAndSetIconStyle().withScale(highlightZoom);

            folder.createAndAddStyle()
                    .withBalloonStyle(mBalloonStyle)
                    .withIconStyle(normalIconStyle)
                    .withId(styleNormalId);

            folder.createAndAddStyle()
                    .withBalloonStyle(mBalloonStyle)
                    .withIconStyle(highlightIconStyle)
                    .withId(styleHighlightId);

            if (mProfilePlacemark.isSymbolAsPhoto()) {
                Icon icon = KmlFactory.createIcon().withHref(String.format("%s/%s.jpg", mThumbsDir.getName(), imageId));
                normalIconStyle.setIcon(icon);
                highlightIconStyle.setIcon(icon);
            }

            if (mProfilePlacemark.isSymbolAsPhoto() || mProfilePhoto.getReference() == ProfilePhoto.Reference.THUMBNAIL) {
                File thumbFile = new File(mThumbsDir, imageId + ".jpg");
                mFileThumbMap.put(file, thumbFile);
                mPhotoInfo.createThumbnail(thumbFile);
            }

            folder.createAndAddStyleMap().withId(styleMapId)
                    .addToPair(KmlFactory.createPair().withKey(StyleState.NORMAL).withStyleUrl(styleNormalId))
                    .addToPair(KmlFactory.createPair().withKey(StyleState.HIGHLIGHT).withStyleUrl(styleHighlightId));

            Placemark placemark = KmlFactory.createPlacemark()
                    .withName(getPlacemarkName(file, exifDate))
                    .withDescription(getPlacemarkDescription(file, mPhotoInfo, exifDate))
                    .withOpen(Boolean.TRUE)
                    .withStyleUrl(styleMapId);

            placemark.createAndSetPoint()
                    .addToCoordinates(mPhotoInfo.getLon(), mPhotoInfo.getLat())
                    .setAltitudeMode(AltitudeMode.CLAMP_TO_GROUND);

            if (mProfilePlacemark.isTimestamp()) {
                TimeStamp timeStamp = KmlFactory.createTimeStamp();
                timeStamp.setWhen(mTimeStampDateFormat.format(exifDate));
                placemark.setTimePrimitive(timeStamp);
            }

            folder.addToFeature(placemark);
            mNumOfPlacemarks++;
        }

        mListener.onOperationLog(file.getAbsolutePath());
    }

    private boolean generateFileList() throws IOException {
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
            FileVisitor fileVisitor = new FileVisitor(pathMatcher, mFiles, file, this);
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
                throw new IOException(String.format("E000 %s", file.getAbsolutePath()));
            }
        } else if (file.isFile() && pathMatcher.matches(file.toPath().getFileName())) {
            mFiles.add(file);
        }

        if (mFiles.isEmpty()) {
            mListener.onOperationFinished(Dict.FILELIST_EMPTY.toString());
        } else {
            Collections.sort(mFiles);
        }

        return true;
    }

    private String getDescPhoto(File sourceFile, int orientation) throws IOException {
        Scaler scaler = new Scaler(new Dimension(mPhotoInfo.getOriginalDimension()));
        boolean thumbRef = mProfilePhoto.getReference() == ProfilePhoto.Reference.THUMBNAIL;
        boolean portrait = (orientation == 6 || orientation == 8) && thumbRef;

        if (mProfilePhoto.isLimitWidth()) {
            int widthLimit = portrait ? mProfilePhoto.getHeightLimit() : mProfilePhoto.getWidthLimit();
            scaler.setWidth(widthLimit);
        }

        if (mProfilePhoto.isLimitHeight()) {
            int heightLimit = portrait ? mProfilePhoto.getWidthLimit() : mProfilePhoto.getHeightLimit();
            scaler.setHeight(heightLimit);
        }

        Dimension newDimension = scaler.getDimension();
        String imageTagFormat = "<p><img src='%s' width='%d' height='%d'></p>";

        int width = portrait ? newDimension.height : newDimension.width;
        int height = portrait ? newDimension.width : newDimension.height;

        String imageTag = String.format(imageTagFormat, getImagePath(sourceFile), width, height);

        return imageTag;
    }

    private Folder getFolder(File file, Date date) {
        String key;
        Folder folder = null;

        switch (mProfileFolder.getFoldersBy()) {
            case ProfileFolder.FOLDER_BY_DIR:
                Path relativePath = mProfileSource.getDir().toPath().relativize(file.getParentFile().toPath());
                key = relativePath.toString();
                folder = getFolder(key);
                break;

            case ProfileFolder.FOLDER_BY_DATE:
                key = mProfileFolder.getFolderDateFormat().format(date);
                folder = getFolder(key);
                break;

            case ProfileFolder.FOLDER_BY_REGEX:
                key = mProfileFolder.getRegexDefault();
                Matcher matcher = mFolderByRegexPattern.matcher(file.getParent());
                if (matcher.find()) {
                    key = matcher.group();
                }
                folder = getFolder(key);
                break;

            case ProfileFolder.FOLDER_BY_NONE:
                folder = mRootFolder;
                break;
        }

        return folder;
    }

    private Folder getFolder(String key) {
        key = StringUtils.replace(key, "\\", "/");
        String[] levels = StringUtils.split(key, "/");

        Folder parent = mRootFolder;
        String path = "";

        for (int i = 0; i < levels.length; i++) {
            String level = levels[i];
            path = String.format("%s/%s", path, level);
            parent = getFolder(path, parent, level);
            if (i == 0) {
                mFolders.put(path, parent);
            }
        }

        return parent;
    }

    private Folder getFolder(String key, Folder parent, String name) {
        if (!mFolders.containsKey(key)) {
            Folder folder = parent.createAndAddFolder().withName(name);
            mFolders.put(key, folder);
        }

        return mFolders.get(key);
    }

    private String getImagePath(File file) {
        String imageSrc;

        switch (mProfilePhoto.getReference()) {
            case ABSOLUTE:
                imageSrc = String.format("file:///%s", file.getAbsolutePath());
                break;

            case ABSOLUTE_PATH:
                imageSrc = String.format("%s%s", mProfilePhoto.getBaseUrlValue(), file.getName());
                break;

            case RELATIVE:
                Path relativePath = mDestinationFile.toPath().relativize(file.toPath());
                imageSrc = StringUtils.replace(relativePath.toString(), "..", ".", 1);
                break;

            case THUMBNAIL:
                Path thumbPath = mDestinationFile.toPath().relativize(mFileThumbMap.get(file).toPath());
                imageSrc = StringUtils.replace(thumbPath.toString(), "..", ".", 1);
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

    private String getPlacemarkDescription(File file, PhotoInfo photoInfo, Date exifDate) throws IOException {
        GpsDirectory gpsDirectory = photoInfo.getGpsDirectory();
        GpsDescriptor gpsDescriptor = null;
        if (gpsDirectory != null) {
            gpsDescriptor = new GpsDescriptor(gpsDirectory);
        }

        String desc = mProfileDescription.isCustom() ? mProfileDescription.getCustomValue() : getStaticDescription();

        if (StringUtils.containsIgnoreCase(desc, DescriptionSegment.PHOTO.toString())) {
            desc = StringUtils.replace(desc, DescriptionSegment.PHOTO.toString(), getDescPhoto(file, photoInfo.getOrientation()));
        }

        desc = StringUtils.replace(desc, DescriptionSegment.FILENAME.toString(), file.getName());
        desc = StringUtils.replace(desc, DescriptionSegment.DATE.toString(), mDateFormatDate.format(exifDate));

        if (gpsDirectory != null && gpsDescriptor != null) {
            desc = StringUtils.replace(desc, DescriptionSegment.ALTITUDE.toString(), gpsDescriptor.getGpsAltitudeDescription());
            desc = StringUtils.replace(desc, DescriptionSegment.COORDINATE.toString(), gpsDescriptor.getDegreesMinutesSecondsDescription());

            String bearing = gpsDescriptor.getGpsDirectionDescription(GpsDirectory.TAG_DEST_BEARING);
            desc = StringUtils.replace(desc, DescriptionSegment.BEARING.toString(), bearing == null ? "" : bearing);
        } else {
            desc = StringUtils.replace(desc, DescriptionSegment.ALTITUDE.toString(), "");
            desc = StringUtils.replace(desc, DescriptionSegment.COORDINATE.toString(), "");
            desc = StringUtils.replace(desc, DescriptionSegment.BEARING.toString(), "");
        }

        return desc;
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
                    logError(String.format("E011 %s", file.getAbsolutePath()));
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
        mListener.onOperationLog("");
        List keys = new ArrayList(mRootFolders.keySet());
        Collections.sort(keys);

        keys.stream().forEach((key) -> {
            mRootFolder.getFeature().add(mRootFolders.get((String) key));
        });

        if (mPathFolder != null) {
            mRootFolder.getFeature().add(mPathFolder);
        }

        if (mProfilePlacemark.isSymbolAsPhoto()) {
            mListener.onOperationLog("\n" + String.format(mBundle.getString("stored_thumbnails"), mThumbsDir.getAbsolutePath()));
        }

        mListener.onOperationLog(String.format(Dict.SAVING.toString(), mDestinationFile.getAbsolutePath()));

        try {
            mKml.marshal(mDestinationFile);

            String files = mBundle.getString("status_files");
            String exif = mBundle.getString("status_exif");
            String coordinate = mBundle.getString("status_coordinate");
            String time = mBundle.getString("status_time");
            String error = " " + Dict.Dialog.ERRORS.toString().toLowerCase();
            String placemarks = mBundle.getString("status_placemarks");

            int rightPad = files.length();
            rightPad = Math.max(rightPad, exif.length());
            rightPad = Math.max(rightPad, coordinate.length());
            rightPad = Math.max(rightPad, time.length());
            rightPad = Math.max(rightPad, error.length());
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

            String placemarksValue = String.valueOf(mNumOfPlacemarks);
            summaryBuilder.append(StringUtils.rightPad(placemarks, rightPad)).append(":").append(StringUtils.leftPad(placemarksValue, leftPad)).append("\n");

            String errorValue = String.valueOf(mNumOfErrors);
            summaryBuilder.append(StringUtils.rightPad(error, rightPad)).append(":").append(StringUtils.leftPad(errorValue, leftPad)).append("\n");

            String timeValue = String.format("%.3f", (System.currentTimeMillis() - mStartTime) / 1000.0).trim();
            summaryBuilder.append(StringUtils.rightPad(time, rightPad)).append(":").append(StringUtils.leftPad(timeValue, leftPad)).append(" ").append(Dict.TIME_SECONDS).append("\n");

            mListener.onOperationFinished(summaryBuilder.toString());
        } catch (FileNotFoundException ex) {
            mListener.onOperationLog(ex.getLocalizedMessage());
        }
    }

    String getExcludePattern() {
        return mProfileSource.getExcludePattern();
    }

    OperationListener getListener() {
        return mListener;
    }

    void logError(String message) {
        mNumOfErrors++;
        mListener.onOperationError(message);
    }
}
