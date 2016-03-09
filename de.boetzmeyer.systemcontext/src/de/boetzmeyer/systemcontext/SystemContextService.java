package de.boetzmeyer.systemcontext;

import de.boetzmeyer.systemmodel.IServer;

abstract class SystemContextService {
	protected final IServer systemAccess;
	
	public SystemContextService(final IServer inSystemAccess) {
		systemAccess = inSystemAccess;
	}

}
