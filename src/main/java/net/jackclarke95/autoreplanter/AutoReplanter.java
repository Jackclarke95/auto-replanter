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

/**
 * Main mod class for the Auto Replanter mod.
 * <p>
 * This mod automatically replants crops when they are harvested by players,
 * eliminating the need to manually replant seeds after breaking crops.
 * The behavior is highly configurable through the {@link AutoReplanterConfig}
 * class.
 * </p>
 * <p>
 * Key features:
 * <ul>
 * <li>Automatic crop replanting after harvesting</li>
 * <li>Configurable tool requirements for activation</li>
 * <li>Tool durability management with enchantment support</li>
 * <li>Mature vs immature crop handling</li>
 * <li>Support for modded crops that extend {@link CropBlock}</li>
 * </ul>
 * </p>
 * 
 * @author jackclarke95
 * @since 1.0.0
 * @see AutoReplanterConfig
 * @see ConfigManager
 */
public class AutoReplanter implements ModInitializer {

	/** The loaded configuration for this mod instance. */
	private AutoReplanterConfig config;

	/** Set of parsed tool tags that are considered valid for auto-replanting. */
	private Set<TagKey<Item>> validToolTags;

	/**
	 * Initializes the Auto Replanter mod.
	 * <p>
	 * This method is called by Fabric when the mod is loaded. It performs the
	 * following:
	 * <ul>
	 * <li>Loads the configuration from the config file</li>
	 * <li>Parses tool tags from string format to TagKey objects</li>
	 * <li>Registers the block break event handler</li>
	 * </ul>
	 * </p>
	 */
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

			// Check if we need a valid tool and if so, whether we have one
			ItemStack mainTool = player.getMainHandStack();

			if (config.requireTool && !isValidTool(mainTool)) {
				return true;
			}

			boolean isMature = isMatureCrop(cropBlock, state);

			// Only drop loot if the crop is mature
			if (isMature) {
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

			// Damage tools based on config settings
			if (config.damageTools && config.requireTool && mainTool.isDamageable() && isValidTool(mainTool)) {
				// Only damage if we should always damage, or if we only damage on mature crops
				// and this is mature
				if (!config.onlyDamageOnMatureCrop || isMature) {
					mainTool.damage(1, player, EquipmentSlot.MAINHAND);
				}
			}

			return false; // Cancel the default break
		});
	}

	/**
	 * Determines if a crop is fully mature and ready for harvest.
	 * 
	 * @param cropBlock the crop block to check
	 * @param state     the current block state of the crop
	 * @return {@code true} if the crop is at maximum age, {@code false} otherwise
	 */
	private boolean isMatureCrop(CropBlock cropBlock, net.minecraft.block.BlockState state) {
		return cropBlock.getAge(state) == cropBlock.getMaxAge();
	}

	/**
	 * Checks if the given tool is valid for auto-replanting based on configured
	 * tool tags.
	 * 
	 * @param tool the ItemStack representing the tool to check
	 * @return {@code true} if the tool matches any of the configured valid tool
	 *         tags,
	 *         {@code false} otherwise
	 * @see AutoReplanterConfig#validToolTags
	 */
	private boolean isValidTool(ItemStack tool) {
		return validToolTags.stream().anyMatch(tool::isIn);
	}

	/**
	 * Parses a string representation of an item tag into a TagKey object.
	 * <p>
	 * Supports both simple tags ("namespace:path") and complex tags with
	 * categories ("namespace:category/subcategory").
	 * </p>
	 * 
	 * @param tagString the string representation of the tag (e.g., "minecraft:hoes"
	 *                  or "farmersdelight:tools/knives")
	 * @return a TagKey object representing the parsed tag
	 * @throws IllegalArgumentException if the tag string format is invalid
	 */
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
