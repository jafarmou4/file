package com.example.file;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

@Service
public class ApiService {
    private static class Body {
        private final Integer karshenasiID;

        private Body(Integer karshenasiID) {
            this.karshenasiID = karshenasiID;
        }

        public Integer getKarshenasiID() {
            return karshenasiID;
        }
    }

    public Object sendVehicleInsuranceQuoteFiles(MultipartFile[] files, Integer karshenasiID) {


        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//        headers.add("Cookie", DatinTokenForceTestManager.getToken());

        MultiValueMap<String, Object> map= new LinkedMultiValueMap<>();

        map.add("body", new Body(karshenasiID));

        // add files to map (with key = "file")
        if (files != null && files.length > 0) {
            Arrays.asList(files).forEach(multipartFile -> {
                String filename = multipartFile.getOriginalFilename();

                File uploadedFile = null;

                try {
                    uploadedFile = File.createTempFile("vehicle_" + karshenasiID + "_" + filename.substring(0, filename.indexOf(".")), filename.substring(filename.indexOf(".")));
                    multipartFile.transferTo(uploadedFile);
                    map.add("file", new FileSystemResource(uploadedFile));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(map, headers);

        ResponseEntity<Object> response;

        try {
            response = restTemplate.postForEntity(
                    applicationProperties.getProperty("msremote.datin.base.url.force.test") +
                            applicationProperties.getProperty("msremote.datin.vehicle.karshenasi.badaneh.upload"),
                    request ,
                    Object.class
            );
        }
        catch (Exception ex) {
            throw new ServiceException(
                    applicationProperties.getCode("application.message.insurance.global.error.upload.code"),
                    applicationProperties.getProperty("application.message.insurance.global.error.upload.text"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new BaseDTO<>(
                MetaDTO.getInstance(applicationProperties),
                response.getBody()
        );
    }
}
