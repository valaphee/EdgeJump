/**
 *  Copyright 2024 Valaphee
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.valaphee.edgejump;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.SimpleOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class EdgeJump implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("edge-jump");
	public static SimpleOption<Boolean> EDGE_JUMP;

	@Override
	public void onInitialize() {
		loadConfig();
		EDGE_JUMP = SimpleOption.ofBoolean("options.edgeJump", true, value -> saveConfig());
	}

	private void loadConfig() {
		Path file = FabricLoader.getInstance().getConfigDir().resolve("edgejump.json");
		if (Files.exists(file)) {
			try (Reader fileReader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
				JsonObject config = new Gson().fromJson(fileReader, JsonObject.class);
				EDGE_JUMP.setValue(config.get("edgeJump").getAsBoolean());
			} catch (Exception ex) {
				LOGGER.warn("Failed to load config", ex);
			}
		}
	}

	private void saveConfig() {
		Path file = FabricLoader.getInstance().getConfigDir().resolve("edgejump.json");
		try (Writer fileWriter = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
			JsonObject config = new JsonObject();
			config.addProperty("edgeJump", EDGE_JUMP.getValue());
			new Gson().toJson(config, fileWriter);
		} catch (Exception ex) {
			LOGGER.warn("Failed to save config", ex);
		}
	}
}
