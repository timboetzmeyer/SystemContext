package de.boetzmeyer.systemcontext;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import de.boetzmeyer.systemmodel.ApplicationConfig;
import de.boetzmeyer.systemmodel.ApplicationDataModel;
import de.boetzmeyer.systemmodel.ApplicationInstallation;
import de.boetzmeyer.systemmodel.ApplicationInterface;
import de.boetzmeyer.systemmodel.ApplicationLink;
import de.boetzmeyer.systemmodel.ApplicationSession;
import de.boetzmeyer.systemmodel.ApplicationType;
import de.boetzmeyer.systemmodel.Computer;
import de.boetzmeyer.systemmodel.ConfigurationItem;
import de.boetzmeyer.systemmodel.ConfigurationItemLink;
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

public class SystemContext {
	private static final int MAX_DEPTH = 100;
	private static SystemContext singleton;
	
	private final IServer systemAccess;
	
	public static SystemContext aquire() {
		return singleton;
	}
	
	public static SystemContext connect(final String inServerName, final int inPort, final String inUser, final String inPassword, final String inDriverClass, final String inDriverProtocol) {
		return new SystemContext(inServerName, inPort, inUser, inPassword, inDriverClass, inDriverProtocol);
	}
	
	public static SystemContext connect(final String inServerName, final int inPort, final String inUser, final String inPassword) {
		return new SystemContext(inServerName, inPort, inUser, inPassword, null, null);
	}
	
	public static SystemContext connect(final String inServerName, final int inPort, final String inUser) {
		return new SystemContext(inServerName, inPort, inUser, null, null, null);
	}
	
	public static SystemContext connect(final String inServerName, final int inPort) {
		return new SystemContext(inServerName, inPort, null, null, null, null);
	}
	
	public static SystemContext connect(final String inPath) {
		return new SystemContext(inPath);
	}
	
	private SystemContext(final String inPath) {
		Settings.setLocaleDatabaseDir(inPath);
		//Settings.setFileAccess(true);
		systemAccess = ServerFactory.create();
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
		//Settings.setFileAccess(false);
		systemAccess = ServerFactory.create();
	}
	
	public SystemConfig addSystem(final SystemConfig inSystem) {
		if ((inSystem != null) && inSystem.isValid()) {
			final SystemModel model = SystemModel.createEmpty();
			model.addSystemConfig(inSystem);
			model.save();
			return inSystem;
		}
		return null;
	}
	
	public ApplicationType addApplicationType(final ApplicationType inApplicationType) {
		if ((inApplicationType != null) && inApplicationType.isValid()) {
			final SystemModel model = SystemModel.createEmpty();
			model.addApplicationType(inApplicationType);
			model.save();
			return inApplicationType;
		}
		return null;
	}
	
	public ApplicationLink connectApps(final ApplicationConfig inSourceApplication, final ApplicationConfig inTargetApplication) {
		if ((inSourceApplication != null) && inSourceApplication.isValid() && (inTargetApplication != null) && inTargetApplication.isValid()) {
			final ApplicationConfig sourceApp = systemAccess.findByIDApplicationConfig(inSourceApplication.getPrimaryKey());
			if (sourceApp != null) {
				final ApplicationConfig targetApp = systemAccess.findByIDApplicationConfig(inTargetApplication.getPrimaryKey());
				if (targetApp != null) {
					ApplicationLink appLink = findAppLink(sourceApp, targetApp);
					if (appLink == null) {
						appLink = ApplicationLink.generate();
						appLink.setSource(sourceApp.getPrimaryKey());
						appLink.setDestination(targetApp.getPrimaryKey());
						final SystemModel model = SystemModel.createEmpty();
						model.addApplicationLink(appLink);
						model.save();
					}
					return appLink;
				}
			}
		}
		return null;
	}
	
	public boolean disonnectApps(final ApplicationConfig inSourceApp, final ApplicationConfig inTargetApp) {
		if ((inSourceApp != null) && inSourceApp.isValid() && (inTargetApp != null) && inTargetApp.isValid()) {
			ApplicationLink appLink = findAppLink(inSourceApp, inTargetApp);
			if (appLink != null) {
				return systemAccess.deleteApplicationLink(appLink.getPrimaryKey());
			}
		}
		return true;
	}
	
	public ApplicationLink findAppLink(final ApplicationConfig inSourceApplication, final ApplicationConfig inTargetApplication) {
		final List<ApplicationLink> targetApps = systemAccess.referencesApplicationLinkBySource(inSourceApplication.getPrimaryKey());
		for (ApplicationLink appLink : targetApps) {
			if (appLink != null) {
				if (appLink.getDestination() == inTargetApplication.getPrimaryKey()) {
					return appLink;
				}
			}
		}
		return null;
	}

	public Infrastructure addInfrastructure(final Infrastructure inInfrastructure) {
		if ((inInfrastructure != null) && inInfrastructure.isValid()) {
			final SystemModel model = SystemModel.createEmpty();
			model.addInfrastructure(inInfrastructure);
			model.save();
			return inInfrastructure;
		}
		return null;
	}
	
	public SystemLink connectSystems(final SystemConfig inSourceSystem, final SystemConfig inTargetSystem) {
		if ((inSourceSystem != null) && inSourceSystem.isValid() && (inTargetSystem != null) && inTargetSystem.isValid()) {
			SystemLink systemLink = findSystemLink(inSourceSystem, inTargetSystem);
			if (systemLink == null) {
				systemLink = SystemLink.generate();
				systemLink.setSource(inSourceSystem.getPrimaryKey());
				systemLink.setDestination(inTargetSystem.getPrimaryKey());
				final SystemModel model = SystemModel.createEmpty();
				model.addSystemLink(systemLink);
				model.save();
			}
			return systemLink;
		}
		return null;
	}
	
	public boolean disonnectSystems(final SystemConfig inSourceSystem, final SystemConfig inTargetSystem) {
		if ((inSourceSystem != null) && inSourceSystem.isValid() && (inTargetSystem != null) && inTargetSystem.isValid()) {
			SystemLink systemLink = findSystemLink(inSourceSystem, inTargetSystem);
			if (systemLink != null) {
				return systemAccess.deleteSystemLink(systemLink.getPrimaryKey());
			}
		}
		return true;
	}
	
	private SystemLink findSystemLink(final SystemConfig inSourceSystem, final SystemConfig inTargetSystem) {
		if ((inSourceSystem != null) && (inTargetSystem != null)) {
			final List<SystemLink> targetLinks = systemAccess.referencesSystemLinkBySource(inSourceSystem.getPrimaryKey());
			for (SystemLink systemLink : targetLinks) {
				if (systemLink != null) {
					if (systemLink.getDestination() == inTargetSystem.getPrimaryKey()) {
						return systemLink;
					}
				}
			}
		}
		return null;
	}
	
	public DatabaseInstallation installDatabase(final DataModel inDataModel, final Computer inComputer) {
		if ((inComputer != null) && (inDataModel != null)) {
			final DataModel dataModel = findDataModel(inDataModel);
			if (dataModel != null) {
				final Computer computer = findComputer(inComputer);
				if (computer != null) {
					DatabaseInstallation dbInstallation = findDatabaseInstallation(dataModel, computer);
					if (dbInstallation == null) {
						dbInstallation = DatabaseInstallation.generate();
						dbInstallation.setDataModel(dataModel.getPrimaryKey());
						dbInstallation.setComputer(computer.getPrimaryKey());
						dbInstallation.save();
					}
					return dbInstallation;
				}
			}
		}
		return null;
	}
	
	public DatabaseInstallation findDatabaseInstallation(final DataModel inDataModel, final Computer inComputer) {
		if ((inComputer != null) && (inDataModel != null)) {
			final List<DatabaseInstallation> dbInstallations = systemAccess.referencesDatabaseInstallationByDataModel(inDataModel.getPrimaryKey());
			for (DatabaseInstallation dbInstallation : dbInstallations) {
				if (dbInstallation != null) {
					if (dbInstallation.getComputer() == inComputer.getPrimaryKey()) {
						return dbInstallation;
					}
				}
			}
		}
		return null;
	}

	private DataModel findDataModel(final DataModel inDataModel) {
		if (inDataModel != null) {
			return systemAccess.findByIDDataModel(inDataModel.getPrimaryKey());
		}
		return null;
	}

	public InterfaceMethod addInterfaceMethod(final InterfaceMethod inInterfaceMethod) {
		if ((inInterfaceMethod != null) && inInterfaceMethod.isValid()) {
			final SystemModel model = SystemModel.createEmpty();
			model.addInterfaceMethod(inInterfaceMethod);
			model.save();
			return inInterfaceMethod;
		}
		return null;
	}

	public InterfaceDataType addInterfaceDataType(final InterfaceDataType inInterfaceDataType) {
		if ((inInterfaceDataType != null) && inInterfaceDataType.isValid()) {
			final SystemModel model = SystemModel.createEmpty();
			model.addInterfaceDataType(inInterfaceDataType);
			model.save();
			return inInterfaceDataType;
		}
		return null;
	}

	public InterfaceMethodParameter addMethodParameter(final InterfaceMethodParameter inInterfaceMethodParameter) {
		if ((inInterfaceMethodParameter != null) && inInterfaceMethodParameter.isValid()) {
			final SystemModel model = SystemModel.createEmpty();
			model.addInterfaceMethodParameter(inInterfaceMethodParameter);
			model.save();
			return inInterfaceMethodParameter;
		}
		return null;
	}

	public ApplicationInterface addAppInterface(final ApplicationInterface inAppInterface) {
		if ((inAppInterface != null) && inAppInterface.isValid()) {
			final SystemModel model = SystemModel.createEmpty();
			model.addApplicationInterface(inAppInterface);
			model.save();
			return inAppInterface;
		}
		return null;
	}

	public ApplicationConfig addApp(final ApplicationConfig inApp) {
		if ((inApp != null) && inApp.isValid()) {
			final SystemModel model = SystemModel.createEmpty();
			model.addApplicationConfig(inApp);
			model.save();
			return inApp;
		}
		return null;
	}
	
	public DataModel addDataModel(final DataModel inDataModel) {
		if ((inDataModel != null) && inDataModel.isValid()) {
			final SystemModel model = SystemModel.createEmpty();
			model.addDataModel(inDataModel);
			model.save();
			return inDataModel;
		}
		return null;
	}
	
	public ModelAttribute addModelAttribute(final ModelAttribute inModelAttribute) {
		if ((inModelAttribute != null) && inModelAttribute.isValid()) {
			final SystemModel model = SystemModel.createEmpty();
			model.addModelAttribute(inModelAttribute);
			model.save();
			return inModelAttribute;
		}
		return null;
	}
	
	public ModelReference addModelReference(final ModelReference inModelReference) {
		if ((inModelReference != null) && inModelReference.isValid()) {
			final SystemModel model = SystemModel.createEmpty();
			model.addModelReference(inModelReference);
			model.save();
			return inModelReference;
		}
		return null;
	}
	
	public ModelDataType addModelDataType(final ModelDataType inModelDataType) {
		if ((inModelDataType != null) && inModelDataType.isValid()) {
			final SystemModel model = SystemModel.createEmpty();
			model.addModelDataType(inModelDataType);
			model.save();
			return inModelDataType;
		}
		return null;
	}
	
	public ModelEntity addModelEntity(final ModelEntity inModelEntity) {
		if ((inModelEntity != null) && inModelEntity.isValid()) {
			final SystemModel model = SystemModel.createEmpty();
			model.addModelEntity(inModelEntity);
			model.save();
			return inModelEntity;
		}
		return null;
	}
	
	public Computer addComputer(final Computer inComputer) {
		if ((inComputer != null) && inComputer.isValid()) {
			final SystemModel model = SystemModel.createEmpty();
			model.addComputer(inComputer);
			model.save();
			return inComputer;
		}
		return null;
	}
	
	public Network addNetwork(final Network inNetwork) {
		if ((inNetwork != null) && inNetwork.isValid()) {
			final SystemModel model = SystemModel.createEmpty();
			model.addNetwork(inNetwork);
			model.save();
			return inNetwork;
		}
		return null;
	}
	
	public SystemConfig getSystemByName(final String inSystemName) {
		final List<SystemConfig> configs = systemAccess.listSystemConfig();
		for (SystemConfig systemConfig : configs) {
			if (inSystemName.equalsIgnoreCase(systemConfig.getSystemName())) {
				return systemConfig;
			}
		}
		return null;
	}
	
	public List<ApplicationConfig> getSystemApplications(final String inSystemName) {
		final SystemConfig systemX = getSystemByName(inSystemName);
		if (systemX != null) {
			return systemAccess.referencesApplicationConfigBySystemConfig(systemX.getPrimaryKey());
		}
		return new ArrayList<ApplicationConfig>();
	}
	
	public ApplicationConfig getApplication(final String inSystemName, final String inApplicationName) {
		final SystemConfig systemX = getSystemByName(inSystemName);
		if (systemX != null) {
			final List<ApplicationConfig> applications = systemAccess.referencesApplicationConfigBySystemConfig(systemX.getPrimaryKey());
			for (ApplicationConfig applicationConfig : applications) {
				if (inApplicationName.equalsIgnoreCase(applicationConfig.getApplicationName())) {
					return applicationConfig;
				}
			}
		}
		return null;
	}
	
	public DatabaseSession login(final DatabaseInstallation inDatabaseInstallation) {
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
	
	public boolean logout(final DatabaseSession inDatabaseSession) {
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
	
	public ApplicationSession login(final ApplicationInstallation inApplicationInstallation) {
		if (inApplicationInstallation != null) {
			final ApplicationSession applicationSession = ApplicationSession.generate();
			applicationSession.setApplicationInstallation(inApplicationInstallation.getPrimaryKey());
			final Date now = new Date();
			applicationSession.setFromDate(now);
			applicationSession.setToDate(new Date(now.getTime()));
			applicationSession.save();
			return applicationSession;
		}
		return null;
	}
	
	public boolean logout(final ApplicationSession inApplicationSession) {
		if (inApplicationSession != null) {
			final ApplicationSession foundSession = systemAccess.findByIDApplicationSession(inApplicationSession.getPrimaryKey());
			if (foundSession != null) {
				foundSession.setToDate(new Date());
				foundSession.save();
				return true;
			}			
		}
		return false;
	}
	
	public List<ApplicationInstallation> getAppInstallations(final ApplicationConfig inApplicationConfig) {
		return systemAccess.referencesApplicationInstallationByApplicationConfig(inApplicationConfig.getPrimaryKey());
	}
	
	public List<ApplicationSession> getSessions(final ApplicationInstallation inApplicationInstallation) {
		return systemAccess.referencesApplicationSessionByApplicationInstallation(inApplicationInstallation.getPrimaryKey());
	}
	
	public List<SessionState> getSessionStates(final ApplicationSession inApplicationSession) {
		return systemAccess.referencesSessionStateByApplicationSession(inApplicationSession.getPrimaryKey());
	}
	
	public List<PropertyState> getStates(final SessionState inSessionState) {
		return systemAccess.referencesPropertyStateBySessionState(inSessionState.getPrimaryKey());
	}
	
	public boolean updatePropertyState(final String inPropertyKey, final String inPropertyValue, final ConfigurationItem inConfigurationItem, final ApplicationSession inApplicationSession) {
		List<PropertyState> propertyStates = getConfigurationItemState(inConfigurationItem, inApplicationSession);
		for (PropertyState propertyState : propertyStates) {
			if (propertyState != null) {
				if (propertyState.getPropertyKey().equalsIgnoreCase(inPropertyKey)) {
					propertyState.setPropertyValue(inPropertyValue);
					propertyState.setLastUpdated(new Date());
					return propertyState.save();
				}
			}
		}
		final PropertyState propertyState = PropertyState.generate();
		propertyState.setPropertyKey(inPropertyKey);
		propertyState.setPropertyValue(inPropertyValue);
		propertyState.setLastUpdated(new Date());
		return propertyState.save();
	}
	
	public Object getPropertyState(final String inPropertyKey, final ConfigurationItem inConfigurationItem, final ApplicationSession inApplicationSession) {
		final Map<String, String> itemState = getItemState(inConfigurationItem, inApplicationSession);
		return itemState.get(inPropertyKey);
	}
	
	public Map<String, String> getItemState(final ConfigurationItem inConfigurationItem, final ApplicationSession inApplicationSession) {
		final Map<String, String> itemState = new HashMap<String, String>();
		final List<PropertyState> propertyStates = getConfigurationItemState(inConfigurationItem, inApplicationSession);
		for (PropertyState propertyState : propertyStates) {
			if (propertyState != null) {
				itemState.put(propertyState.getPropertyKey(), propertyState.getPropertyValue());
			}
		}
		return itemState;
	}
	
	public List<PropertyState> getConfigurationItemState(final ConfigurationItem inConfigurationItem, final ApplicationSession inApplicationSession) {
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
	
	public ConfigurationItem findRootItem(final ApplicationConfig inApp) {
		ConfigurationItem root = null;
		if (inApp != null) {
			final List<ConfigurationItem> items = systemAccess.referencesConfigurationItemByApplicationConfig(inApp.getPrimaryKey());
			if (items.size() > 0) {
				final AtomicInteger recursionCounter = new AtomicInteger(0);
				ConfigurationItem item = items.get(0);
				while (item != null) {
					root = item;
					recursionCounter.incrementAndGet();
					if (recursionCounter.get() >= MAX_DEPTH) {
						break;
					}
					final List<ConfigurationItemLink> inputLinks = systemAccess.referencesConfigurationItemLinkByDestination(item.getPrimaryKey());
					if (inputLinks.size() > 0) {
						final ConfigurationItemLink link = inputLinks.get(0);
						if (link != null) {
							item = link.getSourceRef();
						}
					}
				}
			}
		}
		return root;
	}
	
	public Map<String, String> getAppConfiguration(final ApplicationConfig inApp) {
		final ConfigurationItem rootItem = findRootItem(inApp);
		return getConfigurations(rootItem);
	}
	
	public Map<String, String> getConfigurations(final ConfigurationItem inConfigurationItem) {
		Map<String, String> config = new HashMap<String, String>();
		final String rootPathKey = getRootPathKey(inConfigurationItem);
		addItem(config, inConfigurationItem, rootPathKey);
		return config;
	}
	
	private String getRootPathKey(final ConfigurationItem inConfigurationItem) {
		if (inConfigurationItem != null) {
			final StringBuilder path = new StringBuilder();
			addPath(path, inConfigurationItem);			
		}
		return null;
	}

	private void addPath(final StringBuilder inPath, final ConfigurationItem inConfigurationItem) {
		if (inConfigurationItem != null) {
			inPath.insert(0, String.format("%s.", inConfigurationItem.getItemKey()));
			final List<ConfigurationItemLink> parentLinks = systemAccess.referencesConfigurationItemLinkByDestination(inConfigurationItem.getPrimaryKey());
			if (parentLinks.size() >= 1) {
				for (ConfigurationItemLink configurationItemLink : parentLinks) {
					final ConfigurationItem parent = configurationItemLink.getSourceRef();
					if (parent != null) {
						addPath(inPath, parent);
					}
				}
			}
		}
	}

	private void addItem(final Map<String, String> inConfig, final ConfigurationItem inConfigurationItem, final String inPathKey) {
		if (inConfigurationItem != null) {
			final String newPathKey;
			if (inPathKey == null) {
				newPathKey = inConfigurationItem.getItemKey();
			} else {
				newPathKey = String.format("%s.%s", inPathKey, inConfigurationItem.getItemKey());
			}
			inConfig.put(newPathKey, inConfigurationItem.getItemValue());
			final List<ConfigurationItemLink> childLinks = systemAccess.referencesConfigurationItemLinkBySource(inConfigurationItem.getPrimaryKey());
			for (ConfigurationItemLink configurationItemLink : childLinks) {
				final ConfigurationItem child = configurationItemLink.getDestinationRef();
				if (child != null) {
					addItem(inConfig, child, newPathKey);
				}
			}
		}
	}
	
	public List<ConfigurationItem> getConfigurationItems(final ApplicationConfig inApplicationConfig) {
		return systemAccess.referencesConfigurationItemByApplicationConfig(inApplicationConfig.getPrimaryKey());
	}
	
	
	public List<Network> getNetworks() {
		return systemAccess.listNetwork();
	}
	
	public List<ApplicationConfig> getApps(final String inApplicationType) {
		return getApps();
	}
	
	public List<Computer> getComputers() {
		return systemAccess.listComputer();
	}
	
	public List<Infrastructure> getInfrastructures() {
		return systemAccess.listInfrastructure();
	}
	
	public SystemModel getSystemInfrastructure(final String inInfrastructureName) {
		if (inInfrastructureName != null) {
			final List<Infrastructure> infrastructures = getInfrastructures();
			for (Infrastructure infrastructure : infrastructures) {
				if (inInfrastructureName.equalsIgnoreCase(infrastructure.getInfrastructureName())) {
					return getSystemInfrastructure(infrastructure);
				}
			}
		}
		return SystemModel.createEmpty();
	}
	
	public SystemModel getAppSystem(final String inSystemName) {
		final SystemConfig system = findSystemByName(inSystemName);
		return getAppSystem(system);
	}
	
	private SystemConfig findSystemByName(final String inSystemName) {
		if (inSystemName != null) {
			final List<SystemConfig> systems = getSystems();
			for (SystemConfig system : systems) {
				if (system != null) {
					if (inSystemName.equalsIgnoreCase(system.getSystemName())) {
						return system;
					}
				}
			}
		}
		return null;
	}

	public SystemModel getSystemInfrastructure(final Infrastructure inInfrastructure) {
		if (inInfrastructure != null) {
			final List<SystemConfig> infrastructureSystems = getSystems(inInfrastructure);
			return systemAccess.contextExportSystemConfig(infrastructureSystems);
		}
		return SystemModel.createEmpty();
	}
	
	public SystemModel getAppSystem(final SystemConfig inSystem) {
		if (inSystem != null) {
			final List<ApplicationConfig> systemApps = getApps(inSystem);
			return systemAccess.contextExportApplicationConfig(systemApps);
		}
		return SystemModel.createEmpty();
	}
	
	public List<SystemConfig> getSystems() {
		return systemAccess.listSystemConfig();
	}
	
	public List<SystemConfig> getSystems(final Infrastructure inInfrastructure) {
		if (inInfrastructure != null) {
			return systemAccess.referencesSystemConfigByInfrastructure(inInfrastructure.getPrimaryKey());
		}
		return new ArrayList<SystemConfig>();
	}
		
	public List<ApplicationConfig> getApps(final Infrastructure inInfrastructure) {
		final List<ApplicationConfig> apps = new ArrayList<ApplicationConfig>();
		if (inInfrastructure != null) {
			final List<SystemConfig> systems = getSystems(inInfrastructure);
			for (SystemConfig system : systems) {
				apps.addAll(getApps(system));
			}
		}
		return apps;
	}
	
	public List<ApplicationConfig> getApps(final ApplicationType inApplicationType) {
		if (inApplicationType != null) {
			return systemAccess.referencesApplicationConfigByApplicationType(inApplicationType.getPrimaryKey());
		}
		return new ArrayList<ApplicationConfig>();
	}
	
	public List<ApplicationConfig> getApps() {
		return systemAccess.listApplicationConfig();
	}
	
	public List<ApplicationConfig> getApps(final SystemConfig inSystem) {
		if (inSystem != null) {
			return systemAccess.referencesApplicationConfigBySystemConfig(inSystem.getPrimaryKey());
		}
		return new ArrayList<ApplicationConfig>();
	}
	
	public List<ApplicationConfig> getApps(final Computer inComputer) {
		final List<ApplicationConfig> apps = new ArrayList<ApplicationConfig>();
		if (inComputer != null) {
			final List<ApplicationInstallation> installations = systemAccess.referencesApplicationInstallationByComputer(inComputer.getPrimaryKey());
			for (ApplicationInstallation installation : installations) {
				final ApplicationConfig app = installation.getApplicationConfigRef();
				if (app != null) {
					apps.add(app);
				}
			}
		}
		return apps;
	}
	
	public Network findNetwork(final String inNetworkName) {
		final List<Network> networks = getNetworks();
		for (Network network : networks) {
			if (inNetworkName.equalsIgnoreCase(network.getNetworkName())) {
				return network;
			}
		}
		return null;
	}
	
	public List<Computer> getComputers(final String inNetworkName) {
		final Network network = findNetwork(inNetworkName);
		return getComputers(network);
	}
	
	public List<Computer> getComputers(final Network inNetwork) {
		if (inNetwork != null) {
			return systemAccess.referencesComputerByNetwork(inNetwork.getPrimaryKey());
		}
		return new ArrayList<Computer>();
	}
	
	public List<ApplicationInstallation> getInstallations(final Computer inComputer) {
		if (inComputer != null) {
			return systemAccess.referencesApplicationInstallationByComputer(inComputer.getPrimaryKey());
		}
		return new ArrayList<ApplicationInstallation>();
	}
	
	public ApplicationInstallation installApp(final String inComputerName, final String inApplicationName) {
		final Computer computer = findComputer(inComputerName);
		if ((computer != null) && (inApplicationName != null)) {
			final ApplicationConfig app = findAppByName(inApplicationName);
			if (app != null) {
				if (computer != null) {
					ApplicationInstallation applicationInstallation = findInstallation(computer, app);
					if (applicationInstallation == null) {
						applicationInstallation = ApplicationInstallation.generate();
						applicationInstallation.setApplicationConfig(app.getPrimaryKey());
						applicationInstallation.setComputer(computer.getPrimaryKey());
					} else {
						applicationInstallation.setInstallationDate(new Date());
					}
					applicationInstallation.save();
					return applicationInstallation;
				}
			}
		}
		return null;
	}
	
	private ApplicationInstallation findInstallation(final Computer inComputer, final ApplicationConfig inApp) {
		if (inApp != null) {
			List<ApplicationInstallation> computerInstallations = getInstallations(inComputer);
			for (ApplicationInstallation installation : computerInstallations) {
				if (inApp.getPrimaryKey() == installation.getApplicationConfig()) {
					return installation;
				}
			}
		}
		return null;
	}

	private Computer findComputer(final String inComputer) {
		final List<Computer> computers = getComputers();
		for (Computer computer : computers) {
			if (inComputer.equalsIgnoreCase(computer.getComputerName())) {
				return computer;
			}
		}
		for (Computer computer : computers) {
			if (inComputer.equalsIgnoreCase(computer.getIPAddress())) {
				return computer;
			}
		}
		return null;
	}

	public ApplicationInstallation installApp(final Computer inComputer, final ApplicationConfig inApplication) {
		if ((inComputer != null) && (inApplication != null)) {
			final ApplicationConfig app = findAppByName(inApplication.getApplicationName());
			if (app != null) {
				final Computer computer = findComputer(inComputer);
				if (computer != null) {
					final ApplicationInstallation applicationInstallation = ApplicationInstallation.generate();
					applicationInstallation.setApplicationConfig(app.getPrimaryKey());
					applicationInstallation.setComputer(computer.getPrimaryKey());
					applicationInstallation.save();
					return applicationInstallation;
				}
			}
		}
		return null;
	}
	
	public void uninstallApp(final ApplicationInstallation inApplicationInstallation) {
		if (inApplicationInstallation != null) {
			final List<ApplicationSession> appSessions = systemAccess.referencesApplicationSessionByApplicationInstallation(inApplicationInstallation.getPrimaryKey());
			for (ApplicationSession appSession : appSessions) {
				final List<SessionState> sessionStates = systemAccess.referencesSessionStateByApplicationSession(appSession.getPrimaryKey());
				for (SessionState sessionState : sessionStates) {
					final List<PropertyState> propertyStates = systemAccess.referencesPropertyStateBySessionState(sessionState.getPrimaryKey());
					for (PropertyState propertyState : propertyStates) {
						systemAccess.deletePropertyState(propertyState.getPrimaryKey());
					}
					systemAccess.deleteSessionState(sessionState.getPrimaryKey());
				}
				systemAccess.deleteApplicationSession(appSession.getPrimaryKey());
			}
			systemAccess.deleteApplicationInstallation(inApplicationInstallation.getPrimaryKey());
		}
	}
	
	public void uninstallDatabase(final DatabaseInstallation inDatabaseInstallation) {
		if (inDatabaseInstallation != null) {
			final List<DatabaseSession> dbSessions = systemAccess.referencesDatabaseSessionByDatabaseInstallation(inDatabaseInstallation.getPrimaryKey());
			for (DatabaseSession dbSession : dbSessions) {
				systemAccess.deleteDatabaseSession(dbSession.getPrimaryKey());
			}
			systemAccess.deleteDatabaseInstallation(inDatabaseInstallation.getPrimaryKey());
		}
	}
	
	public Computer findComputer(final Computer inComputer) {
		if (inComputer != null) {
			return systemAccess.findByIDComputer(inComputer.getPrimaryKey());
		}
		return null;
	}

	public ApplicationConfig findAppByName(final String inApplicationName) {
		final List<ApplicationConfig> apps = systemAccess.listApplicationConfig();
		for (ApplicationConfig app : apps) {
			if (inApplicationName.equalsIgnoreCase(app.getApplicationName())) {
				return app;
			}
		}
		return null;
	}

	public List<ApplicationInstallation> getInstallationsSince(final Computer inComputer, final Date inSince) {
		if (inComputer != null) {
			final List<ApplicationInstallation> installations = systemAccess.referencesApplicationInstallationByComputer(inComputer.getPrimaryKey());
			if (inSince != null) {
				final List<ApplicationInstallation> latestInstallations = new ArrayList<ApplicationInstallation>();
				for (ApplicationInstallation applicationInstallation : installations) {
					if (applicationInstallation != null) {
						if (inSince.before(applicationInstallation.getInstallationDate())) {
							latestInstallations.add(applicationInstallation);
						}
					}
				}
				return latestInstallations;
			} else {
				return installations;
			}
		}
		return new ArrayList<ApplicationInstallation>();
	}
	
	public List<ApplicationSession> getRunningApps(final Computer inComputer) {
		final List<ApplicationInstallation> installations = getInstallations(inComputer);
		final List<ApplicationSession> runningSessions = new ArrayList<ApplicationSession>();
		final Date now = new Date();
		for (ApplicationInstallation installation : installations) {
			final List<ApplicationSession> sessions = systemAccess.referencesApplicationSessionByApplicationInstallation(installation.getPrimaryKey());
			for (ApplicationSession session : sessions) {
				if ((now.after(session.getFromDate()))) {
					if (session.getFromDate() == session.getToDate()) {
						runningSessions.add(session);
					}
				}
			}
		}
		return runningSessions;
	}
	
	public List<InterfaceMethod> getAppInterface(final ApplicationInterface inApplicationInterface) {
		if (inApplicationInterface != null) {
			return systemAccess.referencesInterfaceMethodByApplicationInterface(inApplicationInterface.getPrimaryKey());
		}
		return new ArrayList<InterfaceMethod>();
	}
	
	public List<DataModel> getDataModels(final ApplicationConfig inApp) {
		final List<DataModel> dataModels = new ArrayList<DataModel>();
		if (inApp != null) {
			final List<ApplicationDataModel> appModels = systemAccess.referencesApplicationDataModelByApplicationConfig(inApp.getPrimaryKey());
			for (ApplicationDataModel appModel : appModels) {
				if (appModel != null) {
					final DataModel dataModel = appModel.getDataModelRef();
					if (dataModel != null) {
						dataModels.add(dataModel);
					}
				}
			}
		}
		return dataModels;
	}
	
	public List<Computer> getComputers(final ApplicationConfig inApp) {
		final List<Computer> computers = new ArrayList<Computer>();
		if (inApp != null) {
			final List<ApplicationInstallation> installations = getAppInstallations(inApp);
			for (ApplicationInstallation installation : installations) {
				final Computer computer = installation.getComputerRef();
				if (computer != null) {
					computers.add(computer);
				}
			}
		}
		return computers;
	}
	
	public List<Computer> getComputers(final DataModel inDataModel) {
		final List<Computer> computers = new ArrayList<Computer>();
		if (inDataModel != null) {
			final List<DatabaseInstallation> installations = getInstallations(inDataModel);
			for (DatabaseInstallation databaseInstallation : installations) {
				final Computer computer = databaseInstallation.getComputerRef();
				if (computer != null) {
					computers.add(computer);
				}
			}
		}
		return computers;
	}
	
	public List<DatabaseInstallation> getInstallations(final DataModel inDataModel) {
		if (inDataModel != null) {
			return systemAccess.referencesDatabaseInstallationByDataModel(inDataModel.getPrimaryKey());
		}
		return new ArrayList<DatabaseInstallation>();
	}
		
	public List<ApplicationInterface> getAppInterfaces(final ApplicationConfig inApplicationConfig) {
		if (inApplicationConfig != null) {
			return systemAccess.referencesApplicationInterfaceByApplicationConfig(inApplicationConfig.getPrimaryKey());
		}
		return new ArrayList<ApplicationInterface>();
	}
	
	public ApplicationInterface addAppInterface(final ApplicationConfig inApplicationConfig, final String inInterfaceName) {
		if (inApplicationConfig != null) {
			final ApplicationConfig app = systemAccess.findByIDApplicationConfig(inApplicationConfig.getPrimaryKey());
			if (app != null) {
				final SystemModel model = SystemModel.createEmpty();
				final ApplicationInterface appInterface = ApplicationInterface.generate();
				appInterface.setApplicationConfig(app.getPrimaryKey());
				appInterface.setInterfaceName(inInterfaceName);
				model.addApplicationInterface(appInterface);
				model.save();
			}
		}
		return null;
	}
	
	public InterfaceMethod addInterfaceMethod(final ApplicationInterface inApplicationInterface, final String inMethodName, final String inMethodDescription, 
			final boolean inAsynchronously, final InterfaceMethodParameter[] inParameters, final InterfaceDataType inReturnType) {
		InterfaceMethod interfaceMethod = null;
		if (inApplicationInterface != null) {
			final ApplicationInterface appInterface = systemAccess.findByIDApplicationInterface(inApplicationInterface.getPrimaryKey());
			if (appInterface != null) {
				final SystemModel model = SystemModel.createEmpty();
				interfaceMethod = InterfaceMethod.generate();
				interfaceMethod.setApplicationInterface(inApplicationInterface.getPrimaryKey());
				interfaceMethod.setMethodName(inMethodName);
				interfaceMethod.setAsynchronouslyCall(inAsynchronously);
				if (inMethodDescription != null) {
					interfaceMethod.setDescription(inMethodDescription);
				}
				model.addInterfaceMethod(interfaceMethod);
				if (inParameters != null) {
					for (int i = 0; i < inParameters.length; i++) {
						if (inParameters[i] != null) {
							inParameters[i].setInterfaceMethod(interfaceMethod.getPrimaryKey());
							model.addInterfaceMethodParameter(inParameters[i]);
						}
					}					
				}
				if (inReturnType != null) {
					interfaceMethod.setReturnType(inReturnType.getPrimaryKey());
				}
				model.save();
			}
		}
		return interfaceMethod;
	}
		
	public InterfaceMethod addInterfaceMethod(final ApplicationInterface inApplicationInterface, final String inMethodName, final String inMethodDescription, 
			final boolean inAsynchronously, final InterfaceMethodParameter[] inParameters) {
		return addInterfaceMethod(inApplicationInterface, inMethodName, inMethodDescription, inAsynchronously, inParameters, null);
	}
		
	public InterfaceMethod addInterfaceMethod(final ApplicationInterface inApplicationInterface, final String inMethodName, final String inMethodDescription) {
		return addInterfaceMethod(inApplicationInterface, inMethodName, inMethodDescription, false, null, null);
	}
	
	public InterfaceMethod addInterfaceMethod(final ApplicationInterface inApplicationInterface, final String inMethodName) {
		return addInterfaceMethod(inApplicationInterface, inMethodName, null, false, null, null);
	}
	
	public InterfaceMethod addInterfaceMethod(final ApplicationInterface inApplicationInterface, final String inMethodName, final InterfaceDataType inReturnType) {
		return addInterfaceMethod(inApplicationInterface, inMethodName, null, false, null, inReturnType);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////

	public SystemModel getDataModel(final DataModel inDataModel) {
		final SystemModel systemModel = SystemModel.createEmpty();
		if (inDataModel != null) {
			final DataModel dataModel = systemModel.findByIDDataModel(inDataModel.getPrimaryKey());
			if (dataModel != null) {
				systemModel.addDataModel(dataModel);
				final List<ModelEntity> modelEntities = systemModel.referencesModelEntityByDataModel(dataModel.getPrimaryKey());
				systemModel.addAllModelEntity(modelEntities);
				for (ModelEntity modelEntity : modelEntities) {
					if (modelEntity != null) {
						final List<ModelAttribute> modelAttributes = systemAccess.referencesModelAttributeByModelEntity(modelEntity.getPrimaryKey());
						final SystemModel attributesModel = systemAccess.exportModelAttribute(modelAttributes);
						systemModel.add(attributesModel);
						final List<ModelReference> modelReferencesIn = systemAccess.referencesModelReferenceBySource(modelEntity.getPrimaryKey());
						final SystemModel inReferencesModel = systemAccess.exportModelReference(modelReferencesIn);
						systemModel.add(inReferencesModel);
						final List<ModelReference> modelReferencesOut = systemAccess.referencesModelReferenceByDestination(modelEntity.getPrimaryKey());
						final SystemModel outReferencesModel = systemAccess.exportModelReference(modelReferencesOut);
						systemModel.add(outReferencesModel);
					}
				}
			}
		}
		return systemModel;
	}

	public SystemModel getAppConfigurationModel(final ApplicationConfig inApp) {
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

	public SystemModel getAppSessionModel(final ApplicationSession inAppSession) {
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

	public SystemModel getComputerModel(final Computer inComputer) {
		final SystemModel systemModel = SystemModel.createEmpty();
		if (inComputer != null) {
			final Computer computer = systemAccess.findByIDComputer(inComputer.getPrimaryKey());
			if (computer != null) {
				systemModel.addComputer(computer);
				final List<DatabaseInstallation> databaseInstallations = systemAccess.referencesDatabaseInstallationByComputer(computer.getPrimaryKey());
				final SystemModel dbModel = systemAccess.exportDatabaseInstallation(databaseInstallations);
				systemModel.add(dbModel);
				final List<ApplicationInstallation> appInstallations = systemAccess.referencesApplicationInstallationByComputer(computer.getPrimaryKey());
				final SystemModel appModel = systemAccess.exportApplicationInstallation(appInstallations);
				systemModel.add(appModel);
			}
		}
		return systemModel;
	}

	public SystemModel getSystemInfrastructureModel(final Infrastructure inInfrastructure) {
		final SystemModel systemModel = SystemModel.createEmpty();
		if (inInfrastructure != null) {
			final Infrastructure infrastructure = systemAccess.findByIDInfrastructure(inInfrastructure.getPrimaryKey());
			if (infrastructure != null) {
				systemModel.addInfrastructure(infrastructure);
				final List<SystemConfig> systems = systemAccess.referencesSystemConfigByInfrastructure(infrastructure.getPrimaryKey());
				final SystemModel systemNetModel = systemAccess.contextExportSystemConfig(systems);
				systemModel.add(systemNetModel);
			}
		}
		return systemModel;
	}

	public SystemModel getSystemModel(final SystemConfig inSystem) {
		final SystemModel systemModel = SystemModel.createEmpty();
		if (inSystem != null) {
			final SystemConfig system = systemAccess.findByIDSystemConfig(inSystem.getPrimaryKey());
			if (system != null) {
				systemModel.addSystemConfig(system);
				final List<ApplicationConfig> apps = systemAccess.referencesApplicationConfigBySystemConfig(system.getPrimaryKey());
				final SystemModel appModel = systemAccess.contextExportApplicationConfig(apps);
				systemModel.add(appModel);
			}
		}
		return systemModel;
	}

	public SystemModel getInterfaceModel(final ApplicationInterface inInterface) {
		final SystemModel systemModel = SystemModel.createEmpty();
		if (inInterface != null) {
			final ApplicationInterface appInterface = systemAccess.findByIDApplicationInterface(inInterface.getPrimaryKey());
			if (appInterface != null) {
				final SystemModel interfaceBasic = systemAccess.exportApplicationInterface(appInterface.getPrimaryKey());
				systemModel.add(interfaceBasic);
				final List<InterfaceMethod> methods = systemAccess.referencesInterfaceMethodByApplicationInterface(appInterface.getPrimaryKey());
				final SystemModel methodModel = systemAccess.contextExportInterfaceMethod(methods);
				systemModel.add(methodModel);
			}
		}
		return systemModel;
	}

	public List<ApplicationInstallation> getAppInstallations(final InterfaceMethod inMethod) {
		if (inMethod != null) {
			final ApplicationInterface appInterface = inMethod.getApplicationInterfaceRef();
			if (appInterface != null) {
				final ApplicationConfig app = appInterface.getApplicationConfigRef();
				if (app != null) {
					return systemAccess.referencesApplicationInstallationByApplicationConfig(app.getPrimaryKey());
				}
			}
		}
		return new ArrayList<ApplicationInstallation>();
	}
	
	public List<ApplicationInstallation> getAppInstallationsAvailable(final InterfaceMethod inMethod) {
		final List<ApplicationInstallation> availableInstallations = new ArrayList<ApplicationInstallation>();
		final List<ApplicationInstallation> allInstallations = getAppInstallations(inMethod);
		for (ApplicationInstallation installation : allInstallations) {
			final List<ApplicationSession> sessions = systemAccess.referencesApplicationSessionByApplicationInstallation(installation.getPrimaryKey());
			for (ApplicationSession session : sessions) {
				if ((session.getFromDate().getTime() == session.getToDate().getTime())) {
					availableInstallations.add(installation);
					break;
				}
			}
		}
		return availableInstallations;
	}

	public void shutdown(final Computer inComputer) {
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

	public void shutdown(final ApplicationSession inAppSession) {
		if (inAppSession != null) {
			inAppSession.setToDate(new Date());
			inAppSession.save();
		}
	}
	
	public void startApp(final ApplicationInstallation inAppInstallation) {
		if (inAppInstallation != null) {
			final ApplicationSession appSession = ApplicationSession.generate();
			final Date now = new Date();
			appSession.setApplicationInstallation(inAppInstallation.getPrimaryKey());
			appSession.setFromDate(now);
			appSession.setToDate(new Date(now.getTime()));
			appSession.save();
		}
	}
	
	public void startDatabase(final DatabaseInstallation inDbInstallation) {
		if (inDbInstallation != null) {
			final DatabaseSession dbSession = DatabaseSession.generate();
			final Date now = new Date();
			dbSession.setDatabaseInstallation(inDbInstallation.getPrimaryKey());
			dbSession.setFromDate(now);
			dbSession.setToDate(new Date(now.getTime()));
			dbSession.save();
		}
	}

	public void installComputer(final String inComputerName, final String inIPAddress, final String inRemarks, final Network inNetwork) {
		final Computer computer = Computer.generate();
		computer.setComputerName(inComputerName);
		computer.setIPAddress(inIPAddress);
		computer.setRemarks(inRemarks);
		if (inNetwork != null) {
			computer.setNetwork(inNetwork.getPrimaryKey());
		}
	}

	public void uninstallComputer(final Computer inComputer) {
		if (inComputer != null) {
			final List<ApplicationInstallation> appInstallations = systemAccess.referencesApplicationInstallationByComputer(inComputer.getPrimaryKey());
			for (ApplicationInstallation applicationInstallation : appInstallations) {
				uninstallApp(applicationInstallation);
			}
			final List<DatabaseInstallation> dbInstallations = systemAccess.referencesDatabaseInstallationByComputer(inComputer.getPrimaryKey());
			for (DatabaseInstallation dbInstallation : dbInstallations) {
				uninstallDatabase(dbInstallation);
			}
			systemAccess.deleteComputer(inComputer.getPrimaryKey());
		}
	}
}
