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
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.lang.reflect.Method;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {
	private static final Method IS_SPACE_AROUND_PLAYER_EMPTY;
	private static final Method METHOD_30263;
	@Shadow private int ticksToNextAutojump;

	public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
		super(world, profile);
	}

	@SneakyThrows
    @Override
	protected Vec3d adjustMovementForSneaking(Vec3d movement, MovementType type) {
		float stepHeight = getStepHeight();
		if (getAbilities().flying || movement.y > 0.0 || !(boolean)METHOD_30263.invoke(this, stepHeight)) {
			return movement;
		}

		double moveX = movement.x;
		double moveZ = movement.z;
		double threshold = 0.05;
		double stepX = Math.signum(moveX) * threshold;
		double stepZ = Math.signum(moveZ) * threshold;

		while (moveX != 0.0 && (boolean) IS_SPACE_AROUND_PLAYER_EMPTY.invoke(this, moveX, 0.0, stepHeight)) {
			if (Math.abs(moveX) <= threshold) {
				moveX = 0.0;
				break;
			}
			moveX -= stepX;
		}

		while (moveZ != 0.0 && (boolean) IS_SPACE_AROUND_PLAYER_EMPTY.invoke(this, 0.0, moveZ, stepHeight)) {
			if (Math.abs(moveZ) <= threshold) {
				moveZ = 0.0;
				break;
			}
			moveZ -= stepZ;
		}

		while (moveX != 0.0 && moveZ != 0.0 && (boolean) IS_SPACE_AROUND_PLAYER_EMPTY.invoke(this, moveX, moveZ, stepHeight)) {
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

	static {
		try {
			IS_SPACE_AROUND_PLAYER_EMPTY = PlayerEntity.class.getDeclaredMethod("isSpaceAroundPlayerEmpty", double.class, double.class, float.class);
			IS_SPACE_AROUND_PLAYER_EMPTY.setAccessible(true);
			METHOD_30263 = PlayerEntity.class.getDeclaredMethod("method_30263", float.class);
			METHOD_30263.setAccessible(true);
		} catch (Throwable thrown) {
			throw new ExceptionInInitializerError(thrown);
		}
	}
}
