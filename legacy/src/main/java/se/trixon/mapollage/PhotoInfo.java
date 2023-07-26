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

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
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
 * @author Patrik Karlström
 */
public class PhotoInfo {

    private ExifSubIFDDirectory mExifDirectory;
    private final File mFile;
    private final double mFormat = 1000000;
    private GeoLocation mGeoLocation;
    private GpsDirectory mGpsDirectory;
    private final ImageScaler mImageScaler = ImageScaler.getInstance();
    private final boolean mIncludeNullCoordinate;
    private Metadata mMetadata;
    private final Options mOptions = Options.getInstance();
    private int mOrientation;
    private Dimension mOriginalDimension = null;

    public PhotoInfo(File file, boolean includeNullCoordinate) {
        mFile = file;
        mIncludeNullCoordinate = includeNullCoordinate;
    }

    public void createThumbnail(File dest) throws IOException {
        if (!dest.exists()) {
            int borderSize = mOptions.getThumbnailBorderSize();
            int thumbnailSize = mOptions.getThumbnailSize();

            BufferedImage scaledImage = mImageScaler.getScaledImage(mFile, new Dimension(thumbnailSize - borderSize * 2, thumbnailSize - borderSize * 2));
            scaledImage = GraphicsHelper.rotate(scaledImage, mOrientation);

            int width = scaledImage.getWidth();
            int height = scaledImage.getHeight();
            int borderedImageWidth = width + borderSize * 2;
            int borderedImageHeight = height + borderSize * 2;

            BufferedImage borderedImage = new BufferedImage(borderedImageWidth, borderedImageHeight, BufferedImage.TYPE_3BYTE_BGR);

            Graphics2D g2 = borderedImage.createGraphics();
            g2.setColor(Color.YELLOW);
            g2.fillRect(0, 0, borderedImageWidth, borderedImageHeight);
            g2.drawImage(scaledImage, borderSize, borderSize, width + borderSize, height + borderSize, 0, 0, width, height, Color.YELLOW, null);

            try {
                ImageIO.write(borderedImage, "jpg", dest);
            } catch (IOException ex) {
                throw new IOException(String.format("E000 %s", dest.getAbsolutePath()));
            }
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

    public int getOrientation() {
        return mOrientation;
    }

    public Dimension getOriginalDimension() throws IOException {
        if (mOriginalDimension == null) {
            try {
                mOriginalDimension = GraphicsHelper.getImgageDimension(mFile);
            } catch (IOException ex) {
                throw new IOException(String.format("E000 %s", mFile.getAbsolutePath()));
            }

            if (mOriginalDimension == null) {
                mOriginalDimension = new Dimension(200, 200);
            }
        }

        return mOriginalDimension;
    }

    public boolean hasExif() {
        return mExifDirectory != null;
    }

    public boolean hasGps() {
        return hasExif() && mGpsDirectory != null;
    }

    public void init() throws ImageProcessingException, IOException {
        try {
            mMetadata = ImageMetadataReader.readMetadata(mFile);
            mExifDirectory = mMetadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            mGpsDirectory = mMetadata.getFirstDirectoryOfType(GpsDirectory.class);
            mGeoLocation = getGeoLocation();

            try {
                ExifIFD0Directory rotationDirectory = mMetadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
                mOrientation = rotationDirectory.getInt(ExifSubIFDDirectory.TAG_ORIENTATION);
            } catch (MetadataException | NullPointerException ex) {
                mOrientation = 1;
            }
        } catch (IOException ex) {
            throw new IOException(String.format("E000 %s", mFile.getAbsolutePath()));
        }
    }

    public boolean isZeroCoordinate() {
        return mGpsDirectory == null || mGpsDirectory.getGeoLocation() == null || mGpsDirectory.getGeoLocation().isZero();
    }

    private GeoLocation getGeoLocation() throws ImageProcessingException {
        GeoLocation geoLocation = null;

        if (mIncludeNullCoordinate) {
            try {
                geoLocation = mGpsDirectory.getGeoLocation();
                if (geoLocation.isZero()) {
                    geoLocation = new GeoLocation(mOptions.getDefaultLat(), mOptions.getDefaultLon());
                }
            } catch (Exception e) {
                //never error
            }
        } else {
            try {
                geoLocation = mGpsDirectory.getGeoLocation();
                geoLocation.isZero(); //Just force a reference
            } catch (NullPointerException e) {
                throw new ImageProcessingException(String.format("E012 %s", mFile.getAbsolutePath()));
            }
        }

        if (geoLocation == null) {
            geoLocation = new GeoLocation(mOptions.getDefaultLat(), mOptions.getDefaultLon());

        }
        return geoLocation;
    }
}
