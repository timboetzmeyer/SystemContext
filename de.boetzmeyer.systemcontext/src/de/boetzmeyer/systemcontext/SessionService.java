package de.boetzmeyer.systemcontext;

import java.util.List;
import java.util.Map;

import de.boetzmeyer.systemmodel.ApplicationInstallation;
import de.boetzmeyer.systemmodel.ApplicationSession;
import de.boetzmeyer.systemmodel.Computer;
import de.boetzmeyer.systemmodel.ConfigurationItem;
import de.boetzmeyer.systemmodel.DatabaseInstallation;
import de.boetzmeyer.systemmodel.DatabaseSession;
import de.boetzmeyer.systemmodel.PropertyState;
import de.boetzmeyer.systemmodel.SessionState;
import de.boetzmeyer.systemmodel.SystemModel;

public interface SessionService {
	ApplicationSession startApp(final ApplicationInstallation inAppInstallation);
	
	DatabaseSession startDatabase(final DatabaseInstallation inDbInstallation);
	
	DatabaseSession login(DatabaseInstallation inDatabaseInstallation);

	boolean logout(DatabaseSession inDatabaseSession);

	ApplicationSession login(ApplicationInstallation inApplicationInstallation);

	boolean logout(ApplicationSession inApplicationSession);

	List<ApplicationSession> getSessions(ApplicationInstallation inApplicationInstallation);

	List<SessionState> getSessionStates(ApplicationSession inApplicationSession);

	List<PropertyState> getStates(SessionState inSessionState);

	boolean updatePropertyState(String inPropertyKey, String inPropertyValue, ConfigurationItem inConfigurationItem,
			ApplicationSession inApplicationSession);

	Object getPropertyState(String inPropertyKey, ConfigurationItem inConfigurationItem,
			ApplicationSession inApplicationSession);

	Map<String, String> getItemState(ConfigurationItem inConfigurationItem, ApplicationSession inApplicationSession);

	List<PropertyState> getConfigurationItemState(ConfigurationItem inConfigurationItem,
			ApplicationSession inApplicationSession);

	void shutdown(ApplicationSession inAppSession);

	List<ApplicationSession> getRunningApps(Computer inComputer);

	SystemModel getAppSessionModel(ApplicationSession inAppSession);

	void shutdown(Computer inComputer);

}
