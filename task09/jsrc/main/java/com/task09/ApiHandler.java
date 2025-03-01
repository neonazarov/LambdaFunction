package com.task09;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;

import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaLayer;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.Architecture;
import com.syndicate.deployment.model.ArtifactExtension;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;
import com.task09.layer.OpenMeteoClient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


@LambdaHandler(
    lambdaName = "api_handler",
	roleName = "api_handler-role",
	isPublishVersion = true,
	aliasName = "${lambdas_alias_name}",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED,
		layers = {"weatherClient"}
)
@LambdaLayer(
		layerName = "weatherClient",
		libraries = {"lib/OpenMeteoClient.jar"},
		runtime = DeploymentRuntime.JAVA11,
		artifactExtension = ArtifactExtension.ZIP
)
@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)
public class ApiHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
	OpenMeteoClient openMeteoClient = new OpenMeteoClient();

	public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent request, Context context) {
		APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();

		String method = getMethod(request);
		String path = getPath(request);

		context.getLogger().log(String.format("Received request: method: %s, path: %s", method, path));

		if (method.equals("GET") && path.equals("/weather")) {
			try {
				HttpResponse<String> httpResponse = openMeteoClient.getWeather();

				response.setStatusCode(200);
				response.setBody(httpResponse.body());
			} catch (URISyntaxException | IOException | InterruptedException e) {
				context.getLogger().log("Error: " + e.getMessage());
				throw new RuntimeException(e);
			}
			return response;
		}
		response.setStatusCode(400);
		response.setBody(String.format("{\"statusCode\": 400, \"message\": \"Bad request syntax or unsupported method. Request path: %s. HTTP method: %s\"}", path, method));
		return response;
	}
	private String getMethod(APIGatewayV2HTTPEvent requestEvent) {
		return requestEvent.getRequestContext().getHttp().getMethod();
	}

	private String getPath(APIGatewayV2HTTPEvent requestEvent) {
		return requestEvent.getRequestContext().getHttp().getPath();
	}
}
