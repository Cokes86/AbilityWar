package daybreak.abilitywar.config.wizard;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

import daybreak.abilitywar.config.AbilityWarSettings;

public abstract class SettingWizard {

	static final Logger logger = Logger.getLogger(SettingWizard.class.getName());

	private Inventory gui = null;
	private final int inventorySize;
	private final String inventoryName;
	final Player player;

	SettingWizard(Player player, int inventorySize, String inventoryName, Plugin plugin) {
		this.inventorySize = inventorySize;
		this.inventoryName = inventoryName;
		this.player = player;
		new Listener() {
			{
				Bukkit.getPluginManager().registerEvents(this, plugin);
			}

			@EventHandler
			private void onInventoryClose(InventoryCloseEvent e) {
				if (e.getInventory().equals(gui)) {
					HandlerList.unregisterAll(this);
					try {
						AbilityWarSettings.update();
					} catch (IOException | InvalidConfigurationException e1) {
						logger.log(Level.SEVERE, "콘피그를 업데이트하는 도중 오류가 발생하였습니다.");
					}
				}
			}

			@EventHandler
			private void onInventoryClick(InventoryClickEvent e) {
				if (e.getInventory().equals(gui)) {
					onClick(e, gui);
				}
			}

		};
	}

	public void Show() {
		this.gui = Bukkit.createInventory(null, inventorySize, inventoryName);
		openGUI(gui);
	}

	abstract void openGUI(Inventory gui);

	abstract void onClick(InventoryClickEvent e, Inventory gui);

}
