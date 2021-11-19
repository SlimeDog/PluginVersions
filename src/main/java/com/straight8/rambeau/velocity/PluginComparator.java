package com.straight8.rambeau.velocity;

import com.velocitypowered.api.plugin.PluginContainer;

import java.util.Comparator;

public class PluginComparator implements Comparator<PluginContainer> {

	@Override
	public int compare(PluginContainer p1, PluginContainer p2) {
		if (!(p1 instanceof PluginContainer) || !(p2 instanceof PluginContainer)) {
			throw new ClassCastException();
		}
		return p1.getDescription().getName().get().compareToIgnoreCase(p2.getDescription().getName().get());
	}
}
