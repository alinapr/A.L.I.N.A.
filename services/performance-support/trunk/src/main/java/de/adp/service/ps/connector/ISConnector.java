package de.adp.service.ps.connector;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.json.JsonObject;

import de.adp.service.ps.HttpException;

public class ISConnector {
	private final HttpClient isClient;
	private final String basePath;
	
	public ISConnector(Vertx vertx, JsonObject serviceConfig) {
		isClient = vertx.createHttpClient();
		isClient.setHost(serviceConfig.getString("host"));
		isClient.setPort(serviceConfig.getInteger("port"));
		isClient.setSSL(serviceConfig.getBoolean("secure", false));
		basePath = serviceConfig.getObject("paths").getString("ihs");
	}
	
	/*
	public void getContentForTask(String userId, String processId, String processInstanceId, String elementId, final AsyncResultHandler<JsonObject> resultHandler) {
		resultHandler.handle(new AsyncResult<JsonObject>() {
			@Override
			public boolean succeeded() {
				return true;
			}
			
			@Override
			public JsonObject result() {
				JsonObject dummy = new JsonObject();
				dummy.putString("contentId", "default");
				return dummy;
			}
			
			@Override
			public boolean failed() {
				return false;
			}
			
			@Override
			public Throwable cause() {
				return null;
			}
		});
	}
	*/
	
	public void getContentForTask(String userId, String processId, String processInstanceId, String elementId, final AsyncResultHandler<JsonObject> resultHandler) {
		StringBuilder path = new StringBuilder();
		path.append(basePath).append("/content").append("?processId=").append(processId).append("&elementId=").append(elementId).append("&userId=").append(userId);
		isClient.get(path.toString(), new Handler<HttpClientResponse>() {
			
			@Override
			public void handle(final HttpClientResponse response) {
				response.bodyHandler(new Handler<Buffer>() {
					
					@Override
					public void handle(final Buffer buffer) {
						
						resultHandler.handle(new AsyncResult<JsonObject>() {
							@Override
							public boolean succeeded() {
								return response.statusCode() == 200;
							}
							
							@Override
							public JsonObject result() {
								return succeeded() ? new JsonObject(buffer.toString()) : null;
							}
							
							@Override
							public boolean failed() {
								return !succeeded();
							}
							
							@Override
							public Throwable cause() {
								return failed() ? new HttpException(buffer.toString(), response.statusCode()) : null;
							}
						});
					}
				});
			}
		}).end();
	}
}
