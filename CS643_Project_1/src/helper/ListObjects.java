package  helper;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.ArrayList;
import java.util.List;
public class ListObjects {
    public static List<S3Object> getObjectList(String bucketName, Region region) {
        Boolean verbose = false;
        List<S3Object> objects = new ArrayList<>();
        try {
            S3Client s3 = S3Client.builder()
                    .region(region)
                    .build();

            ListObjectsRequest listObjects = ListObjectsRequest
                    .builder()
                    .bucket(bucketName)
                    .build();

            ListObjectsResponse res = s3.listObjects(listObjects);
            objects = res.contents();
            if ( verbose )
                for (S3Object myValue : objects) {
                    System.out.print("\n The name of the key is " + myValue.key());
                    System.out.print("\n The object is " + calKb(myValue.size()) + " KBs");
                    System.out.print("\n The owner is " + myValue.owner());
                }
        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        return objects;
    }

    // convert bytes to kbs.
    private static long calKb(Long val) {
        return val / 1024;
    }
}