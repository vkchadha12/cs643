package config;
import software.amazon.awssdk.regions.Region;


public class CS643_Config {
    public static String AWS_QUEUE_NAME;
    public static String AWS_S3_BUCKET;
    public static Region AWS_DEFAULT_REGION =Region.US_EAST_1;
    public static String OUTPUT_FILE = "";
    public Double THRESHOLD = 80.0;
    public static void setAwsQueueName(String awsQueueName) {
        AWS_QUEUE_NAME = awsQueueName;
    }

    public static void setAwsS3Bucket(String awsS3Bucket) {
        AWS_S3_BUCKET = awsS3Bucket;
    }

    public static void setAwsDefaultRegion(Region awsDefaultRegion) {
        AWS_DEFAULT_REGION = awsDefaultRegion;
    }

    public static void setOutputFile(String outputFile) {
        OUTPUT_FILE = outputFile;
    }

    public void setTHRESHOLD(Double THRESHOLD) {
        this.THRESHOLD = THRESHOLD;
    }

    public static String getAwsQueueName() {
        return AWS_QUEUE_NAME;
    }

    public static String getAwsS3Bucket() {
        return AWS_S3_BUCKET;
    }

    public static Region getAwsDefaultRegion() {
        return AWS_DEFAULT_REGION;
    }

    public static String getOutputFile() {
        return OUTPUT_FILE;
    }

    public Double getTHRESHOLD() {
        return THRESHOLD;
    }



}
