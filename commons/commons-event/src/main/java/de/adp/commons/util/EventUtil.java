package de.adp.commons.util;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import de.adp.commons.event.*;
import de.adp.commons.event.ADPEvent.Type;

/**
 * Utility class for event handling.
 * @author simon.schwantzer(at)im-c.de
 */
public class EventUtil {
	private EventUtil() {};
	
	/**
	 * Parses a json map to generate an event object.
	 * @param content Map representing the json content tree.
	 * @return Instance of a specific appsist event class.  
	 * @throws IllegalArgumentException The given map does not represent a supported event implementation.
	 * @deprecated Use typed method {@link EventUtil#parseEvent(Map, Class)}. 
	 */
	@Deprecated
	public static ADPEvent parseEvent(Map<String, Object> content) throws IllegalArgumentException {
		String modelId = (String) content.get("modelId");
		if (modelId == null) {
			throw new IllegalArgumentException("Model id missing.");
		}
		ADPEvent event;
		switch (modelId) {
		case ProcessStartEvent.MODEL_ID:
			event = new ProcessStartEvent(content);
			break;
		case ProcessCompleteEvent.MODEL_ID:
			event = new ProcessCompleteEvent(content);
			break;
		case ProcessErrorEvent.MODEL_ID:
			event = new ProcessErrorEvent(content);
			break;
		case ProcessCancelledEvent.MODEL_ID:
			event = new ProcessCancelledEvent(content);
			break;
		case CallActivityEvent.MODEL_ID:
			event = new CallActivityEvent(content);
			break;
		case ManualTaskEvent.MODEL_ID:
			event = new ManualTaskEvent(content);
			break;
		case ServiceTaskEvent.MODEL_ID:
			event = new ServiceTaskEvent(content);
			break;
		case UserTaskEvent.MODEL_ID:
			event = new UserTaskEvent(content);
			break;
		case ProcessUserRequestEvent.MODEL_ID:
			event = new ProcessUserRequestEvent(content);
			break;
		case SupportRequestEvent.MODEL_ID:
			event = new SupportRequestEvent(content);
			break;
		case UserOnlineEvent.MODEL_ID:
			event = new UserOnlineEvent(content);
			break;
		case UserOfflineEvent.MODEL_ID:
			event = new UserOfflineEvent(content);
			break;
		case MachineStateChangedEvent.MODEL_ID :
			event = new MachineStateChangedEvent(content);
			break;
		default:
			event = new ADPEvent(content, Type.UNKNOWN);
		}
		return event;
	}
	
	/**
	 * Parses a json map to generate a typed event object.
	 * @param content Map representing the json content tree.
	 * @param type Event class to generate, e.g., <code>UserOnlineEvent.class</code>.
	 * @return Instance of a typed appsist event.  
	 * @throws IllegalArgumentException The given map does not represent the given event class.
	 */
	public static <T extends ADPEvent> T parseEvent(Map<String, Object> content, Class<T> type) throws IllegalArgumentException {
		String modelId = (String) content.get("modelId");
		if (modelId == null) {
			throw new IllegalArgumentException("Model id missing.");
		}
		try {
			T event = type.getConstructor(Map.class).newInstance(content);
			String classModelId = (String) type.getField("MODEL_ID").get(event);
			if (modelId.equals(classModelId)) {
				return event;
			} else {
				throw new IllegalArgumentException("Invalid model id: Expected " + classModelId + " but found " + modelId + ".");
			}
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException | NoSuchFieldException e) {
			throw new IllegalArgumentException("Failed to instantiate class.", e);
		}
	}
}
