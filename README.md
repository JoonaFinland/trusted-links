# Trusted Links

<img src="assets/modrinth-thumbnail.png" alt="Trusted Links icon" width="128">

A small, client-side Fabric mod that lets you skip Minecraft's external-link confirmation for hostnames you explicitly trust.

[![Minecraft](https://img.shields.io/badge/Minecraft-26.1.2-62B47A?logo=minecraft)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Loader-Fabric-DBD0B4)](https://fabricmc.net/)
[![Java](https://img.shields.io/badge/Java-25-ED8B00?logo=openjdk)](https://adoptium.net/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

Minecraft normally asks for confirmation before opening a URL. Trusted Links preserves that warning for every unknown link, while offering a convenient **Yes & Trust Hostname** button for sites you use regularly.

## Features

- Bypasses the warning only for explicitly trusted HTTP and HTTPS hostnames.
- Adds **Yes & Trust Hostname** to Minecraft's normal link confirmation screen.
- Saves trusted hostnames immediately to a human-editable JSON file.
- Supports secure hostname-boundary matching instead of unsafe substring checks.
- Remains entirely client-side; servers do not need the mod.
- Preserves Minecraft's normal behavior for malformed URLs, unsupported schemes, disabled chat links, and untrusted sites.
- Has no configuration-screen dependency.

## Requirements

| Component | Version |
| --- | --- |
| Minecraft Java Edition | 26.1.2 |
| Fabric Loader | 0.19.3 or newer |
| Fabric API | 0.155.2+26.1.2 |
| Java | 25 or newer |

This build targets Minecraft **26.1.2 only**. It is not a 1.21.x mod.

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for Minecraft 26.1.2.
2. Install the matching [Fabric API](https://modrinth.com/mod/fabric-api).
3. Place the Trusted Links JAR in your Minecraft `mods` directory.
4. Launch the game once to create the configuration file.

## Usage

When Minecraft shows its external-link confirmation, choose one of the normal actions or click **Yes & Trust Hostname**. The new action opens the URL and saves its exact hostname.

For a URL such as:

```text
https://store.example.com/products/123
```

Trusted Links stores:

```text
store.example.com
```

It does not reduce that value to `example.com`.

### Matching rules

A configured hostname matches itself and its deeper subdomains:

| Configured value | URL hostname | Result |
| --- | --- | --- |
| `example.com` | `example.com` | Trusted |
| `example.com` | `www.example.com` | Trusted |
| `store.example.com` | `cdn.store.example.com` | Trusted |
| `store.example.com` | `example.com` | Not trusted |
| `store.example.com` | `blog.example.com` | Not trusted |
| `example.com` | `example.com.evil.com` | Not trusted |
| `example.com` | `evil-example.com` | Not trusted |

Only the parsed URL hostname is checked. Link display text, paths, query strings, usernames, and fragments cannot grant trust. Only normal HTTP and HTTPS URLs are eligible.

## Configuration

The configuration is stored at:

```text
.minecraft/config/trusted-links.json
```

It is created automatically with an empty allowlist:

```json
{
  "trustedDomains": []
}
```

You can edit the list manually while Minecraft is closed:

```json
{
  "trustedDomains": [
    "hypixel.net",
    "store.example.com"
  ]
}
```

Restart Minecraft after manual edits. Entries added with the in-game button are written immediately. If the file is missing or malformed, the mod safely falls back to an empty allowlist and records the problem in the Minecraft log.

## How it works

In Minecraft 26.1.2, clickable URLs flow through `Screen.defaultHandleClickEvent(...)` to `Screen.clickUrlAction(Minecraft, Screen, URI)`. The latter checks Minecraft's `chatLinks` and `chatLinksPrompt` options and normally displays `ConfirmLinkScreen` before calling `Util.getPlatform().openUri(URI)`.

Trusted Links uses two narrowly scoped client mixins:

- `ScreenMixin` bypasses only the call that displays `ConfirmLinkScreen`, and only when the parsed HTTP(S) hostname is already trusted.
- `ConfirmLinkScreenMixin` adds the **Yes & Trust Hostname** button to the vanilla confirmation screen.

All unrelated click actions and all untrusted URLs retain vanilla behavior.

## Building from source

Install Java 25 and run:

```bash
./gradlew build
```

On Windows:

```powershell
.\gradlew.bat build
```

The release JAR is generated in `build/libs/`. The build also runs the hostname-matching unit tests.

## Contributing

Bug reports and focused pull requests are welcome. Please read [CONTRIBUTING.md](CONTRIBUTING.md) before contributing. Security-sensitive issues should follow [SECURITY.md](SECURITY.md) instead of being reported publicly.

## License

Trusted Links is available under the [MIT License](LICENSE).
