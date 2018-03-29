package de.adp.commons.event;

import java.util.Map;

/**
 * Event generated when user logs in successfully. 
 * @author simon.schwantzer(at)im-c.de
 *
 */
public class MachineStateChangedEvent
    extends ADPEvent
{
    public static final String MODEL_ID = "machinestateChanged";

	/**
	 * Creates the event based on a content map.
	 * @param content Map with the event content.
	 * @throws IllegalArgumentException One or more required fields are missing.
	 */
    public MachineStateChangedEvent(Map<String, Object> content)
        throws IllegalArgumentException
    {
        super(content, Type.SERVICE);
		if (super.getSessionId() == null) {
			throw new IllegalArgumentException("Missing field: session");
		}		
		if (super.getPayload() == null) {
			throw new IllegalArgumentException("Missing field: payload");
		}

        if (!super.getPayload().containsKey("machineId")
                && !super.getPayload().containsKey("stationId")) {
            throw new IllegalArgumentException(
                    "Missing fields: payload.machineId and payload.stationId");
		}
        if (!super.getPayload().containsKey("machineState")) {
            throw new IllegalArgumentException("Missing field: payload.userId");
        }
	}
	
	/**
	 * Creates the event.
	 * @param id ID for the event instance.
	 * @param sessionId ID of the user who logged in.
	 * @param userId ID of the user who logged in.
	 * @param deviceId ID of the device the user logged in with. 
	 */
    public MachineStateChangedEvent(String id, String sessionId, String machineId,
 String stationId,
            String severityLevel, String machineState, String errorTag)
    {
        super(id, MODEL_ID, Type.SERVICE);
		setSessionId(sessionId);
		getPayload().put("machineId", machineId);
		getPayload().put("stationId", stationId);
        getPayload().put("severityLevel", severityLevel);
		getPayload().put("machineState", machineState);
        getPayload().put("errorTag", errorTag);
	}
	
	    /**
     * Returns the ID of the machine whose state changed
     * 
     * @return ID of machine.
     */
    public String getMachineId()
    {
		return (String) getPayload().get("userId");
	}
	
	    /**
     * Returns the ID of the machine whose state changed
     * 
     * @return station ID.
     */
    public String getStationId()
    {
        return (String) getPayload().get("stationId");
	}

    /**
     * Returns the severity level of the machinestate
     * 
     * @return severity level.
     */
    public String getSeverityLevel()
    {
        return (String) getPayload().get("severityLevel");
    }

    /**
     * Returns the severity level of the machinestate
     * 
     * @return machine state.
     */
    public String getMachineState()
    {
        return (String) getPayload().get("machineState");
    }

    /**
     * Returns the error state of the machinestate
     * 
     * @return error tag.
     */
    public String getErrorTag()
    {
        return (String) getPayload().get("errorTag");
    }
}
