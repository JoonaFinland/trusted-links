package dev.joona.trustedlinks;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class TrustedLinksConfigScreen extends Screen {
	private static final int ROW_HEIGHT = 24;
	private final Screen parent;
	private int page;

	public TrustedLinksConfigScreen(Screen parent) {
		super(Component.translatable("trustedlinks.config.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		List<String> hostnames = TrustedLinksClient.getTrustedHostnames();
		int rowsPerPage = rowsPerPage();
		int pageCount = Math.max(1, (hostnames.size() + rowsPerPage - 1) / rowsPerPage);
		page = Math.min(page, pageCount - 1);
		int first = page * rowsPerPage;
		int last = Math.min(hostnames.size(), first + rowsPerPage);

		for (int index = first; index < last; index++) {
			String hostname = hostnames.get(index);
			int y = 56 + (index - first) * ROW_HEIGHT;
			addRenderableWidget(Button.builder(
				Component.translatable("trustedlinks.config.remove", hostname),
				button -> {
					TrustedLinksClient.removeTrustedHostname(hostname);
					rebuildWidgets();
				}
			).bounds(width / 2 - 150, y, 300, 20).build());
		}

		int footerY = height - 28;
		Button previous = addRenderableWidget(Button.builder(
			Component.translatable("trustedlinks.config.previous"),
			button -> { page--; rebuildWidgets(); }
		).bounds(width / 2 - 154, footerY, 100, 20).build());
		previous.active = page > 0;

		addRenderableWidget(Button.builder(
			Component.translatable("trustedlinks.config.done"),
			button -> onClose()
		).bounds(width / 2 - 50, footerY, 100, 20).build());

		Button next = addRenderableWidget(Button.builder(
			Component.translatable("trustedlinks.config.next"),
			button -> { page++; rebuildWidgets(); }
		).bounds(width / 2 + 54, footerY, 100, 20).build());
		next.active = page + 1 < pageCount;
	}

	private int rowsPerPage() {
		return Math.max(1, (height - 104) / ROW_HEIGHT);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		graphics.centeredText(font, title, width / 2, 20, 0xFFFFFF);
		Component message = TrustedLinksClient.getTrustedHostnames().isEmpty()
			? Component.translatable("trustedlinks.config.empty")
			: Component.translatable("trustedlinks.config.hint");
		graphics.centeredText(font, message, width / 2, 36, 0xA0A0A0);
	}

	@Override
	public void onClose() {
		minecraft.setScreen(parent);
	}
}