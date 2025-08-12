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
import net.minecraft.text.Text;

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
	private Map<String, Block> customReplacementMap;

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

		// Build custom replacement map
		customReplacementMap = new HashMap<>();

		for (AutoReplanterConfig.CustomBlockReplacement rule : config.customBlockReplacements) {
			Block replacement = Registries.BLOCK.get(Identifier.of(rule.replacement));

			for (String targetId : rule.targets) {
				if (replacement != null) {
					customReplacementMap.put(targetId, replacement);
				}
			}
		}

		PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
			if (world.isClient || !config.enableAutoReplanting) {
				return true;
			}

			// Get the block ID string directly from the block being broken
			String blockId = Registries.BLOCK.getId(state.getBlock()).toString();

			// Log for debugging
			player.sendMessage(Text.literal("Broken block: " + blockId), false);

			// Check sneak requirements based on configured mode
			if (!isValidSneakRequirements(player)) {
				return true;
			}

			ItemStack mainTool = player.getMainHandStack();
			if (config.requireTool && !isValidTool(mainTool)) {
				return true;
			}

			// Directly compare blockId string to config keys
			if (customReplacementMap.containsKey(blockId)) {
				Block replacement = customReplacementMap.get(blockId);

				// Drop the block's loot as normal
				List<ItemStack> droppedStacks = Block.getDroppedStacks(state, (ServerWorld) world, pos, blockEntity,
						player, mainTool);

				for (ItemStack stack : droppedStacks) {
					if (!stack.isEmpty()) {
						ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5,
								pos.getZ() + 0.5, stack);
						world.spawnEntity(itemEntity);
					}
				}

				// Replace with the configured block
				world.setBlockState(pos, replacement.getDefaultState(), 3);

				// Optionally damage tool (always, since "maturity" doesn't apply)
				damageTool(player, mainTool, true);

				return false;
			}

			// Crop logic
			Block block = state.getBlock();
			if (!(block instanceof CropBlock cropBlock)) {
				return true;
			}

			boolean isMature = isMatureCrop(cropBlock, state);

			processLoot(world, player, pos, state, blockEntity, cropBlock, mainTool, isMature);

			// Replant the crop at age 0 (regardless of maturity)
			world.setBlockState(pos, cropBlock.withAge(0), 3);

			// Damage tools based on config settings
			damageTool(player, mainTool, isMature);

			return false; // Cancel the default break
		});
	}

	/**
	 * Checks if the player's current sneaking state matches the configured sneak
	 * mode.
	 * <p>
	 * This determines whether auto-replanting should proceed based on whether the
	 * player
	 * is sneaking, standing, or if sneaking is ignored.
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
	 * Handles the logic for dropping modified loot when a crop
	 * is broken.
	 * <p>
	 * If the crop is mature, this method drops the appropriate items (with one seed
	 * removed for replanting)
	 * and spawns them in the world.
	 * </p>
	 *
	 * @param world       The world where the crop is being broken.
	 * @param player      The player breaking the crop.
	 * @param pos         The position of the crop block.
	 * @param state       The block state of the crop.
	 * @param blockEntity The block entity at the crop's position, if any.
	 * @param cropBlock   The crop block being broken.
	 * @param mainTool    The tool used to break the crop.
	 * @param isMature    Whether the crop is fully grown.
	 */
	private void processLoot(World world, PlayerEntity player, BlockPos pos, BlockState state,
			@Nullable BlockEntity blockEntity, CropBlock cropBlock, ItemStack mainTool, boolean isMature) {
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
	 * @param shouldDamage Whether the break should trigger damage.
	 */
	private void damageTool(PlayerEntity player, ItemStack mainTool, boolean shouldDamage) {
		if (config.damageTools && config.requireTool && mainTool.isDamageable()
				&& isValidTool(mainTool)) {
			// Only damage if we should always damage, or if we only damage on mature crops
			// and this is mature
			if (!config.onlyDamageOnMatureCrop || shouldDamage) {
				mainTool.damage(1, player, EquipmentSlot.MAINHAND);
			}
		}
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
	 * tool validation settings or if it has the Auto Replanter enchantment.
	 * 
	 * @param tool the ItemStack representing the tool to check
	 * @return {@code true} if the tool matches any of the configured validation
	 *         criteria or has the Auto Replanter enchantment,
	 *         {@code false} otherwise
	 * @see AutoReplanterConfig#validToolTags
	 * @see AutoReplanterConfig#validTools
	 * @see AutoReplanterConfig#useValidToolTags
	 * @see AutoReplanterConfig#useValidTools
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
	 * @param tool the ItemStack to check for the enchantment
	 * @return {@code true} if the tool has the Auto Replanter enchantment,
	 *         {@code false} otherwise
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
