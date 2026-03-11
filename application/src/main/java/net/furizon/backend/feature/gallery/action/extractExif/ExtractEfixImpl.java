package net.furizon.backend.feature.gallery.action.extractExif;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.annotations.NotNull;
import com.drew.lang.annotations.Nullable;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDescriptor;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.furizon.backend.feature.gallery.dto.GalleryProcessorUploadData;
import net.furizon.backend.feature.gallery.dto.UploadImageMetadata;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.TimeZone;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExtractEfixImpl implements ExtractExif {
    private static final ZoneId GMT =  ZoneId.of("GMT");

    @Override
    public void parseExif(@NotNull String path, @NotNull GalleryProcessorUploadData resultObj) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(new File(path));

            for (Directory directory : metadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    log.info("[{}] {} {}: {}", tag.getDirectoryName(), tag.getTagTypeHex(), tag.getTagName(), tag.getDescription());
                }
            }

            String cameraMaker = null;
            String cameraModel = null;
            String lensMaker = null;
            String lensModel = null;
            String focal = null;
            String shutter = null;
            String aperture = null;
            String iso = null;
            LocalDateTime shotTime = null;

            //var baseDir = metadata.getFirstDirectoryOfType(ExifDirectoryBase.class);
            //if (baseDir != null) {
            //    baseDir.getString(ExifDirectoryBase.TAG_LENS_MODEL);
            //}

            var subIfdDir = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (subIfdDir != null) {
                var subIfdDesc = new ExifSubIFDDescriptor(subIfdDir);

                focal = subIfdDesc.getFocalLengthDescription();
                shutter = subIfdDesc.getShutterSpeedDescription();
                aperture = subIfdDesc.getApertureValueDescription();
                iso = subIfdDesc.getIsoEquivalentDescription();

                TimeZone zone = getTimeZone(subIfdDir);
                ZoneId zoneId = zone == null ? GMT : zone.toZoneId();
                var date = subIfdDir.getDateOriginal();
                if (date != null) {
                    shotTime = LocalDateTime.ofInstant(date.toInstant(), zoneId);
                }

                lensMaker = subIfdDir.getString(ExifIFD0Directory.TAG_LENS_MAKE);
                lensModel = subIfdDir.getString(ExifIFD0Directory.TAG_LENS_MODEL);
            }

            var ifd0Dir = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (ifd0Dir != null) {
                //var ifd0Desc = new ExifIFD0Descriptor(ifd0Dir);

                cameraMaker = ifd0Dir.getString(ExifIFD0Directory.TAG_MAKE);
                cameraModel = ifd0Dir.getString(ExifIFD0Directory.TAG_MODEL);

                lensMaker = lensMaker != null ? lensMaker : ifd0Dir.getString(ExifIFD0Directory.TAG_LENS_MAKE);
                lensModel = lensModel != null ? lensModel : ifd0Dir.getString(ExifIFD0Directory.TAG_LENS_MODEL);

            }

            if (cameraMaker == null
                    && cameraModel == null
                    && lensMaker == null
                    && lensModel == null
                    && focal == null
                    && shutter == null
                    && aperture == null
                    && iso == null
                    && shotTime == null) {
                return;
            }

            var obj = UploadImageMetadata.builder()
                    .cameraMaker(cameraMaker)
                    .cameraModel(cameraModel)
                    .lensMaker(lensMaker)
                    .lensModel(lensModel)
                    .focal(focal)
                    .shutter(shutter)
                    .aperture(aperture)
                    .iso(iso)
                .build();

            resultObj.setPhotoMetadata(obj);
            resultObj.setShotTimestamp(shotTime);

        } catch (IOException e) {
            log.warn("Error while reading uploaded file");
        } catch (ImageProcessingException e) {
            log.warn("Error while parsing uploaded file's metadata");
        }
    }

    @Nullable
    private TimeZone getTimeZone(ExifSubIFDDirectory directory) {
        String timeOffset = directory.getString(ExifSubIFDDirectory.TAG_TIME_ZONE_ORIGINAL);
        return timeOffset != null && timeOffset.matches("[\\+\\-]\\d\\d:\\d\\d") ? TimeZone.getTimeZone("GMT" + timeOffset) : null;
    }
}
