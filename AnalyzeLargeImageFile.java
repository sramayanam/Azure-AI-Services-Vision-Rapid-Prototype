package org.example;

import java.io.*;

import com.azure.core.http.policy.ExponentialBackoffOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.specialized.BlobInputStream;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.azure.ai.vision.imageanalysis.ImageAnalysisAsyncClient;
import com.azure.ai.vision.imageanalysis.ImageAnalysisClientBuilder;
import com.azure.ai.vision.imageanalysis.models.CropRegion;
import com.azure.ai.vision.imageanalysis.models.DenseCaption;
import com.azure.ai.vision.imageanalysis.models.DetectedObject;
import com.azure.ai.vision.imageanalysis.models.DetectedPerson;
import com.azure.ai.vision.imageanalysis.models.DetectedTag;
import com.azure.ai.vision.imageanalysis.models.DetectedTextLine;
import com.azure.ai.vision.imageanalysis.models.DetectedTextWord;
import com.azure.ai.vision.imageanalysis.models.ImageAnalysisOptions;
import com.azure.ai.vision.imageanalysis.models.ImageAnalysisResult;
import com.azure.ai.vision.imageanalysis.models.VisualFeatures;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.BinaryData;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class AnalyzeLargeImageFile {
    /**
     * This sample demonstrates how to analyze a large image file from Azure Blob Storage using the Azure Cognitive Services Computer Vision SDK.
     * Build blob service client with default Azure credentials and an exponential retry policy.
     * #1: Sets up exponential backoff retry options with adjustable retry count, base delay,
     * and maximum delay for robust error handling during operations.
     * #2: Builds a BlobServiceClient using DefaultAzureCredential and a retry policy,
     * enabling secure and resilient communication with Azure Blob Storage.
     * <p>
     * Create an ImageAnalysisAsyncClient to analyze a large image file from Azure Blob Storage.
     * <ul>
     *   <li>Obtain an image stream from a specified Azure Blob Storage container and blob name.</li>
     *   <li>Configure and use an ImageAnalysisAsyncClient to analyze the image for various visual features.</li>
     *   <li>Handle the analysis results asynchronously and manage potential errors.</li>
     * </ul>
     **/
    public static void main(String[] args) {
        // Create retry options with exponential backoff
        RetryOptions retryOptions = new RetryOptions(new ExponentialBackoffOptions()
                .setMaxRetries(3)
                .setBaseDelay(Duration.ofSeconds(1))
                .setMaxDelay(Duration.ofSeconds(10)));

        // Build BlobServiceClient with DefaultAzureCredential and retry policy
        // Uses Azure Entra for Authentication
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("https://{Your Azure Storage Account}.blob.core.windows.net")
                .addPolicy(new RetryPolicy(retryOptions))
                .buildClient();

        String containerName = "samplefiles";
        String blobName = "AdobeStock_584748923.jpeg";

        BlobClient blobClient = blobServiceClient.getBlobContainerClient(containerName).getBlobClient(blobName);

        // Image analysis client setup
        String endpoint = System.getenv("VISION_ENDPOINT");
        if (endpoint == null || endpoint.isBlank()) {
            endpoint = "https://{Your AI Services Account}.cognitiveservices.azure.com/";
        }

        ImageAnalysisAsyncClient client = new ImageAnalysisClientBuilder()
                .endpoint(endpoint)
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildAsyncClient();

        ImageAnalysisOptions options = new ImageAnalysisOptions()
                .setLanguage("en")
                .setGenderNeutralCaption(true)
                .setSmartCropsAspectRatios(Arrays.asList(0.9, 1.33))
                .setModelVersion("latest");
        /* Wait until the file is read and image is analyzed */

        CountDownLatch latch = new CountDownLatch(1);
        // Open the blob input stream
        try (BlobInputStream blobInputStream = blobClient.openInputStream()) {
            // Prepare a buffer to store retrieved bytes
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4 * 1024];
            int bytesRead;
            // Read from blob and write to local output
            while ((bytesRead = blobInputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            BinaryData imageData = BinaryData.fromBytes(byteArrayOutputStream.toByteArray());

            // Analuze image features Asynchronously
            client.analyze(
                    imageData,
                    Arrays.asList(
                            VisualFeatures.SMART_CROPS,
                            VisualFeatures.CAPTION,
                            VisualFeatures.DENSE_CAPTIONS,
                            VisualFeatures.OBJECTS,
                            VisualFeatures.PEOPLE,
                            VisualFeatures.READ,
                            VisualFeatures.TAGS),
                    options).subscribe(
                    result -> {
                        printAnalysisResults(result);

                        latch.countDown();
                    },
                    error -> {
                        System.err.println("Image analysis terminated with error message: " + error);
                        latch.countDown();
                    });

            latch.await();

        } catch (HttpResponseException e) {
            System.out.println("Exception: " + e.getClass().getSimpleName());
            System.out.println("Status code: " + e.getResponse().getStatusCode());
            System.out.println("Message: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Message: " + e.getMessage());
        }
    }

    // Helper to save the analysis results to a JSON file
    public static void saveAnalysisResultsAsJson(ImageAnalysisResult result, String filePath) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), result);
            System.out.println("Analysis results saved to " + filePath);
        } catch (IOException e) {
            System.err.println("Failed to save analysis results as JSON: " + e.getMessage());
        }
    }

    // Helper to print analysis results
    public static void printAnalysisResults(ImageAnalysisResult result) {
        System.out.println("Image analysis results:");

        if (result.getCaption() != null) {
            System.out.println(" Caption:");
            System.out.println("   \"" + result.getCaption().getText() + "\", Confidence "
                    + String.format("%.4f", result.getCaption().getConfidence()));
        }

        if (result.getDenseCaptions() != null) {
            System.out.println(" Dense Captions:");
            for (DenseCaption denseCaption : result.getDenseCaptions().getValues()) {
                System.out.println("   \"" + denseCaption.getText() + "\", Bounding box "
                        + denseCaption.getBoundingBox() + ", Confidence " + String.format("%.4f", denseCaption.getConfidence()));
            }
        }

        if (result.getRead() != null && result.getRead().getBlocks() != null && !result.getRead().getBlocks().isEmpty()) {
            System.out.println(" Read:");
            for (DetectedTextLine line : result.getRead().getBlocks().getFirst().getLines()) {
                System.out.println("   Line: '" + line.getText()
                        + "', Bounding polygon " + line.getBoundingPolygon());
                for (DetectedTextWord word : line.getWords()) {
                    System.out.println("     Word: '" + word.getText()
                            + "', Bounding polygon " + word.getBoundingPolygon()
                            + ", Confidence " + String.format("%.4f", word.getConfidence()));
                }
            }
        } else {
            System.out.println(" No text to read from the image");
        }

        if (result.getTags() != null) {
            System.out.println(" Tags:");
            for (DetectedTag tag : result.getTags().getValues()) {
                System.out.println("   \"" + tag.getName() + "\", Confidence " + String.format("%.4f", tag.getConfidence()));
            }
        }

        if (result.getObjects() != null) {
            System.out.println(" Objects:");
            for (DetectedObject detectedObject : result.getObjects().getValues()) {
                System.out.println("   \"" + detectedObject.getTags().getFirst().getName() + "\", Bounding box "
                        + detectedObject.getBoundingBox() + ", Confidence " + String.format("%.4f", detectedObject.getTags().getFirst().getConfidence()));
            }
        }

        if (result.getPeople() != null) {
            System.out.println(" People:");
            for (DetectedPerson person : result.getPeople().getValues()) {
                System.out.println("   Bounding box "
                        + person.getBoundingBox() + ", Confidence " + String.format("%.4f", person.getConfidence()));
            }
        }

        if (result.getSmartCrops() != null) {
            System.out.println(" Crop Suggestions:");
            for (CropRegion cropRegion : result.getSmartCrops().getValues()) {
                System.out.println("   Aspect ratio "
                        + cropRegion.getAspectRatio() + ": Bounding box " + cropRegion.getBoundingBox());
            }
        }
        saveAnalysisResultsAsJson(result, "/Users/sreeram/Documents/workspace/gcptoazurefilecopy/vision/aivision/src/main/java/org/example/vision.json");
        System.out.println(" Image height = " + result.getMetadata().getHeight());
        System.out.println(" Image width = " + result.getMetadata().getWidth());
        System.out.println(" Model version = " + result.getModelVersion());
    }
}
