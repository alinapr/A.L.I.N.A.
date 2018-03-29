package de.adp.service.pki;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.DecodeException;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import de.adp.commons.process.EventAnnotation;
import de.adp.commons.process.ProcessDefinition;
import de.adp.commons.process.ProcessElement;
import de.adp.commons.process.ProcessElementInstance;
import de.adp.commons.process.ProcessInstance;
import de.adp.commons.process.ServiceCallAnnotation;
import de.adp.commons.process.TriggerAnnotation;
import de.adp.commons.process.bpmn.BPMNAnnotations;
import de.adp.commons.process.bpmn.BPMNEventAnnotation;
import de.adp.commons.process.bpmn.BPMNServiceCallAnnotation;
import de.adp.commons.process.bpmn.BPMNTriggerAnnotation;
import de.adp.commons.process.bpmn.exception.InvalidBPMNFragmentException;
import de.adp.commons.process.bpmn.exception.InvalidBPMNProcessStructure;
import de.adp.commons.process.exception.AmbiguousFlowException;
import de.adp.commons.util.ProcessConverterUtil;
import de.adp.service.pki.bpmn.BasicBPMNProcessService;
import de.adp.service.pki.util.JsonObjectUtil;

/*
 * This verticle is executed with the module itself, i.e. initializes all components required by the service.
 * The super class provides two main objects used to interact with the container:
 * - <code>vertx</code> provides access to the Vert.x runtime like event bus, servers, etc.
 * - <code>container</code> provides access to the container, e.g, for accessing the module configuration an the logging mechanism.  
 */
public class PKIMainVerticle extends Verticle {
	private JsonObject moduleConfig;
	private RouteMatcher routeMatcher;
	private ProcessService processService;

	@Override
	public void start() {
		/* 
		 * The module can be configured by one of the following ways:
		 * - The module is executed with from the command line with the option "-conf <filename>".
		 * - The module is executed programmatically with a configuration object. 
		 * - The (hardcoded) default configuration is applied if none of the above options has been applied.  
		 */
		if (container.config() != null && container.config().size() > 0) {
			moduleConfig = container.config();
		} else {
			container.logger().warn("Warning: No configuration applied! Using default settings.");
			moduleConfig = getDefaultConfiguration();
		}
		
		
		/*
		 * In this method the verticle is registered at the event bus in order to receive messages. 
		 */
		initializeEventBusHandler();
		
		/*
		 * This block initializes the HTTP interface for the service. 
		 */
		initializeHTTPRouting();
		vertx.createHttpServer()
			.requestHandler(routeMatcher)
			.listen(moduleConfig.getObject("webserver")
			.getInteger("port"));
		
		ServiceCallHelper serviceCallHelper = new ServiceCallHelper(vertx, container.logger(), moduleConfig.getObject("services"));
		switch (moduleConfig.getString("processEngine")) {
		case "simple":
		default:
			processService = new BasicBPMNProcessService(vertx, container.logger(), serviceCallHelper);
		}
		
		// Load stored processes.
		String processDefinitionPath = moduleConfig.getString("processDefinitionPath");
		if (processDefinitionPath != null) {
			container.logger().info("Loading process definition files from: " + processDefinitionPath);
			File dir = new File(processDefinitionPath);
			if (dir.exists()) {
				for (File bpmnFile : dir.listFiles()) {
					StringBuilder logInfo = new StringBuilder(200);
					try {
						List<ProcessDefinition> processDefinitions = ProcessConverterUtil.bpmnProcessCollection2ProcessDefinitions(bpmnFile);
						for (ProcessDefinition processDefinition : processDefinitions) {
							processService.registerProcess(processDefinition);
							logInfo.append("\nLoaded process definition: ").append(processDefinition.getId());
						}
						container.logger().info(logInfo.toString());
					} catch (InvalidBPMNFragmentException | InvalidBPMNProcessStructure e) {
						container.logger().warn("Failed to load process definition.", e);
					}
				}
			} else {
				container.logger().warn("Failed to load process definitions: Directory not found.");
			}
			
		}
		
		container.logger().info("Service \"Prozess-Koordinationsinstanz\" has been initialized with the following configuration:\n" + moduleConfig.encodePrettily());
		testEventBus();
	}
	
	
	private void testEventBus() {
		vertx.eventBus().registerHandler("adp:event:processEvent:test", new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> event) {
				System.out.println("[PKI] Received test event:\n" + event.body().encode());
			}
		});
		vertx.eventBus().registerHandler("adp:event:processEvent:processStart", new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> event) {
				System.out.println("[PKI] Process started:\n" + event.body().encode());
			}
		});
		vertx.eventBus().registerHandler("adp:event:processEvent:processComplete", new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> event) {
				System.out.println("[PKI] Process completed:\n" + event.body().encode());
			}
		});
		vertx.eventBus().registerHandler("adp:event:processEvent:processError", new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> event) {
				System.out.println("[PKI] Process ended with error:\n" + event.body().encode());
			}
		});
		vertx.eventBus().registerHandler("adp:event:processEvent:userTask", new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> event) {
				System.out.println("[PKI] User task active:\n" + event.body().encode());
			}
		});
		vertx.eventBus().registerHandler("adp:event:processEvent:serviceTask", new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> event) {
				System.out.println("[PKI] Service task active:\n" + event.body().encode());
			}
		});
		vertx.eventBus().registerHandler("adp:event:processEvent:manualTask", new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> event) {
				System.out.println("[PKI] Manual task active:\n" + event.body().encode());
			}
		});
		
		vertx.eventBus().registerHandler("adp:event:processEvent:callActivity", new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> event) {
				System.out.println("[PKI] Activity active:\n" + event.body().encode());
			}
		});
		
		vertx.eventBus().registerHandler("adp:event:processEvent:userRequest", new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> event) {
				System.out.println("[PKI] User request:\n" + event.body().encode());
			}
		});
	}
	
	
	@Override
	public void stop() {
		container.logger().info("Service \"Prozess-Koordinationsinstanz\" has been stopped.");
	}
	
	/**
	 * Create a configuration which used if no configuration is passed to the module.
	 * @return Configuration object.
	 */
	private static JsonObject getDefaultConfiguration() {
		JsonObject defaultConfig =  new JsonObject();
		
		JsonObject webserver = new JsonObject();
		webserver.putNumber("port", 8080);
		webserver.putString("statics", "www");
		webserver.putString("basePath", "");
		defaultConfig.putObject("webserver", webserver);
		
		JsonObject services = new JsonObject();
		services.putString("host", "localhost");
		services.putNumber("port", 8080);
		services.putBoolean("secure", false);
		services.putString("baseUrl", "/services");
		defaultConfig.putObject("services", services);
		
		defaultConfig.putString("processEngine", "simple");
		
		return defaultConfig;
	}
	
	
	/**
	 * In this method the handlers for the event bus are initialized.
	 */
	private void initializeEventBusHandler() {
	}
	
	/**
	 * In this method the HTTP API build using a route matcher.
	 */
	private void initializeHTTPRouting() {
		routeMatcher = new BasePathRouteMatcher(moduleConfig.getObject("webserver").getString("basePath"));
		Handler<HttpServerRequest> notImplementedHandler = new NotImplementedHandler();
		
		routeMatcher.get("/processes", new Handler<HttpServerRequest>() {
			@Override
			public void handle(HttpServerRequest request) {
				JsonObject responseObject = JsonObjectUtil.encodeProcessList(processService);
				request.response().end(responseObject.encode());
			}
		});
		routeMatcher.get("/processes/:id", new Handler<HttpServerRequest>() {
			@Override
			public void handle(HttpServerRequest request) {
				HttpServerResponse response = request.response();
				String processId = request.params().get("id");
				try {
					ProcessDefinition processDefinition = processService.getProcessById(processId);
					JsonObject responseObject = JsonObjectUtil.encodeProcessDefinition(processDefinition);
					response.end(responseObject.encode());					
				} catch (IllegalArgumentException e) {
					response.setStatusCode(404);
					response.end("Unknown process: " + processId);
				}
			}
		});
		routeMatcher.get("/processes/:id/xml", new Handler<HttpServerRequest>() {
			@Override
			public void handle(HttpServerRequest request) {
				HttpServerResponse response = request.response();
				String processId = request.params().get("id");
				try {
					ProcessDefinition processDefinition = processService.getProcessById(processId);
					response.end(processDefinition.toString());					
				} catch (IllegalArgumentException e) {
					response.setStatusCode(404);
					response.end("Unknown process: " + processId);
				}
			}
		});
		routeMatcher.delete("/processes/:id", new Handler<HttpServerRequest>() {
			
			@Override
			public void handle(HttpServerRequest request) {
				HttpServerResponse response = request.response();
				String processId = request.params().get("id");
				try {
					processService.unregisterProcess(processId);
					response.end();					
				} catch (IllegalArgumentException e) {
					response.setStatusCode(404);
					response.end("Unknown process: " + processId);
				}
				
			}
		});
		
		routeMatcher.put("/processes", new Handler<HttpServerRequest>() {
			@Override
			public void handle(HttpServerRequest request) {
				final HttpServerResponse response = request.response();
				request.bodyHandler(new Handler<Buffer>() {
					
					@Override
					public void handle(Buffer buffer) {
						String body = buffer.toString();
						try {
							List<ProcessDefinition> processDefinitions = ProcessConverterUtil.bpmnProcessCollection2ProcessDefinitions(body);
							JsonArray resultArray = new JsonArray();
							for (ProcessDefinition processDefinition : processDefinitions) {
								String processId = processDefinition.getId();
								if (processService.getAllProcessIds().contains(processId)) {
									processService.unregisterProcess(processId);
									container.logger().info("Updated process: " + processId);
								} else {
									container.logger().info("Registering process: " + processId);
								}
								processService.registerProcess(processDefinition);
								resultArray.addObject(JsonObjectUtil.encodeProcessDefinition(processDefinition));
							}
							JsonObject responseObject = new JsonObject();
							responseObject.putArray("importedProcesses", resultArray);
							response.end(responseObject.encode());
						} catch (InvalidBPMNFragmentException | InvalidBPMNProcessStructure e) {
							response.setStatusCode(400);
							response.end("Failed to parse process definition: " + e.getMessage());
							e.printStackTrace();
						}	
					}
				});
			}
		});
		routeMatcher.put("/processes/:id", new Handler<HttpServerRequest>() {
			@Override
			public void handle(HttpServerRequest request) {
				final HttpServerResponse response = request.response();
				final String id = request.params().get("id");
				request.bodyHandler(new Handler<Buffer>() {
					
					@Override
					public void handle(Buffer buffer) {
						String body = buffer.toString();
						try {
							ProcessDefinition processDefinition = ProcessConverterUtil.bpmnProcess2ProcessDefinition(body);
							String processId = processDefinition.getId();
							if (!id.equals(processId)) {
								response.setStatusCode(400);
								response.end("The given id does not match the one of the process definition. Aborted operation.");
								return;
							}
							
							if (processService.getAllProcessIds().contains(processId)) {
								processService.unregisterProcess(processId);
								container.logger().info("Updated process: " + processId);
							} else {
								container.logger().info("Registering process: " + processId);
							}
							processService.registerProcess(processDefinition);
							JsonObject responseObject = JsonObjectUtil.encodeProcessDefinition(processDefinition);
							response.end(responseObject.encode());
						} catch (InvalidBPMNFragmentException | InvalidBPMNProcessStructure e) {
							response.setStatusCode(400);
							response.end("Failed to parse process definition: " + e.getMessage());
							e.printStackTrace();
						}	
					}
				});				
			}
		});
		routeMatcher.get("/processes/:id/instances", new Handler<HttpServerRequest>() {
			@Override
			public void handle(HttpServerRequest request) {
				HttpServerResponse response = request.response();
				String processId = request.params().get("id");
				boolean isRunning = "false".equals(request.params().get("isRunning")) ? false : true;
				try {
					JsonObject responseObject = JsonObjectUtil.encodeProcessInstanceIds(processId, isRunning, processService);
					response.end(responseObject.encode());
				} catch (IllegalArgumentException e) {
					response.setStatusCode(404);
					response.end("Unknown process: " + processId);
				}
			}
		});
		routeMatcher.get("/processes/:id/elements", new Handler<HttpServerRequest>() {
			@Override
			public void handle(HttpServerRequest request) {
				HttpServerResponse response = request.response();
				String processId = request.params().get("id");
				try {
					ProcessDefinition processDefinition = processService.getProcessById(processId);
					JsonObject responseObject = JsonObjectUtil.encodeProcessElementIds(processDefinition);
					response.end(responseObject.encode());
				} catch (IllegalArgumentException e) {
					response.setStatusCode(404);
					response.end("Unknown process: " + processId);
				}
			}
		});
		routeMatcher.get("/processes/:id/elements/:elementId", new Handler<HttpServerRequest>() {
			@Override
			public void handle(HttpServerRequest request) {
				HttpServerResponse response = request.response();
				String processId = request.params().get("id");
				String elementId = request.params().get("elementId");
				try {
					ProcessElement processElement = processService.getProcessById(processId).getProcessElementById(elementId);
					JsonObject responseObject = JsonObjectUtil.encodeProcessElement(processElement);
					response.end(responseObject.encode());
				} catch (IllegalArgumentException e) {
					response.setStatusCode(404);
					response.end("Unknown process or element: " + processId + " -> " + elementId);
				}
			}
		});
		routeMatcher.post("/processes/:id/instantiate", new Handler<HttpServerRequest>() {
			@Override
			public void handle(HttpServerRequest request) {
				final HttpServerResponse response = request.response();
				final String processId = request.params().get("id");
				final String userId = request.params().get("userId");
				final String sessionId = request.params().get("sid");
				try {
					ProcessInstance processInstance = processService.instantiateProcess(processId, userId, sessionId, null);
					JsonObject responseObject = JsonObjectUtil.encodeProcessInstance(processInstance);
					response.end(responseObject.encode());
				} catch (IllegalArgumentException e) {
					response.setStatusCode(404);
					response.end("Unknown process: " + processId);
				}
			}
		});
		routeMatcher.get("/processes/:id/localData", new Handler<HttpServerRequest>() {
			@Override
			public void handle(HttpServerRequest request) {
				HttpServerResponse response = request.response();
				String processId = request.params().get("id");
				String elementId = request.params().get("elementId");
				try {
					ProcessDefinition processDefinition = processService.getProcessById(processId);
					Map<String, Object> localData;
					if (elementId != null) {
						ProcessElement processElement = processDefinition.getProcessElementById(elementId);
						localData = processElement.getAnnotations().getLocalDataAnnotations();
					} else {
						localData = processDefinition.getAnnotations().getLocalDataAnnotations();
					}
					JsonObject responseObject = new JsonObject(localData);
					response.end(responseObject.encode());
				} catch (IllegalArgumentException e) {
					response.setStatusCode(404);
					response.end("Unknown process or element: " + processId + (elementId != null ? " -> " + elementId : ""));
				}
			}
		});
		routeMatcher.get("/processes/:id/localData/:key", new Handler<HttpServerRequest>() {
			@Override
			public void handle(HttpServerRequest request) {
				HttpServerResponse response = request.response();
				String processId = request.params().get("id");
				String key = request.params().get("key"); 
				String elementId = request.params().get("elementId");
				try {
					ProcessDefinition processDefinition = processService.getProcessById(processId);
					Map<String, Object> localData;
					if (elementId != null) {
						ProcessElement processElement = processDefinition.getProcessElementById(elementId);
						localData = processElement.getAnnotations().getLocalDataAnnotations();
					} else {
						localData = processDefinition.getAnnotations().getLocalDataAnnotations();
					}
					if (localData.containsKey(key)) {
						JsonObject localDataObject = new JsonObject(localData);
						response.end(localDataObject.getValue(key).toString());
					} else {
						response.setStatusCode(404);
						response.end("No entry for key: " + processId + " -> " + elementId + " -> " + key);
					}
				} catch (IllegalArgumentException e) {
					response.setStatusCode(404);
					response.end("Unknown process or element: " + processId + (elementId != null ? " -> " + elementId : ""));
				}
			}
		});
		routeMatcher.put("/processes/:id/localData", new Handler<HttpServerRequest>() {
			@Override
			public void handle(HttpServerRequest request) {
				final HttpServerResponse response = request.response();
				String processId = request.params().get("id");
				String elementId = request.params().get("elementId");
				final ProcessDefinition processDefinition; 
				final ProcessElement processElement;
				try {
					processDefinition = processService.getProcessById(processId);
					if (elementId != null) {
						processElement = processDefinition.getProcessElementById(elementId);
					} else {
						processElement = null;
					}
				} catch (IllegalArgumentException e) {
					response.setStatusCode(404);
					response.end("Unknown process or element: " + processId + (elementId != null ? " -> " + elementId : ""));
					return;
				}				 
				
				request.bodyHandler(new Handler<Buffer>() {
					@Override
					public void handle(Buffer buffer) {
						try {
							JsonObject jsonObject = new JsonObject(buffer.toString());
							Map<String, Object> inputMap = jsonObject.toMap();
							Map<String, Object> localData = processElement != null ? processElement.getAnnotations().getLocalDataAnnotations() : processDefinition.getAnnotations().getLocalDataAnnotations();
							
							for (String key : inputMap.keySet()) {
								localData.put(key, inputMap.get(key));
							}
							response.end();
						} catch (DecodeException e) {
							response.setStatusCode(400);
							response.end("Invalid data. Expects a json object with fields 'key' and 'value'.");
						}
						
					}
				});
			}
		});
		routeMatcher.delete("/processes/:id/localData/:key", new Handler<HttpServerRequest>() {
			@Override
			public void handle(HttpServerRequest request) {
				final HttpServerResponse response = request.response();
				String processId = request.params().get("id");
				String key = request.params().get("key");
				String elementId = request.params().get("elementId");
				final ProcessDefinition processDefinition; 
				final ProcessElement processElement;
				try {
					processDefinition = processService.getProcessById(processId);
					Map<String, Object> localData;
					if (elementId != null) {
						processElement = processDefinition.getProcessElementById(elementId);
						localData = processElement.getAnnotations().getLocalDataAnnotations();
					} else {
						localData = processDefinition.getAnnotations().getLocalDataAnnotations();
					}
					
					if (localData.containsKey(key)) {
						localData.remove(key);
						response.end();
					} else {
						response.setStatusCode(404);
						response.end("No entry for \"" + key + "\" available.");
					}
				} catch (IllegalArgumentException e) {
					response.setStatusCode(404);
					response.end("Unknown process or element: " + processId + (elementId != null ? " -> " + elementId : ""));
					return;
				}
			}
		});
		
		routeMatcher.get("/processes/:id/triggers", new Handler<HttpServerRequest>() {
			@Override
			public void handle(HttpServerRequest request) {
				HttpServerResponse response = request.response();
				String processId = request.params().get("id");
				String elementId = request.params().get("elementId");
				try {
					ProcessDefinition processDefinition = processService.getProcessById(processId);
					JsonObject responseObject = new JsonObject();
					responseObject.putString("processId", processId);
					List<? extends TriggerAnnotation> triggerAnnotations;
					if (elementId != null) {
						ProcessElement processElement = processDefinition.getProcessElementById(elementId);
						triggerAnnotations = processElement.getAnnotations().getTriggerAnnotations();
						responseObject.putString("processElementId", elementId);
					} else {
						triggerAnnotations = processDefinition.getAnnotations().getTriggerAnnotations();
					}
					JsonArray triggers = new JsonArray();
					for (TriggerAnnotation triggerAnnotation : triggerAnnotations) {
						triggers.addObject(JsonObjectUtil.encodeTrigger(triggerAnnotation));
					}
					responseObject.putArray("triggers", triggers);
					response.end(responseObject.encode());
				} catch (IllegalArgumentException e) {
					response.setStatusCode(404);
					response.end("Unknown process or element: " + processId + (elementId != null ? " -> " + elementId : ""));
				}
			}
		});
		routeMatcher.put("/processes/:id/triggers", new Handler<HttpServerRequest>() {
			@Override
			public void handle(HttpServerRequest request) {
				final HttpServerResponse response = request.response();
				String processId = request.params().get("id");
				String elementId = request.params().get("elementId");
				final ProcessDefinition processDefinition; 
				final ProcessElement processElement;
				try {
					processDefinition = processService.getProcessById(processId);
					if (elementId != null) {
						processElement = processDefinition.getProcessElementById(elementId);
					} else {
						processElement = null;
					}
				} catch (IllegalArgumentException e) {
					response.setStatusCode(404);
					response.end("Unknown process or element: " + processId + (elementId != null ? " -> " + elementId : ""));
					return;
				}				 
				
				request.bodyHandler(new Handler<Buffer>() {
					@Override
					public void handle(Buffer buffer) {
						try {
							JsonObject body = new JsonObject(buffer.toString());
							BPMNTriggerAnnotation triggerAnnotation = JsonObjectUtil.decodeBPMNTrigger(body);
							BPMNAnnotations annotations = processElement != null ? (BPMNAnnotations) processElement.getAnnotations() : (BPMNAnnotations) processDefinition.getAnnotations();
							annotations.addTriggerAnnotation(triggerAnnotation);
							response.end();
						} catch (DecodeException e) {
							response.setStatusCode(400);
							response.end(e.getMessage());
						}
						
					}
				});
			}
		});
		routeMatcher.get("/processes/:id/triggers/:index", new Handler<HttpServerRequest>() {
			@Override
			public void handle(HttpServerRequest request) {
				HttpServerResponse response = request.response();
				String processId = request.params().get("id");
				String elementId = request.params().get("elementId");
				String indexString = request.params().get("index");
				try {
					int index = Integer.parseInt(indexString);
					ProcessDefinition processDefinition = processService.getProcessById(processId);
					List<? extends TriggerAnnotation> triggerAnnotations;
					if (elementId != null) {
						ProcessElement processElement = processDefinition.getProcessElementById(elementId);
						triggerAnnotations = processElement.getAnnotations().getTriggerAnnotations();
					} else {
						triggerAnnotations = processDefinition.getAnnotations().getTriggerAnnotations();
					}
					if (index <= triggerAnnotations.size() - 1) {
						JsonObject responseObject = JsonObjectUtil.encodeTrigger(triggerAnnotations.get(index));
						response.end(responseObject.encode());
					} else {
						response.setStatusCode(400);
						response.end("Invalid index: " + indexString);
					}
				} catch (NumberFormatException e) {
					response.setStatusCode(400);
					response.end("Invalid index: " + indexString);
				} catch (IllegalArgumentException e) {
					response.setStatusCode(404);
					response.end("Unknown process or element: " + processId + (elementId != null ? " -> " + elementId : ""));
				}
			}
		});
		routeMatcher.delete("/processes/:id/triggers/:index", new Handler<HttpServerRequest>() {
			@Override
			public void handle(HttpServerRequest request) {
				final HttpServerResponse response = request.response();
				String processId = request.params().get("id");
				String indexString = request.params().get("index");
				int index;
				try {
					index = Integer.parseInt(indexString);
				} catch (NumberFormatException e) {
					response.setStatusCode(400);
					response.end("Invalid index.");
					return;
				}
				String elementId = request.params().get("elementId");
				final ProcessDefinition processDefinition; 
				final ProcessElement processElement;
				try {
					processDefinition = processService.getProcessById(processId);
					BPMNAnnotations annotations;
					if (elementId != null) {
						processElement = processDefinition.getProcessElementById(elementId);
						annotations = (BPMNAnnotations) processElement.getAnnotations();
					} else {
						annotations = (BPMNAnnotations) processDefinition.getAnnotations();
					}
					
					if (annotations.getTriggerAnnotations().size() > index) {
						annotations.removeTriggerAnnotation(annotations.getTriggerAnnotations().get(index));
						response.end();
					} else {
						response.setStatusCode(404);
						response.end("No trigger with index [" + index + "] available.");
					}
				} catch (IllegalArgumentException e) {
					response.setStatusCode(404);
					response.end("Unknown process or element: " + processId + (elementId != null ? " -> " + elementId : ""));
					return;
				}
			}
		});
		routeMatcher.get("/processes/:id/serviceCalls", new Handler<HttpServerRequest>() {
			@Override
			public void handle(HttpServerRequest request) {
				HttpServerResponse response = request.response();
				String processId = request.params().get("id");
				String elementId = request.params().get("elementId");
				try {
					ProcessDefinition processDefinition = processService.getProcessById(processId);
					JsonObject responseObject = new JsonObject();
					responseObject.putString("processId", processId);
					List<? extends ServiceCallAnnotation> serviceCallAnnotations;
					if (elementId != null) {
						ProcessElement processElement = processDefinition.getProcessElementById(elementId);
						serviceCallAnnotations = processElement.getAnnotations().getServiceCallAnnotations();
						responseObject.putString("processElementId", elementId);
					} else {
						serviceCallAnnotations = processDefinition.getAnnotations().getServiceCallAnnotations();
					}
					JsonArray serviceCalls = new JsonArray();
					for (ServiceCallAnnotation serviceCallAnnotation : serviceCallAnnotations) {
						serviceCalls.addObject(JsonObjectUtil.encodeServiceCall(serviceCallAnnotation));
					}
					responseObject.putArray("serviceCalls", serviceCalls);
					response.end(responseObject.encode());
				} catch (IllegalArgumentException e) {
					response.setStatusCode(404);
					response.end("Unknown process or element: " + processId + " -> " + elementId);
				}
			}
		});
		routeMatcher.put("/processes/:id/serviceCalls", new Handler<HttpServerRequest>() {
			@Override
			public void handle(HttpServerRequest request) {
				final HttpServerResponse response = request.response();
				String processId = request.params().get("id");
				String elementId = request.params().get("elementId");
				final ProcessDefinition processDefinition; 
				final ProcessElement processElement;
				try {
					processDefinition = processService.getProcessById(processId);
					if (elementId != null) {
						processElement = processDefinition.getProcessElementById(elementId);
					} else {
						processElement = null;
					}
				} catch (IllegalArgumentException e) {
					response.setStatusCode(404);
					response.end("Unknown process or element: " + processId + (elementId != null ? " -> " + elementId : ""));
					return;
				}				 
				
				request.bodyHandler(new Handler<Buffer>() {
					@Override
					public void handle(Buffer buffer) {
						try {
							JsonObject body = new JsonObject(buffer.toString());
							BPMNServiceCallAnnotation serviceCallAnnotation = JsonObjectUtil.decodeBPMNServiceCall(body);
							BPMNAnnotations annotations = processElement != null ? (BPMNAnnotations) processElement.getAnnotations() : (BPMNAnnotations) processDefinition.getAnnotations();
							annotations.addServiceCallAnnotation(serviceCallAnnotation);
							response.end();
						} catch (DecodeException e) {
							response.setStatusCode(400);
							response.end(e.getMessage());
						}
						
					}
				});
			}
		});
		routeMatcher.get("/processes/:id/serviceCalls/:index", new Handler<HttpServerRequest>() {
			@Override
			public void handle(HttpServerRequest request) {
				HttpServerResponse response = request.response();
				String processId = request.params().get("id");
				String elementId = request.params().get("elementId");
				String indexString = request.params().get("index");
				try {
					int index = Integer.parseInt(indexString);
					ProcessDefinition processDefinition = processService.getProcessById(processId);
					List<? extends ServiceCallAnnotation> serviceCallAnnotations;
					if (elementId != null) {
						ProcessElement processElement = processDefinition.getProcessElementById(elementId);
						serviceCallAnnotations = processElement.getAnnotations().getServiceCallAnnotations();
					} else {
						serviceCallAnnotations = processDefinition.getAnnotations().getServiceCallAnnotations();
					}
					if (index <= serviceCallAnnotations.size() - 1) {
						JsonObject responseObject = JsonObjectUtil.encodeServiceCall(serviceCallAnnotations.get(index));
						response.end(responseObject.encode());
					} else {
						response.setStatusCode(400);
						response.end("Invalid index: " + indexString);
					}
				} catch (NumberFormatException e) {
					response.setStatusCode(400);
					response.end("Invalid index: " + indexString);
				} catch (IllegalArgumentException e) {
					response.setStatusCode(404);
					response.end("Unknown process or element: " + processId + " -> " + elementId);
				}
			}
		});
		routeMatcher.delete("/processes/:id/serviceCalls/:index", new Handler<HttpServerRequest>() {
			@Override
			public void handle(HttpServerRequest request) {
				final HttpServerResponse response = request.response();
				String processId = request.params().get("id");
				String indexString = request.params().get("index");
				int index;
				try {
					index = Integer.parseInt(indexString);
				} catch (NumberFormatException e) {
					response.setStatusCode(400);
					response.end("Invalid index.");
					return;
				}
				String elementId = request.params().get("elementId");
				final ProcessDefinition processDefinition; 
				final ProcessElement processElement;
				try {
					processDefinition = processService.getProcessById(processId);
					BPMNAnnotations annotations;
					if (elementId != null) {
						processElement = processDefinition.getProcessElementById(elementId);
						annotations = (BPMNAnnotations) processElement.getAnnotations();
					} else {
						annotations = (BPMNAnnotations) processDefinition.getAnnotations();
					}
					
					if (annotations.getServiceCallAnnotations().size() > index) {
						annotations.removeServiceCallAnnotation(annotations.getServiceCallAnnotations().get(index));
						response.end();
					} else {
						response.setStatusCode(404);
						response.end("No service call with index [" + index + "] available.");
					}
				} catch (IllegalArgumentException e) {
					response.setStatusCode(404);
					response.end("Unknown process or element: " + processId + (elementId != null ? " -> " + elementId : ""));
					return;
				}
			}
		});
		routeMatcher.get("/processes/:id/events", new Handler<HttpServerRequest>() {
			@Override
			public void handle(HttpServerRequest request) {
				HttpServerResponse response = request.response();
				String processId = request.params().get("id");
				String elementId = request.params().get("elementId");
				try {
					ProcessDefinition processDefinition = processService.getProcessById(processId);
					JsonObject responseObject = new JsonObject();
					responseObject.putString("processId", processId);
					List<? extends EventAnnotation> eventAnnotations;
					if (elementId != null) {
						ProcessElement processElement = processDefinition.getProcessElementById(elementId);
						eventAnnotations = processElement.getAnnotations().getEventAnnotations();
						responseObject.putString("processElementId", elementId);
					} else {
						eventAnnotations = processDefinition.getAnnotations().getEventAnnotations();
					}
					JsonArray events = new JsonArray();
					for (EventAnnotation eventAnnotation : eventAnnotations) {
						events.addObject(JsonObjectUtil.encodeEvent(eventAnnotation));
					}
					responseObject.putArray("events", events);
					response.end(responseObject.encode());
				} catch (IllegalArgumentException e) {
					response.setStatusCode(404);
					response.end("Unknown process or element: " + processId + " -> " + elementId);
				}
			}
		});
		routeMatcher.put("/processes/:id/events", new Handler<HttpServerRequest>() {
			@Override
			public void handle(HttpServerRequest request) {
				final HttpServerResponse response = request.response();
				String processId = request.params().get("id");
				String elementId = request.params().get("elementId");
				final ProcessDefinition processDefinition; 
				final ProcessElement processElement;
				try {
					processDefinition = processService.getProcessById(processId);
					if (elementId != null) {
						processElement = processDefinition.getProcessElementById(elementId);
					} else {
						processElement = null;
					}
				} catch (IllegalArgumentException e) {
					response.setStatusCode(404);
					response.end("Unknown process or element: " + processId + (elementId != null ? " -> " + elementId : ""));
					return;
				}				 
				
				request.bodyHandler(new Handler<Buffer>() {
					@Override
					public void handle(Buffer buffer) {
						try {
							JsonObject body = new JsonObject(buffer.toString());
							BPMNEventAnnotation eventAnnotation = JsonObjectUtil.decodeBPMNEvent(body);
							BPMNAnnotations annotations = processElement != null ? (BPMNAnnotations) processElement.getAnnotations() : (BPMNAnnotations) processDefinition.getAnnotations();
							annotations.addEventAnnotation(eventAnnotation);
							response.end();
						} catch (DecodeException e) {
							response.setStatusCode(400);
							response.end(e.getMessage());
						}
						
					}
				});
			}
		});
		routeMatcher.get("/processes/:id/events/:index", new Handler<HttpServerRequest>() {
			@Override
			public void handle(HttpServerRequest request) {
				HttpServerResponse response = request.response();
				String processId = request.params().get("id");
				String elementId = request.params().get("elementId");
				String indexString = request.params().get("index");
				try {
					int index = Integer.parseInt(indexString);
					ProcessDefinition processDefinition = processService.getProcessById(processId);
					List<? extends EventAnnotation> eventAnnotations;
					if (elementId != null) {
						ProcessElement processElement = processDefinition.getProcessElementById(elementId);
						eventAnnotations = processElement.getAnnotations().getEventAnnotations();
					} else {
						eventAnnotations = processDefinition.getAnnotations().getEventAnnotations();
					}
					if (index <= eventAnnotations.size() - 1) {
						JsonObject responseObject = JsonObjectUtil.encodeEvent(eventAnnotations.get(index));
						response.end(responseObject.encode());
					} else {
						response.setStatusCode(400);
						response.end("Invalid index: " + indexString);
					}
				} catch (NumberFormatException e) {
					response.setStatusCode(400);
					response.end("Invalid index: " + indexString);
				} catch (IllegalArgumentException e) {
					response.setStatusCode(404);
					response.end("Unknown process or element: " + processId + " -> " + elementId);
				}
			}
		});
		routeMatcher.delete("/processes/:id/events/:index", new Handler<HttpServerRequest>() {
			@Override
			public void handle(HttpServerRequest request) {
				final HttpServerResponse response = request.response();
				String processId = request.params().get("id");
				String indexString = request.params().get("index");
				int index;
				try {
					index = Integer.parseInt(indexString);
				} catch (NumberFormatException e) {
					response.setStatusCode(400);
					response.end("Invalid index.");
					return;
				}
				String elementId = request.params().get("elementId");
				final ProcessDefinition processDefinition; 
				final ProcessElement processElement;
				try {
					processDefinition = processService.getProcessById(processId);
					BPMNAnnotations annotations;
					if (elementId != null) {
						processElement = processDefinition.getProcessElementById(elementId);
						annotations = (BPMNAnnotations) processElement.getAnnotations();
					} else {
						annotations = (BPMNAnnotations) processDefinition.getAnnotations();
					}
					
					if (annotations.getEventAnnotations().size() > index) {
						annotations.removeEventAnnotation(annotations.getEventAnnotations().get(index));
						response.end();
					} else {
						response.setStatusCode(404);
						response.end("No event with index [" + index + "] available.");
					}
				} catch (IllegalArgumentException e) {
					response.setStatusCode(404);
					response.end("Unknown process or element: " + processId + (elementId != null ? " -> " + elementId : ""));
					return;
				}
			}
		});
		
		routeMatcher.get("/instances", new Handler<HttpServerRequest>() {
			@Override
			public void handle(HttpServerRequest request) {
				HttpServerResponse response = request.response();
				String processId = request.params().get("processId");
				String userId = request.params().get("userId");
				try {
					JsonObject responseObject = JsonObjectUtil.encodeInstanceList(processService, processId, userId);
					response.end(responseObject.encode());
				} catch (IllegalArgumentException e) {
					response.setStatusCode(404);
					response.end("Unknown process: " + processId);
				}
			}
		});
		routeMatcher.get("/instances/:id", new Handler<HttpServerRequest>() {
			@Override
			public void handle(HttpServerRequest request) {
				HttpServerResponse response = request.response();
				String processInstanceId = request.params().get("id");
				try {
					ProcessInstance processInstance = processService.getProcessInstanceById(processInstanceId);
					JsonObject responseObject = JsonObjectUtil.encodeProcessInstance(processInstance);
					response.end(responseObject.encode());					
				} catch (IllegalArgumentException e) {
					response.setStatusCode(404);
					response.end("Unknown process instance: " + processInstanceId);
				}
			}
		});
		routeMatcher.get("/instances/:id/currentElement", new Handler<HttpServerRequest>() {
			@Override
			public void handle(HttpServerRequest request) {
				HttpServerResponse response = request.response();
				String processInstanceId = request.params().get("id");
				try {
					ProcessInstance processInstance = processService.getProcessInstanceById(processInstanceId);
					ProcessElementInstance processElementInstance  = processInstance.getCurrentState();
					if (processElementInstance != null) {
						JsonObject responseObject = JsonObjectUtil.encodeProcessElementInstance(processElementInstance);
						response.end(responseObject.encode());					
					} else {
						response.setStatusCode(400);
						response.end("The process instance is not running.");
					}
				} catch (IllegalArgumentException e) {
					response.setStatusCode(404);
					response.end("Unknown process instance: " + processInstanceId);
				}
			}
		});
		routeMatcher.post("/instances/:id/next", new Handler<HttpServerRequest>() {
			@Override
			public void handle(HttpServerRequest request) {
				HttpServerResponse response = request.response();
				String processInstanceId = request.params().get("id");
				String elementId = request.params().get("elementId");
				try {
					ProcessInstance processInstance = processService.getProcessInstanceById(processInstanceId);
					ProcessElementInstance elementInstance;
					if (elementId != null) {
						elementInstance = processInstance.stepForward(elementId);
					} else {
						elementInstance = processInstance.stepForward();
					}
					if (elementInstance != null) {
						JsonObject responseObject = JsonObjectUtil.encodeProcessElementInstance(elementInstance);
						response.end(responseObject.encode());
					} else {
						response.end();
					}
				} catch (IllegalArgumentException e) {
					response.setStatusCode(400);
					response.end("Unknown process instance or element, or invalid step: " + processInstanceId + (elementId != null ? " -> " + elementId : ""));
					e.printStackTrace();
				} catch (AmbiguousFlowException e) {
					response.setStatusCode(400);
					response.end("Multiple flows are possible. Use elementId to specify the process flow.");
				}
			}
		});
		routeMatcher.post("/instances/:id/confirm", new Handler<HttpServerRequest>() {
			@Override
			public void handle(HttpServerRequest request) {
				HttpServerResponse response = request.response();
				String processInstanceId = request.params().get("id");
				try {
					ProcessInstance processInstance = processService.getProcessInstanceById(processInstanceId);
					ProcessInstance subprocessInstance = processInstance.enterSubprocess();
					JsonObject responseObject = JsonObjectUtil.encodeProcessInstance(subprocessInstance);
					response.end(responseObject.encode());
				} catch (IllegalStateException | IllegalArgumentException e) {
					response.setStatusCode(400);
					response.end(e.getMessage());
				} 
			}
		});
		routeMatcher.post("/instances/:id/previous", new Handler<HttpServerRequest>() {
			@Override
			public void handle(HttpServerRequest request) {
				HttpServerResponse response = request.response();
				String processInstanceId = request.params().get("id");
				try {
					ProcessInstance processInstance = processService.getProcessInstanceById(processInstanceId);
					ProcessElementInstance elementInstance = processInstance.stepBackward();
					if (elementInstance != null) {
						JsonObject responseObject = JsonObjectUtil.encodeProcessElementInstance(elementInstance);
						response.end(responseObject.encode());
					} else {
						response.end();
					}
				} catch (IllegalArgumentException e) {
					response.setStatusCode(400);
					response.end("Unknown process instance or invalid step: " + processInstanceId);
				}
			}
		});
		routeMatcher.get("/instances/:id/history", new Handler<HttpServerRequest>() {
			
			@Override
			public void handle(HttpServerRequest request) {
				HttpServerResponse response = request.response();
				String processInstanceId = request.params().get("id");
				try {
					ProcessInstance processInstance = processService.getProcessInstanceById(processInstanceId);
					JsonObject responseObject = JsonObjectUtil.encodeProcessInstanceHistory(processInstance);
					response.end(responseObject.encode());					
				} catch (IllegalArgumentException e) {
					response.setStatusCode(404);
					response.end("Unknown process instance: " + processInstanceId);
				}
			}
		});
		routeMatcher.post("/instances/:id/terminate", new Handler<HttpServerRequest>() {
			@Override
			public void handle(HttpServerRequest request) {
				HttpServerResponse response = request.response();
				String processInstanceId = request.params().get("id");
				try {
					ProcessInstance processInstance = processService.getProcessInstanceById(processInstanceId);
					processInstance.terminate();
					
					response.end();
				} catch (IllegalArgumentException e) {
					response.setStatusCode(400);
					response.end("Unknown process instance: " + processInstanceId);
				}
			}
		});
		
		/* routeMatcher.post("/test", new Handler<HttpServerRequest>() {
			
			@Override
			public void handle(final HttpServerRequest request) {
				request.bodyHandler(new Handler<Buffer>() {
					
					@Override
					public void handle(Buffer buffer) {
						System.out.println(">> Service call performed: " + buffer.toString());
						JsonObject responseObject = new JsonObject();
						responseObject.putString("response", "pong");
						request.response().end(responseObject.encode());
					}
				});
			}
		});
		
		routeMatcher.post("/sendEvent", new Handler<HttpServerRequest>() {
			
			@Override
			public void handle(final HttpServerRequest request) {
				request.bodyHandler(new Handler<Buffer>() {
					
					@Override
					public void handle(Buffer buffer) {
						JsonObject body = new JsonObject(buffer.toString());
						String address = body.getString("address");
						vertx.eventBus().publish(address, body.getObject("message"));
						System.out.println(">> Event sent: " + body.encode());
						request.response().end();
					}
				});
			}
		});*/
		
		routeMatcher.allWithRegEx("/.*", notImplementedHandler);
	}
}
