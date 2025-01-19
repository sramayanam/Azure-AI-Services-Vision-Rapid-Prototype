package com.demo.lifecyclemgmt;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("blob")
public class BlobController {
    private static final Logger logger = LoggerFactory.getLogger(BlobController.class);

//    @GetMapping
    @GetMapping("/listBlobsByTag")

    public List<BlobInfo> listBlobsByTag() {
        String tagName = "customModifiedDate";
        String tagValue = "2025-01-05";
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("https://aaaorgeastus2.blob.core.windows.net")
                .buildClient();
        List<BlobInfo> blobDetails = new ArrayList<>();
        String tagFilter = String.format("\"%s\"<='%s'", tagName, tagValue);
        blobServiceClient.findBlobsByTags(tagFilter).forEach(blobItem -> {
            System.out.println("Blob name: " + blobItem.getName());
            System.out.println("Blob container: " + blobItem.getContainerName());

            BlobInfo blobInfo = new BlobInfo(blobItem.getContainerName(), blobItem.getName());
            logger.info("Blob name: " + blobItem.getName());
            logger.info("Blob container: " + blobItem.getContainerName());
            blobDetails.add(blobInfo);
        });

        return blobDetails;
    }
}