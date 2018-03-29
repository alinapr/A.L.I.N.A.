package de.adp.commons.event;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Events generated when a manual task becomes active during process execution.
 * @author simon.schwantzer(at)im-c.de
 */
public class ProcessUserRequestEvent extends ProcessEvent {
	public static final String MODEL_ID = "processEvent:userRequest";
	
	/**
	 * Creates the event based on a content map.
	 * @param content Map with the event content.
	 * @throws IllegalArgumentException One or more required fields are missing.
	 */
	public ProcessUserRequestEvent(Map<String, Object> content) throws IllegalArgumentException {
		super(content);
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> request = (Map<String, Object>) getPayload().get("request");
			if (request != null) {
				if (!request.containsKey("message")) {
					throw new IllegalArgumentException("Missing field: payload.request.message");
				}
				if (!request.containsKey("options")) {
					throw new IllegalArgumentException("Missing field: payload.request.options");
				}
			} else {
				throw new IllegalArgumentException("Missing field: payload.request");
			}
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("Wrong field type: payload.request");
		}
	}
	
	/**
	 * Creates a user request event.
	 * @param id ID for the event instance.
	 * @param processId ID of the related process definition.
	 * @param processInstanceId ID of the related process instance.
	 * @param elementId ID of the related gateway element.
	 * @param message Message to display to the user.
	 * @param Map<String, String> options Map with targeted process elements as keys and option texts as values. Is is recommended to use an ordered map, e.g. a {@link java.util.LinkedHashMap}.
	 */
	public ProcessUserRequestEvent(String id, String processId, String processInstanceId, String elementId, String message, Map<String, String> options) {
		super(id, MODEL_ID, processId, processInstanceId);
		super.setElementId(elementId);
		Map<String, Object> request = new LinkedHashMap<String, Object>();
		request.put("message", message);
		List<Object> optionList = new ArrayList<Object>();
		for (Map.Entry<String, String> entry : options.entrySet()) {
			Map<String, Object> optionListEntry = new LinkedHashMap<String, Object>();
			optionListEntry.put("display", entry.getValue());
			optionListEntry.put("target", entry.getKey());
			optionList.add(optionListEntry);
		}
		request.put("options", optionList);
		getPayload().put("request", request);
	}
	
	/**
	 * Returns the message to display to the user.
	 * @return Message to display.
	 */
	public String getMessage() {
		@SuppressWarnings("unchecked")
		Map<String, Object> request = (Map<String, Object>) getPayload().get("request");
		return (String) request.get("message");
	}
	
	/**
	 * Returns the options to display.
	 * @return Map with identifier of the targeted process element as keys and the display texts as values.
	 */
	@SuppressWarnings("unchecked")
	public Map<String, String> getOptions() {
		Map<String, Object> request = (Map<String, Object>) getPayload().get("request");
		List<Object> optionList = (List<Object>) request.get("options");
		Map<String, String> options = new LinkedHashMap<String, String>();
		for (Object optionListEntryObject : optionList) {
			Map<String, Object> optionListEntry = (Map<String, Object>) optionListEntryObject;
			options.put((String) optionListEntry.get("target"), (String) optionListEntry.get("display"));
		}
		return options;
	}
}