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

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.TimeZone;
import javax.imageio.ImageIO;
import se.trixon.almond.util.GraphicsHelper;
import se.trixon.almond.util.ImageScaler;

/**
 *
 * @author Patrik Karlsson
 */
public class PhotoInfo {

    private final ExifSubIFDDirectory mExifDirectory;
    private final File mFile;
    private final double mFormat = 1000000;
    private final GeoLocation mGeoLocation;
    private final GpsDirectory mGpsDirectory;
    private final ImageScaler mImageScaler = ImageScaler.getInstance();
    private final Metadata mMetadata;
    private final Options mOptions = Options.getInstance();
    private Dimension mOriginalDimension = null;

    public PhotoInfo(File file) throws ImageProcessingException, IOException {
        mFile = file;
        mMetadata = ImageMetadataReader.readMetadata(file);
        mExifDirectory = mMetadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        mGpsDirectory = mMetadata.getFirstDirectoryOfType(GpsDirectory.class);
        mGeoLocation = getGeoLocation();
    }

    public Dimension getOriginalDimension() {
        if (mOriginalDimension == null) {
            try {
                mOriginalDimension = GraphicsHelper.getImgageDimension(mFile);
            } catch (IOException ex) {
                System.err.println(ex.getLocalizedMessage());
            }

            if (mOriginalDimension == null) {
                mOriginalDimension = new Dimension(200, 200);
            }
        }

        return mOriginalDimension;
    }

    public void createThumbnail(File dest) throws IOException {
        if (!dest.exists()) {
            int borderSize = mOptions.getThumbnailBorderSize();
            int thumbnailSize = mOptions.getThumbnailSize();
            BufferedImage scaledImage = mImageScaler.getScaledImage(mFile, new Dimension(thumbnailSize - borderSize * 2, thumbnailSize - borderSize * 2));

            int width = scaledImage.getWidth();
            int height = scaledImage.getHeight();
            int borderedImageWidth = width + borderSize * 2;
            int borderedImageHeight = height + borderSize * 2;

            BufferedImage borderedImage = new BufferedImage(borderedImageWidth, borderedImageHeight, BufferedImage.TYPE_3BYTE_BGR);

            Graphics2D g2d = borderedImage.createGraphics();
            g2d.setColor(Color.YELLOW);
            g2d.fillRect(0, 0, borderedImageWidth, borderedImageHeight);
            g2d.drawImage(scaledImage, borderSize, borderSize, width + borderSize, height + borderSize, 0, 0, width, height, Color.YELLOW, null);

            ImageIO.write(borderedImage, "jpg", dest);
        }
    }

    public Date getDate() {
        Date date;

        if (mExifDirectory.containsTag(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL)) {
            date = mExifDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL, TimeZone.getDefault());
        } else {
            long millis = 0;
            try {
                BasicFileAttributes attr = Files.readAttributes(mFile.toPath(), BasicFileAttributes.class);
                millis = attr.lastModifiedTime().toMillis();
            } catch (IOException ex) {
                millis = mFile.lastModified();
            } finally {
                date = new Date(millis);
            }
        }

        return date;
    }

    public ExifSubIFDDirectory getExifDirectory() {
        return mExifDirectory;
    }

    public GpsDirectory getGpsDirectory() {
        return mGpsDirectory;
    }

    public double getLat() {
        int latInt = (int) (mGeoLocation.getLatitude() * mFormat);

        return latInt / mFormat;
    }

    public double getLon() {
        int lonInt = (int) (mGeoLocation.getLongitude() * mFormat);

        return lonInt / mFormat;
    }

    public Metadata getMetadata() {
        return mMetadata;
    }

    public boolean hasExif() {
        return mExifDirectory != null;
    }

    public boolean hasGps() {
        return hasExif() && mGpsDirectory != null;
    }

    private GeoLocation getGeoLocation() throws ImageProcessingException {
        GeoLocation geoLocation = null;

        geoLocation = new GeoLocation(mOptions.getDefaultLat(), mOptions.getDefaultLon());

        if (mGpsDirectory != null) {
            geoLocation = mGpsDirectory.getGeoLocation();
            if (geoLocation == null) {
                throw new ImageProcessingException(mFile.getAbsolutePath());
            }

            if (geoLocation.isZero()) {
                geoLocation = new GeoLocation(mOptions.getDefaultLat(), mOptions.getDefaultLon());
//                gpsDirectory = null;
            }
        }

        return geoLocation;
    }
}
