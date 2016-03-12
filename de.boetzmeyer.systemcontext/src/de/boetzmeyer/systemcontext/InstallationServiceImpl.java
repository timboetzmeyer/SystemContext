package de.boetzmeyer.systemcontext;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.boetzmeyer.systemmodel.ApplicationConfig;
import de.boetzmeyer.systemmodel.ApplicationInstallation;
import de.boetzmeyer.systemmodel.ApplicationInterface;
import de.boetzmeyer.systemmodel.ApplicationSession;
import de.boetzmeyer.systemmodel.Computer;
import de.boetzmeyer.systemmodel.DataModel;
import de.boetzmeyer.systemmodel.DatabaseInstallation;
import de.boetzmeyer.systemmodel.DatabaseSession;
import de.boetzmeyer.systemmodel.IServer;
import de.boetzmeyer.systemmodel.InterfaceMethod;
import de.boetzmeyer.systemmodel.Network;
import de.boetzmeyer.systemmodel.PropertyState;
import de.boetzmeyer.systemmodel.SessionState;

final class InstallationServiceImpl extends SystemContextService implements InstallationService {

	public InstallationServiceImpl(final IServer inSystemAccess) {
		super(inSystemAccess);
	}

	@Override
	public List<ApplicationInstallation> getInstallations(Computer inComputer) {
		if (inComputer != null) {
			return systemAccess.referencesApplicationInstallationByComputer(inComputer.getPrimaryKey());
		}
		return new ArrayList<ApplicationInstallation>();
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
	
	private List<Computer> getComputers() {
		return systemAccess.listComputer();
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

	@Override
	public ApplicationInstallation installApp(String inComputerName, String inApplicationName) {
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
	
	private ApplicationConfig findAppByName(final String inApplicationName) {
		final List<ApplicationConfig> apps = systemAccess.listApplicationConfig();
		for (ApplicationConfig app : apps) {
			if (inApplicationName.equalsIgnoreCase(app.getApplicationName())) {
				return app;
			}
		}
		return null;
	}

	@Override
	public ApplicationInstallation installApp(Computer inComputer, ApplicationConfig inApplication) {
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

	private Computer findComputer(Computer inComputer) {
		if (inComputer != null) {
			return systemAccess.findByIDComputer(inComputer.getPrimaryKey());
		}
		return null;
	}

	@Override
	public void uninstallApp(ApplicationInstallation inApplicationInstallation) {
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

	@Override
	public void uninstallDatabase(DatabaseInstallation inDatabaseInstallation) {
		if (inDatabaseInstallation != null) {
			final List<DatabaseSession> dbSessions = systemAccess.referencesDatabaseSessionByDatabaseInstallation(inDatabaseInstallation.getPrimaryKey());
			for (DatabaseSession dbSession : dbSessions) {
				systemAccess.deleteDatabaseSession(dbSession.getPrimaryKey());
			}
			systemAccess.deleteDatabaseInstallation(inDatabaseInstallation.getPrimaryKey());
		}
	}

	@Override
	public DatabaseInstallation installDatabase(DataModel inDataModel, Computer inComputer) {
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

	private DataModel findDataModel(final DataModel inDataModel) {
		if (inDataModel != null) {
			return systemAccess.findByIDDataModel(inDataModel.getPrimaryKey());
		}
		return null;
	}

	@Override
	public DatabaseInstallation findDatabaseInstallation(DataModel inDataModel, Computer inComputer) {
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

	@Override
	public List<ApplicationInstallation> getAppInstallations(ApplicationConfig inApplicationConfig) {
		return systemAccess.referencesApplicationInstallationByApplicationConfig(inApplicationConfig.getPrimaryKey());
	}

	@Override
	public List<ApplicationInstallation> getInstallationsSince(Computer inComputer, Date inSince) {
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

	@Override
	public List<ApplicationInstallation> getAppInstallations(InterfaceMethod inMethod) {
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

	@Override
	public List<ApplicationInstallation> getAppInstallationsAvailable(InterfaceMethod inMethod) {
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

	@Override
	public List<DatabaseInstallation> getInstallations(DataModel inDataModel) {
		if (inDataModel != null) {
			return systemAccess.referencesDatabaseInstallationByDataModel(inDataModel.getPrimaryKey());
		}
		return new ArrayList<DatabaseInstallation>();
	}

	@Override
	public Computer installComputer(String inComputerName, String inIPAddress, String inRemarks, Network inNetwork) {
		final Computer computer = Computer.generate();
		computer.setComputerName(inComputerName);
		computer.setIPAddress(inIPAddress);
		computer.setRemarks(inRemarks);
		if (inNetwork != null) {
			computer.setNetwork(inNetwork.getPrimaryKey());
		}
		computer.save();
		return computer;
	}

	@Override
	public void uninstallComputer(Computer inComputer) {
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
