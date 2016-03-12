package de.boetzmeyer.systemcontext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import de.boetzmeyer.systemmodel.ApplicationConfig;
import de.boetzmeyer.systemmodel.ApplicationInstallation;
import de.boetzmeyer.systemmodel.ApplicationSession;
import de.boetzmeyer.systemmodel.ApplicationType;
import de.boetzmeyer.systemmodel.Computer;
import de.boetzmeyer.systemmodel.ConfigurationItem;
import de.boetzmeyer.systemmodel.Network;
import de.boetzmeyer.systemmodel.SessionState;
import de.boetzmeyer.systemmodel.SystemConfig;
import de.boetzmeyer.systemmodel.SystemType;

public class SessionServiceTest {

	private static final String ITEM_VALUE_2 = "MyValue 2";
	private static final String ITEM_VALUE_1 = "MyValue 1";
	private static final String ITEM_KEY_2 = "publicItem2";
	private static final String ITEM_KEY_1 = "publicItem1";

	@Test
	public void testSessionService() {
		try {
			AllTests.deleteExistingSystemModelFiles();
			
			// connect with infrastructure service
			final InfrastructureService infrastructureService = SystemContext.connect(AllTests.DIR.getAbsolutePath());
			
			// create a system type locally
			final SystemType systemType = SystemType.generate();
			systemType.setTypeName("Web Shop");
			
			// computer not known on the server-side
			assertEquals(0, infrastructureService.getSystemTypes().size());
			
			// save the system type on the server-side
			infrastructureService.addSystemType(systemType);
			
			// computer not known on the server-side
			assertEquals(1, infrastructureService.getSystemTypes().size());
						
			// create 5 systems locally
			final SystemConfig system1 = SystemConfig.generate();
			system1.setSystemName("Customer-Relationship Management System");
			system1.setSystemType(systemType.getPrimaryKey());
			
			// connect with application service
			final ApplicationService appService = SystemContext.connect(AllTests.DIR.getAbsolutePath());
			
			// create application type locally
			final ApplicationType applicationType = ApplicationType.generate();
			applicationType.setTypeName("Web application");
			applicationType.setDescription("This application type is reachable through a browser");
						
			// application type not known on the server-side
			assertEquals(0, appService.getApplicationTypes().size());
			
			// save infrastructure on the server-side
			appService.addApplicationType(applicationType);
			
			// now application type should be known on the server-side
			assertEquals(1, appService.getApplicationTypes().size());
						
			// create 4 application locally
			final ApplicationConfig customerApp = ApplicationConfig.generate();
			customerApp.setApplicationName("Customer Management");
			customerApp.setDescription("This application is used to manage customers");
			customerApp.setApplicationType(applicationType.getPrimaryKey());
			customerApp.setSystemConfig(system1.getPrimaryKey());
						
			// application not known on the server-side
			assertEquals(0, appService.getApplications().size());
			
			// save application on the server-side
			appService.addApp(customerApp);
			
			// now application should be known on the server-side
			assertEquals(1, appService.getApplications().size());
			
			// create configuration item for customer management app
			final Map<String, String> items = new HashMap<String, String>();
			items.put(ITEM_KEY_1, ITEM_VALUE_1);
			items.put(ITEM_KEY_2, ITEM_VALUE_2);
			
			// save application on the server-side
			appService.configureApp(customerApp, items);
			
			// query application configuration
			assertEquals(ITEM_VALUE_2, appService.getConfigurationValue(customerApp, ITEM_KEY_2));
			
			// connect with installation service
			final InstallationService installationService = SystemContext.connect(AllTests.DIR.getAbsolutePath());
			
			// create network locally
			final Network network = Network.generate();
			network.setNetworkName("companynet");
			network.setDescription("The intranet of the company");
			
			// save network on the server-side
			infrastructureService.addNetwork(network);
			
			// now network should be known on the server-side
			assertEquals(1, infrastructureService.getNetworks().size());
			
			// create computer on the server-side
			final Computer computer = installationService.installComputer("proxy", "192.168.2.1", "The proxy server of the local network", network);
			
			// now computer should be known on the server-side
			assertEquals(1, infrastructureService.getComputers().size());

			// create app installation on the server-side
			final ApplicationInstallation appInstallation = installationService.installApp(computer, customerApp);

			// now app installation should be known on the server-side
			assertEquals(1, installationService.getAppInstallations(customerApp).size());
			
			// connect with session service
			final SessionService sessionService = SystemContext.connect(AllTests.DIR.getAbsolutePath());
						
			// now app installation should be known on the server-side
			assertEquals(0, sessionService.getActiveSessions(appInstallation).size());
			
			// create an app session on the server side
			final ApplicationSession appSession1 = sessionService.startApp(appInstallation);			
			assertEquals(1, sessionService.getActiveSessions(appInstallation).size());			
			
			// create an app session on the server side
			final ApplicationSession appSession2 = sessionService.startApp(appInstallation);			
			assertEquals(2, sessionService.getActiveSessions(appInstallation).size());
			
			// two instances of the customer app are running
			assertEquals(2, sessionService.getRunningApps(computer).size());
			
			// publish item state of in app session 1
			final ConfigurationItem configurationItem = appService.findRootItem(customerApp);
			sessionService.updatePropertyState("selectedCustomerName", "Mike Miller", configurationItem, appSession1);
			sessionService.updatePropertyState("selectedCustomerCity", "Bangalore", configurationItem, appSession1);

			// try to read the published state from the wrong running customer app instance
			assertEquals(0, sessionService.getSessionStates(appSession2).size());
			
			// try to read the published state from the correct running customer app instance
			final List<SessionState> sessionStates = sessionService.getSessionStates(appSession1);
			assertEquals(1, sessionStates.size());
			assertEquals(2, sessionService.getStates(sessionStates.get(0)).size());
			
			// stop one application instance/session
			sessionService.shutdown(appSession2);
			assertEquals(1, sessionService.getActiveSessions(appInstallation).size());
			
			sessionService.shutdown(appSession1);
			assertEquals(0, sessionService.getActiveSessions(appInstallation).size());
			
			
			AllTests.deleteExistingSystemModelFiles();
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

}
