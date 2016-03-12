package de.boetzmeyer.systemcontext;

import java.util.Date;
import java.util.List;
import java.util.Map;

import de.boetzmeyer.systemmodel.ApplicationConfig;
import de.boetzmeyer.systemmodel.ApplicationInstallation;
import de.boetzmeyer.systemmodel.ApplicationInterface;
import de.boetzmeyer.systemmodel.ApplicationLink;
import de.boetzmeyer.systemmodel.ApplicationSession;
import de.boetzmeyer.systemmodel.ApplicationType;
import de.boetzmeyer.systemmodel.Computer;
import de.boetzmeyer.systemmodel.ConfigurationItem;
import de.boetzmeyer.systemmodel.DataModel;
import de.boetzmeyer.systemmodel.DatabaseInstallation;
import de.boetzmeyer.systemmodel.DatabaseSession;
import de.boetzmeyer.systemmodel.IServer;
import de.boetzmeyer.systemmodel.Infrastructure;
import de.boetzmeyer.systemmodel.InterfaceDataType;
import de.boetzmeyer.systemmodel.InterfaceMethod;
import de.boetzmeyer.systemmodel.InterfaceMethodParameter;
import de.boetzmeyer.systemmodel.ModelAttribute;
import de.boetzmeyer.systemmodel.ModelDataType;
import de.boetzmeyer.systemmodel.ModelEntity;
import de.boetzmeyer.systemmodel.ModelReference;
import de.boetzmeyer.systemmodel.Network;
import de.boetzmeyer.systemmodel.PropertyState;
import de.boetzmeyer.systemmodel.ServerFactory;
import de.boetzmeyer.systemmodel.SessionState;
import de.boetzmeyer.systemmodel.Settings;
import de.boetzmeyer.systemmodel.SystemConfig;
import de.boetzmeyer.systemmodel.SystemLink;
import de.boetzmeyer.systemmodel.SystemModel;
import de.boetzmeyer.systemmodel.SystemType;

public class SystemContext implements ISystemContext {
	private static ISystemContext singleton;
	
	private final ApplicationService applicationService;
	private final DataModelService dataModelService;
	private final InfrastructureService infrastructureService;
	private final InstallationService installationService;
	private final SessionService sessionService;
	
	public synchronized static ISystemContext aquire() {
		if (singleton == null) {
			throw new IllegalStateException("You are not connected to the system context. Please call one of the connect(...) methods before aquiring the context.");
		}
		return singleton;
	}
	
	public synchronized static ISystemContext connect(final String inServerName, final int inPort, final String inUser, final String inPassword, final String inDriverClass, final String inDriverProtocol) {
		if (singleton == null) {
			singleton = new SystemContext(inServerName, inPort, inUser, inPassword, inDriverClass, inDriverProtocol);
		}
		return singleton;
	}
	
	public synchronized static ISystemContext connect(final String inServerName, final int inPort, final String inUser, final String inPassword) {
		if (singleton == null) {
			singleton = new SystemContext(inServerName, inPort, inUser, inPassword, null, null);
		}
		return singleton;
	}
	
	public synchronized static ISystemContext connect(final String inServerName, final int inPort, final String inUser) {
		if (singleton == null) {
			singleton = new SystemContext(inServerName, inPort, inUser, null, null, null);
		}
		return singleton;
	}
	
	public synchronized static ISystemContext connect(final String inServerName, final int inPort) {
		if (singleton == null) {
			singleton = new SystemContext(inServerName, inPort, null, null, null, null);
		}
		return singleton;
	}
	
	public synchronized static ISystemContext connect(final String inPath) {
		if (singleton == null) {
			singleton = new SystemContext(inPath);
		}
		return singleton;
	}
	
	private SystemContext(final String inPath) {
		Settings.setLocaleDatabaseDir(inPath);
		//Settings.setFileAccess(true);
		final IServer systemAccess = ServerFactory.create();
		applicationService = new ApplicationServiceImpl(systemAccess);
		dataModelService = new DataModelServiceImpl(systemAccess);
		infrastructureService = new InfrastructureServiceImpl(systemAccess);
		installationService = new InstallationServiceImpl(systemAccess);
		sessionService = new SessionServiceImpl(systemAccess);
	}
	
	private SystemContext(final String inServerName, final int inPort, final String inUser, final String inPassword, final String inDriverClass, final String inDriverProtocol) {
		Settings.setServerName(inServerName);
		Settings.setPort(inPort);
		if (inUser != null) {
			Settings.setUserName(inUser);
		}
		if (inPassword != null) {
			Settings.setPassword(inPassword);
		}
		if (inDriverClass != null) {
			Settings.setDriverClass(inDriverClass);
		}
		if (inDriverProtocol != null) {
			Settings.setDriverProtocol(inDriverProtocol);
		}
		//Settings.setFileAccess(false);  // TODO
		final IServer systemAccess = ServerFactory.create();
		applicationService = new ApplicationServiceImpl(systemAccess);
		dataModelService = new DataModelServiceImpl(systemAccess);
		infrastructureService = new InfrastructureServiceImpl(systemAccess);
		installationService = new InstallationServiceImpl(systemAccess);
		sessionService = new SessionServiceImpl(systemAccess);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#addSystem(de.boetzmeyer.systemmodel.SystemConfig)
	 */
	@Override
	public SystemConfig addSystem(final SystemConfig inSystem) {
		return infrastructureService.addSystem(inSystem);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#addApplicationType(de.boetzmeyer.systemmodel.ApplicationType)
	 */
	@Override
	public ApplicationType addApplicationType(final ApplicationType inApplicationType) {
		return applicationService.addApplicationType(inApplicationType);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#connectApps(de.boetzmeyer.systemmodel.ApplicationConfig, de.boetzmeyer.systemmodel.ApplicationConfig)
	 */
	@Override
	public ApplicationLink connectApps(final ApplicationConfig inSourceApplication, final ApplicationConfig inTargetApplication) {
		return applicationService.connectApps(inSourceApplication, inTargetApplication);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#disonnectApps(de.boetzmeyer.systemmodel.ApplicationConfig, de.boetzmeyer.systemmodel.ApplicationConfig)
	 */
	@Override
	public boolean disconnectApps(final ApplicationConfig inSourceApp, final ApplicationConfig inTargetApp) {
		return applicationService.disconnectApps(inSourceApp, inTargetApp);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#findAppLink(de.boetzmeyer.systemmodel.ApplicationConfig, de.boetzmeyer.systemmodel.ApplicationConfig)
	 */
	@Override
	public ApplicationLink findAppLink(final ApplicationConfig inSourceApplication, final ApplicationConfig inTargetApplication) {
		return applicationService.findAppLink(inSourceApplication, inTargetApplication);
	}

	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#addInfrastructure(de.boetzmeyer.systemmodel.Infrastructure)
	 */
	@Override
	public Infrastructure addInfrastructure(final Infrastructure inInfrastructure) {
		return infrastructureService.addInfrastructure(inInfrastructure);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#connectSystems(de.boetzmeyer.systemmodel.SystemConfig, de.boetzmeyer.systemmodel.SystemConfig)
	 */
	@Override
	public SystemLink connectSystems(final SystemConfig inSourceSystem, final SystemConfig inTargetSystem) {
		return infrastructureService.connectSystems(inSourceSystem, inTargetSystem);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#disonnectSystems(de.boetzmeyer.systemmodel.SystemConfig, de.boetzmeyer.systemmodel.SystemConfig)
	 */
	@Override
	public boolean disonnectSystems(final SystemConfig inSourceSystem, final SystemConfig inTargetSystem) {
		return infrastructureService.disonnectSystems(inSourceSystem, inTargetSystem);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#installDatabase(de.boetzmeyer.systemmodel.DataModel, de.boetzmeyer.systemmodel.Computer)
	 */
	@Override
	public DatabaseInstallation installDatabase(final DataModel inDataModel, final Computer inComputer) {
		return installationService.installDatabase(inDataModel, inComputer);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#findDatabaseInstallation(de.boetzmeyer.systemmodel.DataModel, de.boetzmeyer.systemmodel.Computer)
	 */
	@Override
	public DatabaseInstallation findDatabaseInstallation(final DataModel inDataModel, final Computer inComputer) {
		return installationService.findDatabaseInstallation(inDataModel, inComputer);
	}

	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#addInterfaceMethod(de.boetzmeyer.systemmodel.InterfaceMethod)
	 */
	@Override
	public InterfaceMethod addInterfaceMethod(final InterfaceMethod inInterfaceMethod) {
		return applicationService.addInterfaceMethod(inInterfaceMethod);
	}

	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#addInterfaceDataType(de.boetzmeyer.systemmodel.InterfaceDataType)
	 */
	@Override
	public InterfaceDataType addInterfaceDataType(final InterfaceDataType inInterfaceDataType) {
		return applicationService.addInterfaceDataType(inInterfaceDataType);
	}

	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#addMethodParameter(de.boetzmeyer.systemmodel.InterfaceMethodParameter)
	 */
	@Override
	public InterfaceMethodParameter addMethodParameter(final InterfaceMethodParameter inInterfaceMethodParameter) {
		return applicationService.addMethodParameter(inInterfaceMethodParameter);
	}

	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#addAppInterface(de.boetzmeyer.systemmodel.ApplicationInterface)
	 */
	@Override
	public ApplicationInterface addAppInterface(final ApplicationInterface inAppInterface) {
		return applicationService.addAppInterface(inAppInterface);
	}

	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#addApp(de.boetzmeyer.systemmodel.ApplicationConfig)
	 */
	@Override
	public ApplicationConfig addApp(final ApplicationConfig inApp) {
		return applicationService.addApp(inApp);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#addDataModel(de.boetzmeyer.systemmodel.DataModel)
	 */
	@Override
	public DataModel addDataModel(final DataModel inDataModel) {
		return dataModelService.addDataModel(inDataModel);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#addModelAttribute(de.boetzmeyer.systemmodel.ModelAttribute)
	 */
	@Override
	public ModelAttribute addModelAttribute(final ModelAttribute inModelAttribute) {
		return dataModelService.addModelAttribute(inModelAttribute);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#addModelReference(de.boetzmeyer.systemmodel.ModelReference)
	 */
	@Override
	public ModelReference addModelReference(final ModelReference inModelReference) {
		return dataModelService.addModelReference(inModelReference);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#addModelDataType(de.boetzmeyer.systemmodel.ModelDataType)
	 */
	@Override
	public ModelDataType addModelDataType(final ModelDataType inModelDataType) {
		return dataModelService.addModelDataType(inModelDataType);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#addModelEntity(de.boetzmeyer.systemmodel.ModelEntity)
	 */
	@Override
	public ModelEntity addModelEntity(final ModelEntity inModelEntity) {
		return dataModelService.addModelEntity(inModelEntity);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#addComputer(de.boetzmeyer.systemmodel.Computer)
	 */
	@Override
	public Computer addComputer(final Computer inComputer) {
		return infrastructureService.addComputer(inComputer);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#addNetwork(de.boetzmeyer.systemmodel.Network)
	 */
	@Override
	public Network addNetwork(final Network inNetwork) {
		return infrastructureService.addNetwork(inNetwork);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getSystemByName(java.lang.String)
	 */
	@Override
	public SystemConfig getSystemByName(final String inSystemName) {
		return infrastructureService.getSystemByName(inSystemName);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getSystemApplications(java.lang.String)
	 */
	@Override
	public List<ApplicationConfig> getSystemApplications(final String inSystemName) {
		return applicationService.getSystemApplications(inSystemName);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getApplication(java.lang.String, java.lang.String)
	 */
	@Override
	public ApplicationConfig getApplication(final String inSystemName, final String inApplicationName) {
		return applicationService.getApplication(inSystemName, inApplicationName);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#login(de.boetzmeyer.systemmodel.DatabaseInstallation)
	 */
	@Override
	public DatabaseSession login(final DatabaseInstallation inDatabaseInstallation) {
		return sessionService.login(inDatabaseInstallation);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#logout(de.boetzmeyer.systemmodel.DatabaseSession)
	 */
	@Override
	public boolean logout(final DatabaseSession inDatabaseSession) {
		return sessionService.logout(inDatabaseSession);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getAppInstallations(de.boetzmeyer.systemmodel.ApplicationConfig)
	 */
	@Override
	public List<ApplicationInstallation> getAppInstallations(final ApplicationConfig inApplicationConfig) {
		return installationService.getAppInstallations(inApplicationConfig);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getSessions(de.boetzmeyer.systemmodel.ApplicationInstallation)
	 */
	@Override
	public List<ApplicationSession> getActiveSessions(final ApplicationInstallation inApplicationInstallation) {
		return sessionService.getActiveSessions(inApplicationInstallation);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getSessionStates(de.boetzmeyer.systemmodel.ApplicationSession)
	 */
	@Override
	public List<SessionState> getSessionStates(final ApplicationSession inApplicationSession) {
		return sessionService.getSessionStates(inApplicationSession);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getStates(de.boetzmeyer.systemmodel.SessionState)
	 */
	@Override
	public List<PropertyState> getStates(final SessionState inSessionState) {
		return sessionService.getStates(inSessionState);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#updatePropertyState(java.lang.String, java.lang.String, de.boetzmeyer.systemmodel.ConfigurationItem, de.boetzmeyer.systemmodel.ApplicationSession)
	 */
	@Override
	public boolean updatePropertyState(final String inPropertyKey, final String inPropertyValue, final ConfigurationItem inConfigurationItem, final ApplicationSession inApplicationSession) {
		return sessionService.updatePropertyState(inPropertyKey, inPropertyValue, inConfigurationItem, inApplicationSession);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getPropertyState(java.lang.String, de.boetzmeyer.systemmodel.ConfigurationItem, de.boetzmeyer.systemmodel.ApplicationSession)
	 */
	@Override
	public Object getPropertyState(final String inPropertyKey, final ConfigurationItem inConfigurationItem, final ApplicationSession inApplicationSession) {
		return sessionService.getPropertyState(inPropertyKey, inConfigurationItem, inApplicationSession);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getItemState(de.boetzmeyer.systemmodel.ConfigurationItem, de.boetzmeyer.systemmodel.ApplicationSession)
	 */
	@Override
	public Map<String, String> getItemState(final ConfigurationItem inConfigurationItem, final ApplicationSession inApplicationSession) {
		return sessionService.getItemState(inConfigurationItem, inApplicationSession);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getConfigurationItemState(de.boetzmeyer.systemmodel.ConfigurationItem, de.boetzmeyer.systemmodel.ApplicationSession)
	 */
	@Override
	public List<PropertyState> getConfigurationItemState(final ConfigurationItem inConfigurationItem, final ApplicationSession inApplicationSession) {
		return sessionService.getConfigurationItemState(inConfigurationItem, inApplicationSession);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#findRootItem(de.boetzmeyer.systemmodel.ApplicationConfig)
	 */
	@Override
	public ConfigurationItem findRootItem(final ApplicationConfig inApp) {
		return applicationService.findRootItem(inApp);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getAppConfiguration(de.boetzmeyer.systemmodel.ApplicationConfig)
	 */
	@Override
	public Map<String, String> getAppConfiguration(final ApplicationConfig inApp) {
		return applicationService.getAppConfiguration(inApp);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getConfigurations(de.boetzmeyer.systemmodel.ConfigurationItem)
	 */
	@Override
	public Map<String, String> getConfigurations(final ConfigurationItem inConfigurationItem) {
		return applicationService.getConfigurations(inConfigurationItem);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getConfigurationItems(de.boetzmeyer.systemmodel.ApplicationConfig)
	 */
	@Override
	public List<ConfigurationItem> getConfigurationItems(final ApplicationConfig inApplicationConfig) {
		return applicationService.getConfigurationItems(inApplicationConfig);
	}
	
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getNetworks()
	 */
	@Override
	public List<Network> getNetworks() {
		return infrastructureService.getNetworks();
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getApps(java.lang.String)
	 */
	@Override
	public List<ApplicationConfig> getApps(final String inApplicationType) {
		return applicationService.getApps(inApplicationType);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getComputers()
	 */
	@Override
	public List<Computer> getComputers() {
		return infrastructureService.getComputers();
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getInfrastructures()
	 */
	@Override
	public List<Infrastructure> getInfrastructures() {
		return infrastructureService.getInfrastructures();
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getSystemInfrastructure(java.lang.String)
	 */
	@Override
	public SystemModel getSystemInfrastructure(final String inInfrastructureName) {
		return infrastructureService.getSystemInfrastructure(inInfrastructureName);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getAppSystem(java.lang.String)
	 */
	@Override
	public SystemModel getAppSystem(final String inSystemName) {
		return infrastructureService.getAppSystem(inSystemName);
	}

	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getSystemInfrastructure(de.boetzmeyer.systemmodel.Infrastructure)
	 */
	@Override
	public SystemModel getSystemInfrastructure(final Infrastructure inInfrastructure) {
		return infrastructureService.getSystemInfrastructure(inInfrastructure);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getAppSystem(de.boetzmeyer.systemmodel.SystemConfig)
	 */
	@Override
	public SystemModel getAppSystem(final SystemConfig inSystem) {
		return infrastructureService.getAppSystem(inSystem);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getSystems()
	 */
	@Override
	public List<SystemConfig> getSystems() {
		return infrastructureService.getSystems();
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getSystems(de.boetzmeyer.systemmodel.Infrastructure)
	 */
	@Override
	public List<SystemConfig> getSystems(final Infrastructure inInfrastructure) {
		return infrastructureService.getSystems(inInfrastructure);
	}
		
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getApps(de.boetzmeyer.systemmodel.Infrastructure)
	 */
	@Override
	public List<ApplicationConfig> getApps(final Infrastructure inInfrastructure) {
		return applicationService.getApps(inInfrastructure);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getApps(de.boetzmeyer.systemmodel.ApplicationType)
	 */
	@Override
	public List<ApplicationConfig> getApps(final ApplicationType inApplicationType) {
		return applicationService.getApps(inApplicationType);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getApps()
	 */
	@Override
	public List<ApplicationConfig> getApps() {
		return applicationService.getApps();
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getApps(de.boetzmeyer.systemmodel.SystemConfig)
	 */
	@Override
	public List<ApplicationConfig> getApps(final SystemConfig inSystem) {
		return applicationService.getApps(inSystem);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getApps(de.boetzmeyer.systemmodel.Computer)
	 */
	@Override
	public List<ApplicationConfig> getApps(final Computer inComputer) {
		return applicationService.getApps(inComputer);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#findNetwork(java.lang.String)
	 */
	@Override
	public Network findNetwork(final String inNetworkName) {
		return infrastructureService.findNetwork(inNetworkName);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getComputers(java.lang.String)
	 */
	@Override
	public List<Computer> getComputers(final String inNetworkName) {
		return infrastructureService.getComputers(inNetworkName);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getComputers(de.boetzmeyer.systemmodel.Network)
	 */
	@Override
	public List<Computer> getComputers(final Network inNetwork) {
		return infrastructureService.getComputers(inNetwork);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getInstallations(de.boetzmeyer.systemmodel.Computer)
	 */
	@Override
	public List<ApplicationInstallation> getInstallations(final Computer inComputer) {
		return installationService.getInstallations(inComputer);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#installApp(java.lang.String, java.lang.String)
	 */
	@Override
	public ApplicationInstallation installApp(final String inComputerName, final String inApplicationName) {
		return installationService.installApp(inComputerName, inApplicationName);
	}

	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#installApp(de.boetzmeyer.systemmodel.Computer, de.boetzmeyer.systemmodel.ApplicationConfig)
	 */
	@Override
	public ApplicationInstallation installApp(final Computer inComputer, final ApplicationConfig inApplication) {
		return installationService.installApp(inComputer, inApplication);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#uninstallApp(de.boetzmeyer.systemmodel.ApplicationInstallation)
	 */
	@Override
	public void uninstallApp(final ApplicationInstallation inApplicationInstallation) {
		installationService.uninstallApp(inApplicationInstallation);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#uninstallDatabase(de.boetzmeyer.systemmodel.DatabaseInstallation)
	 */
	@Override
	public void uninstallDatabase(final DatabaseInstallation inDatabaseInstallation) {
		installationService.uninstallDatabase(inDatabaseInstallation);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#findComputer(de.boetzmeyer.systemmodel.Computer)
	 */
	@Override
	public Computer findComputer(final Computer inComputer) {
		return infrastructureService.findComputer(inComputer);
	}

	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#findAppByName(java.lang.String)
	 */
	@Override
	public ApplicationConfig findAppByName(final String inApplicationName) {
		return applicationService.findAppByName(inApplicationName);
	}

	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getInstallationsSince(de.boetzmeyer.systemmodel.Computer, java.util.Date)
	 */
	@Override
	public List<ApplicationInstallation> getInstallationsSince(final Computer inComputer, final Date inSince) {
		return installationService.getInstallationsSince(inComputer, inSince);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getRunningApps(de.boetzmeyer.systemmodel.Computer)
	 */
	@Override
	public List<ApplicationSession> getRunningApps(final Computer inComputer) {
		return sessionService.getRunningApps(inComputer);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getAppInterface(de.boetzmeyer.systemmodel.ApplicationInterface)
	 */
	@Override
	public List<InterfaceMethod> getAppInterface(final ApplicationInterface inApplicationInterface) {
		return applicationService.getAppInterface(inApplicationInterface);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getDataModels(de.boetzmeyer.systemmodel.ApplicationConfig)
	 */
	@Override
	public List<DataModel> getDataModels(final ApplicationConfig inApp) {
		return dataModelService.getDataModels(inApp);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getComputers(de.boetzmeyer.systemmodel.ApplicationConfig)
	 */
	@Override
	public List<Computer> getComputers(final ApplicationConfig inApp) {
		return infrastructureService.getComputers(inApp);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getComputers(de.boetzmeyer.systemmodel.DataModel)
	 */
	@Override
	public List<Computer> getComputers(final DataModel inDataModel) {
		return infrastructureService.getComputers(inDataModel);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getInstallations(de.boetzmeyer.systemmodel.DataModel)
	 */
	@Override
	public List<DatabaseInstallation> getInstallations(final DataModel inDataModel) {
		return installationService.getInstallations(inDataModel);
	}
		
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getAppInterfaces(de.boetzmeyer.systemmodel.ApplicationConfig)
	 */
	@Override
	public List<ApplicationInterface> getAppInterfaces(final ApplicationConfig inApplicationConfig) {
		return applicationService.getAppInterfaces(inApplicationConfig);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#addAppInterface(de.boetzmeyer.systemmodel.ApplicationConfig, java.lang.String)
	 */
	@Override
	public ApplicationInterface addAppInterface(final ApplicationConfig inApplicationConfig, final String inInterfaceName) {
		return applicationService.addAppInterface(inApplicationConfig, inInterfaceName);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#addInterfaceMethod(de.boetzmeyer.systemmodel.ApplicationInterface, java.lang.String, java.lang.String, boolean, de.boetzmeyer.systemmodel.InterfaceMethodParameter[], de.boetzmeyer.systemmodel.InterfaceDataType)
	 */
	@Override
	public InterfaceMethod addInterfaceMethod(final ApplicationInterface inApplicationInterface, final String inMethodName, final String inMethodDescription, 
			final boolean inAsynchronously, final InterfaceMethodParameter[] inParameters, final InterfaceDataType inReturnType) {
		return applicationService.addInterfaceMethod(inApplicationInterface, inMethodName, inMethodDescription, 
			inAsynchronously, inParameters, inReturnType);
	}
		
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#addInterfaceMethod(de.boetzmeyer.systemmodel.ApplicationInterface, java.lang.String, java.lang.String, boolean, de.boetzmeyer.systemmodel.InterfaceMethodParameter[])
	 */
	@Override
	public InterfaceMethod addInterfaceMethod(final ApplicationInterface inApplicationInterface, final String inMethodName, final String inMethodDescription, 
			final boolean inAsynchronously, final InterfaceMethodParameter[] inParameters) {
		return applicationService.addInterfaceMethod(inApplicationInterface, inMethodName, inMethodDescription, inAsynchronously, inParameters);
	}
		
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#addInterfaceMethod(de.boetzmeyer.systemmodel.ApplicationInterface, java.lang.String, java.lang.String)
	 */
	@Override
	public InterfaceMethod addInterfaceMethod(final ApplicationInterface inApplicationInterface, final String inMethodName, final String inMethodDescription) {
		return addInterfaceMethod(inApplicationInterface, inMethodName, inMethodDescription, false, null, null);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#addInterfaceMethod(de.boetzmeyer.systemmodel.ApplicationInterface, java.lang.String)
	 */
	@Override
	public InterfaceMethod addInterfaceMethod(final ApplicationInterface inApplicationInterface, final String inMethodName) {
		return addInterfaceMethod(inApplicationInterface, inMethodName, null, false, null, null);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#addInterfaceMethod(de.boetzmeyer.systemmodel.ApplicationInterface, java.lang.String, de.boetzmeyer.systemmodel.InterfaceDataType)
	 */
	@Override
	public InterfaceMethod addInterfaceMethod(final ApplicationInterface inApplicationInterface, final String inMethodName, final InterfaceDataType inReturnType) {
		return addInterfaceMethod(inApplicationInterface, inMethodName, null, false, null, inReturnType);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////

	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getDataModel(de.boetzmeyer.systemmodel.DataModel)
	 */
	@Override
	public SystemModel getDataModel(final DataModel inDataModel) {
		return dataModelService.getDataModel(inDataModel);
	}

	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getAppConfigurationModel(de.boetzmeyer.systemmodel.ApplicationConfig)
	 */
	@Override
	public SystemModel getAppConfigurationModel(final ApplicationConfig inApp) {
		return applicationService.getAppConfigurationModel(inApp);
	}

	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getAppSessionModel(de.boetzmeyer.systemmodel.ApplicationSession)
	 */
	@Override
	public SystemModel getAppSessionModel(final ApplicationSession inAppSession) {
		return sessionService.getAppSessionModel(inAppSession);
	}

	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getComputerModel(de.boetzmeyer.systemmodel.Computer)
	 */
	@Override
	public SystemModel getComputerModel(final Computer inComputer) {
		return infrastructureService.getComputerModel(inComputer);
	}

	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getSystemInfrastructureModel(de.boetzmeyer.systemmodel.Infrastructure)
	 */
	@Override
	public SystemModel getSystemInfrastructureModel(final Infrastructure inInfrastructure) {
		return infrastructureService.getSystemInfrastructureModel(inInfrastructure);
	}

	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getSystemModel(de.boetzmeyer.systemmodel.SystemConfig)
	 */
	@Override
	public SystemModel getSystemModel(final SystemConfig inSystem) {
		return infrastructureService.getSystemModel(inSystem);
	}

	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getInterfaceModel(de.boetzmeyer.systemmodel.ApplicationInterface)
	 */
	@Override
	public SystemModel getInterfaceModel(final ApplicationInterface inInterface) {
		return applicationService.getInterfaceModel(inInterface);
	}

	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getAppInstallations(de.boetzmeyer.systemmodel.InterfaceMethod)
	 */
	@Override
	public List<ApplicationInstallation> getAppInstallations(final InterfaceMethod inMethod) {
		return installationService.getAppInstallations(inMethod);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getAppInstallationsAvailable(de.boetzmeyer.systemmodel.InterfaceMethod)
	 */
	@Override
	public List<ApplicationInstallation> getAppInstallationsAvailable(final InterfaceMethod inMethod) {
		return installationService.getAppInstallationsAvailable(inMethod);
	}

	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#shutdown(de.boetzmeyer.systemmodel.Computer)
	 */
	@Override
	public void shutdown(final Computer inComputer) {
		sessionService.shutdown(inComputer);;
	}

	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#shutdown(de.boetzmeyer.systemmodel.ApplicationSession)
	 */
	@Override
	public void shutdown(final ApplicationSession inAppSession) {
		sessionService.shutdown(inAppSession);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#startApp(de.boetzmeyer.systemmodel.ApplicationInstallation)
	 */
	@Override
	public ApplicationSession startApp(final ApplicationInstallation inAppInstallation) {
		return sessionService.startApp(inAppInstallation);
	}
	
	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#startDatabase(de.boetzmeyer.systemmodel.DatabaseInstallation)
	 */
	@Override
	public DatabaseSession startDatabase(final DatabaseInstallation inDbInstallation) {
		return sessionService.startDatabase(inDbInstallation);
	}

	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#installComputer(java.lang.String, java.lang.String, java.lang.String, de.boetzmeyer.systemmodel.Network)
	 */
	@Override
	public Computer installComputer(final String inComputerName, final String inIPAddress, final String inRemarks, final Network inNetwork) {
		return installationService.installComputer(inComputerName, inIPAddress, inRemarks, inNetwork);
	}

	/* (non-Javadoc)
	 * @see de.boetzmeyer.systemcontext.ISystemContext#uninstallComputer(de.boetzmeyer.systemmodel.Computer)
	 */
	@Override
	public void uninstallComputer(final Computer inComputer) {
		installationService.uninstallComputer(inComputer);
	}

	@Override
	public List<SystemType> getSystemTypes() {
		return infrastructureService.getSystemTypes();
	}

	@Override
	public SystemType addSystemType(SystemType systemType) {
		return infrastructureService.addSystemType(systemType);
	}

	@Override
	public List<ApplicationType> getApplicationTypes() {
		return applicationService.getApplicationTypes();
	}

	@Override
	public List<ApplicationConfig> getApplications() {
		return applicationService.getApplications();
	}

	@Override
	public SystemModel getAppDependencies(ApplicationConfig inApp) {
		return applicationService.getAppDependencies(inApp);
	}

	@Override
	public void configureApp(ApplicationConfig inApp, Map<String, String> inItems) {
		applicationService.configureApp(inApp, inItems);
	}

	@Override
	public String getConfigurationValue(ApplicationConfig inApp, String inKey) {
		return applicationService.getConfigurationValue(inApp, inKey);
	}
}
