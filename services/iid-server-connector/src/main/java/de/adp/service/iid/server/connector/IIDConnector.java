package de.adp.service.iid.server.connector;

import java.util.List;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import de.adp.service.iid.server.model.AssistanceStep;
import de.adp.service.iid.server.model.LearningObject;
import de.adp.service.iid.server.model.Location;
import de.adp.service.iid.server.model.Notification;
import de.adp.service.iid.server.model.Popup;
import de.adp.service.iid.server.model.ServiceItem;
import de.adp.service.iid.server.model.SiteOverview;
import de.adp.service.iid.server.model.StationInfo;

/**
 * Connector for the content interaction service.
 * @author simon.schwantzer(at)im-c.de
 */
public class IIDConnector {
	public static final String DEFAULT_ADDRESS = "adp:service:iid";
	private class VoidHandler implements Handler<Message<JsonObject>> {
		private final AsyncResultHandler<Void> resultHandler;
		
		private VoidHandler(AsyncResultHandler<Void> resultHandler) {
			this.resultHandler = resultHandler;
		}
		
		@Override
		public void handle(Message<JsonObject> response) {
			final JsonObject body = response.body();
			if (resultHandler != null) resultHandler.handle(new AsyncResult<Void>() {
				
				@Override
				public boolean succeeded() {
					return "ok".equals(body.getString("status"));
				}
				
				@Override
				public Void result() {
					return null;
				}
				
				@Override
				public boolean failed() {
					return !succeeded();
				}
				
				@Override
				public Throwable cause() {
					if (failed()) {
						String message = body.getString("message");
						Integer code = body.getInteger("code");
						return new Throwable(message + " [" + code + "]");
					} else {
						return null;
					}
				}
			});
		}
	}
	
	private final EventBus eventBus;
	private final String address;
	
	/**
	 * Creates a new connector using the given event bus information.
	 * @param eventBus Event bus to communicate with the service.
	 * @param address Address of the service.
	 */
	public IIDConnector(EventBus eventBus, String address) {
		this.eventBus = eventBus;
		this.address = address;
	}
	
	/**
	 * Adds service items to the catalog of user session.
	 * @param sessionId Identifier of the user session.
	 * @param serviceItems List of service items to add.
	 * @param resultHandler Handler to check if the operation succeeded. May be <code>null</code>.
	 */
	public void addServiceItems(String sessionId, List<ServiceItem> serviceItems, AsyncResultHandler<Void> resultHandler) {
		JsonObject message = new JsonObject();
		message.putString("action", "addServiceItems");
		message.putString("sessionId", sessionId);
		JsonArray items = new JsonArray();
		for (ServiceItem item : serviceItems) {
			items.addObject(item.asJson());
		}
		message.putArray("items", items);
		eventBus.send(address, message, new VoidHandler(resultHandler));
	}
	
	/**
	 * Removes all service items related to a service.
	 * @param sessionId User session to update service items.
	 * @param serviceId Identifier of the service the items should be purged for.
	 * @param resultHandler Handler to check if the operation succeeded. May be <code>null</code>.
	 */
	public void purgeServiceItems(String sessionId, String serviceId, AsyncResultHandler<Void> resultHandler) {
		JsonObject message = new JsonObject();
		message.putString("action", "purgeServiceItems");
		message.putString("sessionId", sessionId);
		message.putString("serviceId", serviceId);
		eventBus.send(address, message, new VoidHandler(resultHandler));
	}
	
	/**
	 * Publish a notification.
	 * @param sessionId Identifier of the user session addressed by the notification.
	 * @param notification Notification to view.
	 * @param resultHandler Handler to check if the operation succeeded. May be <code>null</code>.
	 */
	public void notify(String sessionId, Notification notification, AsyncResultHandler<Void> resultHandler) {
		JsonObject message = new JsonObject();
		message.putString("action", "notify");
		message.putString("sessionId", sessionId);
		message.putObject("notification", notification.asJson());
		eventBus.send(address, message, new VoidHandler(resultHandler));
	}
	
	/**
	 * Publish a notification to a specific view.
	 * @param sessionId Identifier of the user session addressed by the notification.
	 * @param viewId Identifier of the view which should display the notification.
	 * @param notification Notification to view.
	 * @param resultHandler Handler to check if the operation succeeded. May be <code>null</code>.
	 */
	public void notifyView(String sessionId, String viewId, Notification notification, AsyncResultHandler<Void> resultHandler) {
		JsonObject message = new JsonObject();
		message.putString("action", "notify");
		message.putString("sessionId", sessionId);
		message.putString("viewId", viewId);
		message.putObject("notification", notification.asJson());
		eventBus.send(address, message, new VoidHandler(resultHandler));
	}
	
	/**
	 * Dismisses a notification.
	 * @param sessionId Identifier of the user session showing the notification.
	 * @param notificationId Identifier of the notification to dismiss.
	 * @param resultHandler Handler to check if the operation succeeded. May be <code>null</code>.
	 */
	public void dismissNotification(String sessionId, String notificationId, AsyncResultHandler<Void> resultHandler) {
		JsonObject message = new JsonObject();
		message.putString("action", "dismissNotification");
		message.putString("sessionId", sessionId);
		message.putString("notificationId", notificationId);
		eventBus.send(address, message, new VoidHandler(resultHandler));
	}
	
	/**
	 * Displays a assistance step.
	 * @param sessionId Identifier of the user session to display content in.
	 * @param serviceId Identifier of the service which wants to display content.
	 * @param assistance Assistance step to display.
	 * @param resultHandler Handler to check if the operation succeeded. May be <code>null</code>.
	 */
	public void displayAssistance(String sessionId, String serviceId, AssistanceStep assistance, AsyncResultHandler<Void> resultHandler) {
		JsonObject message = new JsonObject();
		message.putString("action", "displayAssistance");
		message.putString("sessionId", sessionId);
		message.putString("serviceId", serviceId);
		message.putObject("assistance", assistance.asJson());
		eventBus.send(address, message, new VoidHandler(resultHandler));
	}
	
	/**
	 * Displays a assistance step on a specific view.
	 * @param sessionId Identifier of the user session to display content in.
	 * @param viewId Identifier of the view to display the content on.
	 * @param serviceId Identifier of the service which wants to display content.
	 * @param assistance Assistance step to display.
	 * @param resultHandler Handler to check if the operation succeeded. May be <code>null</code>.
	 */
	public void displayAssistanceOnView(String sessionId, String viewId, String serviceId, AssistanceStep assistance, AsyncResultHandler<Void> resultHandler) {
		JsonObject message = new JsonObject();
		message.putString("action", "displayAssistance");
		message.putString("sessionId", sessionId);
		message.putString("viewId", viewId);
		message.putString("serviceId", serviceId);
		message.putObject("assistance", assistance.asJson());
		eventBus.send(address, message, new VoidHandler(resultHandler));
	}
	
	/**
	 * Displays a learning object.
	 * @param sessionId Identifier for the user session to display content in.
	 * @param serviceId Identifier of the service requesting the update.
	 * @param learningObject Learning object to display.
	 * @param resultHandler Handler to check if the operation succeeded. May be <code>null</code>.
	 */
	public void displayLearningObject(String sessionId, String serviceId, LearningObject learningObject, AsyncResultHandler<Void> resultHandler) {
		JsonObject message = new JsonObject();
		message.putString("action", "displayLearningContent");
		message.putString("sessionId", sessionId);
		message.putString("serviceId", serviceId);
		message.putObject("learningObject", learningObject.asJson());
		eventBus.send(address, message, new VoidHandler(resultHandler));
	}
	
	/**
	 * Displays a site overview page.
	 * @param sessionId Identifier for the user session to display content in.
	 * @param serviceId Identifier of the service requesting the update.
	 * @param siteOverview Site overview to display
	 * @param resultHandler Handler to check if the operation succeeded. May be <code>null</code>.
	 */
	public void displaySiteOverview(String sessionId, String serviceId, SiteOverview siteOverview, AsyncResultHandler<Void> resultHandler) {
		JsonObject message = new JsonObject();
		message.putString("action", "displaySiteOverview");
		message.putString("sessionId", sessionId);
		message.putString("serviceId", serviceId);
		message.putObject("siteOverview", siteOverview.asJson());
		eventBus.send(address, message, new VoidHandler(resultHandler));
	}
	
	/**
	 * Displays a station info page.
	 * @param sessionId Identifier for the user session to display content in.
	 * @param serviceId Identifier of the service requesting the update.
	 * @param stationInfo Station info page to display.
	 * @param resultHandler Handler to check if the operation succeeded. May be <code>null</code>.
	 */
	public void displayStationInfo(String sessionId, String serviceId, StationInfo stationInfo, AsyncResultHandler<Void> resultHandler) {
		JsonObject message = new JsonObject();
		message.putString("action", "displayStationInfo");
		message.putString("sessionId", sessionId);
		message.putString("serviceId", serviceId);
		message.putObject("stationInfo", stationInfo.asJson());
		eventBus.send(address, message, new VoidHandler(resultHandler));
	}
	
	/**
	 * Releases all views of a user session.
	 * @param sessionId Identifier of the session which displays content.
	 * @param serviceId Identifier of the service currently displaying the content.
	 * @param resultHandler Handler to check if the operation succeeded. May be <code>null</code>.
	 */
	public void endDisplay(String sessionId, String serviceId, AsyncResultHandler<Void> resultHandler) {
		JsonObject message = new JsonObject();
		message.putString("action", "endDisplay");
		message.putString("sessionId", sessionId);
		message.putString("serviceId", serviceId);
		eventBus.send(address, message, new VoidHandler(resultHandler));
	}
	
	/**
	 * Releases a specific view of a user session.
	 * @param sessionId Identifier of the session which displays content.
	 * @param viewId Identifier of the view to release.
	 * @param serviceId Identifier of the service currently displaying the content.
	 * @param resultHandler Handler to check if the operation succeeded. May be <code>null</code>.
	 */
	public void endDisplayOfView(String sessionId, String viewId, String serviceId, AsyncResultHandler<Void> resultHandler) {
		JsonObject message = new JsonObject();
		message.putString("action", "endDisplay");
		message.putString("sessionId", sessionId);
		message.putString("viewId", viewId);
		message.putString("serviceId", serviceId);
		eventBus.send(address, message, new VoidHandler(resultHandler));
	}
	
	/**
	 * Displays a popup on one or more clients of a user session.
	 * @param sessionId Identifier of the user session to display popup.
	 * @param viewId Identifier of the view to display the popup on. If <code>null</code> the decision is made by the IID.
	 * @param serviceId Identifier of the service pushing the popup.
	 * @param popup Popup to display.
	 * @param resultHandler Handler to check if the operation succeeded. May be <code>null</code>.
	 */
	public void displayPopup(String sessionId, String viewId, String serviceId, Popup popup, AsyncResultHandler<Void> resultHandler) {
		JsonObject message = new JsonObject();
		message.putString("action", "displayPopup");
		message.putString("sessionId", sessionId);
		message.putString("viewId", viewId);
		message.putString("serviceId", serviceId);
		message.putObject("popup", popup.asJson());
		eventBus.send(address, message, new VoidHandler(resultHandler));
	}
	
	/**
	 * Returns the last known location of a user.
	 * @param sessionId Identifier of the user session.
	 * @param resultHandler Handler for the location information. The result is <code>null</code> if no location is known.
	 */
	public void getLastKnownLocation(String sessionId, final AsyncResultHandler<Location> resultHandler) {
		JsonObject message = new JsonObject()
			.putString("action", "getLastKnownLocation")
			.putString("sessionId", sessionId);
		eventBus.send(address, message, new Handler<Message<JsonObject>>() {

			@Override
			public void handle(Message<JsonObject> message) {
				final JsonObject body = message.body();
				resultHandler.handle(new AsyncResult<Location>() {
					
					@Override
					public boolean succeeded() {
						return "ok".equals(body.getString("status"));
					}
					
					@Override
					public Location result() {
						if (succeeded()) {
							JsonObject locationObject = body.getObject("location");
							return locationObject != null ? new Location(locationObject) : null;
						} else {
							return null;
						}
					}
					
					@Override
					public boolean failed() {
						return !succeeded();
					}
					
					@Override
					public Throwable cause() {
						if (failed()) {
							String message = body.getString("message");
							Integer code = body.getInteger("code");
							return new Throwable(message + " [" + code + "]");
						} else {
							return null;
						}
					}
				});
			}
		});
	}
}
