package de.boetzmeyer.systemcontext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.Test;

import de.boetzmeyer.systemmodel.Computer;
import de.boetzmeyer.systemmodel.Infrastructure;
import de.boetzmeyer.systemmodel.Network;
import de.boetzmeyer.systemmodel.SystemConfig;
import de.boetzmeyer.systemmodel.SystemModel;
import de.boetzmeyer.systemmodel.SystemType;

public class InfrastructureServiceTest {
	private static final File DIR = new File(System.getProperty("user.dir") + Character.toString(File.separatorChar) + "JUnitSystemContext");
	
	@Test
	public void testInfrastructureService() {
		try {
			deleteExistingSystemModelFiles();
			
			final InfrastructureService infrastructureService = SystemContext.connect(DIR.getAbsolutePath());
			
			// create infrastructure locally
			final Infrastructure infrastructure = Infrastructure.generate();
			infrastructure.setInfrastructureName("I1");
			infrastructure.setDescription("I1-Desc");
			
			// infrastructure not yet known on the server-side
			assertEquals(0, infrastructureService.getInfrastructures().size());
			
			// save infrastructure on the server-side
			infrastructureService.addInfrastructure(infrastructure);
			
			// now infrastructure should be known on the server-side
			assertEquals(1, infrastructureService.getInfrastructures().size());

			// create network locally
			final Network network = Network.generate();
			network.setNetworkName("Net");
			network.setDescription("Net 1");
			
			// network not yet known on the server-side
			assertEquals(0, infrastructureService.getNetworks().size());
			
			// save network on the server-side
			infrastructureService.addNetwork(network);
			
			// now network should be known on the server-side
			assertEquals(1, infrastructureService.getNetworks().size());
			
			// create computer locally
			final Computer computer1a = Computer.generate();
			computer1a.setComputerName("Computer 1a");
			computer1a.setIPAddress("127.0.0.1");
			computer1a.setNetwork(network.getPrimaryKey());
			
			// computer not known on the server-side
			assertEquals(0, infrastructureService.getComputers().size());
			
			// save computer on the server-side
			infrastructureService.addComputer(computer1a);
			
			// now network should be known on the server-side
			assertEquals(1, infrastructureService.getComputers().size());
						
			// save the same computer on the server-side once more
			infrastructureService.addComputer(computer1a);
			
			// the server-side still only knows one computer
			assertEquals(1, infrastructureService.getComputers().size());
			
			// the network owns one computer
			assertEquals(1, infrastructureService.getComputers(network).size());
			
			// create computer locally
			final Computer computer1b = Computer.generate();
			computer1b.setComputerName("Computer 1b");
			computer1b.setIPAddress("127.0.0.2");
			computer1b.setNetwork(network.getPrimaryKey());
			
			// save computer on the server-side
			infrastructureService.addComputer(computer1b);
			
			// now the network owns two computers
			assertEquals(2, infrastructureService.getComputers(network).size());
			
			// create computer locally but do not add it to the network
			final Computer computer2 = Computer.generate();
			computer2.setComputerName("Computer 2");
			computer2.setIPAddress("127.0.0.3");
			computer2.setNetwork(0L);
			
			// save computer on the server-side
			infrastructureService.addComputer(computer2);
			
			// the server-side knows three computers
			assertEquals(3, infrastructureService.getComputers().size());
			
			// the network still owns just two of them
			assertEquals(2, infrastructureService.getComputers(network).size());
		
			// create a system type locally
			final SystemType systemType = SystemType.generate();
			systemType.setTypeName("Car");
			
			// computer not known on the server-side
			assertEquals(0, infrastructureService.getSystemTypes().size());
			
			// save the system type on the server-side
			infrastructureService.addSystemType(systemType);
			
			// computer not known on the server-side
			assertEquals(1, infrastructureService.getSystemTypes().size());
						
			// create 5 systems locally
			final SystemConfig system1 = SystemConfig.generate();
			system1.setSystemName("System 1");
			system1.setInfrastructure(infrastructure.getPrimaryKey());
			system1.setSystemType(systemType.getPrimaryKey());
			
			final SystemConfig system2 = SystemConfig.generate();
			system2.setSystemName("System 2");
			system2.setInfrastructure(infrastructure.getPrimaryKey());
			system2.setSystemType(systemType.getPrimaryKey());
			
			final SystemConfig system3 = SystemConfig.generate();
			system3.setSystemName("System 3");
			system3.setInfrastructure(infrastructure.getPrimaryKey());
			system3.setSystemType(systemType.getPrimaryKey());
			
			final SystemConfig system4 = SystemConfig.generate();
			system4.setSystemName("System 4");
			system4.setInfrastructure(0L);
			system4.setSystemType(systemType.getPrimaryKey());
			
			final SystemConfig system5 = SystemConfig.generate();
			system5.setSystemName("System 5");
			system5.setInfrastructure(0L);
			system5.setSystemType(systemType.getPrimaryKey());
			
			// systems not known on the server-side
			assertEquals(0, infrastructureService.getSystems(infrastructure).size());
			
			// save the systems on the server-side
			infrastructureService.addSystem(system1);
			infrastructureService.addSystem(system2);
			infrastructureService.addSystem(system3);
			infrastructureService.addSystem(system4);
			infrastructureService.addSystem(system5);
			
			// the server-side knows 5 systems
			assertEquals(5, infrastructureService.getSystems().size());
			
			// the infrastructure owns just three of them
			assertEquals(3, infrastructureService.getSystems(infrastructure).size());
						
			// connect systems on the server-side
			infrastructureService.connectSystems(system1, system2);
			infrastructureService.connectSystems(system1, system3);
			infrastructureService.connectSystems(system2, system3);
			infrastructureService.connectSystems(system4, system5);
			
			// get infrastructure related artifacts
			final SystemModel model = infrastructureService.getSystemInfrastructure(infrastructure);
			assertEquals(1, model.listInfrastructure().size());   // "I1" 
			assertEquals(1, model.listSystemType().size());       // "Car"
			assertEquals(3, model.listSystemConfig().size());     // "System 1", "System 2", "System 3" 
			assertEquals(3, model.listSystemLink().size());       // "1 -> 2", "1 -> 3", "2 -> 3"
			
			deleteExistingSystemModelFiles();
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	private void deleteExistingSystemModelFiles() throws IOException {
		if (DIR.exists()) {
			final File[] files = DIR.listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					if ((files[i] != null) && (files[i].isFile())) {
						files[i].delete();
					}
				}
			}
			Files.delete(DIR.toPath());
		}
		DIR.mkdirs();
	}

}
