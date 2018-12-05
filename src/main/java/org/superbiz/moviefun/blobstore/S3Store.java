package org.superbiz.moviefun.blobstore;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class S3Store implements BlobStore {

    private AmazonS3Client s3Client;
    private String photoStorageBucket;

    public S3Store(AmazonS3Client s3Client, String photoStorageBucket) {
        this.s3Client = s3Client;
        this.photoStorageBucket = photoStorageBucket;
    }

    @Override
    public void put(Blob blob) throws IOException {
        ObjectMetadata metadata = new ObjectMetadata();

        byte[] bytes = IOUtils.toByteArray(blob.inputStream);

        InputStream inputStream = new ByteArrayInputStream(bytes);

        metadata.setContentLength(bytes.length);
        s3Client.putObject(photoStorageBucket, blob.name, inputStream, metadata);


    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        try {
            S3Object o = s3Client.getObject(photoStorageBucket, name);
            S3ObjectInputStream s3is = o.getObjectContent();
            return Optional.of(new Blob(name, s3is, null));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteAll() {

    }
}
