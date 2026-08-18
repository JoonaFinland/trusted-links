package dev.joona.trustedlinks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class TrustedLinksClient implements ClientModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger("trustedlinks");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static volatile TrustedDomainMatcher matcher = new TrustedDomainMatcher(List.of());
	private static Path configPath;
	private static List<String> trustedDomains = List.of();

	@Override
	public void onInitializeClient() {
		configPath = FabricLoader.getInstance().getConfigDir().resolve("trusted-links.json");
		trustedDomains = sanitizeDomains(loadConfig(configPath).trustedDomains());
		matcher = new TrustedDomainMatcher(trustedDomains);
	}

	public static boolean isTrusted(URI uri) {
		return matcher.isTrusted(uri);
	}

	public static boolean canTrustHostname(URI uri) {
		if (uri == null || uri.isOpaque()) return false;
		String scheme = uri.getScheme();
		return scheme != null
			&& (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))
			&& TrustedDomainMatcher.normalizeDomain(uri.getHost()) != null;
	}

	public static synchronized void trustHostname(URI uri) {
		if (!canTrustHostname(uri)) return;
		String hostname = TrustedDomainMatcher.normalizeDomain(uri.getHost());
		Set<String> updated = new LinkedHashSet<>(trustedDomains);
		if (!updated.add(hostname)) return;
		List<String> domains = List.copyOf(updated);
		if (saveConfig(configPath, new Config(domains))) {
			trustedDomains = domains;
			matcher = new TrustedDomainMatcher(domains);
			LOGGER.info("Added trusted hostname {} to {}", hostname, configPath);
		}
	}

	public static synchronized List<String> getTrustedHostnames() {
		return List.copyOf(trustedDomains);
	}

	public static synchronized boolean removeTrustedHostname(String hostname) {
		String normalized = TrustedDomainMatcher.normalizeDomain(hostname);
		if (normalized == null || !trustedDomains.contains(normalized)) return false;
		List<String> domains = trustedDomains.stream()
			.filter(domain -> !domain.equals(normalized))
			.toList();
		if (!saveConfig(configPath, new Config(domains))) return false;
		trustedDomains = domains;
		matcher = new TrustedDomainMatcher(domains);
		LOGGER.info("Removed trusted hostname {} from {}", normalized, configPath);
		return true;
	}
	private static List<String> sanitizeDomains(List<String> domains) {
		if (domains == null) return List.of();
		Set<String> sanitized = new LinkedHashSet<>();
		for (String domain : domains) {
			String normalized = TrustedDomainMatcher.normalizeDomain(domain);
			if (normalized != null) sanitized.add(normalized);
		}
		return new ArrayList<>(sanitized);
	}

	private static Config loadConfig(Path path) {
		if (Files.notExists(path)) {
			Config config = new Config(List.of());
			if (saveConfig(path, config)) LOGGER.info("Created empty trusted-links configuration at {}", path);
			return config;
		}
		try (Reader reader = Files.newBufferedReader(path)) {
			Config config = GSON.fromJson(reader, Config.class);
			if (config == null || config.trustedDomains() == null) {
				LOGGER.warn("Configuration {} has no trustedDomains list; using an empty allowlist", path);
				return new Config(List.of());
			}
			LOGGER.info("Loaded {} trusted domain(s) from {}", config.trustedDomains().size(), path);
			return config;
		} catch (IOException | JsonParseException exception) {
			LOGGER.error("Could not read trusted-links configuration at {}; using an empty allowlist", path, exception);
			return new Config(List.of());
		}
	}

	private static boolean saveConfig(Path path, Config config) {
		if (path == null) return false;
		Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(temporary)) { GSON.toJson(config, writer); }
			try {
				Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
			} catch (IOException unsupportedAtomicMove) {
				Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
			}
			return true;
		} catch (IOException exception) {
			LOGGER.error("Could not save trusted-links configuration at {}", path, exception);
			return false;
		}
	}

	private record Config(List<String> trustedDomains) {}
}
