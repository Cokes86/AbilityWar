package daybreak.abilitywar.utils.base.minecraft;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Mojang API Wrapper
 *
 * @author Daybreak 새벽
 */
public class MojangAPI {

	private MojangAPI() {
	}

	public static String getNickname(String uuid) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new URL("https://api.mojang.com/user/profiles/" + uuid + "/names").openStream(), StandardCharsets.UTF_8));

		String result = "";
		String line;
		while ((line = br.readLine()) != null) {
			result = result.concat(line);
		}

		JsonArray array = JsonParser.parseString(result).getAsJsonArray();
		JsonObject nickname = array.get(array.size() - 1).getAsJsonObject();
		return nickname.get("name").getAsString();
	}

}
