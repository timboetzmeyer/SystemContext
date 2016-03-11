package de.boetzmeyer.systemcontext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import de.boetzmeyer.systemmodel.ApplicationConfig;
import de.boetzmeyer.systemmodel.ApplicationInstallation;
import de.boetzmeyer.systemmodel.ApplicationInterface;
import de.boetzmeyer.systemmodel.ApplicationLink;
import de.boetzmeyer.systemmodel.ApplicationType;
import de.boetzmeyer.systemmodel.Computer;
import de.boetzmeyer.systemmodel.ConfigurationItem;
import de.boetzmeyer.systemmodel.ConfigurationItemLink;
import de.boetzmeyer.systemmodel.IServer;
import de.boetzmeyer.systemmodel.Infrastructure;
import de.boetzmeyer.systemmodel.InterfaceDataType;
import de.boetzmeyer.systemmodel.InterfaceMethod;
import de.boetzmeyer.systemmodel.InterfaceMethodParameter;
import de.boetzmeyer.systemmodel.SystemConfig;
import de.boetzmeyer.systemmodel.SystemModel;

final class ApplicationServiceImpl extends SystemContextService implements ApplicationService {
	private static final int MAX_DEPTH = 100;

	public ApplicationServiceImpl(final IServer inSystemAccess) {
		super(inSystemAccess);
	}

	@Override
	public ApplicationType addApplicationType(ApplicationType inApplicationType) {
		if ((inApplicationType != null) && inApplicationType.isValid()) {
			final SystemModel model = SystemModel.createEmpty();
			model.addApplicationType(inApplicationType);
			model.save();
			return inApplicationType;
		}
		return null;
	}

	@Override
	public ApplicationLink connectApps(ApplicationConfig inSourceApplication, ApplicationConfig inTargetApplication) {
		if ((inSourceApplication != null) && inSourceApplication.isValid() && (inTargetApplication != null)
				&& inTargetApplication.isValid()) {
			final ApplicationConfig sourceApp = systemAccess
					.findByIDApplicationConfig(inSourceApplication.getPrimaryKey());
			if (sourceApp != null) {
				final ApplicationConfig targetApp = systemAccess
						.findByIDApplicationConfig(inTargetApplication.getPrimaryKey());
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

	@Override
	public boolean disconnectApps(ApplicationConfig inSourceApp, ApplicationConfig inTargetApp) {
		if ((inSourceApp != null) && inSourceApp.isValid() && (inTargetApp != null) && inTargetApp.isValid()) {
			ApplicationLink appLink = findAppLink(inSourceApp, inTargetApp);
			if (appLink != null) {
				return systemAccess.deleteApplicationLink(appLink.getPrimaryKey());
			}
		}
		return true;
	}

	@Override
	public ApplicationLink findAppLink(ApplicationConfig inSourceApplication, ApplicationConfig inTargetApplication) {
		final List<ApplicationLink> targetApps = systemAccess
				.referencesApplicationLinkBySource(inSourceApplication.getPrimaryKey());
		for (ApplicationLink appLink : targetApps) {
			if (appLink != null) {
				if (appLink.getDestination() == inTargetApplication.getPrimaryKey()) {
					return appLink;
				}
			}
		}
		return null;
	}

	@Override
	public InterfaceMethod addInterfaceMethod(InterfaceMethod inInterfaceMethod) {
		if ((inInterfaceMethod != null) && inInterfaceMethod.isValid()) {
			final SystemModel model = SystemModel.createEmpty();
			model.addInterfaceMethod(inInterfaceMethod);
			model.save();
			return inInterfaceMethod;
		}
		return null;
	}

	@Override
	public InterfaceDataType addInterfaceDataType(InterfaceDataType inInterfaceDataType) {
		if ((inInterfaceDataType != null) && inInterfaceDataType.isValid()) {
			final SystemModel model = SystemModel.createEmpty();
			model.addInterfaceDataType(inInterfaceDataType);
			model.save();
			return inInterfaceDataType;
		}
		return null;
	}

	@Override
	public InterfaceMethodParameter addMethodParameter(InterfaceMethodParameter inInterfaceMethodParameter) {
		if ((inInterfaceMethodParameter != null) && inInterfaceMethodParameter.isValid()) {
			final SystemModel model = SystemModel.createEmpty();
			model.addInterfaceMethodParameter(inInterfaceMethodParameter);
			model.save();
			return inInterfaceMethodParameter;
		}
		return null;
	}

	@Override
	public ApplicationInterface addAppInterface(ApplicationInterface inAppInterface) {
		if ((inAppInterface != null) && inAppInterface.isValid()) {
			final SystemModel model = SystemModel.createEmpty();
			model.addApplicationInterface(inAppInterface);
			model.save();
			return inAppInterface;
		}
		return null;
	}

	@Override
	public ApplicationConfig addApp(ApplicationConfig inApp) {
		if ((inApp != null) && inApp.isValid()) {
			final SystemModel model = SystemModel.createEmpty();
			model.addApplicationConfig(inApp);
			model.save();
			return inApp;
		}
		return null;
	}

	@Override
	public List<ApplicationConfig> getSystemApplications(String inSystemName) {
		final SystemConfig systemX = getSystemByName(inSystemName);
		if (systemX != null) {
			return systemAccess.referencesApplicationConfigBySystemConfig(systemX.getPrimaryKey());
		}
		return new ArrayList<ApplicationConfig>();
	}

	@Override
	public ApplicationConfig getApplication(String inSystemName, String inApplicationName) {
		final SystemConfig systemX = getSystemByName(inSystemName);
		if (systemX != null) {
			final List<ApplicationConfig> applications = systemAccess
					.referencesApplicationConfigBySystemConfig(systemX.getPrimaryKey());
			for (ApplicationConfig applicationConfig : applications) {
				if (inApplicationName.equalsIgnoreCase(applicationConfig.getApplicationName())) {
					return applicationConfig;
				}
			}
		}
		return null;
	}

	private SystemConfig getSystemByName(String inSystemName) {
		final List<SystemConfig> configs = systemAccess.listSystemConfig();
		for (SystemConfig systemConfig : configs) {
			if (inSystemName.equalsIgnoreCase(systemConfig.getSystemName())) {
				return systemConfig;
			}
		}
		return null;
	}

	@Override
	public ConfigurationItem findRootItem(ApplicationConfig inApp) {
		ConfigurationItem root = null;
		if (inApp != null) {
			final List<ConfigurationItem> items = systemAccess
					.referencesConfigurationItemByApplicationConfig(inApp.getPrimaryKey());
			if (items.size() > 0) {
				final AtomicInteger recursionCounter = new AtomicInteger(0);
				ConfigurationItem item = items.get(0);
				while (item != null) {
					root = item;
					recursionCounter.incrementAndGet();
					if (recursionCounter.get() >= MAX_DEPTH) {
						break;
					}
					final List<ConfigurationItemLink> inputLinks = systemAccess
							.referencesConfigurationItemLinkByDestination(item.getPrimaryKey());
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

	@Override
	public Map<String, String> getAppConfiguration(ApplicationConfig inApp) {
		final ConfigurationItem rootItem = findRootItem(inApp);
		return getConfigurations(rootItem);
	}

	@Override
	public Map<String, String> getConfigurations(ConfigurationItem inConfigurationItem) {
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
			final List<ConfigurationItemLink> parentLinks = systemAccess
					.referencesConfigurationItemLinkByDestination(inConfigurationItem.getPrimaryKey());
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

	private void addItem(final Map<String, String> inConfig, final ConfigurationItem inConfigurationItem,
			final String inPathKey) {
		if (inConfigurationItem != null) {
			final String newPathKey;
			if (inPathKey == null) {
				newPathKey = inConfigurationItem.getItemKey();
			} else {
				newPathKey = String.format("%s.%s", inPathKey, inConfigurationItem.getItemKey());
			}
			inConfig.put(newPathKey, inConfigurationItem.getItemValue());
			final List<ConfigurationItemLink> childLinks = systemAccess
					.referencesConfigurationItemLinkBySource(inConfigurationItem.getPrimaryKey());
			for (ConfigurationItemLink configurationItemLink : childLinks) {
				final ConfigurationItem child = configurationItemLink.getDestinationRef();
				if (child != null) {
					addItem(inConfig, child, newPathKey);
				}
			}
		}
	}

	@Override
	public List<ConfigurationItem> getConfigurationItems(ApplicationConfig inApplicationConfig) {
		if (inApplicationConfig != null) {
			return systemAccess.referencesConfigurationItemByApplicationConfig(inApplicationConfig.getPrimaryKey());
		}
		return new ArrayList<ConfigurationItem>();
	}

	@Override
	public List<ApplicationConfig> getApps(String inApplicationType) {
		// TODO Auto-generated method stub
		return new ArrayList<ApplicationConfig>();
	}

	@Override
	public List<ApplicationInterface> getAppInterfaces(ApplicationConfig inApplicationConfig) {
		if (inApplicationConfig != null) {
			return systemAccess.referencesApplicationInterfaceByApplicationConfig(inApplicationConfig.getPrimaryKey());
		}
		return new ArrayList<ApplicationInterface>();
	}

	@Override
	public ApplicationInterface addAppInterface(ApplicationConfig inApplicationConfig, String inInterfaceName) {
		if (inApplicationConfig != null) {
			final ApplicationConfig app = systemAccess.findByIDApplicationConfig(inApplicationConfig.getPrimaryKey());
			if (app != null) {
				final SystemModel model = SystemModel.createEmpty();
				final ApplicationInterface appInterface = ApplicationInterface.generate();
				appInterface.setApplicationConfig(app.getPrimaryKey());
				appInterface.setInterfaceName(inInterfaceName);
				model.addApplicationInterface(appInterface);
				model.save();
				return appInterface;
			}
		}
		return null;
	}

	@Override
	public InterfaceMethod addInterfaceMethod(ApplicationInterface inApplicationInterface, String inMethodName,
			String inMethodDescription, boolean inAsynchronously, InterfaceMethodParameter[] inParameters,
			InterfaceDataType inReturnType) {
		InterfaceMethod interfaceMethod = null;
		if (inApplicationInterface != null) {
			final ApplicationInterface appInterface = systemAccess
					.findByIDApplicationInterface(inApplicationInterface.getPrimaryKey());
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

	@Override
	public InterfaceMethod addInterfaceMethod(ApplicationInterface inApplicationInterface, String inMethodName,
			String inMethodDescription, boolean inAsynchronously, InterfaceMethodParameter[] inParameters) {
		return addInterfaceMethod(inApplicationInterface, inMethodName, inMethodDescription, inAsynchronously,
				inParameters, null);
	}

	@Override
	public InterfaceMethod addInterfaceMethod(ApplicationInterface inApplicationInterface, String inMethodName,
			String inMethodDescription) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InterfaceMethod addInterfaceMethod(ApplicationInterface inApplicationInterface, String inMethodName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InterfaceMethod addInterfaceMethod(ApplicationInterface inApplicationInterface, String inMethodName,
			InterfaceDataType inReturnType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<InterfaceMethod> getAppInterface(ApplicationInterface inApplicationInterface) {
		if (inApplicationInterface != null) {
			return systemAccess.referencesInterfaceMethodByApplicationInterface(inApplicationInterface.getPrimaryKey());
		}
		return new ArrayList<InterfaceMethod>();
	}

	@Override
	public SystemModel getAppConfigurationModel(ApplicationConfig inApp) {
		final SystemModel systemModel = SystemModel.createEmpty();
		if (inApp != null) {
			final ApplicationConfig app = systemAccess.findByIDApplicationConfig(inApp.getPrimaryKey());
			if (app != null) {
				systemModel.addApplicationConfig(app);
				final List<ConfigurationItem> configurationItems = systemAccess
						.referencesConfigurationItemByApplicationConfig(app.getPrimaryKey());
				systemModel.addAllConfigurationItem(configurationItems);
				for (ConfigurationItem configurationItem : configurationItems) {
					final List<ConfigurationItemLink> configurationLinksItemIn = systemAccess
							.referencesConfigurationItemLinkBySource(configurationItem.getPrimaryKey());
					systemModel.addAllConfigurationItemLink(configurationLinksItemIn);
					final List<ConfigurationItemLink> configurationLinksItemOut = systemAccess
							.referencesConfigurationItemLinkByDestination(configurationItem.getPrimaryKey());
					systemModel.addAllConfigurationItemLink(configurationLinksItemOut);
				}
			}
		}
		return systemModel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getApps(de.boetzmeyer.
	 * systemmodel.Infrastructure)
	 */
	@Override
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

	private List<SystemConfig> getSystems(Infrastructure inInfrastructure) {
		if (inInfrastructure != null) {
			return systemAccess.referencesSystemConfigByInfrastructure(inInfrastructure.getPrimaryKey());
		}
		return new ArrayList<SystemConfig>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getApps(de.boetzmeyer.
	 * systemmodel.ApplicationType)
	 */
	@Override
	public List<ApplicationConfig> getApps(final ApplicationType inApplicationType) {
		if (inApplicationType != null) {
			return systemAccess.referencesApplicationConfigByApplicationType(inApplicationType.getPrimaryKey());
		}
		return new ArrayList<ApplicationConfig>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getApps()
	 */
	@Override
	public List<ApplicationConfig> getApps() {
		return systemAccess.listApplicationConfig();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getApps(de.boetzmeyer.
	 * systemmodel.SystemConfig)
	 */
	@Override
	public List<ApplicationConfig> getApps(final SystemConfig inSystem) {
		if (inSystem != null) {
			return systemAccess.referencesApplicationConfigBySystemConfig(inSystem.getPrimaryKey());
		}
		return new ArrayList<ApplicationConfig>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.boetzmeyer.systemcontext.ISystemContext#getApps(de.boetzmeyer.
	 * systemmodel.Computer)
	 */
	@Override
	public List<ApplicationConfig> getApps(final Computer inComputer) {
		final List<ApplicationConfig> apps = new ArrayList<ApplicationConfig>();
		if (inComputer != null) {
			final List<ApplicationInstallation> installations = systemAccess
					.referencesApplicationInstallationByComputer(inComputer.getPrimaryKey());
			for (ApplicationInstallation installation : installations) {
				final ApplicationConfig app = installation.getApplicationConfigRef();
				if (app != null) {
					apps.add(app);
				}
			}
		}
		return apps;
	}

	@Override
	public ApplicationConfig findAppByName(String inApplicationName) {
		final List<ApplicationConfig> apps = systemAccess.listApplicationConfig();
		for (ApplicationConfig app : apps) {
			if (inApplicationName.equalsIgnoreCase(app.getApplicationName())) {
				return app;
			}
		}
		return null;
	}

	@Override
	public SystemModel getInterfaceModel(ApplicationInterface inInterface) {
		final SystemModel systemModel = SystemModel.createEmpty();
		if (inInterface != null) {
			final ApplicationInterface appInterface = systemAccess
					.findByIDApplicationInterface(inInterface.getPrimaryKey());
			if (appInterface != null) {
				final SystemModel interfaceBasic = systemAccess
						.exportApplicationInterface(appInterface.getPrimaryKey());
				systemModel.add(interfaceBasic);
				final List<InterfaceMethod> methods = systemAccess
						.referencesInterfaceMethodByApplicationInterface(appInterface.getPrimaryKey());
				final SystemModel methodModel = systemAccess.contextExportInterfaceMethod(methods);
				systemModel.add(methodModel);
			}
		}
		return systemModel;
	}

	@Override
	public List<ApplicationType> getApplicationTypes() {
		return systemAccess.listApplicationType();
	}

	@Override
	public List<ApplicationConfig> getApplications() {
		return systemAccess.listApplicationConfig();
	}

	@Override
	public SystemModel getAppDependencies(ApplicationConfig inApp) {
		final SystemModel dependencyGraph = SystemModel.createEmpty();
		if (inApp != null) {
			final ApplicationConfig rootApp = systemAccess.findByIDApplicationConfig(inApp.getPrimaryKey());
			addToGraph(rootApp, dependencyGraph);
		}
		return dependencyGraph;
	}

	private void addToGraph(final ApplicationConfig inApp, final SystemModel dependencyGraph) {
		if (inApp != null) {
			dependencyGraph.addApplicationConfig(inApp);
			final List<ApplicationLink> requiredAppLinks = systemAccess.referencesApplicationLinkBySource(inApp.getPrimaryKey());
			for (ApplicationLink applicationLink : requiredAppLinks) {
				dependencyGraph.addApplicationLink(applicationLink);
				addToGraph(applicationLink.getDestinationRef(), dependencyGraph);
			}
		}
	}

	@Override
	public void configureApp(final ApplicationConfig inApp, Map<String, String> inItems) {
		if (inApp != null) {
			final ApplicationConfig foundApp = systemAccess.findByIDApplicationConfig(inApp.getPrimaryKey());
			if (foundApp != null) {
				final SystemModel model = SystemModel.createEmpty();
				for (Entry<String, String> entry : inItems.entrySet()) {
					if (entry != null) {
						final ConfigurationItem item = ConfigurationItem.generate();
						item.setApplicationConfig(foundApp.getPrimaryKey());
						item.setItemKey(entry.getKey());
						item.setItemValue(entry.getValue());
						model.addConfigurationItem(item);
					}
				}
				if (model.sizeConfigurationItem() == inItems.size()) {
					model.save();
				}
			}
		}
	}

	@Override
	public String getConfigurationValue(ApplicationConfig inApp, String inKey) {
		if ((inApp != null) && (inKey != null)) {
			final List<ConfigurationItem> appConfigItems = systemAccess.referencesConfigurationItemByApplicationConfig(inApp.getPrimaryKey());
			for (ConfigurationItem configurationItem : appConfigItems) {
				if (configurationItem != null) {
					if (inKey.equals(configurationItem.getItemKey())) {
						return configurationItem.getItemValue();
					}
				}
			}
		}
		return null;
	}

}
