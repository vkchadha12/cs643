package aws.sqs.helper;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.Map;

public class SqsQueueManager {

    private CreateQueueResponse createQueue(SqsClient sqs , String queueName) {

        CreateQueueRequest request = CreateQueueRequest.builder()
                .attributesWithStrings(Map.of("FifoQueue", "true", "ContentBasedDeduplication", "true"))
                .queueName(queueName)
                .build();
        return sqs.createQueue(request);
    }
    public String getQueueURL(SqsClient sqs , String queueName ) {
        try {
            ListQueuesRequest queues = ListQueuesRequest.builder()
                    .queueNamePrefix(queueName)
                    .build();
            ListQueuesResponse results = sqs.listQueues(queues);

            if (results.queueUrls().isEmpty()) {
                CreateQueueResponse queue = createQueue(sqs, queueName);
                System.out.println("Queue does not exists, Queue Created"  + queueName);
                return getURL(sqs, queueName);
            }
            return results.queueUrls().get(0);

        } catch (QueueNameExistsException e ) {
            throw e;
        }
    }

    private String getURL(SqsClient sqs, String queueName){

            GetQueueUrlRequest getQueueURL = GetQueueUrlRequest.builder()
                    .queueName(queueName)
                    .build();
            return  sqs.getQueueUrl(getQueueURL).queueUrl();
    }
}
