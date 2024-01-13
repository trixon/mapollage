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
package se.trixon.mapollage.core;

import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.exif.GpsDescriptor;
import com.drew.metadata.exif.GpsDirectory;
import de.micromata.opengis.kml.v_2_2_0.BalloonStyle;
import de.micromata.opengis.kml.v_2_2_0.ColorMode;
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
import de.micromata.opengis.kml.v_2_2_0.Snippet;
import de.micromata.opengis.kml.v_2_2_0.StyleState;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;
import se.trixon.almond.nbp.output.OutputHelper;
import se.trixon.almond.nbp.output.OutputLineMode;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.Scaler;
import se.trixon.almond.util.ext.GrahamScan;
import se.trixon.mapollage.Options;
import se.trixon.mapollage.core.TaskDescription.DescriptionSegment;

/**
 *
 * @author Patrik Karlström
 */
public class Operation {

    private final BalloonStyle mBalloonStyle;
    private final Snippet mBlankSnippet = new Snippet();
    private final ResourceBundle mBundle = NbBundle.getBundle(Operation.class);
    private final DateFormat mDateFormatDate = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
    private final File mDestinationFile;
    private final HashMap<String, Properties> mDirToDesc = new HashMap<>();
    private final Document mDocument;
    private final HashMap<File, File> mFileThumbMap = new HashMap<>();
    private final List<File> mFiles = new ArrayList<>();
    private final Pattern mFolderByRegexPattern;
    private final HashMap<Folder, ArrayList<Coordinate>> mFolderPolygonInputs = new HashMap<>();
    private final Map<String, Folder> mFolders = new HashMap<>();
    private boolean mInterrupted = false;
    private final Kml mKml = new Kml();
    private final ArrayList<LineNode> mLineNodes = new ArrayList<>();
    private int mNumOfExif;
    private int mNumOfGps;
    private int mNumOfPlacemarks;
    private final Options mOptions = Options.getInstance();
    private Folder mPathFolder;
    private Folder mPathGapFolder;
    private PhotoInfo mPhotoInfo;
    private Folder mPolygonFolder;
    private final HashMap<Folder, Folder> mPolygonRemovals = new HashMap<>();
    private final Task mTask;
    private final TaskDescription mTaskDescription;
    private final TaskFolder mTaskFolder;
    private final TaskPath mTaskPath;
    private final TaskPhoto mTaskPhoto;
    private final TaskPlacemark mTaskPlacemark;
    private final TaskSource mTaskSource;
    private Folder mRootFolder;
    private final Map<String, Folder> mRootFolders = new HashMap<>();
    private File mThumbsDir;
    private final SimpleDateFormat mTimeStampDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
    private final InputOutput mInputOutput;
    private final OutputHelper mOutputHelper;

    public Operation(Task task, InputOutput inputOutput, OutputHelper outputHelper) {
        mTask = task;
        mInputOutput = inputOutput;
        mOutputHelper = outputHelper;

        mTaskSource = mTask.getSource();
        mTaskFolder = mTask.getFolder();
        mTaskPath = mTask.getPath();
        mTaskPlacemark = mTask.getPlacemark();
        mTaskDescription = mTask.getDescription();
        mTaskPhoto = mTask.getPhoto();
        mDestinationFile = mTask.getDestinationFile();

        mFolderByRegexPattern = Pattern.compile(mTaskFolder.getRegex());

        mDocument = mKml.createAndSetDocument().withOpen(true);
        mBalloonStyle = KmlFactory.createBalloonStyle()
                .withId("BalloonStyleId")
                //aabbggrr
                .withBgColor("ff272420")
                .withTextColor("ffeeeeee")
                .withText("$[description]");

//        mListener.onOperationStarted();
//        mListener.onOperationLog(dateFormat.format(date));
    }

    public boolean start() {
        mRootFolder = mDocument.createAndAddFolder().withName(getSafeXmlString(mTaskFolder.getRootName())).withOpen(true);

        var href = "<a href=\"https://trixon.se/mapollage/\">Mapollage</a>";
        var description = "<p>%s %s, %s</p>%s".formatted(
                Dict.MADE_WITH.toString(),
                href,
                LocalDateTime.now().toString(),
                mTaskFolder.getRootDescription().replaceAll("\\n", "<br />"));
        mRootFolder.setDescription(getSafeXmlString(description));

//        mListener.onOperationProcessingStarted();
        try {
            mInterrupted = !generateFileList();
        } catch (IOException ex) {
            mInputOutput.getErr().println(ex.getMessage());
        }

        return mInterrupted;
    }

    public void run() {
        String status;

        if (!mInterrupted && !mFiles.isEmpty()) {
            if (isUsingThumbnails()) {
                mThumbsDir = new File(mDestinationFile.getParent() + String.format("/%s-thumbnails", FilenameUtils.getBaseName(mDestinationFile.getAbsolutePath())));
                try {
                    FileUtils.forceMkdir(mThumbsDir);
                } catch (IOException ex) {
                    mInputOutput.getErr().println(ex.getMessage());
                }
            }

//            mListener.onOperationLog(String.format(mBundle.getString("found_count"), mFiles.size()));
//            mListener.onOperationLog("");
            int progress = 0;
            for (File file : mFiles) {
//                mListener.onOperationProgress(file.getAbsolutePath());
//                mListener.onOperationProgress(++progress, mFiles.size());

                try {
                    addPhoto(file);
                } catch (ImageProcessingException ex) {
                    mInputOutput.getErr().println(ex.getMessage());
                } catch (IOException ex) {
                    mInputOutput.getErr().println(file.getAbsolutePath());
                }

                if (Thread.interrupted()) {
                    mInterrupted = true;
                    break;
                }
            }

            if (mTaskPath.isDrawPath() && mLineNodes.size() > 1) {
                addPath();
            }
        }

        if (mInterrupted) {
            status = Dict.TASK_ABORTED.toString();
//            mListener.onOperationLog("\n" + status);
//            mListener.onOperationInterrupted();
        } else if (!mFiles.isEmpty()) {
            if (mTaskPath.isDrawPolygon()) {
                addPolygons();
            }
            saveToFile();
            mTask.setLastRun(System.currentTimeMillis());
        }

//        if (mNumOfErrors > 0) {
//            logError(mBundle.getString("error_description"));
//        }
    }

    HashMap<String, Properties> getDirToDesc() {
        return mDirToDesc;
    }

    String getExcludePattern() {
        return mTaskSource.getExcludePattern();
    }

//    OperationListener getListener() {
//        return mListener;
//    }
    TaskDescription getTaskDescription() {
        return mTaskDescription;
    }

    private void addPath() {
        Collections.sort(mLineNodes, (LineNode o1, LineNode o2) -> o1.getDate().compareTo(o2.getDate()));

        mPathFolder = KmlFactory.createFolder().withName(Dict.Geometry.PATH.toString());
        mPathGapFolder = KmlFactory.createFolder().withName(Dict.Geometry.PATH_GAP.toString());

        String pattern = getPattern(mTaskPath.getSplitBy());
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);

        TreeMap<String, ArrayList<LineNode>> map = new TreeMap<>();

        mLineNodes.forEach((node) -> {
            String key = dateFormat.format(node.getDate());
            if (!map.containsKey(key)) {
                map.put(key, new ArrayList<>());
            }
            map.get(key).add(node);
        });

        //Add paths
        for (var nodes : map.values()) {
            if (nodes.size() > 1) {
                var pathPlacemark = mPathFolder.createAndAddPlacemark()
                        .withName(LineNode.getName(nodes));

                var pathStyle = pathPlacemark.createAndAddStyle();
                pathStyle.createAndSetLineStyle()
                        .withColor("ff0000ff")
                        .withWidth(mTaskPath.getWidth());

                var line = pathPlacemark
                        .createAndSetLineString()
                        .withExtrude(false)
                        .withTessellate(true);

                nodes.forEach((node) -> {
                    line.addToCoordinates(node.getLon(), node.getLat());
                });
            }
        }

        //Add path gap
        ArrayList<LineNode> previousNodes = null;
        for (var nodes : map.values()) {
            if (previousNodes != null) {
                var pathPlacemark = mPathGapFolder.createAndAddPlacemark()
                        .withName(LineNode.getName(previousNodes, nodes));

                var pathStyle = pathPlacemark.createAndAddStyle();
                pathStyle.createAndSetLineStyle()
                        .withColor("ff00ffff")
                        .withWidth(mTaskPath.getWidth());

                var line = pathPlacemark
                        .createAndSetLineString()
                        .withExtrude(false)
                        .withTessellate(true);

                LineNode prevLast = previousNodes.get(previousNodes.size() - 1);
                LineNode currentFirst = nodes.get(0);

                line.addToCoordinates(prevLast.getLon(), prevLast.getLat());
                line.addToCoordinates(currentFirst.getLon(), currentFirst.getLat());
            }
            previousNodes = nodes;
        }
    }

    private void addPhoto(File file) throws ImageProcessingException, IOException {
        mPhotoInfo = new PhotoInfo(file, mTaskSource.isIncludeNullCoordinate());
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

        var exifDate = mPhotoInfo.getDate();
        if (hasLocation && mTaskPath.isDrawPath()) {
            mLineNodes.add(new LineNode(exifDate, mPhotoInfo.getLat(), mPhotoInfo.getLon()));
        }

        if (hasLocation || mTaskSource.isIncludeNullCoordinate()) {
            var folder = getFolder(file, exifDate);

            String imageId = String.format("%08x", FileUtils.checksumCRC32(file));
            String styleNormalId = String.format("s_%s", imageId);
            String styleHighlightId = String.format("s_%s_hl", imageId);
            String styleMapId = String.format("m_%s", imageId);

            var normalStyle = mDocument
                    .createAndAddStyle()
                    .withId(styleNormalId);

            var normalIconStyle = normalStyle
                    .createAndSetIconStyle()
                    .withScale(1.0);

            var highlightStyle = mDocument
                    .createAndAddStyle()
                    .withBalloonStyle(mBalloonStyle)
                    .withId(styleHighlightId);

            var highlightIconStyle = highlightStyle
                    .createAndSetIconStyle()
                    .withScale(1.1);

            if (mTaskPlacemark.isSymbolAsPhoto()) {
                var icon = KmlFactory.createIcon().withHref(String.format("%s/%s.jpg", mThumbsDir.getName(), imageId));
                normalIconStyle.setIcon(icon);
                normalIconStyle.setScale(mTaskPlacemark.getScale());

                double highlightZoom = mTaskPlacemark.getZoom() * mTaskPlacemark.getScale();
                highlightIconStyle.setIcon(icon);
                highlightIconStyle.setScale(highlightZoom);
            }

            if (isUsingThumbnails()) {
                var thumbFile = new File(mThumbsDir, imageId + ".jpg");
                mFileThumbMap.put(file, thumbFile);
                if (Files.isWritable(thumbFile.getParentFile().toPath())) {
                    mPhotoInfo.createThumbnail(thumbFile);
                } else {
//                    mListener.onOperationLog(String.format(mBundle.getString("insufficient_privileges"), mDestinationFile.getAbsolutePath()));
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            mDocument.createAndAddStyleMap().withId(styleMapId)
                    .addToPair(KmlFactory.createPair().withKey(StyleState.NORMAL).withStyleUrl("#" + styleNormalId))
                    .addToPair(KmlFactory.createPair().withKey(StyleState.HIGHLIGHT).withStyleUrl("#" + styleHighlightId));

            var placemark = KmlFactory.createPlacemark()
                    .withName(getSafeXmlString(getPlacemarkName(file, exifDate)))
                    .withSnippet(mBlankSnippet)
                    .withStyleUrl("#" + styleMapId);

            String desc = getPlacemarkDescription(file, mPhotoInfo, exifDate);
            if (!StringUtils.isBlank(desc)) {
                placemark.setDescription(desc);
            }

            placemark.createAndSetPoint()
                    .addToCoordinates(mPhotoInfo.getLon(), mPhotoInfo.getLat(), 0F);

            if (mTaskPlacemark.isTimestamp()) {
                var timeStamp = KmlFactory.createTimeStamp();
                timeStamp.setWhen(mTimeStampDateFormat.format(exifDate));
                placemark.setTimePrimitive(timeStamp);
            }

            folder.addToFeature(placemark);
            mNumOfPlacemarks++;
        }

//        mListener.onOperationLog(file.getAbsolutePath());
    }

    private void addPolygon(String name, ArrayList<Coordinate> coordinates, Folder polygonFolder) {
        List<Point2D.Double> inputs = new ArrayList<>();
        coordinates.forEach((coordinate) -> {
            inputs.add(new Point2D.Double(coordinate.getLongitude(), coordinate.getLatitude()));
        });

        try {
            var convexHull = GrahamScan.getConvexHullDouble(inputs);
            var placemark = polygonFolder
                    .createAndAddPlacemark()
                    .withName(name);

            var style = placemark.createAndAddStyle();
            style.createAndSetLineStyle()
                    .withColor("00000000")
                    .withWidth(0.0);

            style.createAndSetPolyStyle()
                    .withColor("ccffffff")
                    .withColorMode(ColorMode.RANDOM);

            var polygon = placemark.createAndSetPolygon();
            var boundary = polygon.createAndSetOuterBoundaryIs();
            var linearRing = boundary.createAndSetLinearRing();

            convexHull.forEach((node) -> {
                linearRing.addToCoordinates(node.x, node.y);

            });
        } catch (IllegalArgumentException e) {
            System.err.println(e);
        }
    }

    private void addPolygons() {
        mPolygonFolder = KmlFactory.createFolder().withName(Dict.POLYGON.toString()).withOpen(false);
        addPolygons(mPolygonFolder, mRootFolder.getFeature());

        scanForFolderRemoval(mPolygonFolder);
//        while (scanForFolderRemoval(mPolygonFolder, false)) {
//            //
//        }

        for (var folder : mPolygonRemovals.keySet()) {
            var parentFolder = mPolygonRemovals.get(folder);
            parentFolder.getFeature().remove(folder);
        }

        mRootFolder.getFeature().add(mPolygonFolder);
    }

    private void addPolygons(Folder polygonParent, List<Feature> features) {
        for (var feature : features) {
            if (feature instanceof Folder) {
                var folder = (Folder) feature;

                if (folder != mPathFolder && folder != mPathGapFolder && folder != mPolygonFolder) {
                    var polygonFolder = polygonParent.createAndAddFolder().withName(folder.getName()).withOpen(true);
                    mFolderPolygonInputs.put(polygonFolder, new ArrayList<>());
                    addPolygons(polygonFolder, folder.getFeature());

                    if (mFolderPolygonInputs.get(polygonFolder) != null) {
                        addPolygon(folder.getName(), mFolderPolygonInputs.get(polygonFolder), polygonParent);
                    }
                }
            }

            if (feature instanceof Placemark) {
                var placemark = (Placemark) feature;
                var point = (Point) placemark.getGeometry();
                ArrayList<Coordinate> coordinates = mFolderPolygonInputs.computeIfAbsent(polygonParent, k -> new ArrayList<>());
                coordinates.addAll(point.getCoordinates());
            }
        }

        var rootCoordinates = mFolderPolygonInputs.get(mPolygonFolder);
        if (polygonParent == mPolygonFolder && rootCoordinates != null) {
            addPolygon(mPolygonFolder.getName(), rootCoordinates, polygonParent);
        }
    }

    private boolean generateFileList() throws IOException {
        mInputOutput.getOut().println();
        mOutputHelper.printSectionHeader(OutputLineMode.INFO, Dict.GENERATING_FILELIST.toString(), "", mTaskSource.getDir().getAbsolutePath());

        var pathMatcher = mTaskSource.getPathMatcher();

        EnumSet<FileVisitOption> fileVisitOptions;
        if (mTaskSource.isFollowLinks()) {
            fileVisitOptions = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
        } else {
            fileVisitOptions = EnumSet.noneOf(FileVisitOption.class);
        }

        var file = mTaskSource.getDir();
        if (file.isDirectory()) {
            var fileVisitor = new FileVisitor();
            try {
                if (mTaskSource.isRecursive()) {
                    Files.walkFileTree(file.toPath(), fileVisitOptions, Integer.MAX_VALUE, fileVisitor);
                } else {
                    Files.walkFileTree(file.toPath(), fileVisitOptions, 1, fileVisitor);
                }

                if (fileVisitor.isInterrupted()) {
                    return false;
                }
            } catch (IOException ex) {
                mInputOutput.getErr().println(ex.getMessage());
            }
        } else if (file.isFile() && pathMatcher.matches(file.toPath().getFileName())) {
            mFiles.add(file);
        }

        if (mFiles.isEmpty()) {
            mInputOutput.getOut().println(Dict.FILELIST_EMPTY.toString());
        } else {
            Collections.sort(mFiles);
        }

        return true;
    }

    private String getDescPhoto(File sourceFile, int orientation) throws IOException {
        var scaler = new Scaler(new Dimension(mPhotoInfo.getOriginalDimension()));
        boolean thumbRef = mTaskPhoto.getReference() == TaskPhoto.Reference.THUMBNAIL;
        boolean portrait = (orientation == 6 || orientation == 8) && thumbRef;

        if (mTaskPhoto.isLimitWidth()) {
            int widthLimit = portrait ? mTaskPhoto.getHeightLimit() : mTaskPhoto.getWidthLimit();
            scaler.setWidth(widthLimit);
        }

        if (mTaskPhoto.isLimitHeight()) {
            int heightLimit = portrait ? mTaskPhoto.getWidthLimit() : mTaskPhoto.getHeightLimit();
            scaler.setHeight(heightLimit);
        }

        var newDimension = scaler.getDimension();
        String imageTagFormat = "<p><img src='%s' width='%d' height='%d'></p>";

        int width = portrait ? newDimension.height : newDimension.width;
        int height = portrait ? newDimension.width : newDimension.height;

        String imageTag = String.format(imageTagFormat, getImagePath(sourceFile), width, height);

        return imageTag;
    }

    private String getExternalDescription(File file) {
        Properties p = mDirToDesc.get(file.getParent());
        final String key = FilenameUtils.getBaseName(file.getName());
        String desc = p.getProperty(key);
        if (desc == null) {
            if (mTaskDescription.isDefaultTo()) {
                if (mTaskDescription.getDefaultMode() == TaskDescription.DescriptionMode.CUSTOM) {
                    desc = mTaskDescription.getCustomValue();
                } else if (mTaskDescription.getDefaultMode() == TaskDescription.DescriptionMode.STATIC) {
                    desc = getStaticDescription();
                }
            } else {
                desc = "&nbsp;";
            }
        }

        return desc;
    }

    private Folder getFolder(File file, Date date) {
        String key;
        Folder folder = null;

        switch (mTaskFolder.getFoldersBy()) {
            case DIR:
                Path relativePath = mTaskSource.getDir().toPath().relativize(file.getParentFile().toPath());
                key = relativePath.toString();
                folder = getFolder(key);
                break;

            case DATE:
                key = mTaskFolder.getFolderDateFormat().format(date);
                folder = getFolder(key);
                break;

            case REGEX:
                key = mTaskFolder.getRegexDefault();
                Matcher matcher = mFolderByRegexPattern.matcher(file.getParent());
                if (matcher.find()) {
                    key = matcher.group();
                }
                folder = getFolder(key);
                break;

            case NONE:
                folder = mRootFolder;
                break;
        }

        return folder;
    }

    private Folder getFolder(String key) {
        key = StringUtils.replace(key, "\\", "/");
        String[] levels = StringUtils.split(key, "/");

        var parent = mRootFolder;
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
            Folder folder = parent.createAndAddFolder().withName(getSafeXmlString(name));
            mFolders.put(key, folder);
        }

        return mFolders.get(key);
    }

    private String getImagePath(File file) {
        String imageSrc;

        switch (mTaskPhoto.getReference()) {
            case ABSOLUTE:
                imageSrc = String.format("file:///%s", file.getAbsolutePath());
                break;

            case ABSOLUTE_PATH:
                imageSrc = String.format("%s%s", mTaskPhoto.getBaseUrlValue(), file.getName());
                break;

            case RELATIVE:
                var relativePath = mDestinationFile.toPath().relativize(file.toPath());
                imageSrc = StringUtils.replace(relativePath.toString(), "..", ".", 1);
                break;

            case THUMBNAIL:
                var thumbPath = mDestinationFile.toPath().relativize(mFileThumbMap.get(file).toPath());
                imageSrc = StringUtils.replace(thumbPath.toString(), "..", ".", 1);
                break;

            default:
                throw new AssertionError();
        }

        if (SystemUtils.IS_OS_WINDOWS) {
            imageSrc = imageSrc.replace("\\", "/");
        }

        if (mTaskPhoto.isForceLowerCaseExtension()) {
            String ext = FilenameUtils.getExtension(imageSrc);
            if (!StringUtils.isBlank(ext) && !StringUtils.isAllLowerCase(ext)) {
                String noExt = FilenameUtils.removeExtension(imageSrc);
                imageSrc = String.format("%s.%s", noExt, ext.toLowerCase());
            }
        }

        return imageSrc;
    }

    private String getPattern(TaskPath.SplitBy splitBy) {
        switch (splitBy) {
            case NONE:
                return "'NO_SPLIT'";
            case HOUR:
                return "yyyyMMddHH";
            case DAY:
                return "yyyyMMdd";
            case WEEK:
                return "yyyyww";
            case MONTH:
                return "yyyyMM";
            case YEAR:
                return "yyyy";
            default:
                return null;
        }
    }

    private String getPlacemarkDescription(File file, PhotoInfo photoInfo, Date exifDate) throws IOException {
        var gpsDirectory = photoInfo.getGpsDirectory();
        GpsDescriptor gpsDescriptor = null;
        if (gpsDirectory != null) {
            gpsDescriptor = new GpsDescriptor(gpsDirectory);
        }

        String desc = "";
        switch (mTaskDescription.getMode()) {
            case CUSTOM:
                desc = mTaskDescription.getCustomValue();
                break;

            case EXTERNAL:
                desc = getExternalDescription(file);
                break;

            case NONE:
                //Do nothing
                break;

            case STATIC:
                desc = getStaticDescription();
                break;
        }

        if (mTaskDescription.getMode() != TaskDescription.DescriptionMode.NONE) {
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

            desc = getSafeXmlString(desc);
        }

        return desc;
    }

    private String getPlacemarkName(File file, Date exifDate) {
        String name;

        switch (mTaskPlacemark.getNameBy()) {
            case DATE:
                try {
                    name = mTaskPlacemark.getDateFormat().format(exifDate);
                } catch (IllegalArgumentException ex) {
                    name = "invalid exif date";
                } catch (NullPointerException ex) {
                    name = "invalid exif date";
                    mInputOutput.getErr().println(file.getAbsolutePath());
                }
                break;

            case FILE:
                name = FilenameUtils.getBaseName(file.getAbsolutePath());
                break;

            case NONE:
                name = "";
                break;

            default:
                throw new AssertionError();
        }

        return name;
    }

    private String getSafeXmlString(String s) {
        if (StringUtils.containsAny(s, '<', '>', '&')) {
            s = new StringBuilder("<![CDATA[").append(s).append("]]>").toString();
        }

        return s;
    }

    private String getStaticDescription() {
        StringBuilder builder = new StringBuilder();

        if (mTaskDescription.hasPhoto()) {
            builder.append(DescriptionSegment.PHOTO.toHtml());
        }

        if (mTaskDescription.hasFilename()) {
            builder.append(DescriptionSegment.FILENAME.toHtml());
        }

        if (mTaskDescription.hasDate()) {
            builder.append(DescriptionSegment.DATE.toHtml());
        }

        if (mTaskDescription.hasCoordinate()) {
            builder.append(DescriptionSegment.COORDINATE.toHtml());
        }

        if (mTaskDescription.hasAltitude()) {
            builder.append(DescriptionSegment.ALTITUDE.toHtml());
        }

        if (mTaskDescription.hasBearing()) {
            builder.append(DescriptionSegment.BEARING.toHtml());
        }

        return builder.toString();
    }

    private boolean isUsingThumbnails() {
        return mTaskPlacemark.isSymbolAsPhoto() || mTaskPhoto.getReference() == TaskPhoto.Reference.THUMBNAIL;
    }

    private void saveToFile() {
//        mListener.onOperationLog("");
        var keys = new ArrayList(mRootFolders.keySet());
        Collections.sort(keys);

        keys.stream().forEach((key) -> {
            mRootFolder.getFeature().add(mRootFolders.get((String) key));
        });

        if (mPathFolder != null && !mPathFolder.getFeature().isEmpty()) {
            mRootFolder.getFeature().add(mPathFolder);
        }

        if (mPathGapFolder != null && !mPathGapFolder.getFeature().isEmpty()) {
            mRootFolder.getFeature().add(mPathGapFolder);
        }

        if (isUsingThumbnails()) {
//            mListener.onOperationLog("\n" + String.format(mBundle.getString("stored_thumbnails"), mThumbsDir.getAbsolutePath()));
        }

        try {
            StringWriter stringWriter = new StringWriter();
            mKml.marshal(stringWriter);
            String kmlString = stringWriter.toString();

            if (mOptions.isCleanNS2()) {
//                mListener.onOperationLog(mBundle.getString("clean_ns2"));
                kmlString = StringUtils.replace(kmlString, "xmlns:ns2=", "xmlns=");
                kmlString = StringUtils.replace(kmlString, "<ns2:", "<");
                kmlString = StringUtils.replace(kmlString, "</ns2:", "</");
            }

            if (mOptions.isCleanSpace()) {
//                mListener.onOperationLog(mBundle.getString("clean_space"));
                kmlString = StringUtils.replace(kmlString, "        ", "\t");
                kmlString = StringUtils.replace(kmlString, "    ", "\t");
            }

            kmlString = StringUtils.replaceEach(kmlString,
                    new String[]{"&lt;", "&gt;"},
                    new String[]{"<", ">"});

            if (mOptions.isLogKml()) {
//                mListener.onOperationLog("\n");
//                mListener.onOperationLog(kmlString);
//                mListener.onOperationLog("\n");
            }

//            mListener.onOperationLog(String.format(Dict.SAVING.toString(), mDestinationFile.getAbsolutePath()));
            FileUtils.writeStringToFile(mDestinationFile, kmlString, "utf-8");

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

//            String errorValue = String.valueOf(mNumOfErrors);
//            summaryBuilder.append(StringUtils.rightPad(error, rightPad)).append(":").append(StringUtils.leftPad(errorValue, leftPad)).append("\n");
//            mListener.onOperationFinished(summaryBuilder.toString(), mFiles.size());
        } catch (IOException ex) {
//            mListener.onOperationFailed(ex.getLocalizedMessage());
        }
    }

    private void scanForFolderRemoval(Folder folder) {
        for (var feature : folder.getFeature()) {
            if (feature instanceof Folder subFolder) {
                if (subFolder.getFeature().isEmpty()) {
                    mPolygonRemovals.put(subFolder, folder);
                } else {
                    scanForFolderRemoval(subFolder);
                }
            }
        }
    }

    public class FileVisitor extends SimpleFileVisitor<Path> {

        private final Properties mDefaultDescProperties = new Properties();
        private final HashMap<String, Properties> mDirToDesc;
        private final String[] mExcludePatterns;
        private final String mExternalFileValue;
        private boolean mInterrupted;
        private final PathMatcher mPathMatcher;
        private final boolean mUseExternalDescription;

        public FileVisitor() {
            mPathMatcher = mTaskSource.getPathMatcher();
            mExcludePatterns = StringUtils.split(getExcludePattern(), "::");
            mDirToDesc = getDirToDesc();

            var mode = getTaskDescription().getMode();
            mUseExternalDescription = mode == TaskDescription.DescriptionMode.EXTERNAL;
            mExternalFileValue = getTaskDescription().getExternalFileValue();

            if (mode == TaskDescription.DescriptionMode.EXTERNAL) {
                try {
                    var file = new File(mTaskSource.getDir(), mExternalFileValue);
                    if (file.isFile()) {
                        mDefaultDescProperties.load(new InputStreamReader(new FileInputStream(file), Charset.defaultCharset()));
                    }
                } catch (IOException ex) {
                    // nvm
                }
            }
        }

        public boolean isInterrupted() {
            return mInterrupted;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            if (mExcludePatterns != null) {
                for (var excludePattern : mExcludePatterns) {
                    if (IOCase.SYSTEM.isCaseSensitive()) {
                        if (StringUtils.contains(dir.toString(), excludePattern)) {
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                    } else {
                        if (StringUtils.containsIgnoreCase(dir.toString(), excludePattern)) {
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                    }
                }
            }

            String[] filePaths = dir.toFile().list();
            mInputOutput.getOut().println(dir.toString());
            if (filePaths != null && filePaths.length > 0) {
                if (mUseExternalDescription) {
                    var p = new Properties(mDefaultDescProperties);

                    try {
                        var file = new File(dir.toFile(), mExternalFileValue);
                        if (file.isFile()) {
                            p.load(new InputStreamReader(new FileInputStream(file), Charset.defaultCharset()));
                        }
                    } catch (IOException ex) {
                        // nvm
                    }

                    mDirToDesc.put(dir.toFile().getAbsolutePath(), p);
                }

                for (var fileName : filePaths) {
                    try {
                        TimeUnit.NANOSECONDS.sleep(1);
                    } catch (InterruptedException ex) {
                        mInterrupted = true;
                        return FileVisitResult.TERMINATE;
                    }

                    var file = new File(dir.toFile(), fileName);
                    if (file.isFile() && mPathMatcher.matches(file.toPath().getFileName())) {
                        boolean exclude = false;
                        if (mExcludePatterns != null) {
                            for (String excludePattern : mExcludePatterns) {
                                if (StringUtils.contains(file.getAbsolutePath(), excludePattern)) {
                                    exclude = true;
                                    break;
                                }
                            }
                        }

                        if (!exclude) {
                            mFiles.add(file);
                        }
                    }
                }
            }

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exception) {
            mInputOutput.getErr().println(file.toString());

            return FileVisitResult.CONTINUE;
        }
    }
}
