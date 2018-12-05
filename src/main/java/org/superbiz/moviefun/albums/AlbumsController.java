package org.superbiz.moviefun.albums;

import javassist.bytecode.ByteArray;
import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.superbiz.moviefun.blobstore.Blob;
import org.superbiz.moviefun.blobstore.FileStore;
import sun.nio.ch.IOUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;
import static java.nio.file.Files.readAllBytes;

@Controller
@RequestMapping("/albums")
public class AlbumsController {

    private final AlbumsBean albumsBean;
    private final FileStore fileStore;

    public AlbumsController(AlbumsBean albumsBean, FileStore fileStore) {
        this.albumsBean = albumsBean;
        this.fileStore = fileStore;
    }


    @GetMapping
    public String index(Map<String, Object> model) {
        model.put("albums", albumsBean.getAlbums());
        return "albums";
    }

    @GetMapping("/{albumId}")
    public String details(@PathVariable long albumId, Map<String, Object> model) {
        model.put("album", albumsBean.find(albumId));
        return "albumDetails";
    }

    @PostMapping("/{albumId}/cover")
    public String uploadCover(@PathVariable long albumId, @RequestParam("file") MultipartFile uploadedFile) throws IOException {
        Blob blob = new Blob(getCoverName(albumId) + uploadedFile.getName(), uploadedFile.getInputStream(), uploadedFile.getContentType());
        fileStore.put(blob);

        return format("redirect:/albums/%d/", albumId);
    }

    @GetMapping("/{albumId}/cover")
    public HttpEntity<byte[]> getCover(@PathVariable long albumId) throws IOException, URISyntaxException {
        final Optional<Blob> maybeBlob = fileStore.get(getCoverName(albumId) + "file");

        final Blob blob = maybeBlob.orElseGet(this::defaultCover);

        byte[] imageBytes = IOUtils.toByteArray(blob.inputStream);
        HttpHeaders headers = createImageHttpHeaders(blob.name, imageBytes);

        return new HttpEntity<>(imageBytes, headers);
    }

    private Blob defaultCover() {
        Blob defaultBlob = null;
        try {
            Path coverFilePath = Paths.get(getSystemResource("default-cover.jpg").toURI());
            defaultBlob = new Blob("default-cover.jpg", new FileInputStream(coverFilePath.toString()),new Tika().detect(coverFilePath));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return defaultBlob;
    }

    private HttpHeaders createImageHttpHeaders(String coverFilePath, byte[] imageBytes) throws IOException {
        String contentType = new Tika().detect(coverFilePath);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(imageBytes.length);
        return headers;
    }

    private String getCoverName(@PathVariable long albumId) {
        return format("covers/%d/", albumId);
    }

}
