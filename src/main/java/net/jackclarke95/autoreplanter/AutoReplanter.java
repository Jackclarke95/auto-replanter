package net.jackclarke95.autoreplanter;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.block.CropBlock;
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
			ItemStack selectedItem = player.getInventory().getMainHandStack();
			ItemStack offHandTool = player.getOffHandStack();
			ItemStack activeItem = player.getActiveItem();
			Block block = state.getBlock();

			player.sendMessage(Text.literal("Broken block: " + block.getName().getString()), false);
			player.sendMessage(Text.literal("Selected item: " + selectedItem.getName().getString()), false);
			player.sendMessage(Text.literal("Equipped tool: " + mainTool.getName().getString()), false);
			player.sendMessage(Text.literal("Offhand tool: " + offHandTool.getName().getString()), false);
			player.sendMessage(Text.literal("Active item: " + activeItem.getName().getString()), false);

			// Check if we're hitting a crop with a knife
			if (block instanceof CropBlock cropBlock && isKnife(mainTool)) {
				player.sendMessage(Text.literal("Auto-replanting crop..."), false);

				// Drop loot for the current crop if it's mature
				if (isMatureCrop(cropBlock, state)) {
					Block.dropStacks(state, world, pos, blockEntity, player, mainTool);
					// // subtract one crop seed from the dropped loot
					// var droppedLoot = Block.getDroppedStacks(state, (ServerWorld) world, pos,
					// blockEntity);

					// droppedLoot.decrement(1);
					// world.spawnEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(),
					// droppedLoot));

				}

				// Replant the crop at age 0
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
