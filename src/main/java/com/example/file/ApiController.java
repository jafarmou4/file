package com.example.file;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;

@RestController
public class ApiController {
    @Value("${application.ms-vehicle-insurance-server.file.default.dir.location}")
    private String baseFileDirectory;
    @Autowired
    private ApiService apiService;

    @GetMapping(value = "/hello", name = "${application.api.name.vehicleInsuranceQuote.vehicleInsuranceQuotePhotos}")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("hello World");
    }

    @GetMapping(value = "/download", name = "${application.api.name.vehicleInsuranceQuote.vehicleInsuranceQuotePhotos}")
    public ResponseEntity<?> sendVehicleInsuranceQuoteFileDownload() throws Exception {
        File imagePath = new File(baseFileDirectory + "/" + "302");
        if (!imagePath.exists()) {
            throw new NoSuchElementException();
        }
        File[] images = new File[1];
        File[] imagesInDirectory = imagePath.listFiles();
        assert imagesInDirectory != null;
        for (File file : imagesInDirectory) {
            if (file.isDirectory()) {
                File[] dirFiles = file.listFiles();
                assert dirFiles != null;
                for (File image : dirFiles) {
                    if (image.getName().toLowerCase().endsWith(".jpeg") || image.getName().toLowerCase().endsWith(".jpg")) {
                        String caption = image.getName().substring(0, image.getName().indexOf("."));
//                        try {
//                            ImageMetadata metadata = Imaging.getMetadata(image);
//                            if (metadata instanceof JpegImageMetadata) {
//                                JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
//                                TiffField field = jpegMetadata.findEXIFValue(TiffTagConstants.TIFF_TAG_IMAGE_DESCRIPTION);
//                                if (field != null)
//                                    caption = new String(Base64.getDecoder().decode(field.getValueDescription().replace("'", "")), "UTF-8");
//                            }
//                        } catch (Exception ignored) {
//
//                        }
                        images[0] = image;

                    }
                }
            } else {
                images[0] = imagesInDirectory[0];
            }
        }

        Path path = Paths.get(images[0].getPath());
//        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));

        InputStreamResource resource = new InputStreamResource(new FileInputStream(images[0]));

//        BaseDTO baseDTO = datinVehicleInsuranceService.sendVehicleInsuranceQuoteFile(draftId, file);
//        return new ResponseEntity<>(images[0], HttpStatus.OK);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=img.jpg");
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(images[0].length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @PostMapping(value = "v1/vehicle/quote/upload", name = "${application.api.name.vehicleInsuranceQuote.vehicleInsuranceQuotePhotos}")
    public ResponseEntity<?> sendVehicleInsuranceQuoteFiles(
            @RequestBody(required = false) MultipartFile[] files,
            @RequestParam Integer karshenasiID) {
        return new ResponseEntity<>(apiService.sendVehicleInsuranceQuoteFiles(files, karshenasiID), HttpStatus.OK);
    }

}
