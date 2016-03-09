package de.boetzmeyer.systemcontext;

import java.util.Date;
import java.util.List;

import de.boetzmeyer.systemmodel.ApplicationConfig;
import de.boetzmeyer.systemmodel.ApplicationInstallation;
import de.boetzmeyer.systemmodel.Computer;
import de.boetzmeyer.systemmodel.DataModel;
import de.boetzmeyer.systemmodel.DatabaseInstallation;
import de.boetzmeyer.systemmodel.InterfaceMethod;
import de.boetzmeyer.systemmodel.Network;

public interface InstallationService {

	void installComputer(String inComputerName, String inIPAddress, String inRemarks, Network inNetwork);

	void uninstallComputer(Computer inComputer);

	List<ApplicationInstallation> getInstallations(Computer inComputer);

	ApplicationInstallation installApp(String inComputerName, String inApplicationName);

	ApplicationInstallation installApp(Computer inComputer, ApplicationConfig inApplication);

	void uninstallApp(ApplicationInstallation inApplicationInstallation);

	void uninstallDatabase(DatabaseInstallation inDatabaseInstallation);

	DatabaseInstallation installDatabase(DataModel inDataModel, Computer inComputer);

	DatabaseInstallation findDatabaseInstallation(DataModel inDataModel, Computer inComputer);

	List<ApplicationInstallation> getAppInstallations(ApplicationConfig inApplicationConfig);

	List<ApplicationInstallation> getInstallationsSince(Computer inComputer, Date inSince);

	List<ApplicationInstallation> getAppInstallations(InterfaceMethod inMethod);

	List<ApplicationInstallation> getAppInstallationsAvailable(InterfaceMethod inMethod);

	List<DatabaseInstallation> getInstallations(DataModel inDataModel);

}
