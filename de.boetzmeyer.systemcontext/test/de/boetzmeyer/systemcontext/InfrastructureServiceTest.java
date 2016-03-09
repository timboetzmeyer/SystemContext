package de.boetzmeyer.systemcontext;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.Test;

import de.boetzmeyer.systemmodel.Infrastructure;

public class InfrastructureServiceTest {
	private static final File DIR = new File(System.getProperty("user.dir") + Character.toString(File.separatorChar) + "JUnitSystemContext");
	
	@Test
	public void testInfrastructureService() {
		try {
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
			
			final InfrastructureService infrastructureService = SystemContext.connect(DIR.getAbsolutePath());
			
			assertEquals(0, infrastructureService.getInfrastructures().size());
			final Infrastructure infrastructure = Infrastructure.generate();
			infrastructure.setInfrastructureName("I1");
			infrastructure.setDescription("I1-Desc");
			infrastructureService.addInfrastructure(infrastructure);
			assertEquals(1, infrastructureService.getInfrastructures().size());

		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

}
