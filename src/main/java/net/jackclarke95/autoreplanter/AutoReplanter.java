package net.jackclarke95.autoreplanter;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.event.GameEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoReplanter implements ModInitializer {
	public static final String MOD_ID = "auto-replanter";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("[AutoReplanter] Mod initializing.");

		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
			if (!(world instanceof ServerWorld serverWorld)) return;

			ItemStack tool = player.getMainHandStack();
			if (!tool.isOf(Items.WOODEN_HOE)) return;

			Block block = state.getBlock();

			if (block instanceof CropBlock cropBlock) {
				int age = state.get(state.getProperties().stream()
						.filter(p -> p.getName().equals("age"))
						.map(p -> (net.minecraft.state.property.IntProperty) p)
						.findFirst()
						.orElseThrow(() -> new IllegalStateException("No age property found")));

				int maxAge = cropBlock.getMaxAge();

				// 🎁 Drop loot ONLY if crop is fully grown
				if (age >= maxAge) {
					Block.dropStacks(state, serverWorld, pos, blockEntity, player, tool);
				}

				// 🌱 Always replant, regardless of growth stage
				serverWorld.setBlockState(pos, cropBlock.getDefaultState(), 3);
				serverWorld.emitGameEvent(player, GameEvent.BLOCK_PLACE, pos);

				LOGGER.info("[AutoReplanter] Replanted {} at {} (age: {})", block.getName().getString(), pos, age);
			}
		});
	}
}
