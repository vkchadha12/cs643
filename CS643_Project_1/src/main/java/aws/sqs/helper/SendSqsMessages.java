package aws.sqs.helper;
import software.amazon.awssdk.core.internal.http.AmazonSyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.endpoints.internal.Value;
import software.amazon.awssdk.services.sqs.model.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class SendSqsMessages {
            //+ new Date().getTime();

    public Boolean send(SqsClient sqs , String QUEUE_NAME, String imageName) {
        //final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();

        //SqsClient sqsClient = SqsClient.create();
        try {
            // snippet-start:[sqs.java2.sqs_example.send__multiple_messages]
            SendMessageBatchRequest sendMessageBatchRequest = SendMessageBatchRequest.builder()
                    .queueUrl(QUEUE_NAME)
                    .entries(SendMessageBatchRequestEntry.builder().id("1").messageBody(imageName).delaySeconds(10).build())
                    .build();
            sqs.sendMessageBatch(sendMessageBatchRequest);
            System.out.println(sendMessageBatchRequest);
            // snippet-end:[sqs.java2.sqs_example.send__multiple_messages]

        } catch (SqsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        return Boolean.TRUE;
    }
}