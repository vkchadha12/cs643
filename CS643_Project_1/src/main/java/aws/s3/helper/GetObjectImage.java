package aws.s3.helper;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.regions.Region;

import java.util.HashMap;

//public class ListBucketObject {
//}

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0


/**
 * List objects within an Amazon S3 bucket.
 *
 * This code expects that you have AWS credentials set up per:
 * http://docs.aws.amazon.com/java-sdk/latest/developer-guide/setup-credentials.html
 */
public class GetObjectImage {

    public static HashMap<String, byte[]> get(Region region, String bucketName, String keyName, String path) {
        HashMap<String, byte[]> imageMap= new HashMap<String,byte[]>();

        try {
            S3Client s3 = S3Client.builder()
                    .region(region)
                    .build();

            GetObjectRequest objectRequest = GetObjectRequest
                    .builder()
                    .key(keyName)
                    .bucket(bucketName)
                    .build();

            ResponseBytes<GetObjectResponse> objectBytes = s3.getObjectAsBytes(objectRequest);
            byte[] data = objectBytes.asByteArray();
            imageMap.put(keyName, data);

            // Write the data to a local file.
//            File myFile = new File(path);
//            OutputStream os = new FileOutputStream(myFile);
//            os.write(data);
//            System.out.println("Successfully obtained bytes from an S3 object");
//            os.close();

        //} //catch (IOException ex) {
           // ex.printStackTrace();
        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        return imageMap;
    }

    // convert bytes to kbs.
    private static long calKb(Long val) {
        return val / 1024;
    }

}