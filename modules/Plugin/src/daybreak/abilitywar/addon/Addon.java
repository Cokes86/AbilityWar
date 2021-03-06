package daybreak.abilitywar.addon;

import com.google.common.base.Enums;
import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.addon.exception.InvalidDescriptionException;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion.Version;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import org.bukkit.plugin.Plugin;

/**
 * 애드온
 *
 * @author Daybreak 새벽
 */
public abstract class Addon {

	private ClassLoader classLoader;
	private AddonDescription description;

	void init(ClassLoader classLoader, AddonDescription description) {
		this.classLoader = classLoader;
		this.description = description;
	}

	protected void onEnable() {
	}

	protected void onDisable() {
	}

	/**
	 * AbilityWar 플러그인을 받아옵니다.
	 */
	protected Plugin getPlugin() {
		return AbilityWar.getPlugin();
	}

	/**
	 * addon.yml에 작성한 애드온의 설명을 받아옵니다.
	 */
	public AddonDescription getDescription() {
		return description;
	}

	/**
	 * 이 애드온을 불러올 때 사용된 ClassLoader를 받아옵니다.
	 */
	protected ClassLoader getClassLoader() {
		return classLoader;
	}

	public static class AddonDescription {

		private final String name, main, version;
		private final Version minVersion;

		AddonDescription(File pluginFile) throws InvalidDescriptionException {
			JarFile jarFile = null;
			try {
				jarFile = new JarFile(pluginFile);
				ZipEntry entry = jarFile.getEntry("addon.yml");
				if (entry != null) {
					Properties description = new Properties();
					description.load(new InputStreamReader(jarFile.getInputStream(entry)));

					this.name = description.getProperty("name", "");
					this.main = description.getProperty("main", "");
					this.version = description.getProperty("version", "");
					this.minVersion = description.containsKey("minVersion") ? Enums.getIfPresent(Version.class, description.getProperty("minVersion")).orNull() : Version.v1_9_R1;
					if (name.isEmpty() || main.isEmpty() || version.isEmpty()) {
						throw new InvalidDescriptionException(jarFile.getName() + ": 올바르지 않은 addon.yml입니다.");
					}
					if (minVersion == null) {
						throw new InvalidDescriptionException(jarFile.getName() + ": minVersion이 잘못된 값입니다. (" + description.getProperty("minVersion") + ")");
					}
				} else {
					throw new InvalidDescriptionException(new FileNotFoundException(jarFile.getName() + ": addon.yml이 존재하지 않습니다."));
				}
			} catch (IOException ex) {
				throw new InvalidDescriptionException(ex);
			} finally {
				if (jarFile != null) {
					try {
						jarFile.close();
					} catch (IOException ignored) {
					}
				}
			}
		}

		public String getName() {
			return name;
		}

		public String getMain() {
			return main;
		}

		public String getVersion() {
			return version;
		}

		public Version getMinVersion() {
			return minVersion;
		}

	}

}
