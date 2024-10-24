/*
Copyright (C) 2021-2024 intact

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

import chrono.mods.arrows.config.ArrowsConfig;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@Mixin(value = Gui.class, priority = 790)
public abstract class GuiMixin {
	@Shadow
	@Final
	private static ResourceLocation HOTBAR_OFFHAND_LEFT_SPRITE;
	@Shadow
	@Final
	private static ResourceLocation HOTBAR_OFFHAND_RIGHT_SPRITE;
	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	private Player getCameraPlayer() {
		return null;
	}

	@Shadow
	private void renderSlot(GuiGraphics gui, int x, int y, DeltaTracker timer, Player player, ItemStack stack,
			int seed) {
	}

	@Inject(method = "renderItemHotbar", at = @At("RETURN"))
	private void cr$renderHotbar(GuiGraphics gui, DeltaTracker timer, CallbackInfo ci) {
		Player player = this.getCameraPlayer();
		if (player == null) {
			return;
		}

		ItemStack offHandStack = player.getOffhandItem();

		ItemStack arrows = player.getProjectile(player.getMainHandItem());
		if (arrows.isEmpty()) {
			arrows = player.getProjectile(offHandStack);
		}
		if (arrows.isEmpty() || arrows == offHandStack) {
			return;
		}

		ArrowsConfig.Placement placement = ArrowsConfig.placement;

		HumanoidArm arm = player.getMainArm();
		if (placement == ArrowsConfig.Placement.OFFHAND) {
			arm = arm.getOpposite();
		}

		int x;
		if (arm == HumanoidArm.LEFT) {
			x = gui.guiWidth() / 2 - 91 - 29;
			if (placement == ArrowsConfig.Placement.OFFHAND && !offHandStack.isEmpty()) {
				x -= 23;
			}
		} else {
			x = gui.guiWidth() / 2 + 91;
			if (placement == ArrowsConfig.Placement.OFFHAND && !offHandStack.isEmpty()) {
				x += 23;
			}
		}

		int y = gui.guiHeight() - 23;

		RenderSystem.defaultBlendFunc();
		RenderSystem.enableBlend();

		gui.pose().pushPose();
		gui.pose().translate(0.0F, 0.0F, -90.0F);
		if (arm == HumanoidArm.LEFT) {
			gui.blitSprite(RenderType::guiTextured, HOTBAR_OFFHAND_LEFT_SPRITE, x, y, 29, 24);
		} else {
			gui.blitSprite(RenderType::guiTextured, HOTBAR_OFFHAND_RIGHT_SPRITE, x, y, 29, 24);
		}
		gui.pose().popPose();

		if (arm == HumanoidArm.LEFT) {
			this.renderSlot(gui, x + 3, y + 4, timer, player, arrows, 1);
		} else {
			this.renderSlot(gui, x + 10, y + 4, timer, player, arrows, 1);
		}

		RenderSystem.disableBlend();
	}
}
