package net.nerdypuzzle.structureviewer;

import net.mcreator.plugin.events.workspace.MCreatorLoadedEvent;
import net.mcreator.plugin.JavaPlugin;
import net.mcreator.plugin.Plugin;
import net.nerdypuzzle.structureviewer.registry.PluginActions;
import net.nerdypuzzle.structureviewer.registry.PluginEventTriggers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Launcher extends JavaPlugin {

	public static final Logger LOG = LogManager.getLogger("GeckoLib Plugin");
	public static PluginActions ACTION_REGISTRY;

	public Launcher(Plugin plugin) {
		super(plugin);
		addListener(MCreatorLoadedEvent.class, event -> {
			ACTION_REGISTRY = new PluginActions(event.getMCreator());
			//SwingUtilities.invokeLater(() -> PluginEventTriggers.modifyMenus(event.getMCreator()));
			PluginEventTriggers.modifyMenus(event.getMCreator());
		});

		LOG.info("Plugin was loaded");
	}

}