package site.geni.stuff.mixins;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TextComponent;
import net.minecraft.text.TextFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import site.geni.stuff.commands.TpaCommand;
import site.geni.stuff.util.AutoAppendTextComponent;
import site.geni.stuff.util.AutoFormatTextComponent;

import java.util.List;

@SuppressWarnings("unused")
@Environment(EnvType.SERVER)
@Mixin(ServerPlayNetworkHandler.class)
public abstract class OnDisconnectMixin {
	@Shadow
	public ServerPlayerEntity player;

	@Inject(at = @At("RETURN"), method = "onDisconnected")
	private void onConnectionLost(CallbackInfo info) {
		if (TpaCommand.getRequests().containsValue(player.getUuid())) {
			/* try to get destination player */
			final ServerPlayerEntity destPlayer = player.server.getPlayerManager().getPlayer(TpaCommand.getRequests().inverse().get(player.getUuid()));

			if (destPlayer != null) {
				/* remove expired TPA request from requests */
				TpaCommand.getRequests().remove(player.getUuid());

				/* prepare for message */
				final TextComponent originPlayerName = new AutoFormatTextComponent(player.getDisplayName().getString(), TextFormat.DARK_RED);
				/* send message to destination player alerting them that the TPA request has expired */
				final TextComponent requestExpiredFromMessage = new AutoAppendTextComponent(TextFormat.GOLD, "TPA request from ", originPlayerName, " has expired.");

				destPlayer.addChatMessage(requestExpiredFromMessage, false);
			}
		}
	}
}
