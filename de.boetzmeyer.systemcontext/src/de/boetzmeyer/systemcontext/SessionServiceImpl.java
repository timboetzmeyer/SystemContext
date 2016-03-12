package de.boetzmeyer.systemcontext;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.boetzmeyer.systemmodel.ApplicationConfig;
import de.boetzmeyer.systemmodel.ApplicationInstallation;
import de.boetzmeyer.systemmodel.ApplicationSession;
import de.boetzmeyer.systemmodel.Computer;
import de.boetzmeyer.systemmodel.ConfigurationItem;
import de.boetzmeyer.systemmodel.ConfigurationItemLink;
import de.boetzmeyer.systemmodel.DatabaseInstallation;
import de.boetzmeyer.systemmodel.DatabaseSession;
import de.boetzmeyer.systemmodel.IServer;
import de.boetzmeyer.systemmodel.PropertyState;
import de.boetzmeyer.systemmodel.SessionState;
import de.boetzmeyer.systemmodel.SystemModel;

final class SessionServiceImpl extends SystemContextService implements SessionService {

	public SessionServiceImpl(final IServer inSystemAccess) {
		super(inSystemAccess);
	}

	@Override
	public DatabaseSession login(DatabaseInstallation inDatabaseInstallation) {
		if (inDatabaseInstallation != null) {
			final DatabaseSession databaseSession = DatabaseSession.generate();
			databaseSession.setDatabaseInstallation(inDatabaseInstallation.getPrimaryKey());
			final Date now = new Date();
			databaseSession.setFromDate(now);
			databaseSession.setToDate(new Date(now.getTime()));
			databaseSession.save();
			return databaseSession;
		}
		return null;
	}

	@Override
	public boolean logout(DatabaseSession inDatabaseSession) {
		if (inDatabaseSession != null) {
			final DatabaseSession foundSession = systemAccess.findByIDDatabaseSession(inDatabaseSession.getPrimaryKey());
			if (foundSession != null) {
				foundSession.setToDate(new Date());
				foundSession.save();
				return true;
			}			
		}
		return false;
	}

	@Override
	public ApplicationSession startApp(final ApplicationInstallation inAppInstallation) {
		if (inAppInstallation != null) {
			final ApplicationSession appSession = ApplicationSession.generate();
			final Date now = new Date();
			appSession.setApplicationInstallation(inAppInstallation.getPrimaryKey());
			appSession.setFromDate(now);
			appSession.setToDate(new Date(now.getTime()));
			appSession.save();
			return appSession;
		}
		return null;
	}

	@Override
	public void shutdown(ApplicationSession inAppSession) {
		if (inAppSession != null) {
			final ApplicationSession foundSession = systemAccess.findByIDApplicationSession(inAppSession.getPrimaryKey());
			if (foundSession != null) {
				foundSession.setToDate(new Date());
				foundSession.save();
			}			
		}
	}

	@Override
	public List<ApplicationSession> getActiveSessions(ApplicationInstallation inApplicationInstallation) {
		final List<ApplicationSession> activeSessions = new ArrayList<ApplicationSession>();
		final List<ApplicationSession> sessions = systemAccess.referencesApplicationSessionByApplicationInstallation(inApplicationInstallation.getPrimaryKey());
		final Date now = new Date();
		for (ApplicationSession session : sessions) {
			if ((now.after(session.getFromDate()))) {
				if (session.getFromDate().equals(session.getToDate())) {
					activeSessions.add(session);
				}
			}
		}
		return activeSessions;
	}

	@Override
	public List<SessionState> getSessionStates(ApplicationSession inApplicationSession) {
		return systemAccess.referencesSessionStateByApplicationSession(inApplicationSession.getPrimaryKey());
	}

	@Override
	public List<PropertyState> getStates(SessionState inSessionState) {
		return systemAccess.referencesPropertyStateBySessionState(inSessionState.getPrimaryKey());
	}

	@Override
	public boolean updatePropertyState(String inPropertyKey, String inPropertyValue,
			ConfigurationItem inConfigurationItem, ApplicationSession inApplicationSession) {
		SessionState sessionStateCI = null;
		final List<PropertyState> foundPropertyStates = new ArrayList<PropertyState>();
		final List<SessionState> sessionStates = getSessionStates(inApplicationSession);
		for (SessionState sessionState : sessionStates) {
			if (sessionState != null) {
				if (sessionState.getConfigurationItem() == inConfigurationItem.getPrimaryKey()) {
					sessionStateCI = sessionState;
					foundPropertyStates.addAll(systemAccess.referencesPropertyStateBySessionState(sessionState.getPrimaryKey()));
					break;
				}
			}
		}
		for (PropertyState propertyState : foundPropertyStates) {
			if (propertyState != null) {
				if (propertyState.getPropertyKey().equalsIgnoreCase(inPropertyKey)) {
					propertyState.setPropertyValue(inPropertyValue);
					propertyState.setLastUpdated(new Date());
					return propertyState.save();
				}
			}
		}
		final SystemModel ta = SystemModel.createEmpty();
		if (sessionStateCI == null) {
			sessionStateCI = SessionState.generate();
			sessionStateCI.setApplicationSession(inApplicationSession.getPrimaryKey());
			sessionStateCI.setConfigurationItem(inConfigurationItem.getPrimaryKey());
			ta.addSessionState(sessionStateCI);
		}
		final PropertyState propertyState = PropertyState.generate();
		propertyState.setSessionState(sessionStateCI.getPrimaryKey());
		propertyState.setPropertyKey(inPropertyKey);
		propertyState.setPropertyValue(inPropertyValue);
		propertyState.setLastUpdated(new Date());
		ta.addPropertyState(propertyState);
		return ta.save();
	}

	@Override
	public Object getPropertyState(String inPropertyKey, ConfigurationItem inConfigurationItem,
			ApplicationSession inApplicationSession) {
		final Map<String, String> itemState = getItemState(inConfigurationItem, inApplicationSession);
		return itemState.get(inPropertyKey);
	}

	@Override
	public Map<String, String> getItemState(ConfigurationItem inConfigurationItem,
			ApplicationSession inApplicationSession) {
		final Map<String, String> itemState = new HashMap<String, String>();
		final List<PropertyState> propertyStates = getConfigurationItemState(inConfigurationItem, inApplicationSession);
		for (PropertyState propertyState : propertyStates) {
			if (propertyState != null) {
				itemState.put(propertyState.getPropertyKey(), propertyState.getPropertyValue());
			}
		}
		return itemState;
	}

	@Override
	public List<PropertyState> getConfigurationItemState(ConfigurationItem inConfigurationItem,
			ApplicationSession inApplicationSession) {
		final List<SessionState> sessionStates = getSessionStates(inApplicationSession);
		for (SessionState sessionState : sessionStates) {
			if (sessionState != null) {
				if (sessionState.getConfigurationItem() == inConfigurationItem.getPrimaryKey()) {
					return systemAccess.referencesPropertyStateBySessionState(sessionState.getPrimaryKey());
				}
			}
		}
		return new ArrayList<PropertyState>();
	}
	
	private List<ApplicationInstallation> getInstallations(Computer inComputer) {
		if (inComputer != null) {
			return systemAccess.referencesApplicationInstallationByComputer(inComputer.getPrimaryKey());
		}
		return new ArrayList<ApplicationInstallation>();
	}

	@Override
	public List<ApplicationSession> getRunningApps(Computer inComputer) {
		final List<ApplicationInstallation> installations = getInstallations(inComputer);
		final List<ApplicationSession> runningSessions = new ArrayList<ApplicationSession>();
		final Date now = new Date();
		for (ApplicationInstallation installation : installations) {
			final List<ApplicationSession> sessions = systemAccess.referencesApplicationSessionByApplicationInstallation(installation.getPrimaryKey());
			for (ApplicationSession session : sessions) {
				if ((now.after(session.getFromDate()))) {
					if (session.getFromDate().equals(session.getToDate())) {
						runningSessions.add(session);
					}
				}
			}
		}
		return runningSessions;
	}

	@Override
	public SystemModel getAppSessionModel(ApplicationSession inAppSession) {
		final SystemModel systemModel = SystemModel.createEmpty();
		if (inAppSession != null) {
			final ApplicationSession appSession = systemAccess.findByIDApplicationSession(inAppSession.getPrimaryKey());
			if (appSession != null) {
				final ApplicationInstallation appInstallation = appSession.getApplicationInstallationRef();
				if (appInstallation != null) {
					final ApplicationConfig app = appInstallation.getApplicationConfigRef();
					if (app != null) {
						systemModel.addApplicationSession(appSession);
						final SystemModel appConfigModel = getAppConfigurationModel(app);
						systemModel.add(appConfigModel);
						final List<SessionState> sessionStates = systemAccess.referencesSessionStateByApplicationSession(appSession.getPrimaryKey());
						final SystemModel sessionStateModel = systemAccess.contextExportSessionState(sessionStates);
						systemModel.add(sessionStateModel);
					}
				}
			}
		}
		return systemModel;
	}
	
	private SystemModel getAppConfigurationModel(ApplicationConfig inApp) {
		final SystemModel systemModel = SystemModel.createEmpty();
		if (inApp != null) {
			final ApplicationConfig app = systemAccess.findByIDApplicationConfig(inApp.getPrimaryKey());
			if (app != null) {
				systemModel.addApplicationConfig(app);
				final List<ConfigurationItem> configurationItems = systemAccess.referencesConfigurationItemByApplicationConfig(app.getPrimaryKey());
				systemModel.addAllConfigurationItem(configurationItems);
				for (ConfigurationItem configurationItem : configurationItems) {
					final List<ConfigurationItemLink> configurationLinksItemIn = systemAccess.referencesConfigurationItemLinkBySource(configurationItem.getPrimaryKey());
					systemModel.addAllConfigurationItemLink(configurationLinksItemIn);
					final List<ConfigurationItemLink> configurationLinksItemOut = systemAccess.referencesConfigurationItemLinkByDestination(configurationItem.getPrimaryKey());
					systemModel.addAllConfigurationItemLink(configurationLinksItemOut);
				}
			}
		}
		return systemModel;
	}

	@Override
	public void shutdown(Computer inComputer) {
		final SystemModel systemModel = SystemModel.createEmpty();
		final Date now = new Date();
		final List<ApplicationSession> openAppSessions = getRunningApps(inComputer);
		for (ApplicationSession applicationSession : openAppSessions) {
			applicationSession.setToDate(now);
			systemModel.addApplicationSession(applicationSession);
		}
		final List<DatabaseSession> openDbSessions = getRunningDatabases(inComputer);
		for (DatabaseSession databaseSession : openDbSessions) {
			databaseSession.setToDate(now);
			systemModel.addDatabaseSession(databaseSession);
		}
		systemModel.save();
	}

	private List<DatabaseSession> getRunningDatabases(final Computer inComputer) {
		final List<DatabaseInstallation> installations = getDatabaseInstallations(inComputer);
		final List<DatabaseSession> runningSessions = new ArrayList<DatabaseSession>();
		final Date now = new Date();
		for (DatabaseInstallation installation : installations) {
			final List<DatabaseSession> sessions = systemAccess.referencesDatabaseSessionByDatabaseInstallation(installation.getPrimaryKey());
			for (DatabaseSession session : sessions) {
				if ((now.after(session.getFromDate()))) {
					if (session.getFromDate() == session.getToDate()) {
						runningSessions.add(session);
					}
				}
			}
		}
		return runningSessions;
	}

	private List<DatabaseInstallation> getDatabaseInstallations(final Computer inComputer) {
		if (inComputer != null) {
			return systemAccess.referencesDatabaseInstallationByComputer(inComputer.getPrimaryKey());
		}
		return new ArrayList<DatabaseInstallation>();
	}
	
	@Override
	public DatabaseSession startDatabase(final DatabaseInstallation inDbInstallation) {
		if (inDbInstallation != null) {
			final DatabaseSession dbSession = DatabaseSession.generate();
			final Date now = new Date();
			dbSession.setDatabaseInstallation(inDbInstallation.getPrimaryKey());
			dbSession.setFromDate(now);
			dbSession.setToDate(new Date(now.getTime()));
			dbSession.save();
			return dbSession;
		}
		return null;
	}

}
