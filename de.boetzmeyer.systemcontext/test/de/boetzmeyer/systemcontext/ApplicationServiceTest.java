package de.boetzmeyer.systemcontext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

import de.boetzmeyer.systemmodel.ApplicationConfig;
import de.boetzmeyer.systemmodel.ApplicationType;
import de.boetzmeyer.systemmodel.SystemConfig;
import de.boetzmeyer.systemmodel.SystemModel;
import de.boetzmeyer.systemmodel.SystemType;

public class ApplicationServiceTest {

	@Test
	public void testApplicationService() {
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
						
			final ApplicationConfig productApp = ApplicationConfig.generate();
			productApp.setApplicationName("Product Management");
			productApp.setDescription("This application is used to manage products");
			productApp.setApplicationType(applicationType.getPrimaryKey());
			productApp.setSystemConfig(system1.getPrimaryKey());
						
			final ApplicationConfig orderApp = ApplicationConfig.generate();
			orderApp.setApplicationName("Order Management");
			orderApp.setDescription("This application is used to manage orders");
			orderApp.setApplicationType(applicationType.getPrimaryKey());
			orderApp.setSystemConfig(system1.getPrimaryKey());
						
			final ApplicationConfig deliveryApp = ApplicationConfig.generate();
			deliveryApp.setApplicationName("Delivery Management");
			deliveryApp.setDescription("This application is used to manage deliveries");
			deliveryApp.setApplicationType(applicationType.getPrimaryKey());
			deliveryApp.setSystemConfig(system1.getPrimaryKey());
						
			// application not known on the server-side
			assertEquals(0, appService.getApplications().size());
			
			// save application on the server-side
			appService.addApp(customerApp);
			appService.addApp(productApp);
			appService.addApp(orderApp);
			appService.addApp(deliveryApp);
			
			// now application should be known on the server-side
			assertEquals(4, appService.getApplications().size());
			
			// connect coupled apps on the server-side
			appService.connectApps(deliveryApp, customerApp);
			appService.connectApps(orderApp, productApp);
			appService.connectApps(deliveryApp, orderApp);
			
			// get system fragments of application
			final SystemModel deliveryGraph = appService.getAppDependencies(deliveryApp);
			assertEquals(4, deliveryGraph.listApplicationConfig().size());
			assertEquals(3, deliveryGraph.listApplicationLink().size());
			
			final SystemModel customerGraph = appService.getAppDependencies(customerApp);
			assertEquals(1, customerGraph.listApplicationConfig().size());
			assertEquals(0, customerGraph.listApplicationLink().size());
			
			final SystemModel orderGraph = appService.getAppDependencies(orderApp);
			assertEquals(2, orderGraph.listApplicationConfig().size());
			assertEquals(1, orderGraph.listApplicationLink().size());
						
			final SystemModel productGraph = appService.getAppDependencies(productApp);
			assertEquals(1, productGraph.listApplicationConfig().size());
			assertEquals(0, productGraph.listApplicationLink().size());
						
			AllTests.deleteExistingSystemModelFiles();
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

}
