package dev.joona.trustedlinks;

import org.junit.jupiter.api.Test;
import java.net.URI;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrustedDomainMatcherTest {
	private final TrustedDomainMatcher matcher = new TrustedDomainMatcher(List.of("example.com"));

	@Test void trustsExactDomainAndSubdomains() {
		assertTrue(matcher.isTrusted(URI.create("https://example.com/path")));
		assertTrue(matcher.isTrusted(URI.create("https://www.example.com/path")));
		assertTrue(matcher.isTrusted(URI.create("http://sub.example.com/path")));
	}

	@Test void rejectsDomainBoundaryAttacksAndUnrelatedDomains() {
		assertFalse(matcher.isTrusted(URI.create("https://example.com.evil.com/")));
		assertFalse(matcher.isTrusted(URI.create("https://evil-example.com/")));
		assertFalse(matcher.isTrusted(URI.create("https://completelydifferent.com/")));
	}

	@Test void aTrustedSubdomainDoesNotTrustItsParentOrSiblings() {
		TrustedDomainMatcher subdomainMatcher = new TrustedDomainMatcher(List.of("store.example.com"));
		assertTrue(subdomainMatcher.isTrusted(URI.create("https://store.example.com/")));
		assertTrue(subdomainMatcher.isTrusted(URI.create("https://deep.store.example.com/")));
		assertFalse(subdomainMatcher.isTrusted(URI.create("https://example.com/")));
		assertFalse(subdomainMatcher.isTrusted(URI.create("https://blog.example.com/")));
	}

	@Test void rejectsUnsupportedSchemesAndMalformedUris() {
		assertFalse(matcher.isTrusted(URI.create("ftp://example.com/file")));
		assertFalse(matcher.isTrusted(URI.create("javascript:example.com")));
		assertFalse(matcher.isTrusted(URI.create("https:///missing-host")));
	}
}
