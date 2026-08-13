package dev.joona.trustedlinks;

import java.net.IDN;
import java.net.URI;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class TrustedDomainMatcher {
	private final Set<String> trustedDomains;

	public TrustedDomainMatcher(Collection<String> trustedDomains) {
		this.trustedDomains = trustedDomains == null ? Set.of() : trustedDomains.stream()
			.map(TrustedDomainMatcher::normalizeDomain)
			.filter(Objects::nonNull)
			.collect(Collectors.toUnmodifiableSet());
	}

	public boolean isTrusted(URI uri) {
		if (uri == null || uri.isOpaque()) return false;
		String scheme = uri.getScheme();
		if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) return false;
		String host = normalizeDomain(uri.getHost());
		if (host == null) return false;
		return trustedDomains.stream().anyMatch(domain -> host.equals(domain) || host.endsWith("." + domain));
	}

	public static String normalizeDomain(String value) {
		if (value == null) return null;
		String domain = value.strip().toLowerCase(Locale.ROOT);
		if (domain.endsWith(".")) domain = domain.substring(0, domain.length() - 1);
		if (domain.isEmpty() || domain.contains(":") || domain.contains("/") || domain.contains("@")) return null;
		try {
			domain = IDN.toASCII(domain, IDN.USE_STD3_ASCII_RULES).toLowerCase(Locale.ROOT);
		} catch (IllegalArgumentException exception) {
			return null;
		}
		if (domain.length() > 253) return null;
		for (String label : domain.split("\\.", -1)) {
			if (label.isEmpty() || label.length() > 63 || label.startsWith("-") || label.endsWith("-")) return null;
		}
		return domain;
	}
}
