package net.jackclarke95.autoreplanter;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.block.CropBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.text.Text;

public class AutoReplanter implements ModInitializer {
	private static final TagKey<Item> KNIVES_TAG = TagKey.of(RegistryKeys.ITEM,
			Identifier.of("farmersdelight", "tools/knives"));

	@Override
	public void onInitialize() {
		PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
			if (world.isClient)
				return true;

			ItemStack tool = player.getMainHandStack();

			// Check if we're hitting a crop with a knife
			if (state.getBlock() instanceof CropBlock cropBlock && tool == tool) {
				player.sendMessage(Text.literal("Auto-replanting crop..."), false);

				// Drop loot for the current crop state (whatever age it is)
				Block.dropStacks(state, world, pos, blockEntity, player, tool);

				// Replant the crop at age 0
				world.setBlockState(pos, cropBlock.withAge(0), 3);

				// Damage the tool
				// tool.damage(1, player, p -> p.sendToolBreakStatus(player.getActiveHand()));

				return false; // Cancel the default break
			}

			return true; // Allow normal breaking for non-knife tools
		});
	}
}
