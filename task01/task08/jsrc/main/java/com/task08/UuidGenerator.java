package com.task08;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.util.StringInputStream;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.events.RuleEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@LambdaHandler(
    lambdaName = "uuid_generator",
	roleName = "uuid_generator-role",
	isPublishVersion = true,
	aliasName = "${lambdas_alias_name}",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@EnvironmentVariables(value = {
		@EnvironmentVariable(key = "region", value = "${region}"),
		@EnvironmentVariable(key = "target_bucket", value = "${target_bucket}")
})
@RuleEventSource(targetRule = "uuid_trigger")
@DependsOn(name = "uuid_trigger", resourceType = ResourceType.CLOUDWATCH_RULE)
@DependsOn(name = "${target_bucket}", resourceType = ResourceType.S3_BUCKET)
public class UuidGenerator implements RequestHandler<ScheduledEvent, String> {
	private final AmazonS3 s3Client = AmazonS3ClientBuilder
			.standard()
			.withRegion(System.getenv("region")).build();
	private final ObjectMapper objectMapper = new ObjectMapper();

	public String handleRequest(ScheduledEvent request, Context context) {
		Map<String, List<String>> uuids = new HashMap<>();
		uuids.put("ids", IntStream.range(0, 10)
				.mapToObj(i -> UUID.randomUUID().toString())
				.collect(Collectors.toList()));

		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentType("application/json");

        PutObjectRequest putObjectRequest = null;
        try {
            putObjectRequest = new PutObjectRequest(
					System.getenv("target_bucket"),
					Instant.now().toString(),
					new StringInputStream(objectMapper.writeValueAsString(uuids)),
					metadata);
			context.getLogger().log("PutObjectRequest: " + putObjectRequest.toString());

        } catch (UnsupportedEncodingException e) {
			context.getLogger().log("UnsupportedEncodingException: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
			context.getLogger().log("JsonProcessingException: " + e.getMessage());
            throw new RuntimeException(e);
        }

        PutObjectResult putObjectResult = s3Client.putObject(putObjectRequest);
		context.getLogger().log("PutObjectResult: " + putObjectResult.toString());

		return "Hello from Lambda";
	}
}
