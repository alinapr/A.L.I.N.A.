package de.adp.commons.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import de.adp.commons.process.ProcessDefinition;
import de.adp.commons.process.bpmn.BPMNNamespace;
import de.adp.commons.process.bpmn.BPMNProcessDefinition;
import de.adp.commons.process.bpmn.exception.InvalidBPMNFragmentException;
import de.adp.commons.process.bpmn.exception.InvalidBPMNProcessStructure;

/**
 * Converter utility for mapping internal data model from/to other serialization formats.
 * 
 * @author simon.schwantzer(at)im-c.de
 */
public final class ProcessConverterUtil {
	private ProcessConverterUtil(){};
	
	public static List<ProcessDefinition> bpmnProcessCollection2ProcessDefinitions(File bpmnFile) throws InvalidBPMNFragmentException, InvalidBPMNProcessStructure {
		SAXBuilder builder = new SAXBuilder();
		try {
			Document document = builder.build(bpmnFile);
			Element rootElement = document.getRootElement();
			if (rootElement.getNamespace() != BPMNNamespace.BPMN) {
				throw new InvalidBPMNFragmentException("Invalid XML namespace; no BPMN data.");
			}
			
			List<ProcessDefinition> processDefinitions = new ArrayList<>();
			for (Element processElement : rootElement.getChildren("process", BPMNNamespace.BPMN)) {
				BPMNProcessDefinition processDefinition = new BPMNProcessDefinition(processElement); 
				processDefinitions.add(processDefinition);
			}
			return processDefinitions;
		} catch (JDOMException | IOException e) {
			throw new InvalidBPMNFragmentException("Failed to read bpmn file.", e);
		}
	}
	
	public static List<ProcessDefinition> bpmnProcessCollection2ProcessDefinitions(String bpmnProcessCollection) throws InvalidBPMNFragmentException, InvalidBPMNProcessStructure {
		SAXBuilder builder = new SAXBuilder();
		try {
			Document document = builder.build(new ByteArrayInputStream(bpmnProcessCollection.getBytes("UTF-8")));
			Element rootElement = document.getRootElement();
			if (rootElement.getNamespace() != BPMNNamespace.BPMN) {
				throw new InvalidBPMNFragmentException("Invalid XML namespace; no BPMN data.");
			}
			
			List<ProcessDefinition> processDefinitions = new ArrayList<>();
			for (Element processElement : rootElement.getChildren("process", BPMNNamespace.BPMN)) {
				BPMNProcessDefinition processDefinition = new BPMNProcessDefinition(processElement); 
				processDefinitions.add(processDefinition);
			}
			return processDefinitions;
		} catch (JDOMException | IOException e) {
			throw new InvalidBPMNFragmentException("Failed to read BPMN process definition.", e);
		}
	}
	
	public static ProcessDefinition bpmnProcess2ProcessDefinition(String bpnmProcess) throws InvalidBPMNFragmentException, InvalidBPMNProcessStructure {
		SAXBuilder builder = new SAXBuilder();
		try {
			Document document = builder.build(new ByteArrayInputStream(bpnmProcess.getBytes("UTF-8")));
			Element rootElement = document.getRootElement();
			if (rootElement.getNamespace() != BPMNNamespace.BPMN || !rootElement.getName().equals("process")) {
				throw new InvalidBPMNFragmentException("Invalid XML namespace or no BPMN process.");
			}
			
			BPMNProcessDefinition processDefinition = new BPMNProcessDefinition(rootElement); 
			return processDefinition;
		} catch (JDOMException | IOException e) {
			throw new InvalidBPMNFragmentException("Failed to read BPMN process.", e);
		}
	}
}
