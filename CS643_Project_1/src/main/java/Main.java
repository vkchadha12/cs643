import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;
import aws.sqs.helper.SendSqsMessages;
import aws.sqs.helper.RetrieveSqsMessages;
import aws.s3.helper.ListObjects;
import aws.classifier.RecognizeCar;

import java.util.*;
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.


public class Main {
    private static final String AWS_QUEUE_NAME = "vc35_queue" ;
    private static final String AWS_S3_BUCKET = "cs643-njit-project1";
    private static final Region AWS_DEFAULT_REGION=Region.US_EAST_1;
    private static final SqsClient sqs = SqsClient.builder()
            .region(AWS_DEFAULT_REGION)
            .build();
    static SendSqsMessages sendSqsMessages = new SendSqsMessages();


    public static void main(String[] args) {
        final String usage = """

                Usage:
                    <Mode >\s
                      "publisher" : If publisher mode reads file from AWS_S3_BUCKET bucket & Published on queue : vc35_queue
                      "receiver" :  If receiver mode reads message from vc35_queue queue and reads AWS_S3_BUCKET + key 
                                    and send to the recognizer
               \s
                """;
        if (args.length != 1) {
            System.out.println(usage);
            System.exit(1);
        }
        String mode = args[0];
        mode = mode.toLowerCase(Locale.ROOT);
        if ( mode.equals("publisher") ) {
            publisherMode();

        }
        if ( mode.equals("receiver") ) {
            receiverMode();
        }

    }

    public static void publisherMode(){

        ListObjects listObjects = new ListObjects();
        List<S3Object> s3Objects = listObjects.getObjectList(AWS_S3_BUCKET, AWS_DEFAULT_REGION);
        HashMap<String, byte[]> imageMap= new HashMap<String,byte[]>();
        RecognizeCar recognizeCar = new RecognizeCar();
        List<String> carKeyList  = new ArrayList<>();

        for (S3Object s3Object : s3Objects) {

            String message = AWS_S3_BUCKET + ":" +s3Object.key();
            if ( recognizeCar.classifyCar( AWS_S3_BUCKET,AWS_DEFAULT_REGION, s3Object ) ) {
                System.out.println( "car found out %s " + message);
                carKeyList.add(message);

            }

        }
        for (String car :carKeyList ) {
            sendSqsMessages.send(sqs, AWS_QUEUE_NAME, car);
        }


    }

    public static void receiverMode(){
        RetrieveSqsMessages retrieveSqsMessages = new RetrieveSqsMessages();
        List<Message> messages = retrieveSqsMessages.receiveMessages(sqs, AWS_QUEUE_NAME);
        HashMap<String, byte[]> imageMap= new HashMap<String,byte[]>();
        ListObjects listObjects = new ListObjects();
        List<S3Object> s3Objects = listObjects.getObjectList(AWS_S3_BUCKET, AWS_DEFAULT_REGION);
        RecognizeCar recognizeCar = new RecognizeCar();


        for (Message message : messages) {
            String[] objectInfo= message.body().split(":");
            if (objectInfo.length != 2 )
                continue;
            System.out.println(objectInfo);
            System.out.println( "Received Car from s3 Bucket" + objectInfo[0] + "; File " +  objectInfo[1]);
            for (S3Object s3Object : s3Objects) {
                if (s3Object.key().equals(objectInfo[1] )) {
                    System.out.println("%s found , Sending for valdiation" + objectInfo[1]);
                    if ( recognizeCar.classifyCar( AWS_S3_BUCKET,AWS_DEFAULT_REGION, s3Object ) ) {
                        System.out.println( "Validated found out %s " + s3Object.key());
                    }
                }
            }
        }


    }

}