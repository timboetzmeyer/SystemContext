package de.boetzmeyer.systemcontext;

import java.util.List;
import java.util.Map;

import de.boetzmeyer.systemmodel.ApplicationConfig;
import de.boetzmeyer.systemmodel.ApplicationInterface;
import de.boetzmeyer.systemmodel.ApplicationLink;
import de.boetzmeyer.systemmodel.ApplicationType;
import de.boetzmeyer.systemmodel.Computer;
import de.boetzmeyer.systemmodel.ConfigurationItem;
import de.boetzmeyer.systemmodel.Infrastructure;
import de.boetzmeyer.systemmodel.InterfaceDataType;
import de.boetzmeyer.systemmodel.InterfaceMethod;
import de.boetzmeyer.systemmodel.InterfaceMethodParameter;
import de.boetzmeyer.systemmodel.SystemConfig;
import de.boetzmeyer.systemmodel.SystemModel;

public interface ApplicationService {

	ApplicationType addApplicationType(ApplicationType inApplicationType);

	ApplicationLink connectApps(ApplicationConfig inSourceApplication, ApplicationConfig inTargetApplication);

	boolean disconnectApps(ApplicationConfig inSourceApp, ApplicationConfig inTargetApp);

	ApplicationLink findAppLink(ApplicationConfig inSourceApplication, ApplicationConfig inTargetApplication);

	InterfaceMethod addInterfaceMethod(InterfaceMethod inInterfaceMethod);

	InterfaceDataType addInterfaceDataType(InterfaceDataType inInterfaceDataType);

	InterfaceMethodParameter addMethodParameter(InterfaceMethodParameter inInterfaceMethodParameter);

	ApplicationInterface addAppInterface(ApplicationInterface inAppInterface);

	ApplicationConfig addApp(ApplicationConfig inApp);

	List<ApplicationConfig> getSystemApplications(String inSystemName);

	ApplicationConfig getApplication(String inSystemName, String inApplicationName);

	ConfigurationItem findRootItem(ApplicationConfig inApp);

	Map<String, String> getAppConfiguration(ApplicationConfig inApp);

	Map<String, String> getConfigurations(ConfigurationItem inConfigurationItem);

	List<ConfigurationItem> getConfigurationItems(ApplicationConfig inApplicationConfig);

	List<ApplicationConfig> getApps(String inApplicationType);

	List<ApplicationInterface> getAppInterfaces(ApplicationConfig inApplicationConfig);

	ApplicationInterface addAppInterface(ApplicationConfig inApplicationConfig, String inInterfaceName);

	InterfaceMethod addInterfaceMethod(ApplicationInterface inApplicationInterface, String inMethodName,
			String inMethodDescription, boolean inAsynchronously, InterfaceMethodParameter[] inParameters,
			InterfaceDataType inReturnType);

	InterfaceMethod addInterfaceMethod(ApplicationInterface inApplicationInterface, String inMethodName,
			String inMethodDescription, boolean inAsynchronously, InterfaceMethodParameter[] inParameters);

	InterfaceMethod addInterfaceMethod(ApplicationInterface inApplicationInterface, String inMethodName,
			String inMethodDescription);

	InterfaceMethod addInterfaceMethod(ApplicationInterface inApplicationInterface, String inMethodName);

	InterfaceMethod addInterfaceMethod(ApplicationInterface inApplicationInterface, String inMethodName,
			InterfaceDataType inReturnType);

	List<InterfaceMethod> getAppInterface(ApplicationInterface inApplicationInterface);

	SystemModel getAppConfigurationModel(ApplicationConfig inApp);

	List<ApplicationConfig> getApps(Infrastructure inInfrastructure);

	List<ApplicationConfig> getApps(ApplicationType inApplicationType);

	List<ApplicationConfig> getApps();

	List<ApplicationConfig> getApps(SystemConfig inSystem);

	List<ApplicationConfig> getApps(Computer inComputer);

	ApplicationConfig findAppByName(String inApplicationName);

	SystemModel getInterfaceModel(ApplicationInterface inInterface);

}
