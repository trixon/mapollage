/*
 * Copyright 2018 Patrik Karlsson.
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
import de.micromata.opengis.kml.v_2_2_0.BalloonStyle;
import de.micromata.opengis.kml.v_2_2_0.Boundary;
import de.micromata.opengis.kml.v_2_2_0.ColorMode;
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Icon;
import de.micromata.opengis.kml.v_2_2_0.IconStyle;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.LineString;
import de.micromata.opengis.kml.v_2_2_0.LineStyle;
import de.micromata.opengis.kml.v_2_2_0.LinearRing;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
import de.micromata.opengis.kml.v_2_2_0.PolyStyle;
import de.micromata.opengis.kml.v_2_2_0.Polygon;
import de.micromata.opengis.kml.v_2_2_0.Style;
import de.micromata.opengis.kml.v_2_2_0.StyleState;
import de.micromata.opengis.kml.v_2_2_0.TimeStamp;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
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
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.Scaler;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.ext.GrahamScan;
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

    private static final Logger LOGGER = Logger.getLogger(Operation.class.getName());

    private final BalloonStyle mBalloonStyle;
    private final ResourceBundle mBundle;
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
    private final OperationListener mListener;
    private int mNumOfErrors = 0;
    private int mNumOfExif;
    private int mNumOfGps;
    private int mNumOfPlacemarks;
    private Options mOptions = Options.getInstance();
    private Folder mPathFolder;
    private Folder mPathGapFolder;
    private PhotoInfo mPhotoInfo;
    private Folder mPolygonFolder;
    private final HashMap<Folder, Folder> mPolygonRemovals = new HashMap<>();
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

        mBundle = SystemHelper.getBundle(Operation.class, "Bundle");
        mDocument = mKml.createAndSetDocument().withOpen(true);
        mBalloonStyle = KmlFactory.createBalloonStyle()
                .withId("BalloonStyleId")
                //aabbggrr
                .withBgColor("ff272420")
                .withTextColor("ffeeeeee")
                .withText("$[description]");
    }

    @Override
    public void run() {
        if (!Files.isWritable(mDestinationFile.getParentFile().toPath())) {
            mListener.onOperationLog(String.format(mBundle.getString("insufficient_privileges"), mDestinationFile.getAbsolutePath()));
            Thread.currentThread().interrupt();
            mListener.onOperationInterrupted();
            return;
        }

        mStartTime = System.currentTimeMillis();
        Date date = new Date(mStartTime);
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        mListener.onOperationStarted();
        mListener.onOperationLog(new SimpleDateFormat().format(new Date()));

        String status;
        mRootFolder = mDocument.createAndAddFolder().withName(getSafeXmlString(mProfileFolder.getRootName())).withOpen(true);

        String href = "<a href=\"https://trixon.se/mapollage/\">Mapollage</a>";
        String description = String.format("<p>%s %s, %s</p>%s",
                Dict.MADE_WITH.toString(),
                href,
                dateFormat.format(date),
                mProfileFolder.getRootDescription().replaceAll("\\n", "<br />"));
        mRootFolder.setDescription(getSafeXmlString(description));

        mListener.onOperationProcessingStarted();
        try {
            mInterrupted = !generateFileList();
        } catch (IOException ex) {
            logError(ex.getMessage());
        }

        if (!mInterrupted && !mFiles.isEmpty()) {
            if (mProfilePlacemark.isSymbolAsPhoto()) {
                mThumbsDir = new File(mDestinationFile.getParent() + String.format("/%s-thumbnails", FilenameUtils.getBaseName(mDestinationFile.getAbsolutePath())));
                try {
                    FileUtils.forceMkdir(mThumbsDir);
                } catch (IOException ex) {
                    logError(String.format("E000 %s", ex.getMessage()));
                }
            }

            mListener.onOperationLog(String.format(mBundle.getString("found_count"), mFiles.size()));
            mListener.onOperationLog("");
            mListener.onOperationProgressInit(mFiles.size());

            for (File file : mFiles) {
                mListener.onOperationProgress(file.getAbsolutePath());

                try {
                    addPhoto(file);
                } catch (ImageProcessingException ex) {
                    logError(String.format("E000 %s", ex.getMessage()));
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
            addPolygons();
            saveToFile();
        }

        if (mNumOfErrors > 0) {
            logError(mBundle.getString("error_description"));
        }
    }

    private void addPath() {
        Collections.sort(mLineNodes, (LineNode o1, LineNode o2) -> o1.getDate().compareTo(o2.getDate()));

        mPathFolder = KmlFactory.createFolder().withName(Dict.PATH_GFX.toString());
        mPathGapFolder = KmlFactory.createFolder().withName(Dict.PATH_GAP_GFX.toString());

        String pattern = getPattern(mProfilePath.getSplitBy());
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
        for (ArrayList<LineNode> nodes : map.values()) {
            if (nodes.size() > 1) {
                Placemark path = mPathFolder.createAndAddPlacemark()
                        .withName(LineNode.getName(nodes));

                Style pathStyle = path.createAndAddStyle();
                pathStyle.createAndSetLineStyle()
                        .withColor("ff0000ff")
                        .withWidth(mProfilePath.getWidth());

                LineString line = path
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
        for (ArrayList<LineNode> nodes : map.values()) {
            if (previousNodes != null) {
                Placemark path = mPathGapFolder.createAndAddPlacemark()
                        .withName(LineNode.getName(previousNodes, nodes));

                Style pathStyle = path.createAndAddStyle();
                pathStyle.createAndSetLineStyle()
                        .withColor("ff00ffff")
                        .withWidth(mProfilePath.getWidth());

                LineString line = path
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
            String styleNormalId = String.format("s_%s", imageId);
            String styleHighlightId = String.format("s_%s_hl", imageId);
            String styleMapId = String.format("m_%s", imageId);
            double highlightZoom = mProfilePlacemark.getZoom() * mProfilePlacemark.getScale();

            Style normalStyle = mDocument
                    .createAndAddStyle()
                    .withId(styleNormalId);

            IconStyle normalIconStyle = normalStyle
                    .createAndSetIconStyle()
                    .withScale(mProfilePlacemark.getScale());

            Style highlightStyle = mDocument
                    .createAndAddStyle()
                    .withBalloonStyle(mBalloonStyle)
                    .withId(styleHighlightId);

            IconStyle highlightIconStyle = highlightStyle
                    .createAndSetIconStyle()
                    .withScale(highlightZoom);

            if (mProfilePlacemark.isSymbolAsPhoto()) {
                Icon icon = KmlFactory.createIcon().withHref(String.format("%s/%s.jpg", mThumbsDir.getName(), imageId));
                normalIconStyle.setIcon(icon);
                highlightIconStyle.setIcon(icon);
            }

            if (mProfilePlacemark.isSymbolAsPhoto() || mProfilePhoto.getReference() == ProfilePhoto.Reference.THUMBNAIL) {
                File thumbFile = new File(mThumbsDir, imageId + ".jpg");
                mFileThumbMap.put(file, thumbFile);
                if (Files.isWritable(thumbFile.getParentFile().toPath())) {
                    mPhotoInfo.createThumbnail(thumbFile);
                } else {
                    mListener.onOperationLog(String.format(mBundle.getString("insufficient_privileges"), mDestinationFile.getAbsolutePath()));
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            mDocument.createAndAddStyleMap().withId(styleMapId)
                    .addToPair(KmlFactory.createPair().withKey(StyleState.NORMAL).withStyleUrl("#" + styleNormalId))
                    .addToPair(KmlFactory.createPair().withKey(StyleState.HIGHLIGHT).withStyleUrl("#" + styleHighlightId));

            Placemark placemark = KmlFactory.createPlacemark()
                    .withName(getSafeXmlString(getPlacemarkName(file, exifDate)))
                    .withOpen(Boolean.TRUE)
                    .withStyleUrl("#" + styleMapId);

            String desc = getPlacemarkDescription(file, mPhotoInfo, exifDate);
            if (!StringUtils.isBlank(desc)) {
                placemark.setDescription(desc);
            }

            placemark.createAndSetPoint()
                    .addToCoordinates(mPhotoInfo.getLon(), mPhotoInfo.getLat(), 0F);

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

    private void addPolygon(String name, ArrayList<Coordinate> coordinates, Folder polygonFolder) {
        List<Point2D.Double> inputs = new ArrayList<>();
        coordinates.forEach((coordinate) -> {
            inputs.add(new Point2D.Double(coordinate.getLongitude(), coordinate.getLatitude()));
        });

        try {
            List<Point2D.Double> convexHull = GrahamScan.getConvexHullDouble(inputs);
            Placemark placemark = polygonFolder
                    .createAndAddPlacemark()
                    .withName(name);

            Style style = placemark.createAndAddStyle();
            LineStyle lineStyle = style.createAndSetLineStyle()
                    .withColor("00000000")
                    .withWidth(0.0);

            PolyStyle polyStyle = style.createAndSetPolyStyle()
                    .withColor("ccffffff")
                    .withColorMode(ColorMode.RANDOM);

            Polygon polygon = placemark.createAndSetPolygon();
            Boundary boundary = polygon.createAndSetOuterBoundaryIs();
            LinearRing linearRing = boundary.createAndSetLinearRing();

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

        for (Folder folder : mPolygonRemovals.keySet()) {
            Folder parentFolder = mPolygonRemovals.get(folder);
            parentFolder.getFeature().remove(folder);
        }

        mRootFolder.getFeature().add(mPolygonFolder);
    }

    private void addPolygons(Folder polygonParent, List<Feature> features) {
        for (Feature feature : features) {
            if (feature instanceof Folder) {
                Folder folder = (Folder) feature;

                if (folder != mPathFolder && folder != mPathGapFolder && folder != mPolygonFolder) {
                    System.out.println("ENTER FOLDER=" + folder.getName());
                    System.out.println("PARENT FOLDER=" + polygonParent.getName());
                    Folder polygonFolder = polygonParent.createAndAddFolder().withName(folder.getName()).withOpen(true);
                    mFolderPolygonInputs.put(polygonFolder, new ArrayList<>());
                    addPolygons(polygonFolder, folder.getFeature());
                    System.out.println("POLYGON FOLDER=" + polygonFolder.getName() + " CONTAINS");

                    if (mFolderPolygonInputs.get(polygonFolder) != null) {
                        addPolygon(folder.getName(), mFolderPolygonInputs.get(polygonFolder), polygonParent);
                    }
                    System.out.println("EXIT FOLDER=" + folder.getName());
                    System.out.println("");
                }
            }

            if (feature instanceof Placemark) {
                Placemark placemark = (Placemark) feature;
                System.out.println("PLACEMARK=" + placemark.getName() + "(PARENT=)" + polygonParent.getName());

                Point point = (Point) placemark.getGeometry();
                point.getCoordinates().forEach((coordinate) -> {
                    mFolderPolygonInputs.get(polygonParent).add(coordinate);
                });

            }
        }
    }

    private boolean generateFileList() throws IOException {
        mListener.onOperationLog("");
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
            mListener.onOperationFinished(Dict.FILELIST_EMPTY.toString(), 0);
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

    private String getExternalDescription(File file) {
        Properties p = mDirToDesc.get(file.getParent());
        final String key = FilenameUtils.getBaseName(file.getName());
        String desc = p.getProperty(key);
        if (desc == null) {
            if (mProfileDescription.isDefaultTo()) {
                if (mProfileDescription.getDefaultMode() == ProfileDescription.DescriptionMode.CUSTOM) {
                    desc = mProfileDescription.getCustomValue();
                } else if (mProfileDescription.getDefaultMode() == ProfileDescription.DescriptionMode.STATIC) {
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

        switch (mProfileFolder.getFoldersBy()) {
            case DIR:
                Path relativePath = mProfileSource.getDir().toPath().relativize(file.getParentFile().toPath());
                key = relativePath.toString();
                folder = getFolder(key);
                break;

            case DATE:
                key = mProfileFolder.getFolderDateFormat().format(date);
                folder = getFolder(key);
                break;

            case REGEX:
                key = mProfileFolder.getRegexDefault();
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
            Folder folder = parent.createAndAddFolder().withName(getSafeXmlString(name));
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

    private String getPattern(ProfilePath.SplitBy splitBy) {
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
        GpsDirectory gpsDirectory = photoInfo.getGpsDirectory();
        GpsDescriptor gpsDescriptor = null;
        if (gpsDirectory != null) {
            gpsDescriptor = new GpsDescriptor(gpsDirectory);
        }

        String desc = "";
        switch (mProfileDescription.getMode()) {
            case CUSTOM:
                desc = mProfileDescription.getCustomValue();
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

        if (mProfileDescription.getMode() != ProfileDescription.DescriptionMode.NONE) {
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

        switch (mProfilePlacemark.getNameBy()) {
            case DATE:
                try {
                    name = mProfilePlacemark.getDateFormat().format(exifDate);
                } catch (IllegalArgumentException ex) {
                    name = "invalid exif date";
                } catch (NullPointerException ex) {
                    name = "invalid exif date";
                    logError(String.format("E011 %s", file.getAbsolutePath()));
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

        if (mPathFolder != null && !mPathFolder.getFeature().isEmpty()) {
            mRootFolder.getFeature().add(mPathFolder);
        }

        if (mPathGapFolder != null && !mPathGapFolder.getFeature().isEmpty()) {
            mRootFolder.getFeature().add(mPathGapFolder);
        }

        if (mProfilePlacemark.isSymbolAsPhoto()) {
            mListener.onOperationLog("\n" + String.format(mBundle.getString("stored_thumbnails"), mThumbsDir.getAbsolutePath()));
        }

        try {
            StringWriter stringWriter = new StringWriter();
            mKml.marshal(stringWriter);
            String kmlString = stringWriter.toString();

            if (mOptions.isCleanNs2()) {
                mListener.onOperationLog(mBundle.getString("clean_ns2"));
                kmlString = StringUtils.replace(kmlString, "xmlns:ns2=", "xmlns=");
                kmlString = StringUtils.replace(kmlString, "<ns2:", "<");
                kmlString = StringUtils.replace(kmlString, "</ns2:", "</");
            }

            if (mOptions.isCleanSpace()) {
                mListener.onOperationLog(mBundle.getString("clean_space"));
                kmlString = StringUtils.replace(kmlString, "        ", "\t");
                kmlString = StringUtils.replace(kmlString, "    ", "\t");
            }

            kmlString = StringUtils.replaceEach(kmlString,
                    new String[]{"&lt;", "&gt;", "&amp;"},
                    new String[]{"<", ">", ""});

            if (mOptions.isLogKml()) {
                mListener.onOperationLog("\n");
                mListener.onOperationLog(kmlString);
                mListener.onOperationLog("\n");
            }

            mListener.onOperationLog(String.format(Dict.SAVING.toString(), mDestinationFile.getAbsolutePath()));
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

            String errorValue = String.valueOf(mNumOfErrors);
            summaryBuilder.append(StringUtils.rightPad(error, rightPad)).append(":").append(StringUtils.leftPad(errorValue, leftPad)).append("\n");

            String timeValue = String.valueOf(Math.round((System.currentTimeMillis() - mStartTime) / 1000.0));
            summaryBuilder.append(StringUtils.rightPad(time, rightPad)).append(":").append(StringUtils.leftPad(timeValue, leftPad)).append(" s").append("\n");

            mListener.onOperationFinished(summaryBuilder.toString(), mFiles.size());
        } catch (IOException ex) {
            mListener.onOperationFailed(ex.getLocalizedMessage());
        }
    }

    private void scanForFolderRemoval(Folder folder) {
        for (Feature feature : folder.getFeature()) {
            if (feature instanceof Folder) {
                Folder subFolder = (Folder) feature;
                if (subFolder.getFeature().isEmpty()) {
                    mPolygonRemovals.put(subFolder, folder);
                } else {
                    scanForFolderRemoval(subFolder);
                }
            }
        }
    }
//    private boolean scanForFolderRemoval(Folder folder, boolean hadEmpty) {
//        for (Feature feature : folder.getFeature()) {
//            if (feature instanceof Folder) {
//                Folder subFolder = (Folder) feature;
//                if (subFolder.getFeature().isEmpty()) {
//                    mPolygonRemovals.put(subFolder, folder);
//                    hadEmpty = true;
//                } else {
//                    scanForFolderRemoval(subFolder, hadEmpty);
//                }
//            }
//        }
//
//        return hadEmpty;
//    }

    HashMap<String, Properties> getDirToDesc() {
        return mDirToDesc;
    }

    String getExcludePattern() {
        return mProfileSource.getExcludePattern();
    }

    OperationListener getListener() {
        return mListener;
    }

    ProfileDescription getProfileDescription() {
        return mProfileDescription;
    }

    void logError(String message) {
        mNumOfErrors++;
        mListener.onOperationError(message);
    }
}
