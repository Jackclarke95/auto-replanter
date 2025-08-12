package net.jackclarke95.autoreplanter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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

	/** Set of tool ID strings that are considered valid for auto-replanting. */
	private Set<String> validToolIds;

	// When building the map
	private Map<String, CustomBlockReplacementEntry> customReplacementMap;

	// Helper class for fast lookup
	private static class CustomBlockReplacementEntry {
		public final Block replacementBlock;
		public final Item replacementItem;
		public final boolean damageTool;

		public CustomBlockReplacementEntry(Block replacementBlock, Item replacementItem, boolean damageTool) {
			this.replacementBlock = replacementBlock;
			this.replacementItem = replacementItem;
			this.damageTool = damageTool;
		}
	}

	/**
	 * Initializes the Auto Replanter mod.
	 * <p>
	 * This method is called by Fabric when the mod is loaded. It performs the
	 * following:
	 * <ul>
	 * <li>Loads the configuration from the config file</li>
	 * <li>Parses tool tags from string format to TagKey objects</li>
	 * <li>Stores valid tool IDs as strings for direct comparison</li>
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

		// Store valid tool IDs as strings for direct comparison
		validToolIds = Set.copyOf(config.validTools);

		// Build custom replacement map (target block ID -> entry)
		customReplacementMap = new HashMap<>();
		for (AutoReplanterConfig.CustomBlockReplacement rule : config.customBlockReplacements) {
			Block replacement = Registries.BLOCK.get(Identifier.of(rule.replacement));
			if (replacement != null) {
				customReplacementMap.put(
						rule.target,
						new CustomBlockReplacementEntry(replacement, replacement.asItem(), rule.damageTool));
			}
		}

		PlayerBlockBreakEvents.BEFORE.register((world, player, position, state, blockEntity) -> {
			if (world.isClient || !config.enableAutoReplanting) {
				return true;
			}

			// Check sneak requirements based on configured mode
			if (!isValidSneakRequirements(player)) {
				return true;
			}

			ItemStack mainTool = player.getMainHandStack();

			if (config.requireTool && !isValidTool(mainTool)) {
				return true;
			}

			// Get the block ID string directly from the block being broken
			String blockId = Registries.BLOCK.getId(state.getBlock()).toString();

			if (config.useCustomBlockReplacements && customReplacementMap.containsKey(blockId)) {
				CustomBlockReplacementEntry entry = customReplacementMap.get(blockId);

				handleBlockBreaking(world, player, position, state, blockEntity, mainTool,
						entry.replacementBlock.getDefaultState(), entry.replacementItem, entry.damageTool);

				return false;
			}

			// Crop logic
			Block block = state.getBlock();
			if (!(block instanceof CropBlock cropBlock)) {
				return true;
			}

			Item seedItem = cropBlock.asItem();

			BlockState blockToReplant = cropBlock.withAge(0);

			handleBlockBreaking(world, player, position, state, blockEntity, mainTool, blockToReplant, seedItem,
					isMatureCrop(cropBlock, state));

			return false; // Cancel the default break
		});
	}

	private void handleBlockBreaking(World world, PlayerEntity player, BlockPos position, BlockState state,
			BlockEntity blockEntity, ItemStack mainTool, BlockState blockToReplant, Item itemToReplant,
			boolean shouldDamageTool) {
		// Handle looting including decrementing "seed" drop by 1 to simulate
		// consumption of replanting
		processLoot(world, player, position, state, blockEntity, itemToReplant, mainTool);

		// Replant the crop at age 0 (regardless of maturity)
		world.setBlockState(position, blockToReplant, 3);

		// Damage tools based on config settings
		if (shouldDamageTool) {
			damageTool(player, mainTool);
		}
	}

	/**
	 * Checks if the player's current sneaking state matches the configured sneak
	 * mode.
	 * <p>
	 * Determines whether auto-replanting should proceed based on the player's
	 * sneaking state
	 * and the mod's configured sneak mode (ALWAYS, ONLY_SNEAKING, ONLY_STANDING).
	 * </p>
	 *
	 * @param player The player entity to check.
	 * @return {@code true} if the player's sneak state matches the configuration,
	 *         {@code false} otherwise.
	 */
	private boolean isValidSneakRequirements(PlayerEntity player) {
		boolean playerSneaking = player.isSneaking();

		switch (config.getSneakMode()) {
			case ONLY_SNEAKING:
				return playerSneaking;
			case ONLY_STANDING:
				return !playerSneaking;
			case ALWAYS:
			default:
				// No sneak restriction
				return true;
		}
	}

	/**
	 * Handles the logic for dropping modified loot when a block is broken and
	 * auto-replanting is triggered.
	 * <p>
	 * This method collects the dropped item stacks for the given block state and
	 * decrements one item from the stack matching {@code itemToDecrement}
	 * (typically the seed or replacement item) to simulate using it for replanting.
	 * The modified drops are then spawned in the world.
	 * </p>
	 *
	 * @param world           The world where the block is being broken.
	 * @param player          The player breaking the block.
	 * @param pos             The position of the block.
	 * @param state           The block state of the block being broken.
	 * @param blockEntity     The block entity at the block's position, if any.
	 * @param itemToDecrement The item to decrement from the drops (seed or
	 *                        replacement).
	 * @param mainTool        The tool used to break the block.
	 */
	private void processLoot(World world, PlayerEntity player, BlockPos pos, BlockState state,
			@Nullable BlockEntity blockEntity, Item itemToDecrement, ItemStack mainTool) {
		// Get the dropped stacks manually
		List<ItemStack> droppedStacks = Block.getDroppedStacks(state, (ServerWorld) world, pos, blockEntity,
				player,
				mainTool);

		// Process each dropped stack
		for (ItemStack stack : droppedStacks) {

			if (stack.getItem() == itemToDecrement) {
				// Decrement by 1 if it's the seed/replacement block/item
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

	/**
	 * Damages the player's tool if appropriate, based on configuration and crop
	 * maturity.
	 * <p>
	 * The tool is only damaged if tool damage is enabled, a tool is required, the
	 * tool is damageable,
	 * and the tool is valid. Damage may be restricted to only mature crops.
	 * </p>
	 *
	 * @param player       The player using the tool.
	 * @param mainTool     The tool to potentially damage.
	 * @param shouldDamage Whether the break should trigger damage (e.g., only on
	 *                     mature crops).
	 */
	private void damageTool(PlayerEntity player, ItemStack mainTool) {
		if (config.damageTools && config.requireTool && mainTool.isDamageable()
				&& isValidTool(mainTool)) {
			// Only damage if we should always damage, or if we only damage on mature crops
			// and this is mature
			if (!config.onlyDamageOnMatureCrop) {
				mainTool.damage(1, player, EquipmentSlot.MAINHAND);
			}
		}
	}

	/**
	 * Determines if a crop is fully mature and ready for harvest.
	 *
	 * @param cropBlock The crop block to check.
	 * @param state     The current block state of the crop.
	 * @return {@code true} if the crop is at maximum age, {@code false} otherwise.
	 */
	private boolean isMatureCrop(CropBlock cropBlock, net.minecraft.block.BlockState state) {
		return cropBlock.getAge(state) == cropBlock.getMaxAge();
	}

	/**
	 * Checks if the given tool is valid for auto-replanting based on configured
	 * tool validation settings
	 * or if it has the Auto Replanter enchantment.
	 *
	 * @param tool The ItemStack representing the tool to check.
	 * @return {@code true} if the tool matches any of the configured validation
	 *         criteria or has the Auto Replanter enchantment,
	 *         {@code false} otherwise.
	 */
	private boolean isValidTool(ItemStack tool) {
		if (tool.isEmpty()) {
			return false;
		}

		// Check traditional valid tool criteria
		boolean validByTag = config.useValidToolTags && validToolTags.stream().anyMatch(tool::isIn);

		if (validByTag) {
			return true;
		}

		boolean validByItem = config.useValidTools && validToolIds.contains(
				net.minecraft.registry.Registries.ITEM.getId(tool.getItem()).toString());

		if (validByItem) {
			return true;
		}

		// Check if tool has Auto Replanter enchantment
		boolean hasEnchantment = hasAutoReplanterEnchantment(tool);

		if (hasEnchantment) {
			return true;
		}

		return false;
	}

	/**
	 * Checks if the given tool has the Auto Replanter enchantment.
	 *
	 * @param tool The ItemStack to check for the enchantment.
	 * @return {@code true} if the tool has the Auto Replanter enchantment,
	 *         {@code false} otherwise.
	 */
	private boolean hasAutoReplanterEnchantment(ItemStack tool) {
		if (tool.isEmpty()) {
			return false;
		}

		Identifier AUTO_REPLANT_ENCHANTMENT_ID = Identifier.of("autoreplanter", "auto_replanter");

		// Check enchantments directly by looking through the enchantment map
		return tool.getEnchantments().getEnchantments().stream()
				.anyMatch(enchantment -> {
					return enchantment.matchesId(AUTO_REPLANT_ENCHANTMENT_ID);
				});
	}

	/**
	 * Parses a string representation of an item tag into a TagKey object.
	 * <p>
	 * Supports both simple tags ("namespace:path") and complex tags with categories
	 * ("namespace:category/subcategory").
	 * </p>
	 *
	 * @param tagString The string representation of the tag (e.g., "minecraft:hoes"
	 *                  or "farmersdelight:tools/knives").
	 * @return A TagKey object representing the parsed tag.
	 * @throws IllegalArgumentException if the tag string format is invalid.
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
