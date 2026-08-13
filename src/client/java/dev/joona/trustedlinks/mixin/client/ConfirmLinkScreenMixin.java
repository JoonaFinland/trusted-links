package dev.joona.trustedlinks.mixin.client;

import dev.joona.trustedlinks.TrustedLinksClient;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.URI;

@Mixin(ConfirmLinkScreen.class)
abstract class ConfirmLinkScreenMixin extends ConfirmScreen {
	@Shadow @Final private String url;

	private ConfirmLinkScreenMixin(BooleanConsumer callback, Component title, Component message) {
		super(callback, title, message);
	}

	@Inject(method = "addButtons", at = @At("TAIL"))
	private void trustedlinks$addTrustHostnameButton(LinearLayout buttonLayout, CallbackInfo ci) {
		URI uri;
		try {
			uri = URI.create(this.url);
		} catch (IllegalArgumentException exception) {
			return;
		}
		if (!TrustedLinksClient.canTrustHostname(uri)) return;
		this.layout.addChild(Button.builder(Component.translatable("trustedlinks.confirm.trust_hostname"), button -> {
			TrustedLinksClient.trustHostname(uri);
			this.callback.accept(true);
		}).width(304).build());
	}
}
