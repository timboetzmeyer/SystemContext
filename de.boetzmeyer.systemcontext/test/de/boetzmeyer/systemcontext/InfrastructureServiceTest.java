package de.boetzmeyer.systemcontext;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.Test;

import de.boetzmeyer.systemmodel.Computer;
import de.boetzmeyer.systemmodel.Infrastructure;
import de.boetzmeyer.systemmodel.Network;

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
			
			// computer not yet known on the server-side
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
