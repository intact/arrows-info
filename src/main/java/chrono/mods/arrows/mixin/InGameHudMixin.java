/*
Copyright (C) 2021 intact

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package chrono.mods.arrows.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin extends DrawableHelper {
	@Shadow
	@Final
	private static Identifier WIDGETS_TEXTURE;
	@Shadow
	@Final
	private MinecraftClient client;
	@Shadow
	private int scaledHeight;
	@Shadow
	private int scaledWidth;

	@Shadow
	private PlayerEntity getCameraPlayer() {
		return null;
	}

	@Shadow
	private void renderHotbarItem(int x, int y, float tickDelta, PlayerEntity player, ItemStack stack) {
	}

	@Inject(method = "render", at = @At("RETURN"))
	private void render(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
		if (this.client.options.hudHidden
				|| this.client.interactionManager.getCurrentGameMode() == GameMode.SPECTATOR) {
			return;
		}

		PlayerEntity player = this.getCameraPlayer();
		if (player == null) {
			return;
		}

		ItemStack offHandStack = player.getOffHandStack();

		ItemStack arrows = player.getArrowType(player.getMainHandStack());
		if (arrows.isEmpty()) {
			arrows = player.getArrowType(offHandStack);
		}
		if (arrows.isEmpty() || arrows == offHandStack) {
			return;
		}

		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.client.getTextureManager().bindTexture(WIDGETS_TEXTURE);

		Arm arm = player.getMainArm().getOpposite();
		int x;
		if (arm == Arm.LEFT) {
			x = this.scaledWidth / 2 - 91 - 29 - (offHandStack.isEmpty() ? 0 : 23);
		} else {
			x = this.scaledWidth / 2 + 91 + (offHandStack.isEmpty() ? 0 : 23);
		}
		int y = this.scaledHeight - 23;

		if (arm == Arm.LEFT) {
			this.drawTexture(matrices, x, y, 24, 22, 29, 24);
		} else {
			this.drawTexture(matrices, x, y, 53, 22, 29, 24);
		}

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		if (arm == Arm.LEFT) {
			this.renderHotbarItem(x + 3, y + 4, tickDelta, player, arrows);
		} else {
			this.renderHotbarItem(x + 10, y + 4, tickDelta, player, arrows);
		}

		RenderSystem.disableBlend();
	}
}
