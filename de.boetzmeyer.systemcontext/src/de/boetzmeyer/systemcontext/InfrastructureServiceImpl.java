package de.boetzmeyer.systemcontext;

import java.util.ArrayList;
import java.util.List;

import de.boetzmeyer.systemmodel.ApplicationConfig;
import de.boetzmeyer.systemmodel.ApplicationInstallation;
import de.boetzmeyer.systemmodel.Computer;
import de.boetzmeyer.systemmodel.DataModel;
import de.boetzmeyer.systemmodel.DatabaseInstallation;
import de.boetzmeyer.systemmodel.IServer;
import de.boetzmeyer.systemmodel.Infrastructure;
import de.boetzmeyer.systemmodel.Network;
import de.boetzmeyer.systemmodel.SystemConfig;
import de.boetzmeyer.systemmodel.SystemLink;
import de.boetzmeyer.systemmodel.SystemModel;

final class InfrastructureServiceImpl extends SystemContextService implements InfrastructureService {

	public InfrastructureServiceImpl(final IServer inSystemAccess) {
		super(inSystemAccess);
	}

	@Override
	public Infrastructure addInfrastructure(Infrastructure inInfrastructure) {
		if ((inInfrastructure != null) && inInfrastructure.isValid()) {
			final SystemModel model = SystemModel.createEmpty();
			model.addInfrastructure(inInfrastructure);
			model.save();
			return inInfrastructure;
		}
		return null;
	}

	@Override
	public SystemLink connectSystems(SystemConfig inSourceSystem, SystemConfig inTargetSystem) {
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

	@Override
	public boolean disonnectSystems(SystemConfig inSourceSystem, SystemConfig inTargetSystem) {
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

	@Override
	public SystemConfig addSystem(SystemConfig inSystem) {
		if ((inSystem != null) && inSystem.isValid()) {
			final SystemModel model = SystemModel.createEmpty();
			model.addSystemConfig(inSystem);
			model.save();
			return inSystem;
		}
		return null;
	}

	@Override
	public Computer addComputer(Computer inComputer) {
		if ((inComputer != null) && inComputer.isValid()) {
			final SystemModel model = SystemModel.createEmpty();
			model.addComputer(inComputer);
			model.save();
			return inComputer;
		}
		return null;
	}

	@Override
	public Network addNetwork(Network inNetwork) {
		if ((inNetwork != null) && inNetwork.isValid()) {
			final SystemModel model = SystemModel.createEmpty();
			model.addNetwork(inNetwork);
			model.save();
			return inNetwork;
		}
		return null;
	}

	@Override
	public SystemConfig getSystemByName(String inSystemName) {
		final List<SystemConfig> configs = systemAccess.listSystemConfig();
		for (SystemConfig systemConfig : configs) {
			if (inSystemName.equalsIgnoreCase(systemConfig.getSystemName())) {
				return systemConfig;
			}
		}
		return null;
	}

	@Override
	public List<Network> getNetworks() {
		return systemAccess.listNetwork();
	}

	@Override
	public List<Computer> getComputers() {
		return systemAccess.listComputer();
	}

	@Override
	public List<Infrastructure> getInfrastructures() {
		return systemAccess.listInfrastructure();
	}

	@Override
	public SystemModel getSystemInfrastructure(String inInfrastructureName) {
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

	@Override
	public Network findNetwork(String inNetworkName) {
		final List<Network> networks = getNetworks();
		for (Network network : networks) {
			if (inNetworkName.equalsIgnoreCase(network.getNetworkName())) {
				return network;
			}
		}
		return null;
	}

	@Override
	public List<Computer> getComputers(String inNetworkName) {
		final Network network = findNetwork(inNetworkName);
		return getComputers(network);
	}

	@Override
	public List<Computer> getComputers(Network inNetwork) {
		if (inNetwork != null) {
			return systemAccess.referencesComputerByNetwork(inNetwork.getPrimaryKey());
		}
		return new ArrayList<Computer>();
	}

	@Override
	public List<Computer> getComputers(ApplicationConfig inApp) {
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
	
	private List<ApplicationInstallation> getAppInstallations(ApplicationConfig inApplicationConfig) {
		return systemAccess.referencesApplicationInstallationByApplicationConfig(inApplicationConfig.getPrimaryKey());
	}

	@Override
	public List<Computer> getComputers(DataModel inDataModel) {
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
	
	private List<DatabaseInstallation> getInstallations(DataModel inDataModel) {
		if (inDataModel != null) {
			return systemAccess.referencesDatabaseInstallationByDataModel(inDataModel.getPrimaryKey());
		}
		return new ArrayList<DatabaseInstallation>();
	}

	@Override
	public SystemModel getComputerModel(Computer inComputer) {
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

	@Override
	public SystemModel getSystemInfrastructureModel(Infrastructure inInfrastructure) {
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

	@Override
	public SystemModel getSystemModel(SystemConfig inSystem) {
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

	@Override
	public SystemModel getAppSystem(String inSystemName) {
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

	@Override
	public SystemModel getSystemInfrastructure(Infrastructure inInfrastructure) {
		if (inInfrastructure != null) {
			final List<SystemConfig> infrastructureSystems = getSystems(inInfrastructure);
			return systemAccess.contextExportSystemConfig(infrastructureSystems);
		}
		return SystemModel.createEmpty();
	}
	
	private List<ApplicationConfig> getApps(final SystemConfig inSystem) {
		if (inSystem != null) {
			return systemAccess.referencesApplicationConfigBySystemConfig(inSystem.getPrimaryKey());
		}
		return new ArrayList<ApplicationConfig>();
	}

	@Override
	public SystemModel getAppSystem(SystemConfig inSystem) {
		if (inSystem != null) {
			final List<ApplicationConfig> systemApps = getApps(inSystem);
			return systemAccess.contextExportApplicationConfig(systemApps);
		}
		return SystemModel.createEmpty();
	}

	@Override
	public List<SystemConfig> getSystems() {
		return systemAccess.listSystemConfig();
	}

	@Override
	public List<SystemConfig> getSystems(Infrastructure inInfrastructure) {
		if (inInfrastructure != null) {
			return systemAccess.referencesSystemConfigByInfrastructure(inInfrastructure.getPrimaryKey());
		}
		return new ArrayList<SystemConfig>();
	}

	@Override
	public Computer findComputer(Computer inComputer) {
		if (inComputer != null) {
			return systemAccess.findByIDComputer(inComputer.getPrimaryKey());
		}
		return null;
	}
}
