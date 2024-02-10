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
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;
import se.trixon.almond.nbp.output.OutputHelper;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.Scaler;
import se.trixon.almond.util.ext.GrahamScan;
import se.trixon.mapollage.core.TaskDescription.DescriptionSegment;

/**
 *
 * @author Patrik Karlström
 */
public class DocumentGenerator {

    private final BalloonStyle mBalloonStyle;
    private final Snippet mBlankSnippet = new Snippet();
    private final ResourceBundle mBundle = NbBundle.getBundle(DocumentGenerator.class);
    private final DateFormat mDateFormatDate = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
    private final File mDestinationFile;
    private final HashMap<String, Properties> mDirToDesc = new HashMap<>();
    private final Document mDocument;
    private final HashMap<File, File> mFileThumbMap = new HashMap<>();
    private final Pattern mFolderByRegexPattern;
    private final HashMap<Folder, ArrayList<Coordinate>> mFolderPolygonInputs = new HashMap<>();
    private final Map<String, Folder> mFolders = new HashMap<>();
    private Folder mImageRootFolder;
    private final InputOutput mInputOutput;
    private final Kml mKml = new Kml();
    private String mKmlString;
    private final ArrayList<LineNode> mLineNodes = new ArrayList<>();
    private int mNumOfExif;
    private int mNumOfGps;
    private int mNumOfPlacemarks;
    private final OutputHelper mOutputHelper;
    private Folder mPathFolder;
    private Folder mPathGapFolder;
    private PhotoInfo mPhotoInfo;
    private Folder mPolygonFolder;
    private final HashMap<Folder, Folder> mPolygonRemovals = new HashMap<>();
    private Folder mRootFolder;
    private final Task mTask;
    private final TaskDescription mTaskDescription;
    private final TaskFolder mTaskFolder;
    private final TaskPath mTaskPath;
    private final TaskPhoto mTaskPhoto;
    private final TaskPlacemark mTaskPlacemark;
    private final TaskSource mTaskSource;
    private File mThumbsDir;
    private final SimpleDateFormat mTimeStampDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");

    public DocumentGenerator(Task task, InputOutput inputOutput, OutputHelper outputHelper) {
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
    }

    public void addPhoto(File file) throws ImageProcessingException, IOException {
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

            var imageId = String.format("%08x", FileUtils.checksumCRC32(file));
            var styleNormalId = String.format("s_%s", imageId);
            var styleHighlightId = String.format("s_%s_hl", imageId);
            var styleMapId = String.format("m_%s", imageId);

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

            var desc = getPlacemarkDescription(file, mPhotoInfo, exifDate);
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

    public void addPolygons() {
        mPolygonFolder = KmlFactory.createFolder()
                .withName(Dict.POLYGON.toString())
                .withOpen(false);
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

    public HashMap<String, Properties> getDirToDesc() {
        return mDirToDesc;
    }

    public String getKmlString() {
        return mKmlString;
    }

    public void saveToFile(int numOfFiles) {
        if (mTask.getPath().isDrawPath() && hasPaths()) {
            addPath();

            if (mPathFolder != null && !mPathFolder.getFeature().isEmpty()) {
                mRootFolder.getFeature().add(mPathFolder);
            }

            if (mPathGapFolder != null && !mPathGapFolder.getFeature().isEmpty()) {
                mRootFolder.getFeature().add(mPathGapFolder);
            }
        }

        if (isUsingThumbnails()) {
//            mListener.onOperationLog("\n" + String.format(mBundle.getString("stored_thumbnails"), mThumbsDir.getAbsolutePath()));
        }

        try {
            var stringWriter = new StringWriter();
            mKml.marshal(stringWriter);
            mKmlString = stringWriter.toString();

            mKmlString = StringUtils.replaceEach(mKmlString,
                    new String[]{"&lt;", "&gt;"},
                    new String[]{"<", ">"});

            FileUtils.writeStringToFile(mDestinationFile, mKmlString, "utf-8");

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
            var sb = new StringBuilder("\n");

            var filesValue = String.valueOf(numOfFiles);
            sb.append(StringUtils.rightPad(files, rightPad)).append(":").append(StringUtils.leftPad(filesValue, leftPad)).append("\n");

            var exifValue = String.valueOf(mNumOfExif);
            sb.append(StringUtils.rightPad(exif, rightPad)).append(":").append(StringUtils.leftPad(exifValue, leftPad)).append("\n");

            var coordinateValue = String.valueOf(mNumOfGps);
            sb.append(StringUtils.rightPad(coordinate, rightPad)).append(":").append(StringUtils.leftPad(coordinateValue, leftPad)).append("\n");

            var placemarksValue = String.valueOf(mNumOfPlacemarks);
            sb.append(StringUtils.rightPad(placemarks, rightPad)).append(":").append(StringUtils.leftPad(placemarksValue, leftPad)).append("\n");

//            String errorValue = String.valueOf(mNumOfErrors);
//            summaryBuilder.append(StringUtils.rightPad(error, rightPad)).append(":").append(StringUtils.leftPad(errorValue, leftPad)).append("\n");
//            mListener.onOperationFinished(summaryBuilder.toString(), mFiles.size());
            mInputOutput.getOut().println(sb.toString());

        } catch (IOException ex) {
            mInputOutput.getErr().println(ex.getMessage());
        }
    }

    public void start() {
        if (isUsingThumbnails()) {
            mThumbsDir = new File(mDestinationFile.getParent() + String.format("/%s-thumbnails", FilenameUtils.getBaseName(mDestinationFile.getAbsolutePath())));
            try {
                FileUtils.forceMkdir(mThumbsDir);
            } catch (IOException ex) {
                mInputOutput.getErr().println(ex.getMessage());
            }
        }
        mRootFolder = mDocument.createAndAddFolder().withName(getSafeXmlString(mTask.getName())).withOpen(true);
        mImageRootFolder = mRootFolder.createAndAddFolder().withName(Dict.IMAGES.toString());

        var href = "<a href=\"https://trixon.se/mapollage/\">Mapollage</a>";
        var description = "%s<p>%s %s, %s</p>".formatted(
                mTask.getDescriptionString().replaceAll("\\n", "<br />"),
                Dict.MADE_WITH.toString(),
                href,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH.mm.ss")));
        mRootFolder.setDescription(getSafeXmlString(description));

//        mListener.onOperationProcessingStarted();
    }

    private void addPath() {
        Collections.sort(mLineNodes, Comparator.comparing(LineNode::getDate));

        mPathFolder = KmlFactory.createFolder().withName(Dict.Geometry.PATH.toString());
        mPathGapFolder = KmlFactory.createFolder().withName(Dict.Geometry.PATH_GAP.toString());

        var pattern = getPattern(mTaskPath.getSplitBy());
        var dateFormat = new SimpleDateFormat(pattern);
        var map = new TreeMap<String, ArrayList<LineNode>>();

        mLineNodes.forEach(node -> {
            var key = dateFormat.format(node.getDate());
            map.computeIfAbsent(key, k -> new ArrayList<>()).add(node);
        });

        //Add paths
        for (var nodes : map.values()) {
            if (nodes.size() > 1) {
                var pathPlacemark = mPathFolder
                        .createAndAddPlacemark()
                        .withName(LineNode.getName(nodes));
                var pathStyle = pathPlacemark.createAndAddStyle();
                pathStyle.createAndSetLineStyle()
                        .withColor("ff0000ff")
                        .withWidth(mTaskPath.getWidth());

                var line = pathPlacemark
                        .createAndSetLineString()
                        .withExtrude(false)
                        .withTessellate(true);

                nodes.forEach(node -> {
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

                var prevLast = previousNodes.get(previousNodes.size() - 1);
                var currentFirst = nodes.get(0);

                line.addToCoordinates(prevLast.getLon(), prevLast.getLat());
                line.addToCoordinates(currentFirst.getLon(), currentFirst.getLat());
            }

            previousNodes = nodes;
        }
    }

    private void addPolygon(String name, ArrayList<Coordinate> coordinates, Folder polygonFolder) {
        var inputs = new ArrayList<Point2D.Double>();
        coordinates.forEach(coordinate -> {
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

            convexHull.forEach(node -> {
                linearRing.addToCoordinates(node.x, node.y);

            });
        } catch (IllegalArgumentException e) {
            System.err.println(e);
        }
    }

    private void addPolygons(Folder polygonParent, List<Feature> features) {
        for (var feature : features) {
            if (feature instanceof Folder folder) {
                if (folder != mPathFolder && folder != mPathGapFolder && folder != mPolygonFolder) {
                    var polygonFolder = polygonParent.createAndAddFolder().withName(folder.getName()).withOpen(true);
                    mFolderPolygonInputs.put(polygonFolder, new ArrayList<>());
                    addPolygons(polygonFolder, folder.getFeature());

                    if (mFolderPolygonInputs.get(polygonFolder) != null) {
                        addPolygon(folder.getName(), mFolderPolygonInputs.get(polygonFolder), polygonParent);
                    }
                }
            }

            if (feature instanceof Placemark placemark) {
                var point = (Point) placemark.getGeometry();
                mFolderPolygonInputs.computeIfAbsent(polygonParent, k -> new ArrayList<>()).addAll(point.getCoordinates());
            }
        }

        var rootCoordinates = mFolderPolygonInputs.get(mPolygonFolder);
        if (polygonParent == mPolygonFolder && rootCoordinates != null) {
            addPolygon(mPolygonFolder.getName(), rootCoordinates, polygonParent);
        }
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
        var imageTagFormat = "<p><img src='%s' width='%d' height='%d'></p>";

        int width = portrait ? newDimension.height : newDimension.width;
        int height = portrait ? newDimension.width : newDimension.height;

        String imageTag = String.format(imageTagFormat, getImagePath(sourceFile), width, height);

        return imageTag;
    }

    private String getExternalDescription(File file) {
        var p = mDirToDesc.get(file.getParent());
        var key = FilenameUtils.getBaseName(file.getName());
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
            case DIR -> {
                var relativePath = mTaskSource.getDir().toPath().relativize(file.getParentFile().toPath());
                key = relativePath.toString();
                folder = getFolder(key);
            }

            case DATE -> {
                key = mTaskFolder.getFolderDateFormat().format(date);
                folder = getFolder(key);
            }

            case REGEX -> {
                key = mTaskFolder.getRegexDefault();
                var matcher = mFolderByRegexPattern.matcher(file.getParent());
                if (matcher.find()) {
                    key = matcher.group();
                }
                folder = getFolder(key);
            }

            case NONE ->
                folder = mImageRootFolder;
        }

        return folder;
    }

    private Folder getFolder(String key) {
        key = StringUtils.replace(key, "\\", "/");
        var levels = StringUtils.split(key, "/");

        var parent = mImageRootFolder;
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
        return mFolders.computeIfAbsent(key, k -> parent.createAndAddFolder().withName(getSafeXmlString(name)));
    }

    private String getImagePath(File file) {
        String imageSrc;

        switch (mTaskPhoto.getReference()) {
            case ABSOLUTE ->
                imageSrc = String.format("file:///%s", file.getAbsolutePath());

            case ABSOLUTE_PATH ->
                imageSrc = String.format("%s%s", mTaskPhoto.getBaseUrlValue(), file.getName());

            case RELATIVE -> {
                var relativePath = mDestinationFile.toPath().relativize(file.toPath());
                imageSrc = StringUtils.replace(relativePath.toString(), "..", ".", 1);
            }

            case THUMBNAIL -> {
                var thumbPath = mDestinationFile.toPath().relativize(mFileThumbMap.get(file).toPath());
                imageSrc = StringUtils.replace(thumbPath.toString(), "..", ".", 1);
            }

            default ->
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
        return switch (splitBy) {
            case NONE ->
                "'NO_SPLIT'";
            case HOUR ->
                "yyyyMMddHH";
            case DAY ->
                "yyyyMMdd";
            case WEEK ->
                "yyyyww";
            case MONTH ->
                "yyyyMM";
            case YEAR ->
                "yyyy";
            default ->
                null;
        };
    }

    private String getPlacemarkDescription(File file, PhotoInfo photoInfo, Date exifDate) throws IOException {
        var gpsDirectory = photoInfo.getGpsDirectory();
        GpsDescriptor gpsDescriptor = null;
        if (gpsDirectory != null) {
            gpsDescriptor = new GpsDescriptor(gpsDirectory);
        }

        String desc = "";
        switch (mTaskDescription.getMode()) {
            case CUSTOM ->
                desc = mTaskDescription.getCustomValue();

            case EXTERNAL ->
                desc = getExternalDescription(file);

            case NONE -> {
            }

            case STATIC ->
                desc = getStaticDescription();
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
            case DATE -> {
                try {
                    name = mTaskPlacemark.getDateFormat().format(exifDate);
                } catch (IllegalArgumentException ex) {
                    name = "invalid exif date";
                } catch (NullPointerException ex) {
                    name = "invalid exif date";
                    mInputOutput.getErr().println(file.getAbsolutePath());
                }
            }

            case FILE ->
                name = FilenameUtils.getBaseName(file.getAbsolutePath());

            case NONE ->
                name = "";

            default ->
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
        var builder = new StringBuilder();

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

    private boolean hasPaths() {
        return mLineNodes.size() > 1;
    }

    private boolean isUsingThumbnails() {
        return mTaskPlacemark.isSymbolAsPhoto() || mTaskPhoto.getReference() == TaskPhoto.Reference.THUMBNAIL;
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

}
