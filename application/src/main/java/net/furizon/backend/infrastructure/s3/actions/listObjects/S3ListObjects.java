package net.furizon.backend.infrastructure.s3.actions.listObjects;

import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.function.Consumer;

public interface S3ListObjects {
    void forEach(Consumer<S3Object> consumer);
}
