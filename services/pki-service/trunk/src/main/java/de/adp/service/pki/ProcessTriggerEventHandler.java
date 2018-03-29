package de.adp.service.pki;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;

import de.adp.commons.event.ADPEvent;
import de.adp.commons.process.ProcessDefinition;
import de.adp.commons.process.TriggerAnnotation;
import de.adp.service.pki.exception.UnsolvedReferenceException;
import de.adp.service.pki.util.EventUtil;

/**
 * Eventbus handler for process triggers.
 * @author simon.schwantzer(at)im-c.de
 */
public class ProcessTriggerEventHandler implements Handler<Message<JsonObject>> {
	private final TriggerAnnotation triggerAnnotation;
	private final ProcessService processService;
	private final ProcessDefinition processDefinition;
	private final Logger logger; 
	
	/**
	 * Creates the handler.
	 * @param triggerAnnotation Trigger annotation specifying the event to listen on.
	 * @param processDefinition Process definition to instantiate if triggered.
	 * @param processService Process service to perform the instantiation.
	 * @param logger Logger for status information.
	 */
	public ProcessTriggerEventHandler(TriggerAnnotation triggerAnnotation, ProcessDefinition processDefinition, ProcessService processService, Logger logger) {
		this.triggerAnnotation = triggerAnnotation;
		this.processService = processService;
		this.processDefinition = processDefinition;
		this.logger = logger;
	}
	
	/**
	 * Returns the annotation specifying the trigger. 
	 * @return Trigger annotation of a process definition.
	 */
	public TriggerAnnotation getTriggerAnnotation() {
		return triggerAnnotation;
	}

	@Override
	public void handle(Message<JsonObject> message) {
		JsonObject body = message.body();
		try {
			ADPEvent event = de.adp.commons.util.EventUtil.parseEvent(body.toMap());
			if (EventUtil.doesEventMatch(event, triggerAnnotation, processDefinition.getAnnotations().getLocalDataAnnotations())) {
				processService.instantiateProcess(processDefinition.getId(), body.getString("userId"), event.getSessionId(), null);
			}
		} catch (IllegalArgumentException e) {
			logger.info("Failed to parse event.", e);
		} catch (UnsolvedReferenceException e) {
			logger.warn("Failed to handle event.", e);
		}
	}
}
