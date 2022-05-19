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

import chrono.mods.arrows.config.ArrowsConfig;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.loader.api.FabricLoader;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@Mixin(value = Gui.class, priority = 790)
public abstract class GuiMixin extends GuiComponent {
	@Shadow
	@Final
	private static ResourceLocation WIDGETS_LOCATION;
	@Shadow
	@Final
	private Minecraft minecraft;
	@Shadow
	private int screenHeight;
	@Shadow
	private int screenWidth;

	@Shadow
	private Player getCameraPlayer() {
		return null;
	}

	@Shadow
	private void renderSlot(int x, int y, float partialTick, Player player, ItemStack stack, int seed) {
	}

	@Inject(method = "renderHotbar", at = @At("RETURN"))
	private void cr$renderHotbar(float partialTick, PoseStack pose, CallbackInfo ci) {
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

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);

		ArrowsConfig.Placement placement = ArrowsConfig.placement;

		HumanoidArm arm = player.getMainArm();
		if (placement == ArrowsConfig.Placement.OFFHAND) {
			arm = arm.getOpposite();
		}

		int x;
		if (arm == HumanoidArm.LEFT) {
			x = this.screenWidth / 2 - 91 - 29;
			if (placement == ArrowsConfig.Placement.OFFHAND && !offHandStack.isEmpty()) {
				x -= 23;
			}
		} else {
			x = this.screenWidth / 2 + 91;
			if (placement == ArrowsConfig.Placement.OFFHAND && !offHandStack.isEmpty()) {
				x += 23;
			}
		}

		int y = this.screenHeight - 23;
		if (FabricLoader.getInstance().getObjectShare().get("raised:distance") instanceof Integer distance) {
			y -= distance;
		}

		int oldBlitOffset = this.getBlitOffset();
		this.setBlitOffset(-90);
		if (arm == HumanoidArm.LEFT) {
			this.blit(pose, x, y, 24, 22, 29, 24);
		} else {
			this.blit(pose, x, y, 53, 22, 29, 24);
		}
		this.setBlitOffset(oldBlitOffset);

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		if (arm == HumanoidArm.LEFT) {
			this.renderSlot(x + 3, y + 4, partialTick, player, arrows, 1);
		} else {
			this.renderSlot(x + 10, y + 4, partialTick, player, arrows, 1);
		}

		RenderSystem.disableBlend();
	}
}
