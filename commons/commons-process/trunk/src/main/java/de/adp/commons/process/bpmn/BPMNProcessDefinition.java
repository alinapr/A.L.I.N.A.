package de.adp.commons.process.bpmn;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import de.adp.commons.process.Annotations;
import de.adp.commons.process.ProcessDefinition;
import de.adp.commons.process.ProcessElement;
import de.adp.commons.process.bpmn.elements.BPMNCallActivity;
import de.adp.commons.process.bpmn.elements.BPMNEndEvent;
import de.adp.commons.process.bpmn.elements.BPMNExclusiveGateway;
import de.adp.commons.process.bpmn.elements.BPMNIntermediateEvent;
import de.adp.commons.process.bpmn.elements.BPMNManualTask;
import de.adp.commons.process.bpmn.elements.BPMNServiceTask;
import de.adp.commons.process.bpmn.elements.BPMNStartEvent;
import de.adp.commons.process.bpmn.elements.BPMNUserTask;
import de.adp.commons.process.bpmn.exception.InvalidBPMNProcessStructure;

/**
 * Process definition for XML encoded BPMN processes.
 * It is implemented as wrapper for the XML object.
 * @author simon.schwantzer(at)im-c.de
 *
 */
public class BPMNProcessDefinition implements ProcessDefinition {
	private Element processXMLElement;
	private BPMNAnnotations annotations;
	private BPMNStartEvent startEvent;
	private Set<BPMNEndEvent> secureEndEvents;
	private DirectedGraph<BPMNProcessElement, DefaultEdge> processGraph;
	private FloydWarshallShortestPaths<BPMNProcessElement, DefaultEdge> shortestPaths;
	private Map<String, BPMNProcessElement> processElements ;
	
	public BPMNProcessDefinition(Element processElement) throws InvalidBPMNProcessStructure {
		this.processXMLElement = processElement;
		processElements = new HashMap<String, BPMNProcessElement>();
		secureEndEvents = new HashSet<BPMNEndEvent>();
		processGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
		
		for (Element element : processXMLElement.getChildren()) {
			if (!BPMNNamespace.BPMN.equals(element.getNamespace())) continue;
			BPMNProcessElement bpmnProcessElement;
			switch (element.getName()) {
			case BPMNUserTask.ELEMENT_TYPE:
				bpmnProcessElement = new BPMNUserTask(element);
				break;
			case BPMNManualTask.ELEMENT_TYPE:
				bpmnProcessElement = new BPMNManualTask(element);
				break;
			case BPMNServiceTask.ELEMENT_TYPE:
				bpmnProcessElement = new BPMNServiceTask(element);
				break;
			case BPMNStartEvent.ELEMENT_TYPE:
				if (startEvent != null) {
					throw new InvalidBPMNProcessStructure("Multiple start events found.");
				}
				startEvent = new BPMNStartEvent(element);
				bpmnProcessElement = startEvent;
				break;
			case BPMNEndEvent.ELEMENT_TYPE:
				BPMNEndEvent endEvent = new BPMNEndEvent(element);
				if (!endEvent.isError()) {
					secureEndEvents.add(endEvent);
				}
				bpmnProcessElement = endEvent;
				break;
			case BPMNCallActivity.ELEMENT_TYPE:
				bpmnProcessElement = new BPMNCallActivity(element);
				break;
			case BPMNExclusiveGateway.ELEMENT_TYPE:
				bpmnProcessElement = new BPMNExclusiveGateway(element);
				break;
			case BPMNIntermediateEvent.ELEMENT_TYPE:
				bpmnProcessElement = new BPMNIntermediateEvent(element);
				break;
			case "sequenceFlow":
				// Sequence flows are addressed separately when building the process graph.
				continue;
			case "extensionElements":
				annotations = new BPMNAnnotations(element);
				continue;
			default:
				if (element.getAttribute("id") != null) {
					bpmnProcessElement = new BPMNProcessElement(element) {
						@Override
						public String getType() {
							return "n/a";
						}
					};
				} else {
					// No valid process element.
					continue;
				}
			}
			if (annotations == null) {
				annotations = new BPMNAnnotations(new Element("extensionElements", processElement.getNamespace()));
			}
			annotations.getLocalDataAnnotations().put("processId", processXMLElement.getAttributeValue("id"));
			processElements.put(bpmnProcessElement.getId(), bpmnProcessElement);
			processGraph.addVertex(bpmnProcessElement);			
		}
		
		if (startEvent == null) {
			throw new InvalidBPMNProcessStructure("Start event is missing.");
		}
		
		if (secureEndEvents.isEmpty()) {
			throw new InvalidBPMNProcessStructure("End event (without error) is missing.");
		}
		
		if (annotations == null) {
			// Create empty annotations.
			Element extensionElementsElement = new Element("extensionElements", BPMNNamespace.BPMN);
			processElement.addContent(0, extensionElementsElement);
			annotations = new BPMNAnnotations(extensionElementsElement);
		}
		
		// Build process graph.
		for (Element sequenceFlowElement : processXMLElement.getChildren("sequenceFlow", BPMNNamespace.BPMN)) {
			String sourceRef = sequenceFlowElement.getAttributeValue("sourceRef");
			String targetRef = sequenceFlowElement.getAttributeValue("targetRef");
			BPMNProcessElement source = processElements.get(sourceRef);
			BPMNProcessElement target = processElements.get(targetRef);
			if (source == null || target == null) {
				continue;
			}
			processGraph.addEdge(source, target);			
		}
		shortestPaths = new FloydWarshallShortestPaths<>(processGraph);
	}

	@Override
	public Annotations getAnnotations() {
		return annotations;
	}

	@Override
	public String getId() {
		String id = (String) annotations.getLocalDataAnnotations().get("processId");
		if (id == null) {
			id = processXMLElement.getAttributeValue("id");
		}
		return id;
	}

	@Override
	public String getLabel() {
		return (String) annotations.getLocalDataAnnotations().get("processName");
	}
	
	@Override
	public Set<ProcessElement> getAllProcessElements() {
		return new HashSet<ProcessElement>(processElements.values());
	}

	@Override
	public ProcessElement getPredecessor(ProcessElement element) {
		if (!processElements.containsValue(element)) {
			throw new IllegalArgumentException("The given element is not part of the process definition.");
		}
		List<BPMNProcessElement> predecessors = Graphs.predecessorListOf(processGraph, (BPMNProcessElement) element);
		if (predecessors.size() > 0) {
			return predecessors.get(0);
		} else {
			return null;
		}
	}

	@Override
	public ProcessElement getProcessElementById(String id) throws IllegalArgumentException {
		return processElements.get(id);
	}

	@Override
	public BPMNProcessElement getProcessStartElement() {
		return startEvent;
	}

	@Override
	public List<BPMNProcessElement> getSuccessors(ProcessElement element) {
		if (!processElements.containsValue(element)) {
			throw new IllegalArgumentException("The given element is not part of the process definition.");
		}
		List<BPMNProcessElement> successors = Graphs.successorListOf(processGraph, (BPMNProcessElement) element);
		return successors;
	}
	
	@Override
	public String toString() {
		return new XMLOutputter(Format.getPrettyFormat()).outputString(processXMLElement);
	}

	@Override
	public int getDistanceFromStart(ProcessElement element) {
		int distance = shortestPaths.getShortestPath(startEvent, (BPMNProcessElement) element).getEdgeList().size();
		return distance;
	}

	@Override
	public int getMaxDistanceToEnd(ProcessElement element) {
		int maxDistance = 0;
		for (BPMNEndEvent endEvent : secureEndEvents) {
			GraphPath<BPMNProcessElement, DefaultEdge> path = shortestPaths.getShortestPath((BPMNProcessElement) element, endEvent);
			if (path != null) {
				int distance = path.getEdgeList().size();
				if (distance > maxDistance) {
					maxDistance = distance;
				}
			}
		}
		return maxDistance;
	}
}
