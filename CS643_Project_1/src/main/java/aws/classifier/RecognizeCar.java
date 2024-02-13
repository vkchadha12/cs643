package aws.classifier;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.*;
import software.amazon.awssdk.services.s3.model.S3Object;
//List,Map
import java.util.*;

public class RecognizeCar {

    public Boolean classifyCar(String bucketName , Region region, S3Object s3Object ) {

            boolean carFound = false;
            //System.out.println("Gathered image in njit-cs-643 S3 bucket: " + s3Object.key());
            RekognitionClient carRek = RekognitionClient.builder().region(region).build();
            Image img = Image.builder().s3Object(software.amazon.awssdk.services.rekognition.model.S3Object
                            .builder().bucket(bucketName).name(s3Object.key()).build())
                    .build();
            DetectLabelsRequest request = DetectLabelsRequest.builder().image(img).minConfidence((float) 90)
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

    }
