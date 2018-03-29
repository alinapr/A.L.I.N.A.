package de.adp.service.pki;

import java.util.Map;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;

import de.adp.commons.event.ADPEvent;
import de.adp.commons.process.ServiceCallAnnotation;
import de.adp.service.pki.exception.UnsolvedReferenceException;
import de.adp.service.pki.util.EventUtil;

/**
 * Event bus handler for service call triggers.
 * @author simon.schwantzer(at)im-c.de
 */
public class ServiceCallTriggerHandler implements Handler<Message<JsonObject>> {
	private final ServiceCallAnnotation serviceCallAnnotation;
	private final Map<String, Object> dataStore, outputStore;
	private final Logger logger; 
	private final ServiceCallHelper serviceCallHelper;
	
	/**
	 * Creates the handler.
	 * @param serviceCallAnnotation Service call annotation observed by this handler.
	 * @param dataStore Data store to retrieve data for the comparison with incoming events.
	 * @param outputStore Data store to store output from service call.
	 * @param serviceCallHelper Helper to perform service calls.
	 * @param logger Logger for system information.
	 */
	public ServiceCallTriggerHandler(ServiceCallAnnotation serviceCallAnnotation, Map<String, Object> dataStore, Map<String, Object> outputStore, ServiceCallHelper serviceCallHelper, Logger logger) {
		this.serviceCallAnnotation = serviceCallAnnotation;
		this.dataStore = dataStore;
		this.outputStore = outputStore;
		this.logger = logger;
		this.serviceCallHelper = serviceCallHelper;
	}
	
	/**
	 * Returns the service call observed by this handler.
	 * @return Service call annotation.
	 */
	public ServiceCallAnnotation getServiceCallAnnotation() {
		return serviceCallAnnotation;
	}

	@Override
	public void handle(Message<JsonObject> message) {
		JsonObject body = message.body();
		try {
			ADPEvent event = de.adp.commons.util.EventUtil.parseEvent(body.toMap());
			if (EventUtil.doesEventMatch(event, serviceCallAnnotation.getTrigger(), dataStore)) {
				serviceCallHelper.performServiceCall(serviceCallAnnotation, dataStore, outputStore);
			}
		} catch (IllegalArgumentException e) {
			logger.info("Failed to parse event.", e);
		} catch (UnsolvedReferenceException e) {
			logger.warn("Failed to handle event.", e);
		}
	}
}
