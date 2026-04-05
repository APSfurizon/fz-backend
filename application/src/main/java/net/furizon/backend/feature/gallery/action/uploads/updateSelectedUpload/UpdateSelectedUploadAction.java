package net.furizon.backend.feature.gallery.action.uploads.updateSelectedUpload;

public interface UpdateSelectedUploadAction {
    void invoke(long uploadId, boolean isSelected);
}
