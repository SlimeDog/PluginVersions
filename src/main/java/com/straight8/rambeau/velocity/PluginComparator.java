package com.straight8.rambeau.velocity;

import com.velocitypowered.api.plugin.PluginContainer;

import java.util.Comparator;

public class PluginComparator implements Comparator<PluginContainer> {

	@Override
	public int compare(PluginContainer p1, PluginContainer p2) {
		if (!(p1 instanceof PluginContainer) || !(p2 instanceof PluginContainer)) {
			throw new ClassCastException();
		}

		if(p1 != null && p1.getDescription().getName().isPresent() && p2 != null && p2.getDescription().getName().isPresent()) {
			String name1 = p1.getDescription().getName().get();
			String name2 = p2.getDescription().getName().get();
			if(name1 != null && name2 != null) {
				return name1.compareToIgnoreCase(name2);
			}
		}
		throw new ClassCastException();
	}
}
