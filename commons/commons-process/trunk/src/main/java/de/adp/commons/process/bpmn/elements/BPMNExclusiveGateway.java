package de.adp.commons.process.bpmn.elements;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Element;

/**
 * Class representing a BPMN Exclusive Gateway.
 * @author simon.schwantzer(at)im-c.de
 */
public class BPMNExclusiveGateway extends BPMNGateway {
	public static final String ELEMENT_TYPE = "exclusiveGateway";

	public BPMNExclusiveGateway(Element element) {
		super(element);
	}

	@Override
	public String getType() {
		return ELEMENT_TYPE;
	}

	/**
	 * Returns a message to be displayed for the user.
	 * The message should enable the user to select one of the possible options. 
	 * @return Message to be displayed.
	 */
	public String getRequestMessage() {
		String message = (String) annotations.getLocalDataAnnotations().get("requestMessage");
		if (message == null) {
			message = super.getLabel();
		}
		return message;
	}
	
	/**
	 * Returns the options for the gateway.
	 * @return Map of keys and process element IDs.
	 */
	@SuppressWarnings("unchecked")
	public Map<String, String> getOptions() {
		Map<String, String> options = new LinkedHashMap<String, String>();
		List<Object> optionList = (List<Object>) annotations.getLocalDataAnnotations().get("responseOptions");
		if (optionList != null) {
			for (Object option : optionList) {
				if (!(option instanceof Map<?, ?>)) continue;
				Map<String, Object> optionDataStore = (Map<String, Object>) option;
				options.put((String) optionDataStore.get("display"), (String) optionDataStore.get("target"));
			}
		}
		return options;
	}

}
