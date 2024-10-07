package net.furizon.backend.service.users.content;

public class TagService {
    public enum Tags {
        FURSUIT("fursuit"),
        UPLOAD_AREA("upload_area"),
        BADGE("badge"),
        BADGE_MAIN("badge_main");

        public final String label;

        private Tags(String label) {
            this.label = label;
        }
    }

}
