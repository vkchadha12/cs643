package classifier;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.*;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.rekognition.model.DetectTextRequest;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.DetectTextResponse;
import software.amazon.awssdk.services.rekognition.model.TextDetection;
import software.amazon.awssdk.services.rekognition.model.RekognitionException;
import java.util.List;
import helper.GetObjectImage;
//List,Map
import java.util.*;

public class RecognizeCar {

    public Boolean classifyCar(String bucketName , Region region, S3Object s3Object, Double THRESHOLD ) {

            boolean carFound = false;
            //System.out.println("Gathered image in njit-cs-643 S3 bucket: " + s3Object.key());
            RekognitionClient carRek = RekognitionClient.builder().region(region).build();
            Image img = Image.builder().s3Object(software.amazon.awssdk.services.rekognition.model.S3Object
                            .builder().bucket(bucketName).name(s3Object.key()).build())
                    .build();
            DetectLabelsRequest request = DetectLabelsRequest.builder().image(img).minConfidence( THRESHOLD.floatValue())
                    .build();
            DetectLabelsResponse result = carRek.detectLabels(request);
            List<Label> labels = result.labels();

            for (Label label : labels) {
                if (label.name().equals("Car")) {
                    carFound = true;
                }
            }
            return carFound;
        }
    public String DetectText (String bucketName , Region region , String keyName, Double threshold){
        RekognitionClient carRek = RekognitionClient.builder().region(region).build();
        GetObjectImage getObjectImage = new GetObjectImage();
        StringBuilder results = new StringBuilder();
        try {

            HashMap<String, byte[]> imgBytes = getObjectImage.get(region , bucketName , keyName , "");
            //InputStream sourceStream = new FileInputStream(imgBytes.get(keyName));
            //SdkBytes sourceBytes = SdkBytes.fromInputStream(sourceStream);
            Image souImage = Image.builder()
                    .bytes(SdkBytes.fromByteArray(imgBytes.get(keyName)))
                    .build();

            DetectTextRequest textRequest = DetectTextRequest.builder()
                    .image(souImage)
                    .build();

            DetectTextResponse textResponse = carRek.detectText(textRequest);
            List<TextDetection> textCollection = textResponse.textDetections();
            System.out.println("Detected lines and words");
            for (TextDetection text : textCollection) {
                System.out.println("Image: " + keyName);
                System.out.println("Detected: " + text.detectedText());
                System.out.println("Confidence: " + text.confidence().toString());
                System.out.println("Id : " + text.id());
                System.out.println("Parent Id: " + text.parentId());
                System.out.println("Type: " + text.type());
                System.out.println();
                if ( text.confidence() > threshold )
                    results.append(text.detectedText()).append(";");
            }
        } catch (RekognitionException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
        return results.toString();
        }
    }
