package de.adp.commons.event;

import java.util.Map;

import de.adp.commons.util.EventUtil;

/**
 * Event generated when a process has been instantiated.
 * @author simon.schwantzer(at)im-c.de
 */
public class ProcessStartEvent extends ProcessEvent {
	public static final String MODEL_ID = "processEvent:processStart";
	
	/**
	 * Creates the event based on a content map.
	 * @param content Map with the event content.
	 * @throws IllegalArgumentException One or more required fields are missing.
	 */
	public ProcessStartEvent(Map<String, Object> content) {
		super(content);
	}
	
	/**
	 * Creates a process start event.
	 * @param id ID for the event instance.
	 * @param processId ID of the related process definition.
	 * @param processInstanceId ID of the related process instance.
	 */
	public ProcessStartEvent(String id, String processId, String processInstanceId) {
		super(id, MODEL_ID, processId, processInstanceId);
	}
	
	/**
	 * Sets the event which triggered the process start. 
	 * @param trigger APPsist event which triggered the process instantiation.
	 */
	public void setTrigger(ADPEvent trigger) {
		super.getPayload().put("trigger", trigger.asMap());
	}
	
	/**
	 * Returns the event which triggered the process start. 
	 * @return APPsist event which triggered the process instantiation.
	 */
	public ADPEvent getTrigger() {
		@SuppressWarnings("unchecked")
		Map<String, Object> content = (Map<String, Object>) super.getPayload().get("trigger");
		if (content != null) {
			return EventUtil.parseEvent(content);
		} else {
			return null;
		}
	}
	
	/**
	 * Sets the ID of the parent process instance.
	 * @param parentInstance Identifier of the process instance which created this one as a subprocess. 
	 */
	public void setParentInstance(String parentInstance) {
		super.getPayload().put("parentInstance", parentInstance);
	}
	
	/**
	 * Returns the ID of the parent process instance.
	 * @return Identifier of the process instance which created this one as a subprocess. Returns <code>null</code> if this is the top level process instance.
	 */
	public String getParentInstance() {
		return (String) super.getPayload().get("parentInstance");
	}
}
