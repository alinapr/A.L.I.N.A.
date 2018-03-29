package de.adp.service.ps;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;

import de.adp.commons.event.CallActivityEvent;
import de.adp.commons.event.ManualTaskEvent;
import de.adp.commons.event.ProcessCompleteEvent;
import de.adp.commons.event.ProcessErrorEvent;
import de.adp.commons.event.ProcessUserRequestEvent;
import de.adp.commons.event.UserTaskEvent;
import de.adp.service.auth.connector.AuthServiceConnector;
import de.adp.service.auth.connector.model.Session;
import de.adp.service.iid.server.connector.IIDConnector;
import de.adp.service.iid.server.model.Action;
import de.adp.service.iid.server.model.AssistanceStep;
import de.adp.service.iid.server.model.AssistanceStepBuilder;
import de.adp.service.iid.server.model.ContentBody;
import de.adp.service.iid.server.model.HttpPostAction;
import de.adp.service.iid.server.model.SendMessageAction;
import de.adp.service.ps.connector.BMDConnector;
import de.adp.service.ps.connector.ISConnector;
import de.adp.service.ps.connector.PKIConnector;

/**
 * Handler for user client requests for both HTTP and event bus. 
 * @author simon.schwantzer(at)im-c.de
 *
 */
public class UserInteractionHandler {
	private final AuthServiceConnector authConnector;
	private final BMDConnector bmdConnector;
	private final PKIConnector pkiConnector;
	private final IIDConnector iidConnector;
	private final ISConnector isConnector;
	private final String baseUrl;
	private final Map<String, Template> templates; // Map with handlebars templates for HTML responses.
	private final Map<String, String> clientTokens; // <sessionId, token>
	private final Map<String, JsonObject> activeElements; // <sessionId, processElementInstance>
	private final Map<String, JsonObject> processDefinitions; // <processDefinitionId, processDefinition>
	private final Map<String, JsonObject> processInstances; // <sessionId, processInstance>
	private final Map<String, Double> lastProgress; // <sessionId, lastDeterminedProgress>
	private final Logger logger;
	
	public UserInteractionHandler(AuthServiceConnector authConnector, BMDConnector bmdConnector, PKIConnector pkiConnector, IIDConnector iidConnector, ISConnector isConnector, Logger logger, String baseUrl) {
		this.authConnector = authConnector;
		this.bmdConnector = bmdConnector;
		this.pkiConnector = pkiConnector;
		this.iidConnector = iidConnector;
		this.isConnector = isConnector;
		this.logger = logger;
		this.baseUrl = baseUrl;
		clientTokens = new HashMap<>();
		activeElements = new HashMap<>();
		processDefinitions = new HashMap<>();
		processInstances = new HashMap<>();
		lastProgress = new HashMap<String, Double>();
		templates = new HashMap<>();
		try {
			TemplateLoader loader = new ClassPathTemplateLoader();
			loader.setPrefix("/templates");
			loader.setSuffix(".html");
			Handlebars handlebars = new Handlebars(loader);
			templates.put("standby", handlebars.compile("standby"));
			templates.put("processOverview", handlebars.compile("processOverview"));
			templates.put("processComplete", handlebars.compile("processComplete"));
			templates.put("processError", handlebars.compile("processError"));
			templates.put("error", handlebars.compile("error"));
		} catch (IOException e) {
			logger.fatal("Failed to load templates.", e);
		}
		registerPkiEvents();
	}
	
	private void registerPkiEvents() {
		pkiConnector.registerManualTaskHandler(new Handler<ManualTaskEvent>() {
			@Override
			public void handle(final ManualTaskEvent event) {
				final String sessionId = event.getSessionId();
				final String processId = event.getProcessId();
				final String processInstanceId = event.getProcessInstanceId();
				final String token = clientTokens.get(sessionId);
				authConnector.getSession(sessionId, token, new AsyncResultHandler<Session>() {
					
					@Override
					public void handle(AsyncResult<Session> sessionRequest) {
						if (sessionRequest.succeeded()) {
							final Session session = sessionRequest.result();
							pkiConnector.getCurrentElement(processInstanceId, sessionId, new AsyncResultHandler<JsonObject>() {
								@Override
								public void handle(AsyncResult<JsonObject> result) {
									if (result.succeeded()) {
										JsonObject currentElement = result.result();
										activeElements.put(sessionId, currentElement);
										updateContent(sessionId, session.getUserId(), currentElement, processId, processInstanceId, null, event.getProgress());
									} else {
										HttpException exception = (HttpException) result.cause();
										logger.warn("Failed to retrieve process element.", exception);
									}
								}
							});
						} else {
							logger.warn("Failed to retrieve session.", sessionRequest.cause());
						}
					}
				});
			}
		});
		
		pkiConnector.registerUserTaskHandler(new Handler<UserTaskEvent>() {
			@Override
			public void handle(final UserTaskEvent event) {
				final String sessionId = event.getSessionId();
				final String processId = event.getProcessId();
				final String processInstanceId = event.getProcessInstanceId();
				final String token = clientTokens.get(sessionId);
				authConnector.getSession(sessionId, token, new AsyncResultHandler<Session>() {
					
					@Override
					public void handle(AsyncResult<Session> sessionRequest) {
						if (sessionRequest.succeeded()) {
							final Session session = sessionRequest.result();
							pkiConnector.getCurrentElement(event.getProcessInstanceId(), session.getId(), new AsyncResultHandler<JsonObject>() {
								@Override
								public void handle(AsyncResult<JsonObject> result) {
									if (result.succeeded()) {
										JsonObject currentElement = result.result();
										activeElements.put(sessionId, currentElement);
										updateContent(sessionId, session.getUserId(), currentElement, processId, processInstanceId, null, event.getProgress());
									} else {
										HttpException exception = (HttpException) result.cause();
										logger.warn("Failed to retrieve process element.", exception);
									}
								}
							});
						} else {
							logger.warn("Failed to retrieve session.", sessionRequest.cause());
						}
						
					}
				});
			}
		});
		
		pkiConnector.registerProcessUserRequestHandler(new Handler<ProcessUserRequestEvent>() {
			@Override
			public void handle(final ProcessUserRequestEvent event) {
				String sessionId = event.getSessionId();
				JsonObject currentElement = activeElements.get(sessionId);
				AssistanceStepBuilder builder = new AssistanceStepBuilder();
				builder.setTitle(currentElement.getString("label"));
				// session.getProcessDefinition().getString("label");
				Double progress = lastProgress.get(sessionId);
				builder.setProgress(progress != null ? progress : 0.0d);
				builder.setInfo(event.getMessage());
				builder.setContentBody(new ContentBody.Empty());
				
				int i = 0;
				for (Entry<String, String> entry : event.getOptions().entrySet()) {
					String display = entry.getValue();
					Action action = new HttpPostAction(baseUrl + "/navigate/next?elementId=" + entry.getKey(), new JsonObject());
					builder.addActionButtonWithText("select-" + i++, display, action);
				}
				
				boolean hasPrevious = currentElement.containsField("previousElement");
				if (hasPrevious) {
					Action action = new HttpPostAction(baseUrl + "/navigate/previous", new JsonObject());
					builder.setBackAction(action);
				}
				builder.setCloseAction(new HttpPostAction(baseUrl + "/navigate/close", new JsonObject()));
				
				try {
					AssistanceStep assistanceStep = builder.build();
					iidConnector.displayAssistance(sessionId, PSMainVerticle.SERVICE_ID, assistanceStep, new AsyncResultHandler<Void>() {
						
						@Override
						public void handle(AsyncResult<Void> event) {
							if (event.failed()) {
								logger.warn("Failed to propagte user request event.", event.cause());
							}
						}
					});
				} catch (IllegalArgumentException e) {
					logger.warn("Failed to propagte user request event.", e);
				}
			}
		});
		
		pkiConnector.registerCallActivityHandler(new Handler<CallActivityEvent>() {
			@Override
			public void handle(final CallActivityEvent event) {
				final String sessionId = event.getSessionId();
				final String token = clientTokens.get(sessionId);
				authConnector.getSession(sessionId, token, new AsyncResultHandler<Session>() {
					
					@Override
					public void handle(AsyncResult<Session> sessionRequest) {
						if (sessionRequest.succeeded()) {
							final Session session = sessionRequest.result();
							// int executions = ledConnector.getNumberOfCompletedExecutions(event.getActivityProcessId(), session.getUserId(), token);
							// if (executions > 10) {
							bmdConnector.isExperienced(sessionId, event.getActivityProcessId(), session.getUserId(), token, new AsyncResultHandler<Boolean>() {
								
								@Override
								public void handle(AsyncResult<Boolean> bmdRequest) {
									boolean isExperienced; 
									if (bmdRequest.succeeded()) {
										isExperienced = bmdRequest.result();
									} else {
										logger.warn(bmdRequest.cause().getMessage());
										isExperienced = false;
									}
									if (isExperienced) {
										pkiConnector.getCurrentElement(event.getProcessInstanceId(), session.getId(), new AsyncResultHandler<JsonObject>() {
											@Override
											public void handle(AsyncResult<JsonObject> result) {
												if (result.succeeded()) {
													JsonObject currentElement = result.result();
													activeElements.put(sessionId, currentElement);
													updateContent(sessionId, session.getUserId(), currentElement, event.getProcessId(), event.getProcessInstanceId(), event.getActivityProcessId(), event.getProgress());
												} else {
													HttpException exception = (HttpException) result.cause();
													logger.warn("Failed to retrieve process element.", exception);
												}
											}
										});
									} else {
										pkiConnector.getProcessDefinition(event.getActivityProcessId(), new AsyncResultHandler<JsonObject>() {
											@Override
											public void handle(AsyncResult<JsonObject> result) {
												if (result.succeeded()) {
													final JsonObject processDefinition = result.result();
													processDefinitions.put(processDefinition.getString("id"), processDefinition);
													pkiConnector.confirm(event.getProcessInstanceId(), sessionId, new AsyncResultHandler<JsonObject>() {
														
														@Override
														public void handle(AsyncResult<JsonObject> confirmRequest) {
															if (confirmRequest.succeeded()) {
																final JsonObject processInstance = confirmRequest.result();
																processInstances.put(sessionId, processInstance);
																pkiConnector.getCurrentElement(processInstance.getString("id"), session.getId(), new AsyncResultHandler<JsonObject>() {
																	@Override
																	public void handle(AsyncResult<JsonObject> result) {
																		if (result.succeeded()) {
																			final JsonObject currentElement = result.result();
																			activeElements.put(sessionId, currentElement);
																			updateContent(sessionId, session.getUserId(), currentElement, processDefinition.getString("id"), processInstance.getString("id"), null, event.getProgress());
																		} else {
																			sendErrorPage(sessionId, (HttpException) result.cause());
																		}
																	}
																});
															} else {
																sendErrorPage(sessionId, (HttpException) confirmRequest.cause());
															}
														}
													});
												} else {
													sendErrorPage(sessionId, (HttpException) result.cause());
												}
											}
										});
									}
								}
							});
							
						} else {
							logger.warn("Failed to retrieve session.", sessionRequest.cause());
						}
					}
				});
			}
		});
		
		pkiConnector.registerProcessCompleteHandler(new Handler<ProcessCompleteEvent>() {
			@Override
			public void handle(final ProcessCompleteEvent event) {
				final String sessionId = event.getSessionId();
				JsonObject localProcessInstance = processInstances.get(sessionId);
				if (localProcessInstance == null || !event.getProcessInstanceId().equals(localProcessInstance.getString("id"))) {
					return;
				}
				final String parentInstanceId = event.getParentInstance();
				if (parentInstanceId != null) {
					// We are continue in the parent process.
					pkiConnector.getProcessInstance(parentInstanceId, new AsyncResultHandler<JsonObject>() {
						@Override
						public void handle(AsyncResult<JsonObject> result) {
							if (result.succeeded()) {
								final JsonObject processInstance = result.result();
								processInstances.put(sessionId, processInstance);
								pkiConnector.next(parentInstanceId, sessionId, null, new AsyncResultHandler<JsonObject>() {
									
									@Override
									public void handle(AsyncResult<JsonObject> nextRequest) {
										if (nextRequest.failed()) {
											logger.warn("Failed to continue parent process.", nextRequest.cause());
										}
									}
								});
							} else {
								sendErrorPage(sessionId, (HttpException) result.cause());
							}
						}
					});
				} else {
					// Top level process. Bring it to an end.
					JsonObject processDefinition = processDefinitions.get(localProcessInstance.getString("processId"));
					String title = processDefinition.getString("label");
					try {
						AssistanceStepBuilder builder = new AssistanceStepBuilder();
						builder.setTitle(title);
						builder.setProgress(1.0d);
						builder.setInfo("Assistenz erfolgreich abgeschlossen.");
						builder.setContentBody(new ContentBody.Empty());
						// TODO Use content packageinstead of info text. 
						JsonObject messageBody = new JsonObject();
						messageBody.putString("action", "endDisplay");
						messageBody.putString("sessionId", sessionId);
						messageBody.putString("serviceId", PSMainVerticle.SERVICE_ID);
						Action closeAction = new SendMessageAction(IIDConnector.DEFAULT_ADDRESS, messageBody);
						builder.setCloseAction(closeAction);
						builder.addActionButtonWithText("close", "Schließen", closeAction);
						AssistanceStep assistanceStep = builder.build();
						iidConnector.displayAssistance(sessionId, PSMainVerticle.SERVICE_ID, assistanceStep, new AsyncResultHandler<Void>() {
							
							@Override
							public void handle(AsyncResult<Void> event) {
								if (event.failed()) {
									logger.warn("Failed to propagate process completion.", event.cause());
								}
							}
						});
						clean(sessionId);
					} catch (IllegalArgumentException e) {
						logger.warn("Failed to propagate process completion.", e);
					}
				}
			}
		});
		
		pkiConnector.registerProcessErrorHandler(new Handler<ProcessErrorEvent>() {
			@Override
			public void handle(final ProcessErrorEvent event) {
				final String sessionId = event.getSessionId();
				JsonObject localProcessInstance = processInstances.get(sessionId);
				if (localProcessInstance == null || !event.getProcessInstanceId().equals(localProcessInstance.getString("id"))) {
					return;
				}
				JsonObject processDefinition = processDefinitions.get(localProcessInstance.getString("processId")); 
				String title = processDefinition.getString("label");
				try {
					processDefinition.putString("errorMessage", event.getErrorMessage());
					processDefinition.putNumber("errorCode", event.getErrorCode());
					AssistanceStepBuilder builder = new AssistanceStepBuilder();
					builder.setTitle(title);
					builder.setProgress(1.0d);
					builder.setInfo("Es ist ein Fehler aufgetreten: " + event.getErrorMessage());
					builder.setContentBody(new ContentBody.Empty());
					// TODO Replace with content package.
					JsonObject messageBody = new JsonObject();
					messageBody.putString("action", "endDisplay");
					messageBody.putString("sessionId", sessionId);
					messageBody.putString("serviceId", PSMainVerticle.SERVICE_ID);
					Action closeAction = new SendMessageAction(IIDConnector.DEFAULT_ADDRESS, messageBody);
					builder.setCloseAction(closeAction);
					// TODO Replace with real contact request.
					Action contactAction = new SendMessageAction("adp:service:ccs", new JsonObject());
					builder.addActionButtonWithText("close", "Techniker kontaktieren", contactAction);
					
					AssistanceStep assistanceStep = builder.build();
					iidConnector.displayAssistance(sessionId, PSMainVerticle.SERVICE_ID, assistanceStep, new AsyncResultHandler<Void>() {
						
						@Override
						public void handle(AsyncResult<Void> event) {
							if (event.failed()) {
								logger.warn("Failed to propagate process error state.", event.cause());
							}
						}
					});
					clean(sessionId);
				} catch (IllegalArgumentException e) {
					logger.warn("Failed to propagate process error state.", e);
				}
			}
		});
	}
	
	private void clean(String sessionId) {
		clientTokens.remove(sessionId);
		processInstances.remove(sessionId);
		activeElements.remove(sessionId);
		lastProgress.remove(sessionId);
	}
	
	/**
	 * 
	 * @param sessionId Session identifier.
	 * @param userId User identifier.
	 * @param currentElement Current element.
	 * @param processId Process identifier.
	 * @param processInstanceId Process instance identifier.
	 * @param activityProcessId ID of the process giving details for the current task. May be <code>null</code>.
	 */
	private void updateContent(final String sessionId, final String userId, final JsonObject currentElement, String processId, String processInstanceId, final String activityProcessId, final double progress) {
		isConnector.getContentForTask(userId, processId, processInstanceId, currentElement.getString("id"), new AsyncResultHandler<JsonObject>() {
			@Override
			public void handle(AsyncResult<JsonObject> result) {
				if (result.succeeded()) {
					String contentId = result.result().getString("contentId");
					if (contentId == null) {
						contentId = "404";
					}
					AssistanceStepBuilder builder = new AssistanceStepBuilder();
					// String title = session.getProcessDefinition().getString("label");
					String title = currentElement.getString("label");
					builder.setTitle(title);
					builder.setProgress(progress);
					builder.setContentBody(new ContentBody.Package(contentId));
					boolean hasPrevious = currentElement.containsField("previousElement");
					if (hasPrevious) {
						Action previousAction = new HttpPostAction(baseUrl + "/navigate/previous", new JsonObject());
						builder.setBackAction(previousAction);
					}
					if (activityProcessId != null) {
						JsonObject body = new JsonObject().putString("activityProcessId", activityProcessId);
						Action detailsAction = new HttpPostAction(baseUrl + "/navigate/details", body);
						builder.addActionButtonWithText("details", "Anleitung anzeigen", detailsAction);
					}
					JsonArray nextElements = currentElement.getArray("nextElements");
					boolean hasNext = nextElements != null && nextElements.size() > 0;
					if (hasNext) {
						Action nextAction = new HttpPostAction(baseUrl + "/navigate/next", new JsonObject());
						// TODO Replace text with title from next process step.
						builder.addActionButtonWithText("next", "Weiter", nextAction);
					}
					try {
						AssistanceStep assistanceStep = builder.build();
						iidConnector.displayAssistance(sessionId, PSMainVerticle.SERVICE_ID, assistanceStep, new AsyncResultHandler<Void>() {
							
							@Override
							public void handle(AsyncResult<Void> event) {
								if (event.failed()) {
									logger.warn("Failed to update content display.", event.cause());
								}
							}
						});
					} catch (IllegalArgumentException e) {
						logger.warn("Failed to update content display.", e);
					}
				} else {
					HttpException exception = (HttpException) result.cause();
					sendErrorPage(sessionId, exception);
				}
			}
		});
	}
	
	private void sendErrorPage(String sessionId, HttpException exception) {
		AssistanceStepBuilder builder = new AssistanceStepBuilder();
		builder.setTitle("Allgemeiner Fehler");
		builder.setInfo("Es ist ein allgemeiner Fehler aufgetreten: " + exception.getMessage());
		builder.setContentBody(new ContentBody.Empty());
		JsonObject messageBody = new JsonObject();
		messageBody.putString("action", "endDisplay");
		messageBody.putString("sessionId", sessionId);
		messageBody.putString("serviceId", PSMainVerticle.SERVICE_ID);
		Action closeAction = new SendMessageAction(IIDConnector.DEFAULT_ADDRESS, messageBody);
		builder.setCloseAction(closeAction);
		builder.addActionButtonWithText("close", "Schließen", closeAction);
		builder.setProgress(1.0d);
		try {
			AssistanceStep assistanceStep = builder.build();
			iidConnector.displayAssistance(sessionId, PSMainVerticle.SERVICE_ID, assistanceStep, new AsyncResultHandler<Void>() {
				
				@Override
				public void handle(AsyncResult<Void> event) {
					if (event.failed()) {
						logger.warn("Failed to display error page.", event.cause());
					}
				}
			});
		} catch (IllegalArgumentException e) {
			logger.warn("Failed to display error page.", e);
		}
	}
	
	public void handleStartSupportRequest(final HttpServerResponse response, final String processId, final Session session) {
		final String sessionId = session.getId();
		pkiConnector.getProcessDefinition(processId, new AsyncResultHandler<JsonObject>() {
			
			@Override
			public void handle(AsyncResult<JsonObject> processDefinitionRequest) {
				if (processDefinitionRequest.succeeded()) {
					JsonObject processDefinition = processDefinitionRequest.result();
					processDefinitions.put(processDefinition.getString("id"), processDefinition);
					pkiConnector.instantiateProcess(processId, session.getId(), session.getUserId(), new AsyncResultHandler<JsonObject>() {
						
						@Override
						public void handle(AsyncResult<JsonObject> result) {
							if (result.succeeded()) {
								JsonObject processInstance = result.result();
								processInstances.put(sessionId, processInstance);
								response.end();
							} else {
								logger.warn("Failed to update session: ", result.cause());
								response.setStatusCode(500).end("Failed to instantiate process.");
							}
						}
					});
				} else {
					HttpException exception = (HttpException) processDefinitionRequest.cause();
					sendErrorPage(sessionId, exception);
					response.setStatusCode(exception.getStatusCode());
					response.end(exception.getMessage());
				}
			}
		});
	}
	
	/*
	public void handleSupportInfoRequest(final HttpServerResponse response, String supportId, final Session session) {
		final String processId = supportId;
		final String sessionId = session.getId();
		pkiConnector.getProcessDefinition(processId, new AsyncResultHandler<JsonObject>() {
			
			@Override
			public void handle(AsyncResult<JsonObject> result) {
				if (result.succeeded()) {
					JsonObject processDefinition = result.result();
					processDefinitions.put(processDefinition.getString("id"), processDefinition);
					try {
						String processId = processDefinition.getString("id");
						
						String html = templates.get("processOverview").apply(processDefinition.toMap());
						HtmlDisplay.Builder builder = new HtmlDisplay.Builder();
						builder.setBody(html);
						
						JsonObject messageBody = new JsonObject();
						messageBody.putString("action", "endDisplay");
						messageBody.putString("sessionId", session.getId());
						messageBody.putString("serviceId", MainVerticle.SERVICE_ID);
						Action cancelAction = new SendMessageAction(IIDConnector.DEFAULT_ADDRESS, messageBody);
						builder.setCloseAction(cancelAction);
						builder.addActionButtonWithText("cancel", "Abbrechen", cancelAction);
						
						Action confirmAction = new HttpPostAction(baseUrl + "/navigate/confirm", new JsonObject().putString("processId", processId));
						builder.addActionButtonWithText("confirm", "Starten", confirmAction);
						
						DisplayUpdate displayUpdate = builder.build();
						iidConnector.displayContent(session.getId(), MainVerticle.SERVICE_ID, displayUpdate, new AsyncResultHandler<Void>() {
							
							@Override
							public void handle(AsyncResult<Void> event) {
								if (event.succeeded()) {
									response.end();
								} else {
									logger.warn("Failed to display support info.", event.cause());
									response.setStatusCode(500);
									response.end("Internal error.");
								}
							}
						});
					} catch (IOException e) {
						logger.warn("Failed to generate process overview page.", e);
						response.setStatusCode(500);
						response.end("Internal error.");
					} catch (IllegalArgumentException e) {
						logger.warn("Failed to display support info.", e);
						response.setStatusCode(500);
						response.end("Internal error.");
					}
				} else {
					HttpException exception = (HttpException) result.cause();
					sendErrorPage(sessionId, exception);
					response.setStatusCode(exception.getStatusCode());
					response.end(exception.getMessage());
				}
			}
		});
	}*/
	
	/*public void handleSupportRequest(SupportRequestEvent event) {
		String sessionId = event.getSessionId();
		final String processId = event.getSupportId();
		if (sessionManager.hasSession(sessionId)) {
			retrieveProcessDefinition(sessionManager.getSession(sessionId), processId);
		} else {
			sessionManager.initializeSession(sessionId, new AsyncResultHandler<Session>() {
				@Override
				public void handle(AsyncResult<Session> result) {
					if (result.succeeded()) {
						retrieveProcessDefinition(result.result(), processId);
					}
				}
			});
		}
	}
	
	private void retrieveProcessDefinition(final Session session, String processId) {
		processExecutionManager.getProcessInfomation(processId, new AsyncResultHandler<JsonObject>() {
			
			@Override
			public void handle(AsyncResult<JsonObject> result) {
				if (result.succeeded()) {
					JsonObject processDefinition = result.result();
					session.setProcessDefinition(processDefinition);
					JsonObject ebObject;
					try {
						ebObject = generateProcessOverviewPage(session.getId(), processDefinition);
						eventBus.send("adp:service:iid#updateDisplay?sid=" + session.getId(), ebObject);
					} catch (IOException e) {
						logger.warn("Failed to generate process overview page.", e);
					}
				}
			}
		});
	}*/
	
	public void handleConfirmRequest(final HttpServerResponse response, final Session session, final String processId) {
		final String sessionId = session.getId();
		pkiConnector.instantiateProcess(processId, session.getId(), session.getUserId(), new AsyncResultHandler<JsonObject>() {
			
			@Override
			public void handle(AsyncResult<JsonObject> result) {
				if (result.succeeded()) {
					JsonObject processInstance = result.result();
					processInstances.put(sessionId, processInstance);
					response.end();
				} else {
					logger.warn("Failed to update session: ", result.cause());
					response.setStatusCode(500).end("Failed to instantiate process.");
				}
			}
		});
	}
	
	public void handleNextRequest(final HttpServerResponse response, final Session session, final String elementId) {
		final String sessionId = session.getId();
		JsonObject processInstance = processInstances.get(sessionId);
		JsonObject currentElement = activeElements.get(sessionId);
		if (processInstance == null || currentElement == null) {
			response.setStatusCode(424);
			response.end("No support process running.");
			return;
		}
		JsonArray nextElements = currentElement.getArray("nextElements");
		String processInstanceId = processInstance.getString("id");
		if (nextElements.size() > 0) {
			pkiConnector.next(processInstanceId, session.getId(), elementId, new AsyncResultHandler<JsonObject>() {
				
				@Override
				public void handle(AsyncResult<JsonObject> result) {
					if (result.succeeded()) {
						// session.setCurrentElement(result.result());
						response.end();
					} else {
						HttpException exception = (HttpException) result.cause();
						sendErrorPage(sessionId, exception);
						response.setStatusCode(exception.getStatusCode());
						response.end(exception.getMessage());
					}
				}
			});
		} else {
			response.end();
		}
	}
	
	public void handlePreviousRequest(final HttpServerResponse response, final Session session) {
		final String sessionId = session.getId();
		JsonObject processInstance = processInstances.get(sessionId);
		JsonObject currentElement = activeElements.get(sessionId);
		if (processInstance == null || currentElement == null) {
			response.setStatusCode(424);
			response.end("No support process running.");
			return;
		}
		String previousElement = currentElement.getString("previousElement");
		String processInstanceId = processInstance.getString("id");
		if (previousElement != null) {
			pkiConnector.previous(processInstanceId, session.getId(), new AsyncResultHandler<JsonObject>() {
				@Override
				public void handle(AsyncResult<JsonObject> result) {
					if (result.succeeded()) {
						// session.setCurrentElement(result.result());
						response.end();
					} else {
						HttpException exception = (HttpException) result.cause();
						sendErrorPage(sessionId, exception);
						response.setStatusCode(exception.getStatusCode());
						response.end(exception.getMessage());
					}
				}
			});
		} else {
			response.end();
		}
	}
	
	public void handleCloseRequest(final HttpServerResponse response, final Session session, final String token) {
		JsonObject processInstance = processInstances.get(session.getId());
		final String sessionId = session.getId();
		pkiConnector.terminate(processInstance.getString("id"), sessionId, new AsyncResultHandler<Void>() {
			
			@Override
			public void handle(AsyncResult<Void> terminateRequest) {
				if (terminateRequest.succeeded()) {
					response.end();
				} else {
					HttpException e = (HttpException) terminateRequest.cause();
					response.setStatusCode(e.getStatusCode()).end(e.getMessage());
				}
				iidConnector.endDisplay(sessionId, PSMainVerticle.SERVICE_ID, null);
			}
		});
	}
	
	public void handleDetailsRequest(final HttpServerResponse response, final Session session, final String token, final String activityProcessId) {
		final String sessionId = session.getId();
		final JsonObject processInstance = processInstances.get(sessionId);
		JsonObject currentElement = activeElements.get(sessionId);
		if (processInstance == null || currentElement == null) {
			response.setStatusCode(424);
			response.end("No support process running.");
			return;
		}
		if (!"callActivity".equals(currentElement.getString("type"))) {
			response.setStatusCode(424);
			response.end("Operation not available for current task.");
			return;
		}
		pkiConnector.getProcessDefinition(activityProcessId, new AsyncResultHandler<JsonObject>() {
			@Override
			public void handle(AsyncResult<JsonObject> processDefinitionRequest) {
				if (processDefinitionRequest.succeeded()) {
					final JsonObject processDefinition = processDefinitionRequest.result();
					processDefinitions.put(processDefinition.getString("id"), processDefinition);
					pkiConnector.confirm(processInstance.getString("id"), sessionId, new AsyncResultHandler<JsonObject>() {
						
						@Override
						public void handle(AsyncResult<JsonObject> confirmRequest) {
							if (confirmRequest.succeeded()) {
								final JsonObject activityProcessInstance = confirmRequest.result();
								processInstances.put(sessionId, activityProcessInstance);
								pkiConnector.getCurrentElement(activityProcessInstance.getString("id"), session.getId(), new AsyncResultHandler<JsonObject>() {
									@Override
									public void handle(AsyncResult<JsonObject> currentElementRequest) {
										if (currentElementRequest.succeeded()) {
											final JsonObject currentElement = currentElementRequest.result();
											response.end();
											activeElements.put(sessionId, currentElement);
											updateContent(sessionId, session.getUserId(), currentElement, processDefinition.getString("id"), activityProcessInstance.getString("id"), null, 0.0d);
										} else {
											response.setStatusCode(500).end(currentElementRequest.cause().getMessage());
										}
									}
								});
							} else {
								response.setStatusCode(500).end(confirmRequest.cause().getMessage());
							}
						}
					});
				} else {
					response.setStatusCode(500).end(processDefinitionRequest.cause().getMessage());
				}
			}
		});
	}
	
	public void setClientToken(String sessionId, String token) {
		clientTokens.put(sessionId, token);
	}
}
