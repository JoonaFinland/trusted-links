package dev.joona.trustedlinks.mixin.client;

import dev.joona.trustedlinks.TrustedLinksClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.net.URI;

@Mixin(Screen.class)
abstract class ScreenMixin {
	@Redirect(method = "clickUrlAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"))
	private static void trustedlinks$openTrustedWithoutPrompt(Minecraft instance, Screen confirmationScreen, Minecraft minecraft, @Nullable Screen parentScreen, URI uri) {
		if (TrustedLinksClient.isTrusted(uri)) {
			Util.getPlatform().openUri(uri);
		} else {
			instance.setScreen(confirmationScreen);
		}
	}
}
