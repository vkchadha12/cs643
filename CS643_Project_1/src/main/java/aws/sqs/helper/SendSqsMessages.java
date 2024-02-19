package aws.sqs.helper;
import software.amazon.awssdk.core.internal.http.AmazonSyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.endpoints.internal.Value;
import software.amazon.awssdk.services.sqs.model.*;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class SendSqsMessages {
    public Boolean send(SqsClient sqs , String QUEUE_NAME, String imageName , String messageGroupId) {
        SqsQueueManager sqsQueueManager = new SqsQueueManager();
        sqsQueueManager.getQueueURL(sqs , QUEUE_NAME);
        String uniqueID = UUID.randomUUID().toString();
        try {
            SendMessageRequest builder = SendMessageRequest.builder()
                    .queueUrl(QUEUE_NAME)
                    .messageBody(imageName)
                    .messageGroupId(messageGroupId)
                    .messageDeduplicationId(uniqueID)
                    .build();
            sqs.sendMessage(builder);
            System.out.println(builder);

        } catch (SqsException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        return Boolean.TRUE;
    }
}