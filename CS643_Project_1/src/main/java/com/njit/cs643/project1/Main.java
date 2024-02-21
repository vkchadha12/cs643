package com.njit.cs643.project1;

import com.njit.cs643.project1.classifier.RecognizeCar;
import com.njit.cs643.project1.helper.SqsQueueManager;
import com.njit.cs643.project1.config.ApplicationConfig;
import com.njit.cs643.project1.config.CS643_Config;
import com.njit.cs643.project1.fileHelper.FileOperation;
import com.njit.cs643.project1.helper.ListObjects;
import com.njit.cs643.project1.helper.RetrieveSqsMessages;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;
import com.njit.cs643.project1.helper.SendSqsMessages;

import java.io.IOException;
import java.util.*;


public class Main {
    private static final Region AWS_DEFAULT_REGION=Region.US_EAST_1;
    private static SqsClient sqs = SqsClient.builder()
            .region(AWS_DEFAULT_REGION)
            .build();
    private static final Double THRESHOLD = 80.0 ;
    static SendSqsMessages sendSqsMessages = new SendSqsMessages();
    static FileOperation fileOperation = new FileOperation();

    public static void main(String[] args) throws IOException {
        final String usage = """
                

                Usage:
                    <Mode>
                      'publisher' : If publisher mode reads file from AWS_S3_BUCKET bucket & Published on queue : vc35_queue.fifo
                      'receiver' :  If receiver mode reads message from vc35_queue.fifo queue and reads AWS_S3_BUCKET + key
                                    and send to the recognizer
                                    Output of the retrieving the files is written to "output.txt"


                    <Config>
                            path for configuration file. With following Json format
                             "com.njit.cs643.project1.classifier.config" : {
                                "AWS_QUEUE_NAME/" : "vc35_queue_1.fifo",
                                "AWS_S3_BUCKET" : "cs643-njit-project1",
                                "AWS_DEFAULT_REGION" :"Region.US_EAST_1",
                                "OUTPUT_FILE" : "output.txt"
                              }

                """;
        if (args.length != 2) {
            System.out.println(usage);
            System.exit(-1);
        }
        String mode = args[0];
        mode = mode.toLowerCase(Locale.ROOT);
        String configFile = args[1];

        ApplicationConfig applicationConfig = new ApplicationConfig();
        CS643_Config configObj = applicationConfig.get(configFile);
        fileOperation.deleteFile(CS643_Config.getOutputFile());
        sqs = initializeSQSClient(configObj);



        if ( mode.equals("publisher") ) {
            publisherMode(configObj);

        }
        if ( mode.equals("receiver") ) {
            receiverMode(configObj);
        }

    }
    public static void publisherMode(CS643_Config configObj){

        List<S3Object> s3Objects = ListObjects.getObjectList(configObj.getAwsS3Bucket() , configObj.getAwsDefaultRegion());
        RecognizeCar recognizeCar = new RecognizeCar();
        List<String> carKeyList  = new ArrayList<>();
        String messageGroup = "group1";
        int count = 1;

        for (S3Object s3Object : s3Objects) {

            String message = configObj.getAwsS3Bucket() + ":" +s3Object.key();
            if ( recognizeCar.classifyCar( configObj.getAwsS3Bucket(),configObj.getAwsDefaultRegion(), s3Object , configObj.getTHRESHOLD()) ) {
                System.out.println( "car found out " +  message);
                carKeyList.add(message);
            }

        }
        for (String car :carKeyList ) {
            sendSqsMessages.send(sqs, configObj.getAwsQueueName(), car, String.valueOf(count) );
            count++;
        }
        //publish last message on the queue as "-1"
        try {
            Thread.sleep(10000);
            String s = "-1:" + String.valueOf(carKeyList.size());
            sendSqsMessages.send(sqs, configObj.getAwsQueueName(), s , messageGroup );
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }

    public static void receiverMode(CS643_Config configObj) throws IOException {
        RetrieveSqsMessages retrieveSqsMessages = new RetrieveSqsMessages();
        RecognizeCar recognizeCar = new RecognizeCar();

        Boolean continueLoop = Boolean.TRUE;
        long t = System.currentTimeMillis();
        int count = 0;
        int totalImages = 0;
        fileOperation.write(CS643_Config.getOutputFile(), new String[]{"ImageFileName: Image Text \n"});
        while (continueLoop) {
            List<Message> messages = retrieveSqsMessages.receiveMessages(sqs, configObj.getAwsQueueName());
            try {
                for (Message message : messages) {
                    if (message.body().contains("-1")) { // last message
                        System.out.println("last message received, total ");
                        continueLoop = Boolean.FALSE;
                        List<String> tempList = List.of(message.body().split(":"));
                        totalImages = Integer.parseInt(tempList.get(1));
                        System.out.println("totalImages to process " + totalImages );
                        continue;
                    }
                    String[] objectInfo = message.body().split(":");
                    if (objectInfo.length != 2) // No need to process. Message in Invalid format
                        continue;
                    List<String> results = processMessage(objectInfo[0], objectInfo[1], recognizeCar, configObj);
                }
                Thread.sleep(5000);
                count++;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println( "Sleeping for " +  (count * 5000) /1000  + " seconds ");

        }

    }

public static List<String> processMessage( String s3bucket
        , String keyName
        , RecognizeCar recognizeCar
        , CS643_Config configObj) throws IOException {
            List<String> results = new ArrayList<String>();
            System.out.println("Received Car from s3 Bucket" + s3bucket + "; File " + keyName);
            String res = detectText(s3bucket, keyName, recognizeCar, configObj);
            String temp_res = "";
            if (res.length() > 1)
                temp_res = s3bucket + ":" +  keyName + ":  [ " + res + "]  \n";
            System.out.println(  temp_res );
            results.add( temp_res );
            fileOperation.write(configObj.getOutputFile(), new String[]{temp_res});
            return results;
        }

public static String detectText(String s3bucket
                            , String keyName
                            , RecognizeCar recognizeCar
                            , CS643_Config configObj) {
        return recognizeCar.DetectText(s3bucket, configObj.getAwsDefaultRegion(), keyName, configObj.getTHRESHOLD());
    }

public static SqsClient initializeSQSClient(CS643_Config configObj){
    SqsQueueManager sqsQueueManager = new SqsQueueManager();
    sqs = sqsQueueManager.getSQSClient(configObj.getAwsDefaultRegion());
    // This will guarantee create the queue if does not exists.
    sqsQueueManager.getQueueURL(sqs , configObj.getAwsQueueName());
    return sqs;
}

}