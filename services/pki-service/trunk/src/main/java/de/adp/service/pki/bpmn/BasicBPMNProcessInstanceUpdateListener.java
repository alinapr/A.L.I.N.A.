package de.adp.service.pki.bpmn;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;

import de.adp.commons.event.ADPEvent;
import de.adp.commons.event.ADPEvent.Type;
import de.adp.commons.event.CallActivityEvent;
import de.adp.commons.event.ManualTaskEvent;
import de.adp.commons.event.ProcessCancelledEvent;
import de.adp.commons.event.ProcessCompleteEvent;
import de.adp.commons.event.ProcessErrorEvent;
import de.adp.commons.event.ProcessStartEvent;
import de.adp.commons.event.ServiceTaskEvent;
import de.adp.commons.event.TaskEvent;
import de.adp.commons.event.UserTaskEvent;
import de.adp.commons.process.Annotations;
import de.adp.commons.process.EventAnnotation;
import de.adp.commons.process.ProcessCallingElement;
import de.adp.commons.process.ProcessDefinition;
import de.adp.commons.process.ProcessElement;
import de.adp.commons.process.ProcessElementInstance;
import de.adp.commons.process.ProcessInstance;
import de.adp.commons.process.ProcessInstanceUpdateListener;
import de.adp.commons.process.ServiceCallAnnotation;
import de.adp.commons.process.TriggerAnnotation;
import de.adp.commons.process.bpmn.elements.BPMNManualTask;
import de.adp.commons.process.bpmn.elements.BPMNServiceTask;
import de.adp.commons.process.bpmn.elements.BPMNTask;
import de.adp.commons.process.bpmn.elements.BPMNUserTask;
import de.adp.commons.process.exception.AmbiguousFlowException;
import de.adp.service.pki.ServiceCallHelper;
import de.adp.service.pki.ServiceCallTriggerHandler;
import de.adp.service.pki.exception.UnsolvedReferenceException;
import de.adp.service.pki.util.EventUtil;

/**
 * Listener for process instance state changes.
 * This listener is used by the {@link BasicBPMNProcessService} and responsible for
 * (1) performing the process and
 * (2) handle actions indicated by process annotations
 * 
 * @author simon.schwantzer(at)im-c.de
 */
public class BasicBPMNProcessInstanceUpdateListener implements ProcessInstanceUpdateListener {
	private ProcessInstance processInstance;
	private final Map<String, BPMNElementTriggerEventHandler> processFlowTriggers;
	private final Vertx vertx;
	private final Logger logger;
	private final ServiceCallHelper serviceCallHelper;
	private final List<ServiceCallTriggerHandler> serviceCallTriggerHandlers, localServiceCallTriggerHandlers;
	private Date creationTime;
	private Date completionTime;
	
	/**
	 * Creates a new update listener.
	 * @param processInstance Process instance this listener is registered for.
	 * @param processService Process service object. Used to instantiate processes when call activities are called.
	 * @param vertx Vertx instance to access the event bus and HTTP client instances.
	 * @param serviceCallHelper Helper to perform service calls.
	 * @param logger Logger to post status information.
	 */
	public BasicBPMNProcessInstanceUpdateListener(ProcessInstance processInstance, Vertx vertx, Logger logger, ServiceCallHelper serviceCallHelper) {
		this.processInstance = processInstance;
		this.processFlowTriggers = new HashMap<String, BPMNElementTriggerEventHandler>();
		this.vertx = vertx;
		this.logger = logger;
		this.serviceCallHelper = serviceCallHelper;
		this.serviceCallTriggerHandlers = new ArrayList<>();
		this.localServiceCallTriggerHandlers = new ArrayList<>();
	}
	
	/**
	 * Publishes an event on the event bus.
	 * @param event Event to publish.
	 */
	private void publishEvent(String prefix, ADPEvent event) {
		JsonObject eventObject = new JsonObject(event.asMap());
		vertx.eventBus().publish(prefix + event.getModelId(), eventObject);
	}
	
	/**
	 * Generates a task event to be published during process execution.
	 * This method can be used for arbitrary BPMN task types.
	 * @param taskElement Task to announce.
	 * @return Event to publish.
	 */
	private ADPEvent generateTaskEvent(BPMNTask taskElement) {
		String id = UUID.randomUUID().toString();
		String processId = processInstance.getProcessDefinition().getId();
		String processInstanceId = processInstance.getId();
		String elementId = taskElement.getId();
		String taskTitle = taskElement.getLabel();
		String taskDescription = taskElement.getDescription();
		String userId = processInstance.getUserId();
		String sessionId = processInstance.getSessionId();
		
		TaskEvent event;
		switch (taskElement.getType()) {
		case BPMNManualTask.ELEMENT_TYPE:
			event = new ManualTaskEvent(id, processId, processInstanceId, elementId, taskTitle);
			break;
		case BPMNServiceTask.ELEMENT_TYPE:
			event = new ServiceTaskEvent(id, processId, processInstanceId, elementId);
			event.setTaskTitle(taskTitle);
			break;
		case BPMNUserTask.ELEMENT_TYPE:
			event = new UserTaskEvent(id, processId, processInstanceId, elementId, taskTitle);
			break;
		default:
			throw new IllegalArgumentException("Invalid task element: " + taskElement.getType());
		}
		if (taskDescription != null) event.setTaskDescription(taskDescription);
		if (userId != null) event.setUserId(userId);
		if (sessionId != null) event.setSessionId(sessionId);

		event.setProgress(calculateProgress());
		return event;
	}
	
	private double calculateProgress() {
		ProcessDefinition processDefinition = processInstance.getProcessDefinition();
		ProcessElement currentElement = processInstance.getCurrentState().getProcessElement();
		int distanceFromStart = processDefinition.getDistanceFromStart(currentElement);
		int distanceToEnd = processDefinition.getMaxDistanceToEnd(currentElement);
		return (double) distanceFromStart / (distanceFromStart + distanceToEnd);
	}
	
	@Override
	public void start() {
		ProcessDefinition processDefinition = processInstance.getProcessDefinition();
		Map<String, Object> contextData = processInstance.getProcessInstanceContext();
		Map<String, Object> processData = processDefinition.getAnnotations().getLocalDataAnnotations();
		Map<String, Object> combinedStore = EventUtil.combineMaps(contextData, processData);
		
		creationTime = new Date();
		
		@SuppressWarnings("unchecked")
		Map<String, Object> executionInfoMap = (Map<String, Object>) contextData.get("executionInfo");
		JsonObject executionInfo = executionInfoMap != null ? new JsonObject(executionInfoMap) : new JsonObject();
		executionInfo.putString("started", EventUtil.getDateAsISOString(creationTime));
		processInstance.getProcessInstanceContext().put("executionInfo", executionInfo.toMap());
		
		String id = UUID.randomUUID().toString();
		String userId = processInstance.getUserId();
		String sessionId = processInstance.getSessionId();
		ProcessStartEvent event = new ProcessStartEvent(id, processDefinition.getId(), processInstance.getId());
		if (userId != null) event.setUserId(userId);
		if (sessionId != null) event.setSessionId(sessionId);
		ProcessInstance parent = processInstance.getParent(); 
		if (parent != null) event.setParentInstance(parent.getId());
		event.setProgress(0.0d);
		publishEvent("adp:event:", event);
		
		for (EventAnnotation eventAnnotation : processDefinition.getAnnotations().getEventAnnotations()) {
			if (eventAnnotation.getType() == EventAnnotation.Type.START) {
				try {
					Map<String, Object> payload = EventUtil.resolveReferenceMap(eventAnnotation.getProperties(), combinedStore);
					ADPEvent newEvent = new ADPEvent(UUID.randomUUID().toString(), eventAnnotation.getEventId(), Type.SERVICE, payload);
					if (sessionId != null) newEvent.setSessionId(sessionId);
					publishEvent("adp:event:", newEvent);
				} catch (UnsolvedReferenceException e) {
					logger.warn("Failed to publish event on process start: " + processDefinition.getId() + " -> " + eventAnnotation.getEventId(), e);
				}
			}
		}
		
		for (ServiceCallAnnotation serviceCallAnnotation : processDefinition.getAnnotations().getServiceCallAnnotations()) {
			switch (serviceCallAnnotation.getType()) {
			case START:
				serviceCallHelper.performServiceCall(serviceCallAnnotation, combinedStore, contextData);
				break;
			case TRIGGER:
				ServiceCallTriggerHandler triggerHandler = new ServiceCallTriggerHandler(serviceCallAnnotation, combinedStore, contextData, serviceCallHelper, logger);
				TriggerAnnotation triggerAnnotation = serviceCallAnnotation.getTrigger();
				serviceCallTriggerHandlers.add(triggerHandler);
				vertx.eventBus().registerHandler("adp:event:" + triggerAnnotation.getEventId(), triggerHandler);
				break;
			default:
				// do nothing for END here
			}
		}
		
		try {
			processInstance.stepForward();
		} catch (AmbiguousFlowException e) {
			logger.warn("Failed to proceed process instance.", e);
		}
	}

	@Override
	public void end() {
		ProcessDefinition processDefinition = processInstance.getProcessDefinition();
		Map<String, Object> contextData = processInstance.getProcessInstanceContext();
		Map<String, Object> processData = processDefinition.getAnnotations().getLocalDataAnnotations();
		Map<String, Object> combinedStore = EventUtil.combineMaps(contextData, processData);
		
		completionTime = new Date();
		@SuppressWarnings("unchecked")
		Map<String, Object> executionInfoMap = (Map<String, Object>) contextData.get("executionInfo");
		JsonObject executionInfo = executionInfoMap != null ? new JsonObject(executionInfoMap) : new JsonObject();
		executionInfo.putString("ended", EventUtil.getDateAsISOString(completionTime));
		processInstance.getProcessInstanceContext().put("executionInfo", executionInfo.toMap());
		
		String id = UUID.randomUUID().toString();
		String userId = processInstance.getUserId();
		String sessionId = processInstance.getSessionId();
		ProcessCompleteEvent event = new ProcessCompleteEvent(id, processDefinition.getId(), processInstance.getId());
		if (userId != null) event.setUserId(userId);
		if (sessionId != null) event.setSessionId(sessionId);
		ProcessInstance parent = processInstance.getParent(); 
		if (parent != null) event.setParentInstance(parent.getId());
		event.setProgress(1.0d);
		publishEvent("adp:event:", event);
		
		for (EventAnnotation eventAnnotation : processDefinition.getAnnotations().getEventAnnotations()) {
			if (eventAnnotation.getType() == EventAnnotation.Type.END) {
				try {
					Map<String, Object> payload = EventUtil.resolveReferenceMap(eventAnnotation.getProperties(), combinedStore);
					ADPEvent newEvent = new ADPEvent(UUID.randomUUID().toString(), eventAnnotation.getEventId(), Type.SERVICE, payload);
					if (sessionId != null) newEvent.setSessionId(sessionId);
					publishEvent("adp:event:", newEvent);
				} catch (UnsolvedReferenceException e) {
					logger.warn("Failed to publish event on process end: " + processDefinition.getId() + " -> " + eventAnnotation.getEventId(), e);
				}
			}
		}
		
		for (ServiceCallAnnotation serviceCallAnnotation : processDefinition.getAnnotations().getServiceCallAnnotations()) {
			switch (serviceCallAnnotation.getType()) {
			case END:
				serviceCallHelper.performServiceCall(serviceCallAnnotation, combinedStore, contextData);
				break;
			default:
				// do nothing
			}
		}
		
		for (ServiceCallTriggerHandler triggerHandler : serviceCallTriggerHandlers) {
			String channel = "adp:event:" + triggerHandler.getServiceCallAnnotation().getTrigger().getEventId();
			vertx.eventBus().unregisterHandler(channel, triggerHandler);
		}
		serviceCallTriggerHandlers.clear();
		
		try {
			processInstance.stepForward();
		} catch (AmbiguousFlowException e) {
			logger.warn("Failed to proceed process instance.", e);
		}
		
		try {
			processInstance.stepForward();
		} catch (AmbiguousFlowException e) {
			logger.warn("Failed to end process.", e);
		}
	}
	
	@Override
	public void cancel() {
		ProcessDefinition processDefinition = processInstance.getProcessDefinition();
		Map<String, Object> contextData = processInstance.getProcessInstanceContext();
		
		completionTime = new Date();
		@SuppressWarnings("unchecked")
		Map<String, Object> executionInfoMap = (Map<String, Object>) contextData.get("executionInfo");
		JsonObject executionInfo = executionInfoMap != null ? new JsonObject(executionInfoMap) : new JsonObject();
		executionInfo.putString("cancelled", EventUtil.getDateAsISOString(completionTime));
		processInstance.getProcessInstanceContext().put("executionInfo", executionInfo.toMap());
		
		String id = UUID.randomUUID().toString();
		String userId = processInstance.getUserId();
		String sessionId = processInstance.getSessionId();
		ProcessCancelledEvent event = new ProcessCancelledEvent(id, processDefinition.getId(), processInstance.getId());
		if (userId != null) event.setUserId(userId);
		if (sessionId != null) event.setSessionId(sessionId);
		ProcessInstance parent = processInstance.getParent(); 
		if (parent != null) event.setParentInstance(parent.getId());
		event.setProgress(1.0d);
		publishEvent("adp:event:", event);
	}
	
	@Override
	public void error(ProcessElement element, int code, String message) {
		@SuppressWarnings("unchecked")
		Map<String, Object> executionInfoMap = (Map<String, Object>) processInstance.getProcessInstanceContext().get("executionInfo");
		JsonObject executionInfo = executionInfoMap != null ? new JsonObject(executionInfoMap) : new JsonObject();
		JsonObject error = new JsonObject();
		error.putString("elementId", element.getId());
		error.putString("time", EventUtil.getDateAsISOString(new Date()));
		error.putNumber("code", code);
		error.putString("message", message);
		executionInfo.putObject("error", error);
		processInstance.getProcessInstanceContext().put("executionInfo", executionInfo.toMap());
		
		String id = UUID.randomUUID().toString();
		String processId = processInstance.getProcessDefinition().getId();
		String processInstanceId = processInstance.getId();
		String elementId = element.getId();
		String userId = processInstance.getUserId();
		String sessionId = processInstance.getSessionId();
		ProcessInstance parent = processInstance.getParent(); 
		
		ProcessErrorEvent event = new ProcessErrorEvent(id, processId, processInstanceId, elementId, code, message);
		if (userId != null) event.setUserId(userId);
		if (sessionId != null) event.setSessionId(sessionId);
		if (parent != null) event.setParentInstance(parent.getId());
		event.setProgress(1.0d);
		publishEvent("adp:event:", event);
		
		try {
			processInstance.stepForward();
		} catch (AmbiguousFlowException e) {
			logger.warn("Failed to end process.", e);
		}
	}

	@Override
	public void activityCalled(ProcessCallingElement caller) {
		String id = UUID.randomUUID().toString();
		String processId = processInstance.getProcessDefinition().getId();
		String processInstanceId = processInstance.getId();
		String elementId = caller.getId();
		String userId = processInstance.getUserId();
		String sessionId = processInstance.getSessionId();
		String activityTitle = caller.getLabel();
		String activityProcessId = caller.getCalledProcess();
		
		CallActivityEvent event = new CallActivityEvent(id, processId, processInstanceId, elementId, activityProcessId);
		
		if (activityTitle != null) event.setActvitiyTitle(activityTitle);
		if (userId != null) event.setUserId(userId);
		if (sessionId != null) event.setSessionId(sessionId);
		event.setProgress(calculateProgress());
		
		publishEvent("adp:event:", event);
	}

	@Override
	public void stepPerformed(ProcessElementInstance oldState, ProcessElementInstance newState) {
		Map<String, Object> contextData = processInstance.getProcessInstanceContext();
		Map<String, Object> processData = processInstance.getProcessDefinition().getAnnotations().getLocalDataAnnotations();
		
		if (oldState != null) {
			// Unregister old event flow triggers.
			for (String channel : processFlowTriggers.keySet()) {
				vertx.eventBus().unregisterHandler(channel, processFlowTriggers.get(channel));
			}
			processFlowTriggers.clear();
			
			ProcessElement processElement = oldState.getProcessElement();
			Annotations annotations = processElement.getAnnotations();
			Map<String, Object> elementData = annotations.getLocalDataAnnotations();
			// Combine data from context, element, and process in this priority.
			Map<String, Object> combinedData = EventUtil.combineMaps(contextData, elementData, processData);

			// Perform all "onEnd" service calls.
			for (ServiceCallAnnotation serviceCallAnnotation : annotations.getServiceCallAnnotations()) {
				if (serviceCallAnnotation.getType() == ServiceCallAnnotation.Type.END) {
					serviceCallHelper.performServiceCall(serviceCallAnnotation, combinedData, contextData);
				}
			}
			
			for (ServiceCallTriggerHandler triggerHandler: localServiceCallTriggerHandlers) {
				String channel = "adp:event:" + triggerHandler.getServiceCallAnnotation().getTrigger().getEventId();
				vertx.eventBus().unregisterHandler(channel, triggerHandler);
			}
			localServiceCallTriggerHandlers.clear();

			// Publish all "onEnd" events.
			for (EventAnnotation eventAnnotation : annotations.getEventAnnotations()) {
				if (eventAnnotation.getType() == EventAnnotation.Type.END) {
					try {
						Map<String, Object> payload = EventUtil.resolveReferenceMap(eventAnnotation.getProperties(), combinedData);
						ADPEvent newEvent = new ADPEvent(UUID.randomUUID().toString(), eventAnnotation.getEventId(), Type.SERVICE, payload);
						String sessionId = processInstance.getSessionId();
						if (sessionId != null) newEvent.setSessionId(sessionId);
						publishEvent("adp:event:", newEvent);
					} catch (UnsolvedReferenceException e) {
						logger.warn("Failed to generate event during process execution.", e);
					}
				}
			}
		}
		if (newState != null) {
			ProcessElement processElement = newState.getProcessElement();
			boolean proceed = false;
			@SuppressWarnings("unchecked")
			Map<String, Object> executionInfoMap = (Map<String, Object>) processInstance.getProcessInstanceContext().get("executionInfo");
			JsonObject executionInfo = executionInfoMap != null ? new JsonObject(executionInfoMap) : new JsonObject();
			ADPEvent event;
			switch (processElement.getType()) {
			case BPMNServiceTask.ELEMENT_TYPE:
				proceed = true;
			case BPMNManualTask.ELEMENT_TYPE:
			case BPMNUserTask.ELEMENT_TYPE:
				event = generateTaskEvent((BPMNTask) processElement);
				publishEvent("adp:event:", event);
				break;
			}
			processInstance.getProcessInstanceContext().put("executionInfo", executionInfo.toMap());

			Annotations annotations = processElement.getAnnotations();
			Map<String, Object> elementData = annotations.getLocalDataAnnotations();
			Map<String, Object> combinedData = EventUtil.combineMaps(contextData, elementData, processData);
			for (EventAnnotation eventAnnotation : annotations.getEventAnnotations()) {
				if (eventAnnotation.getType() == EventAnnotation.Type.START) {
					try {
						Map<String, Object> payload = EventUtil.resolveReferenceMap(eventAnnotation.getProperties(), combinedData);
						ADPEvent newEvent = new ADPEvent(UUID.randomUUID().toString(), eventAnnotation.getEventId(), Type.SERVICE, payload);
						String sessionId = processInstance.getSessionId();
						if (sessionId != null) newEvent.setSessionId(sessionId);
						publishEvent("adp:event:", newEvent);
					} catch (UnsolvedReferenceException e) {
						logger.warn("Failed to generate event during process execution.", e);
					}
				}
			}
			for (ServiceCallAnnotation serviceCallAnnotation : annotations.getServiceCallAnnotations()) {
				switch (serviceCallAnnotation.getType()) {
				case START:
					serviceCallHelper.performServiceCall(serviceCallAnnotation, combinedData, contextData);
					break;
				case TRIGGER:
					ServiceCallTriggerHandler triggerHandler = new ServiceCallTriggerHandler(serviceCallAnnotation, combinedData, contextData, serviceCallHelper, logger);
					TriggerAnnotation triggerAnnotation = serviceCallAnnotation.getTrigger();
					localServiceCallTriggerHandlers.add(triggerHandler);
					vertx.eventBus().registerHandler("adp:event:" + triggerAnnotation.getEventId(), triggerHandler);
					break;
				default:
					// do nothing
				}
			}
			for (TriggerAnnotation triggerAnnotation : annotations.getTriggerAnnotations()) {
				String eventBusChannel = "adp:event:" + triggerAnnotation.getEventId();
				BPMNElementTriggerEventHandler triggerHandler = new BPMNElementTriggerEventHandler(processInstance, processElement, triggerAnnotation, combinedData, logger);
				
				processFlowTriggers.put(eventBusChannel, triggerHandler);
				vertx.eventBus().registerHandler(eventBusChannel, triggerHandler); 
			}
			if (proceed) {
				try {
					processInstance.stepForward();
				} catch (AmbiguousFlowException e) {
					logger.warn("Failed to proceed process instance.", e);
				}
			}
		}
	}
	
	/**
	 * Returns the time when the process instance was started.
	 * @return Time the start event was executed. May be <code>null</code> (not started yet).
	 */
	public Date getCreationTime() {
		return creationTime;
	}
	
	/**
	 * Returns the time when the process instance was completed.
	 * @return Time the end event was executed. May be <code>null</code> (not ended yet).
	 */
	public Date getCompletionTime() {
		return completionTime;
	}
}