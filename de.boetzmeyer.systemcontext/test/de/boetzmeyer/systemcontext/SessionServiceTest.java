package de.boetzmeyer.systemcontext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import de.boetzmeyer.systemmodel.ApplicationConfig;
import de.boetzmeyer.systemmodel.ApplicationType;
import de.boetzmeyer.systemmodel.SystemConfig;
import de.boetzmeyer.systemmodel.SystemType;

public class SessionServiceTest {

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
			items.put("publicItem1", "MyValue 1");
			items.put("publicItem2", "MyValue 2");
			
			// save application on the server-side
			appService.configureApp(customerApp, items);
			
			// query application configuration
			assertEquals("MyValue 2", appService.getConfigurationValue(customerApp, "publicItem2"));
			
			
			AllTests.deleteExistingSystemModelFiles();
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

}
