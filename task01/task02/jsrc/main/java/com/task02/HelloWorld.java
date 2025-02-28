package com.task02;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;

import java.util.Map;



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
public class HelloWorld implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();


	public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent request, Context context) {
		APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
		ResponseBody body = new ResponseBody();

		String method = getMethod(request);
		String path = getPath(request);

		response.setHeaders(Map.of("Content-Type", "application/json"));

		if ("/hello".equals(path) && "GET".equals(method)) {
			body.setStatusCode(200);
			body.setMessage("Hello from Lambda");

			response.setStatusCode(200);
			response.setBody(gson.toJson(body));
		} else {
			body.setStatusCode(400);
			body.setMessage("Bad request syntax or unsupported method. Request path: " + path + ". HTTP method: " + method);

			response.setStatusCode(400);
			response.setBody(gson.toJson(body));
		}
		return response;
	}

	private String getMethod(APIGatewayV2HTTPEvent requestEvent) {
		return requestEvent.getRequestContext().getHttp().getMethod();
	}

	private String getPath(APIGatewayV2HTTPEvent requestEvent) {
		return requestEvent.getRequestContext().getHttp().getPath();
	}
}