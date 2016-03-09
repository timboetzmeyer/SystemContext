package de.boetzmeyer.systemcontext;

import java.util.ArrayList;
import java.util.List;

import de.boetzmeyer.systemmodel.SystemConfig;
import de.boetzmeyer.systemmodel.SystemLink;

final class Analyser {

	public Analyser() {
	}

	public final List<SystemLink> getStrongestSystemLinks(final List<SystemConfig> inSystem, final int inMaxConnections) {
		final List<SystemLink> links = new ArrayList<SystemLink>();
		final List<SystemLink> strongestLinks = new ArrayList<SystemLink>();
		final int systemCount = inSystem.size();
		for (int i = 0; i < systemCount; i++) {
			for (int j = 0; j < systemCount; j++) {
				final SystemLink link = SystemLink.generate();
				final SystemConfig from = inSystem.get(i);
				final SystemConfig to = inSystem.get(j);
				if ((from != null) && (to != null)) {
					link.setSource(from.getPrimaryKey());
					link.setDestination(to.getPrimaryKey());
					final double weight = analyseConnection(link);
					links.add(link);
				}
			}
		}
		//SystemLink.sortByWeight(links, true);
		for (int i = 0; i < inMaxConnections; i++) {
			strongestLinks.add(links.get(i));
		}
		return strongestLinks;
	}

	private double analyseConnection(SystemLink link) {
		return 0;
	}
}
