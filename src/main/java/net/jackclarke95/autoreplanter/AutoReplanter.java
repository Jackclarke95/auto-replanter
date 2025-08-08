package net.jackclarke95.autoreplanter;

import java.util.List;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.text.Text;

public class AutoReplanter implements ModInitializer {
	private static final TagKey<Item> KNIVES_TAG = TagKey.of(RegistryKeys.ITEM,
			Identifier.of("farmersdelight", "tools/knives"));

	@Override
	public void onInitialize() {
		PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
			if (world.isClient) {
				return true;
			}

			world = (ServerWorld) world;

			ItemStack mainTool = player.getMainHandStack();
			Block block = state.getBlock();

			// Check if we're hitting a crop with a knife
			if (block instanceof CropBlock cropBlock && isKnife(mainTool)) {
				player.sendMessage(Text.literal("Auto-replanting crop..."), false);

				// Only drop loot if the crop is mature
				if (isMatureCrop(cropBlock, state)) {
					// Get the dropped stacks manually
					List<ItemStack> droppedStacks = Block.getDroppedStacks(state, (ServerWorld) world, pos, blockEntity,
							player,
							mainTool);

					// Get the seed item for this crop
					Item seedItem = cropBlock.asItem();

					// Process each dropped stack
					for (ItemStack stack : droppedStacks) {
						if (stack.getItem() == seedItem && stack.getCount() > 1) {
							// Decrement by 1 if it's the seed and there's more than 1
							stack.decrement(1);
						}

						// Spawn the modified stack if it's not empty
						if (!stack.isEmpty()) {
							ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5,
									pos.getZ() + 0.5, stack);
							world.spawnEntity(itemEntity);
						}
					}
				}

				// Replant the crop at age 0 (regardless of maturity)
				world.setBlockState(pos, cropBlock.withAge(0), 3);

				return false; // Cancel the default break
			}

			return true; // Allow normal breaking for non-knife tools
		});
	}

	private boolean isMatureCrop(CropBlock cropBlock, net.minecraft.block.BlockState state) {
		return cropBlock.getAge(state) == cropBlock.getMaxAge();
	}

	private boolean isKnife(ItemStack tool) {
		return tool.isIn(KNIVES_TAG);
	}
}
