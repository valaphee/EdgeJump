/**
 *  Copyright 2024 Valaphee.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.valaphee.edgejump.mixin;

import com.mojang.authlib.GameProfile;
import com.valaphee.edgejump.EdgeJump;
import lombok.SneakyThrows;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.lang.reflect.Method;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {
	@Shadow private int ticksToNextAutojump;

	public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
		super(world, profile);
	}

    @Override
	protected Vec3d adjustMovementForSneaking(Vec3d movement, MovementType type) {
		float stepHeight = getStepHeight();
		if (getAbilities().flying || movement.y > 0.0 || !method_30263(stepHeight)) {
			return movement;
		}

		double moveX = movement.x;
		double moveZ = movement.z;
		double threshold = 0.05;
		double stepX = Math.signum(moveX) * threshold;
		double stepZ = Math.signum(moveZ) * threshold;

		while (moveX != 0.0 && isSpaceAroundPlayerEmpty(moveX, 0.0, stepHeight)) {
			if (Math.abs(moveX) <= threshold) {
				moveX = 0.0;
				break;
			}
			moveX -= stepX;
		}

		while (moveZ != 0.0 && isSpaceAroundPlayerEmpty(0.0, moveZ, stepHeight)) {
			if (Math.abs(moveZ) <= threshold) {
				moveZ = 0.0;
				break;
			}
			moveZ -= stepZ;
		}

		while (moveX != 0.0 && moveZ != 0.0 && isSpaceAroundPlayerEmpty(moveX, moveZ, stepHeight)) {
			moveX = Math.abs(moveX) <= threshold ? 0.0 : moveX - stepX;
			if (Math.abs(moveZ) <= threshold) {
				moveZ = 0.0;
				continue;
			}
			moveZ -= stepZ;
		}

		if (this.clipAtLedge() && type != MovementType.SELF && type != MovementType.PLAYER) {
			return new Vec3d(moveX, movement.y, moveZ);
		}
		if (EdgeJump.EDGE_JUMP.getValue() && (moveX != movement.x || moveZ != movement.z)) {
			ticksToNextAutojump = 1;
		}
		return movement;
	}

	@Unique
	private boolean method_30263(float stepHeight) {
		return isOnGround() || fallDistance < stepHeight && !isSpaceAroundPlayerEmpty(0.0, 0.0, stepHeight - fallDistance);
	}

	@Unique
	private boolean isSpaceAroundPlayerEmpty(double offsetX, double offsetZ, float stepHeight) {
		Box box = this.getBoundingBox();
		return getWorld().isSpaceEmpty(this, new Box(box.minX + offsetX, box.minY - (double)stepHeight - MathHelper.EPSILON, box.minZ + offsetZ, box.maxX + offsetX, box.minY, box.maxZ + offsetZ));
	}
}
