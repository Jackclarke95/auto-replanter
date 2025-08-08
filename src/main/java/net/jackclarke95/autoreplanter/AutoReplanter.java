package net.jackclarke95.autoreplanter;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public class AutoReplanter implements ModInitializer {
	private AutoReplanterConfig config;
	private Set<TagKey<Item>> validToolTags;

	@Override
	public void onInitialize() {
		// Load configuration
		config = ConfigManager.loadConfig();

		// Convert string tags to TagKey objects
		validToolTags = config.validToolTags.stream()
				.map(this::parseTagString)
				.collect(Collectors.toSet());

		PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
			if (world.isClient || !config.enableAutoReplanting) {
				return true;
			}

			Block block = state.getBlock();

			// Check if the block is a crop
			if (!(block instanceof CropBlock cropBlock)) {
				return true;
			}

			ItemStack mainTool = player.getMainHandStack();

			// Check if we need a valid tool and if so, whether we have one
			if (config.requireTool && !isValidTool(mainTool)) {
				return true;
			}

			world = (ServerWorld) world;

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

			// Only damage tools if tool requirement is enabled and we have a valid tool
			if (config.damageTools && config.requireTool && mainTool.isDamageable() && isValidTool(mainTool)) {
				mainTool.damage(1, player, EquipmentSlot.MAINHAND);
			}

			return false; // Cancel the default break
		});
	}

	private boolean isMatureCrop(CropBlock cropBlock, net.minecraft.block.BlockState state) {
		return cropBlock.getAge(state) == cropBlock.getMaxAge();
	}

	private boolean isValidTool(ItemStack tool) {
		return validToolTags.stream().anyMatch(tool::isIn);
	}

	private TagKey<Item> parseTagString(String tagString) {
		String[] parts = tagString.split(":");
		if (parts.length == 2) {
			return TagKey.of(RegistryKeys.ITEM, Identifier.of(parts[0], parts[1]));
		} else {
			// Handle tags with more colons (e.g., "namespace:category/subcategory")
			int firstColon = tagString.indexOf(':');
			String namespace = tagString.substring(0, firstColon);
			String path = tagString.substring(firstColon + 1);
			return TagKey.of(RegistryKeys.ITEM, Identifier.of(namespace, path));
		}
	}
}
