// Copyright (c) Sree Ram for Private use of his customers. All rights reserved.
// Licensed under the MIT License.
package org.example;

import com.azure.ai.vision.face.FaceAsyncClient;
import com.azure.ai.vision.face.FaceClientBuilder;
import com.azure.ai.vision.face.models.*;
import com.azure.core.credential.KeyCredential;
import com.azure.core.http.policy.ExponentialBackoffOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Flux;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import static com.azure.ai.vision.face.models.FaceAttributeType.ModelDetection01;
import static com.azure.ai.vision.face.models.FaceAttributeType.ModelDetection03;
import static com.azure.ai.vision.face.models.FaceAttributeType.ModelRecognition04;

public class FaceDetectionAndRecognitionA {
    /**
     * The entry point of the application.
     * <p>
     * This method configures and initializes the Face detection and recognition
     * client using Azure Cognitive Services.
     * It reads an image file and applies various detection features such as
     * exposure, occlusion, head pose, mask detection,
     * blur measurement, and quality for recognition. The face detection process is
     * performed asynchronously, and the results
     * or errors are logged accordingly.
     * </p>
     * 
     * @param args an array of String arguments passed to the program (not used in
     *             this implementation)
     */
    public static void main(String[] args) {
        // Note: The endpoint and key may be different for you. Be sure to check the
        // values in the Azure portal.
        // This code needs Azure Face Cognitive Services part of Azure Multi Services
        // Account to be created in Azure and the endpoint and key to be set.
        String endpoint = "https://{your region}.api.cognitive.microsoft.com/";
        String key = "{your-key}";
        CountDownLatch latch = new CountDownLatch(1);
        RetryOptions retryOptions = new RetryOptions(new ExponentialBackoffOptions()
                .setMaxRetries(3)
                .setBaseDelay(Duration.ofSeconds(1))
                .setMaxDelay(Duration.ofSeconds(10)));

        FaceAsyncClient client = new FaceClientBuilder()
                .endpoint(endpoint)
                .credential(new KeyCredential(key))
                .retryPolicy(new RetryPolicy(retryOptions))
                .buildAsyncClient();

        String imagePathString = "{your local image path}/facesample.jpg";
        Path path = Paths.get(imagePathString);
        BinaryData imageData = BinaryData.fromFile(path);

        /**
         * Detects faces from the provided image data using the specified detection and
         * recognition models.
         * Includes advanced attributes such as exposure, occlusion, head pose, mask
         * detection, blur measurement,
         * and quality for recognition.
         * 
         * @param imageData              the image data to perform the face detection on
         * @param detectionModel         the face detection model version to use
         * @param recognitionModel       the face recognition model version to use
         * @param returnFaceId           flag indicating whether or not to return a
         *                               unique face identifier
         * @param detectionFeatures      a list of additional features to detect and
         *                               analyze, such as exposure and occlusion
         * @param recognizeCelebrities   whether or not to recognize celebrities within
         *                               the image, if supported
         * @param returnRecognitionModel whether the response should include the
         *                               recognition model used
         * @param timeoutInSeconds       the maximum time (in seconds) to wait for the
         *                               detection to complete
         * @return a reactive flux stream emitting detection results for each face found
         *         in the image
         */
        Flux<FaceDetectionResult> flux = client.detect(
                imageData,
                FaceDetectionModel.DETECTION_03,
                FaceRecognitionModel.RECOGNITION_04,
                true,
                Arrays.asList(
                        ModelDetection01.EXPOSURE,
                        ModelDetection01.OCCLUSION,
                        ModelDetection03.HEAD_POSE,
                        ModelDetection03.MASK,
                        ModelDetection03.BLUR,
                        ModelRecognition04.QUALITY_FOR_RECOGNITION),
                true,
                true,
                120)
                .flatMapMany(Flux::fromIterable);

        // Subscribe to the flux to process face detection results asynchronously
        flux.subscribe(
                faceResult -> {
                    // Log the detected face information
                    Utils.log("Detected Face by file:" + Utils.toString(faceResult) + "\n");
                    // Save the detection results in a JSON file
                    saveFaceDetectionResultsAsJson(faceResult, "face_detection_results.json");
                    // Decrement the latch to signal completion
                    latch.countDown();
                },
                error -> {
                    // Log the error message upon failure
                    System.err.println("Face detection terminated with error message: " + error.getMessage());
                    // Decrement the latch to avoid blocking
                    latch.countDown();
                });

        // Wait until face detection completes or is interrupted
        try {
            latch.await();
        } catch (InterruptedException e) {
            System.err.println("Latch interrupted: " + e.getMessage());
        }

    }

    /**
     * Saves the face detection results as a JSON file.
     * 
     * @param result   the face detection result to save
     * @param filePath the path to save the JSON file
     */
    public static void saveFaceDetectionResultsAsJson(FaceDetectionResult result, String filePath) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), result);
            System.out.println("Face detection results saved to " + filePath);
        } catch (IOException e) {
            System.err.println("Failed to save face detection results as JSON: " + e.getMessage());
        }
    }
}
