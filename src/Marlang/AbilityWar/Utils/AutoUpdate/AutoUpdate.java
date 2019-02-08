package Marlang.AbilityWar.Utils.AutoUpdate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.Event;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import Marlang.AbilityWar.AbilityWar;
import Marlang.AbilityWar.Utils.Messager;

/**
 * Github Auto Update
 * @author _Marlang 말랑
 */
public class AutoUpdate {
	
	private final String Author;
	private final String Repository;
	private final Plugin Plugin;
	private final Branch PluginBranch;
	private final Branch ServerBranch;
	
	public AutoUpdate(String Author, String Repository, Plugin Plugin, Branch PluginBranch) {
		this.Author = Author;
		this.Repository = Repository;
		this.Plugin = Plugin;
		this.PluginBranch = PluginBranch;
		this.ServerBranch = Branch.getBranchByVersion(ServerVersion.getVersion());
	}

	/**
	 * 업데이트 확인
	 * @return 최신버전 여부 (업데이트를 확인할 수 없을 경우에도 True 반환)
	 */
	public boolean Check() {
		try {
			if(ServerBranch != null) {
				if(PluginBranch.equals(ServerBranch)) { //동일 버전일 경우
					UpdateObject Update = getLatestUpdate(PluginBranch.getName());
					if (!isPluginLatest(Update)) {
						Messager.sendMessage(Messager.formatUpdate(Update));
						
						unload(Plugin);
						
						Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f업데이트를 시작합니다."));
						
						Download(Update);
						
						Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f업데이트를 완료하였습니다."));
						load(Plugin);
					} else {
						Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f플러그인이 최신 버전입니다."));
						return true;
					}
				} else { //버전이 다를 경우
					Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f플러그인 버전과 서버 버전이 다릅니다. &b" + PluginBranch.getVersion() + " &7<=> &b" + ServerBranch.getVersion()));

					UpdateObject Update = getLatestUpdate(ServerBranch.getName());

					unload(Plugin);
					
					Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f플러그인 호환 및 업데이트 작업을 시작합니다."));
					
					Download(Update);
					
					Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f호환 및 업데이트 작업을 완료하였습니다."));
					load(Plugin);
				}
			} else {
				UpdateObject Update = getLatestUpdate(PluginBranch.getName());
				if (!isPluginLatest(Update)) {
					Messager.sendMessage(Messager.formatUpdate(Update));

					unload(Plugin);
					
					Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f업데이트를 시작합니다."));
					
					Download(Update);
					
					Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f업데이트를 완료하였습니다."));
					load(Plugin);
				} else {
					Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f플러그인이 지원하지 않는 버전을 이용하고 있습니다."));
					unload(Plugin);
				}
			}
		} catch (Exception ex) {
			Messager.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f플러그인 최신 업데이트를 확인할 수 없습니다."));
			return true;
		}
		
		return false;
	}
	
	private boolean isPluginLatest(UpdateObject Update) throws Exception {
		return Plugin.getDescription().getVersion().equalsIgnoreCase(Update.getVersion());
	}
	
	private void Download(UpdateObject Update) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) Update.getDownloadURL().openConnection();
		connection.setRequestMethod("GET");
		
		InputStream input = connection.getInputStream();
		FileOutputStream output = new FileOutputStream(getJarPath(), false);
		
		byte[] data = new byte[1024];
		
		Integer Count;
		while ((Count = input.read(data)) >= 0) {
			output.write(data, 0, Count);
		}
		
		output.flush();
		output.close();
	}
	
	private String getJarPath() {
		URL fileURL = Plugin.getClass().getProtectionDomain().getCodeSource().getLocation();

		String path = fileURL.getPath();
		String[] split = path.split("/");
		String Jar = split[split.length - 1];
		
		return "plugins/" + Jar;
	}
	
	private UpdateObject getLatestUpdate(String Branch) throws Exception {
		URL releases = new URL("https://api.github.com/repos/" + Author + "/" + Repository + "/releases");
		BufferedReader br = new BufferedReader(new InputStreamReader(releases.openStream(), "UTF-8"));
		
		String line;
		String result = "";
		
		while((line = br.readLine()) != null) {
			result = result.concat(line);
		}
		
		JSONParser parser = new JSONParser();
		JSONArray array = (JSONArray) parser.parse(result);
		
		for(Integer i = 0; i < array.size(); i++) {
			JSONObject object = (JSONObject) array.get(i);
			
			String BranchName = (String) object.get("target_commitish");
			if(BranchName.equalsIgnoreCase(Branch)) {
				String Version = (String) object.get("name");
				String Tag = (String) object.get("tag_name");

				JSONArray parse_assets = (JSONArray) object.get("assets");
				JSONObject latest = (JSONObject) parse_assets.get(0);
				String url = (String) latest.get("browser_download_url");
				URL downloadURL = new URL(url);
				
				String[] patchNote = ((String) object.get("body")).split("\\n");
				
				return new UpdateObject(Version, Tag, downloadURL, patchNote);
			}
		}
		
		throw new Exception();
	}
	
	public class UpdateObject {
		
		private String Version;
		private String Tag;
		private URL downloadURL;
		private String[] patchNote;
		
		public UpdateObject(String Version, String Tag, URL downloadURL, String... patchNote) {
			this.Version = Version;
			this.Tag = Tag;
			this.downloadURL = downloadURL;
			this.patchNote = patchNote;
		}
		
		public String getVersion() {
			return Version;
		}
		
		public String getTag() {
			return Tag;
		}
		
		public URL getDownloadURL() {
			return downloadURL;
		}
		
		public String[] getPatchNote() {
			return patchNote;
		}
		
		public int getFileSize() throws IOException {
			return downloadURL.openConnection().getContentLength();
		}
		
	}
	
	public enum Branch {
		
		Master("master", "1.12"),
		Alpha("1.13", "1.13");
		
		String Name;
		String Version;
		
		private Branch(String Name, String Version) {
			this.Name = Name;
			this.Version = Version;
		}

		public String getName() {
			return Name;
		}
		
		public String getVersion() {
			return Version;
		}
		
		public static Branch getBranchByVersion(Integer Version) {
			switch(Version) {
				case 12:
					return Branch.Master;
				case 13:
					return Branch.Alpha;
				default:
					return null;
			}
		}
		
	}
	
	public void load(Plugin plugin) {
		load(plugin.getName());
	}
	
	/**
	 * Loads and enables a plugin.
	 *
	 * @param name plugin's name
	 * @return status message
	 */
	public void load(String name) {

		Plugin target = null;

		File pluginDir = new File("plugins");

		if (!pluginDir.isDirectory()) {
			return;
		}

		File pluginFile = new File(pluginDir, name + ".jar");

		if (!pluginFile.isFile()) {
			for (File f : pluginDir.listFiles()) {
				if (f.getName().endsWith(".jar")) {
					try {
						PluginDescriptionFile desc = AbilityWar.getPlugin().getPluginLoader().getPluginDescription(f);
						if (desc.getName().equalsIgnoreCase(name)) {
							pluginFile = f;
							break;
						}
					} catch (InvalidDescriptionException e) {
						Messager.sendErrorMessage();
					}
				}
			}
		}

		try {
			target = Bukkit.getPluginManager().loadPlugin(pluginFile);
		} catch (InvalidDescriptionException | InvalidPluginException e) {
			e.printStackTrace();
			Messager.sendErrorMessage();
			return;
		}

		target.onLoad();
		Bukkit.getPluginManager().enablePlugin(target);
	}

	/**
	 * Reload a plugin.
	 *
	 * @param plugin the plugin to reload
	 */
	public void reload(Plugin plugin) {
		if (plugin != null) {
			unload(plugin);
			load(plugin);
		}
	}
	
	/**
	 * 플러그인을 Unload합니다.
	 */
	@SuppressWarnings("unchecked")
	public void unload(Plugin plugin) {

		String name = plugin.getName();

		PluginManager pluginManager = Bukkit.getPluginManager();

		SimpleCommandMap commandMap = null;

		List<Plugin> plugins = null;

		Map<String, Plugin> names = null;
		Map<String, Command> commands = null;
		Map<Event, SortedSet<RegisteredListener>> listeners = null;

		boolean reloadlisteners = true;

		if (pluginManager != null) {
			
			pluginManager.disablePlugin(plugin);
			
			try {

				Field pluginsField = Bukkit.getPluginManager().getClass().getDeclaredField("plugins");
				pluginsField.setAccessible(true);
				plugins = (List<Plugin>) pluginsField.get(pluginManager);

				Field lookupNamesField = Bukkit.getPluginManager().getClass().getDeclaredField("lookupNames");
				lookupNamesField.setAccessible(true);
				names = (Map<String, Plugin>) lookupNamesField.get(pluginManager);

				try {
					Field listenersField = Bukkit.getPluginManager().getClass().getDeclaredField("listeners");
					listenersField.setAccessible(true);
					listeners = (Map<Event, SortedSet<RegisteredListener>>) listenersField.get(pluginManager);
				} catch (Exception e) {
					reloadlisteners = false;
				}

				Field commandMapField = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
				commandMapField.setAccessible(true);
				commandMap = (SimpleCommandMap) commandMapField.get(pluginManager);

				Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
				knownCommandsField.setAccessible(true);
				commands = (Map<String, Command>) knownCommandsField.get(commandMap);

			} catch (NoSuchFieldException | IllegalAccessException e) {
				Messager.sendErrorMessage();
				return;
			}
		}

		pluginManager.disablePlugin(plugin);

		if (plugins != null && plugins.contains(plugin))
			plugins.remove(plugin);

		if (names != null && names.containsKey(name))
			names.remove(name);

		if (listeners != null && reloadlisteners) {
			for (SortedSet<RegisteredListener> set : listeners.values()) {
				for (Iterator<RegisteredListener> it = set.iterator(); it.hasNext();) {
					RegisteredListener value = it.next();
					if (value.getPlugin() == plugin) {
						it.remove();
					}
				}
			}
		}

		if (commandMap != null) {
			for (Iterator<Map.Entry<String, Command>> it = commands.entrySet().iterator(); it.hasNext();) {
				Map.Entry<String, Command> entry = it.next();
				if (entry.getValue() instanceof PluginCommand) {
					PluginCommand c = (PluginCommand) entry.getValue();
					if (c.getPlugin() == plugin) {
						c.unregister(commandMap);
						it.remove();
					}
				}
			}
		}
		
		ClassLoader cl = plugin.getClass().getClassLoader();

		if (cl instanceof URLClassLoader) {

			try {

				Field pluginField = cl.getClass().getDeclaredField("plugin");
				pluginField.setAccessible(true);
				pluginField.set(cl, null);

				Field pluginInitField = cl.getClass().getDeclaredField("pluginInit");
				pluginInitField.setAccessible(true);
				pluginInitField.set(cl, null);

			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
				Logger.getLogger(AutoUpdate.class.getName()).log(Level.SEVERE, null, ex);
			}

			try {
				((URLClassLoader) cl).close();
			} catch (IOException ex) {
				Messager.sendErrorMessage();
			}

		}
	}
	
}
