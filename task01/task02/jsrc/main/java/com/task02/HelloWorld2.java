package com.task02;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;

import java.text.spi.DateFormatProvider;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.UUID;


@LambdaHandler(
    lambdaName = "hello_world",
	roleName = "hello_world-role",
	isPublishVersion = true,
	aliasName = "${lambdas_alias_name}",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)
public class HelloWorld2 implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private final DynamoDbClient dynamoDbClient = DynamoDbClient.builder().region(Region.of("${region}")).build();
	private final ObjectMapper objectMapper = new ObjectMapper();

	public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent request, Context context) {
		APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();

        try {
            Request requestBody = objectMapper.readValue(request.getBody(), Request.class);
			Item item = new Item();
			item.setPrincipleId(requestBody.getPrincipleId());
			item.setBody(requestBody.getContent());

			putItem(item);

			APIGatewayV2HTTPResponse resp = new APIGatewayV2HTTPResponse();
			resp.setStatusCode(200);

			Response responseEvent = new Response();
			responseEvent.setStatusCode(201);
			responseEvent.setEvent(item);

			resp.setBody(gson.toJson(responseEvent));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return response;
	}

	private void putItem(Item request) throws Exception {
		PutItemRequest putItemRequest = PutItemRequest.builder()
				.tableName("${target_table}")
				.item(request.mapperToAttributeValueMap()).build();

		PutItemResponse response = dynamoDbClient.putItem(putItemRequest);

	}

	private String getMethod(APIGatewayV2HTTPEvent requestEvent) {
		return requestEvent.getRequestContext().getHttp().getMethod();
	}

	private String getPath(APIGatewayV2HTTPEvent requestEvent) {
		return requestEvent.getRequestContext().getHttp().getPath();
	}
}