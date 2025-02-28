import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.syndicate.deployment.annotations.events.SqsTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;

import java.util.HashMap;
import java.util.Map;

@LambdaHandler(
    lambdaName = "sqs_handler",
    roleName = "sqs_handler-role",
    isPublishVersion = true,
    aliasName = "${lambdas_alias_name}",
    logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@SqsTriggerEventSource(
    targetQueue = "async_queue",
    batchSize = 10
)
@DependsOn(
    name = "async_queue",
    resourceType = ResourceType.SQS_QUEUE
)
public class SqsHandler implements RequestHandler<SQSEvent, Void> {

    public Void handleRequest(SQSEvent request, Context context) {
        context.getLogger().log(request.toString());
        System.out.println("Hello from lambda");

        // You can still create and use the Map, but you cannot return it
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("statusCode", 200);
        resultMap.put("body", "Hello from Lambda");

        // Log the result map if needed
        context.getLogger().log("Result Map: " + resultMap.toString());

        // Return null since the method must return Void
        return null;
    }
}