package net.furizon.backend.feature.gallery;

import java.util.regex.Pattern;

public class GalleryConstant {

    public static final String FILE_NAME_REGEX = "^[\\p{L}\\p{N}\\p{M}_\\-'()\\[\\]~. ]{2,63}$";
    public static final Pattern FILE_NAME_PATTERN = Pattern.compile(FILE_NAME_REGEX);

    public static final String GALLERY_POST_EVENT_REMIND_JTE = "gallery_post_event_reminder.jte";
}
