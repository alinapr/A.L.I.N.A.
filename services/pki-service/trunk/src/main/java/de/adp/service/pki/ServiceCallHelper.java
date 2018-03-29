package de.adp.service.pki;

import java.util.Collections;
import java.util.Map;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.json.DecodeException;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;

import de.adp.commons.process.ServiceCallAnnotation;
import de.adp.service.pki.exception.UnsolvedReferenceException;
import de.adp.service.pki.util.EventUtil;

/**
 * Helper to perform service calls.
 * @author simon.schwantzer(at)im-c.de
 */
public class ServiceCallHelper {
	private final Vertx vertx;
	private final JsonObject serviceConfig;
	private final Logger logger;
	
	/**
	 * Creates a service call helper.
	 * @param vertx Vert.x instance to create HTTP client instances.
	 * @param logger Logger for system information.
	 * @param serviceConfig Service configuration.
	 */
	public ServiceCallHelper(Vertx vertx, Logger logger, JsonObject serviceConfig) {
		this.vertx = vertx;
		this.serviceConfig = serviceConfig;
		this.logger = logger;
	}
	
	/**
	 * Performs a service call as specified in a service call annotation.
	 * @param serviceCallAnnotation Service call annotation specifying the call. 
	 * @param combinedStore Data storage containing all runtime data to resolve service call references.
	 * @param outputStore Data storage to store output in.
	 */
	public void performServiceCall(final ServiceCallAnnotation serviceCallAnnotation, Map<String, Object> combinedStore, final Map<String, Object> outputStore) {
		String serviceId = serviceCallAnnotation.getService();
		String methodId = serviceCallAnnotation.getMethod();
		StringBuilder pathBuilder = new StringBuilder(200);
		pathBuilder.append(serviceConfig.getString("baseUrl", "/services"));
		pathBuilder.append("/").append(serviceId);
		pathBuilder.append("/").append(methodId);
		String path = pathBuilder.toString();
		
		JsonObject inputObject;
		Map<String, String> inputMapping = serviceCallAnnotation.getInputMapping();
		Map<String, Object> inputMap;
		try {
			inputMap = EventUtil.resolveReferenceMap(Collections.<String, Object>unmodifiableMap(inputMapping), combinedStore);
			inputObject = new JsonObject(inputMap);
		} catch (UnsolvedReferenceException e) {
			logger.warn("Failed to resolve input parameter to perform service call.", e);
			return;
		}
		
		HttpClient httpClient = vertx.createHttpClient();
		httpClient.setHost(serviceConfig.getString("host", "localhost"));
		httpClient.setPort(serviceConfig.getInteger("port", 8080));
		httpClient.setSSL(serviceConfig.getBoolean("secure", false));
		HttpClientRequest request = httpClient.post(
			path,
			new Handler<HttpClientResponse>() {
			
			@Override
			public void handle(HttpClientResponse response) {
				response.bodyHandler(new Handler<Buffer>() {
					
					@Override
					public void handle(Buffer event) {
						String response = event.toString();
						String outputReference = serviceCallAnnotation.getOutputReference();
						if (outputReference != null) {
							outputStore.put(outputReference, response);
						}
						Map<String, String> outputMapping = serviceCallAnnotation.getOutputMapping();
						if (outputMapping != null) try {
							JsonObject responseObject = new JsonObject(response);
							for (String key : outputMapping.keySet()) {
								if (responseObject.containsField(key)) {
									outputStore.put(outputMapping.get(key), responseObject.getField(key));
								}
							}
						} catch (DecodeException e) {
							logger.warn("Failed to decode response.", e);
						}
					}
				});
			}
		});
		request.end(inputObject.encode());
	}
}
