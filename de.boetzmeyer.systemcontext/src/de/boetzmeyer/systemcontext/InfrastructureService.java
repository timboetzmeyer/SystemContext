package de.boetzmeyer.systemcontext;

import java.util.List;

import de.boetzmeyer.systemmodel.ApplicationConfig;
import de.boetzmeyer.systemmodel.Computer;
import de.boetzmeyer.systemmodel.DataModel;
import de.boetzmeyer.systemmodel.Infrastructure;
import de.boetzmeyer.systemmodel.Network;
import de.boetzmeyer.systemmodel.SystemConfig;
import de.boetzmeyer.systemmodel.SystemLink;
import de.boetzmeyer.systemmodel.SystemModel;
import de.boetzmeyer.systemmodel.SystemType;

public interface InfrastructureService {

	Infrastructure addInfrastructure(Infrastructure inInfrastructure);

	SystemLink connectSystems(SystemConfig inSourceSystem, SystemConfig inTargetSystem);

	boolean disonnectSystems(SystemConfig inSourceSystem, SystemConfig inTargetSystem);

	SystemConfig addSystem(SystemConfig inSystem);

	Computer addComputer(Computer inComputer);

	Network addNetwork(Network inNetwork);

	SystemConfig getSystemByName(String inSystemName);

	List<Network> getNetworks();

	List<Computer> getComputers();

	List<Infrastructure> getInfrastructures();

	SystemModel getSystemInfrastructure(String inInfrastructureName);

	Network findNetwork(String inNetworkName);

	List<Computer> getComputers(String inNetworkName);

	List<Computer> getComputers(Network inNetwork);

	List<Computer> getComputers(ApplicationConfig inApp);

	List<Computer> getComputers(DataModel inDataModel);

	SystemModel getComputerModel(Computer inComputer);

	SystemModel getSystemInfrastructureModel(Infrastructure inInfrastructure);

	SystemModel getSystemModel(SystemConfig inSystem);

	SystemModel getAppSystem(String inSystemName);

	SystemModel getSystemInfrastructure(Infrastructure inInfrastructure);

	SystemModel getAppSystem(SystemConfig inSystem);

	List<SystemConfig> getSystems();

	List<SystemType> getSystemTypes();

	List<SystemConfig> getSystems(Infrastructure inInfrastructure);

	Computer findComputer(Computer inComputer);

	SystemType addSystemType(SystemType systemType);

}
