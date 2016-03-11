package de.boetzmeyer.systemcontext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({InfrastructureServiceTest.class, ApplicationServiceTest.class, SessionServiceTest.class})
public class AllTests {
	protected static final File DIR = new File(System.getProperty("user.dir") + Character.toString(File.separatorChar) + "JUnitSystemContext");
	
	protected static void deleteExistingSystemModelFiles() throws IOException {
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
