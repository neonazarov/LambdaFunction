// Source code is decompiled from a .class file using FernFlower decompiler.
package com.task06;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.events.DynamoDbTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

@LambdaHandler(
		lambdaName = "audit_producer",
		roleName = "audit_producer-role",
		isPublishVersion = true,
		aliasName = "${lambdas_alias_name}",
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@DynamoDbTriggerEventSource(
		targetTable = "Configuration",
		batchSize = 1
)
@DependsOn(
		name = "Configuration",
		resourceType = ResourceType.DYNAMODB_TABLE
)
@EnvironmentVariables({@EnvironmentVariable(
		key = "region",
		value = "${region}"
), @EnvironmentVariable(
		key = "target_table",
		value = "${target_table}"
)})
public class AuditProducer implements RequestHandler<DynamodbEvent, Void> {
	private static final String INSERT = "INSERT";
	private static final String MODIFY = "MODIFY";
	private static final DynamoDbClient dynamoDb = (DynamoDbClient)((DynamoDbClientBuilder)DynamoDbClient.builder().region(Region.of(System.getenv("region")))).build();

	public AuditProducer() {
	}

	public Void handleRequest(DynamodbEvent event, Context context) {
		Iterator var4 = event.getRecords().iterator();

		while(true) {
			DynamodbEvent.DynamodbStreamRecord record;
			do {
				if (!var4.hasNext()) {
					return null;
				}

				record = (DynamodbEvent.DynamodbStreamRecord)var4.next();
			} while(!"INSERT".equals(record.getEventName()) && !"MODIFY".equals(record.getEventName()));

			this.processRecord(record, context);
		}
	}

	private void processRecord(DynamodbEvent.DynamodbStreamRecord record, Context context) {
		context.getLogger().log("Stream record is: " + String.valueOf(record));
		Map<String, AttributeValue> oldItem = record.getDynamodb().getOldImage();
		Map<String, AttributeValue> newItem = record.getDynamodb().getNewImage();
		context.getLogger().log("Old item is: " + String.valueOf(oldItem));
		context.getLogger().log("New item is: " + String.valueOf(newItem));
		String itemKey = ((AttributeValue)newItem.get("key")).getS();
		Map auditItem;
		if (oldItem == null) {
			context.getLogger().log("Inserting");
			Map<String, software.amazon.awssdk.services.dynamodb.model.AttributeValue> newItemValue = Map.of("key", (software.amazon.awssdk.services.dynamodb.model.AttributeValue)software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder().s(((AttributeValue)newItem.get("key")).getS()).build(), "value", (software.amazon.awssdk.services.dynamodb.model.AttributeValue)software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder().n(((AttributeValue)newItem.get("value")).getN()).build());
			context.getLogger().log("New item value is: " + String.valueOf(newItemValue));
			auditItem = Map.of("id", (software.amazon.awssdk.services.dynamodb.model.AttributeValue)software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder().s(UUID.randomUUID().toString()).build(), "itemKey", (software.amazon.awssdk.services.dynamodb.model.AttributeValue)software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder().s(itemKey).build(), "modificationTime", (software.amazon.awssdk.services.dynamodb.model.AttributeValue)software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder().s(Instant.now().toString()).build(), "newValue", (software.amazon.awssdk.services.dynamodb.model.AttributeValue)software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder().m(newItemValue).build());
		} else {
			context.getLogger().log("Modifying");
			int oldValue = Integer.parseInt(((AttributeValue)oldItem.get("value")).getN());
			int newValue = Integer.parseInt(((AttributeValue)newItem.get("value")).getN());
			auditItem = Map.of("id", (software.amazon.awssdk.services.dynamodb.model.AttributeValue)software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder().s(UUID.randomUUID().toString()).build(), "itemKey", (software.amazon.awssdk.services.dynamodb.model.AttributeValue)software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder().s(itemKey).build(), "modificationTime", (software.amazon.awssdk.services.dynamodb.model.AttributeValue)software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder().s(Instant.now().toString()).build(), "updatedAttribute", (software.amazon.awssdk.services.dynamodb.model.AttributeValue)software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder().s("value").build(), "oldValue", (software.amazon.awssdk.services.dynamodb.model.AttributeValue)software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder().n(String.valueOf(oldValue)).build(), "newValue", (software.amazon.awssdk.services.dynamodb.model.AttributeValue)software.amazon.awssdk.services.dynamodb.model.AttributeValue.builder().n(String.valueOf(newValue)).build());
		}

		context.getLogger().log("AuditItem is:" + String.valueOf(auditItem));
		dynamoDb.putItem((PutItemRequest)PutItemRequest.builder().tableName(System.getenv("target_table")).item(auditItem).build());
	}
}
