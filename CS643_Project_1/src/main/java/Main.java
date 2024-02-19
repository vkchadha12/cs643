import fileHelper.FileOperation;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;
import aws.sqs.helper.SendSqsMessages;
import aws.sqs.helper.RetrieveSqsMessages;
import aws.s3.helper.ListObjects;
import aws.classifier.RecognizeCar;
import aws.sqs.helper.SqsQueueManager;

import java.io.IOException;
import java.util.*;
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.


public class Main {
    private static final String AWS_QUEUE_NAME = "vc35_queue_1.fifo" ;
    private static final String AWS_S3_BUCKET = "cs643-njit-project1";
    private static final Region AWS_DEFAULT_REGION=Region.US_EAST_1;
    public static final String OUTPUT_FILE= "output.txt";

    private static final SqsClient sqs = SqsClient.builder()
            .region(AWS_DEFAULT_REGION)
            .build();
    private static final Double THRESHOLD = 80.0 ;
    static SendSqsMessages sendSqsMessages = new SendSqsMessages();
    static FileOperation fileOperation = new FileOperation();

    public static void main(String[] args) throws IOException {
        final String usage = """

                Usage:
                    <Mode >\s
                      "publisher" : If publisher mode reads file from AWS_S3_BUCKET bucket & Published on queue : vc35_queue.fifo
                      "receiver" :  If receiver mode reads message from vc35_queue.fifo queue and reads AWS_S3_BUCKET + key 
                                    and send to the recognizer
                                    Output of the retrieving the files is written to "output.txt"
                                    
               \s
                """;
        if (args.length != 1) {
            System.out.println(usage);
            System.exit(1);
        }
        String mode = args[0];
        mode = mode.toLowerCase(Locale.ROOT);
        fileOperation.deleteFile(OUTPUT_FILE);

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
        String messageGroup = "group1";
        int count = 1;

        for (S3Object s3Object : s3Objects) {

            String message = AWS_S3_BUCKET + ":" +s3Object.key();
            if ( recognizeCar.classifyCar( AWS_S3_BUCKET,AWS_DEFAULT_REGION, s3Object ) ) {
                System.out.println( "car found out " +  message);
                carKeyList.add(message);
            }

        }
        for (String car :carKeyList ) {
            sendSqsMessages.send(sqs, AWS_QUEUE_NAME, car, String.valueOf(count) );
            count++;
        }
        //publish last message on the queue as "-1"
        try {
            Thread.sleep(10000);
            String s = "-1:" + String.valueOf(carKeyList.size());
            sendSqsMessages.send(sqs, AWS_QUEUE_NAME, s , messageGroup );
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }

    public static void receiverMode() throws IOException {
        RetrieveSqsMessages retrieveSqsMessages = new RetrieveSqsMessages();
        ListObjects listObjects = new ListObjects();
        List<S3Object> s3Objects = listObjects.getObjectList(AWS_S3_BUCKET, AWS_DEFAULT_REGION);
        RecognizeCar recognizeCar = new RecognizeCar();

        Boolean continueLoop = Boolean.TRUE;
        long t = System.currentTimeMillis();
        int count = 0;
        int totalImages = 0;
        fileOperation.write(OUTPUT_FILE, new String[]{"ImageFileName: Image Text \n"});
        while (continueLoop) {
            List<Message> messages = retrieveSqsMessages.receiveMessages(sqs, AWS_QUEUE_NAME);
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
                    List<String> results = processMessage(objectInfo[0], objectInfo[1], recognizeCar);
                }
                Thread.sleep(5000);
                count++;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println( "sleep for " +  (count * 5000) /1000  + " seconds ");

        }

    }

public static List<String> processMessage( String s3bucket, String keyName, RecognizeCar recognizeCar) throws IOException {
            List<String> results = new ArrayList<String>();
            System.out.println("Received Car from s3 Bucket" + s3bucket + "; File " + keyName);
            String res = detectText(s3bucket, keyName, recognizeCar);

            String temp_res = "";
            if (res.length() > 1)
                temp_res = keyName + ":  [ " + res + "]  \n";
            else
                temp_res = keyName + ":  [ NO IMAGE Text Found ]  \n";
            System.out.println(  temp_res );
            results.add( temp_res );
            fileOperation.write( OUTPUT_FILE, new String[]{temp_res});
            return results;
        }

public static String detectText(String s3bucket, String keyName, RecognizeCar recognizeCar) {
        return recognizeCar.DetectText(s3bucket, AWS_DEFAULT_REGION, keyName, THRESHOLD);
    }
}