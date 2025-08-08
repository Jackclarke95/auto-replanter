# Auto Replanter

A Minecraft Fabric mod that automatically replants crops when harvested, eliminating the tedious task of manually replanting seeds after breaking crops.

## Demo

![Auto Replanter Showcase](Auto%20Replanter%20Showcase.gif)

## Features

- **Automatic Crop Replanting**: Crops are instantly replanted at age 0 when broken
- **Dual Tool Validation System**: Choose between tag-based and specific item-based tool requirements
- **Configurable Tool Requirements**: Choose whether specific tools are required for auto-replanting
- **Smart Tool Damage**: Tools take durability damage with full enchantment support (such as Unbreaking)
- **Mature vs Immature Handling**: Different behaviour for fully grown vs growing crops
- **Sneak Mode Control**: Configure auto-replanting to work always, only while sneaking, or only while standing
- **Highly Configurable**: Extensive configuration options to customise behaviour
- **Mod Compatibility**: Works with any modded crops that extend Minecraft's `CropBlock`

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/)
2. Download the mod jar from [CurseForge](your-curseforge-link-here)
3. Place the jar file in your `mods` folder
4. Launch the game

## Configuration

The mod creates a configuration file at `config/autoreplanter.json` on first run. You can edit this file to customise the mod's behaviour.

### Configuration Options

```json
{
  "validToolTags": ["minecraft:hoes", "farmersdelight:tools/knives"],
  "validTools": ["minecraft:diamond_hoe", "farmersdelight:flint_knife"],
  "enableAutoReplanting": true,
  "damageTools": true,
  "requireTool": true,
  "useValidToolTags": true,
  "useValidTools": false,
  "onlyDamageOnMatureCrop": true,
  "sneakMode": "ALWAYS"
}
```

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `validToolTags` | Array | `["minecraft:hoes", "farmersdelight:tools/knives"]` | List of item tags that are considered valid tools for auto-replanting. **Only takes effect when `useValidToolTags` is `true`** |
| `validTools` | Array | `["minecraft:diamond_hoe", "farmersdelight:flint_knife"]` | List of specific item IDs that are considered valid tools for auto-replanting. **Only takes effect when `useValidTools` is `true`** |
| `enableAutoReplanting` | Boolean | `true` | Master switch to enable/disable the mod |
| `damageTools` | Boolean | `true` | Whether tools should take durability damage when used |
| `requireTool` | Boolean | `true` | Whether a valid tool is required for auto-replanting to occur |
| `useValidToolTags` | Boolean | `true` | Whether to use tag-based tool validation. When `false`, the `validToolTags` list is ignored |
| `useValidTools` | Boolean | `false` | Whether to use specific item-based tool validation. When `false`, the `validTools` list is ignored |
| `onlyDamageOnMatureCrop` | Boolean | `true` | Whether tools should only take damage when harvesting mature crops |
| `sneakMode` | String | `"ALWAYS"` | Controls when auto-replanting occurs based on sneak state. Options: `"ALWAYS"`, `"ONLY_SNEAKING"`, `"ONLY_STANDING"` |

### Sneak Mode Options

The `sneakMode` setting controls when auto-replanting should occur based on whether the player is sneaking:

- **`"ALWAYS"`** (default): Auto-replanting works regardless of sneak state
- **`"ONLY_SNEAKING"`**: Auto-replanting only works while the player is sneaking
- **`"ONLY_STANDING"`**: Auto-replanting only works while the player is NOT sneaking

### Tool Validation Systems

The mod offers two complementary tool validation systems that can be used independently or together:

#### Tag-Based Validation (`validToolTags`)

The `validToolTags` option accepts item tags in the following formats:

- Simple tags: `"minecraft:hoes"`
- Category tags: `"farmersdelight:tools/knives"`

You can add tags from any mod to customise which tools trigger auto-replanting. **This list only takes effect when `useValidToolTags` is set to `true`.**

#### Specific Item Validation (`validTools`)

The `validTools` option accepts specific item IDs in the format:

- `"namespace:item_id"` (e.g., `"minecraft:diamond_hoe"`, `"farmersdelight:flint_knife"`)

This allows you to specify exact tools that should work with auto-replanting. **This list only takes effect when `useValidTools` is set to `true`.**

#### Combining Both Systems

You can use both validation systems simultaneously by setting both `useValidToolTags` and `useValidTools` to `true`. In this case, a tool is considered valid if it matches **either** the tag-based criteria **or** the specific item criteria.

## Usage Examples

### Default Behaviour

- Use a hoe (from the `minecraft:hoes` tag) or knife (from the `farmersdelight:tools/knives` tag)
- Works regardless of whether you're sneaking
- Mature crops drop their items (minus one seed)
- Crop is automatically replanted
- Tool takes 1 durability damage

### Sneak-Only Mode

Set `"sneakMode": "ONLY_SNEAKING"` to only auto-replant while sneaking. This gives you precise control over when crops are replanted, preventing accidental replanting when walking through your farm.

### No-Sneak Mode

Set `"sneakMode": "ONLY_STANDING"` to only auto-replant while not sneaking. This allows you to disable auto-replanting by holding sneak, useful when you want to manually break crops without replanting (e.g., when relocating your farm).

### Tag-Based Tools Only

Set `"useValidTools": false` to only use tag-based validation. The `validTools` list will be ignored.

### Specific Tools Only

Set `"useValidToolTags": false` to only use specific item validation. The `validToolTags` list will be ignored.

### No Tool Required

Set `"requireTool": false` to enable auto-replanting regardless of what tool is used (or no tool at all). When this is disabled, both validation systems are bypassed.

### Preserve Tool Durability

Set `"damageTools": false` to prevent tools from taking any durability damage.

### Only Damage on Harvest

Set `"onlyDamageOnMatureCrop": true` (default) to only damage tools when actually harvesting mature crops, not when replanting immature ones. This saves needless tool durability loss when accidentally breaking crops that are not fully grown when they are automatically replanted.

## Supported Crops

The mod works with:

- All vanilla Minecraft crops (wheat, carrots, potatoes, beetroot, etc.)
- Any modded crops that extend Minecraft's `CropBlock` class
- This includes crops from popular mods like Farmer's Delight (tested and working), and should work for other mods such as Croptopia, and many others, though these have not been tested.

## Compatibility

- **Minecraft Version**: 1.21.1
- **Mod Loader**: Fabric
- **Dependencies**: Fabric API
- **Compatible with**: Most farming and agriculture mods, specifically those that extend Minecraft's `CropBlock` class.

## Technical Details

- Uses Fabric's `PlayerBlockBreakEvents.BEFORE` to intercept crop breaking
- Respects enchantments like Unbreaking
- Server-side only logic (works on both single-player and multiplayer)
- Automatic config generation and validation
- Dual tool validation system supports both tag-based and item-specific matching

## FAQ

### **Q: Does this work on servers?**

A: Yes! The mod works on both single-player and multiplayer servers.

### **Q: Can I change the config without restarting?**

A: No, you need to restart Minecraft (or the server) for config changes to take effect.

### **Q: Does this work with modded crops?**

A: Yes, as long as the modded crops extend Minecraft's `CropBlock` class, then this should work.

### **Q: Will tools with Unbreaking last longer?**

A: Yes! The mod uses Minecraft's built-in durability system, so all enchantments work normally.

### **Q: I added items to `validTools` but they don't work. Why?**

A: Make sure `useValidTools` is set to `true` in your config. If it's `false`, the `validTools` list is ignored.

### **Q: Can I use both tag-based and specific item validation?**

A: Yes! Set both `useValidToolTags` and `useValidTools` to `true`. A tool will be considered valid if it matches either system.

### **Q: How do I disable one of the validation systems?**

A: Set either `useValidToolTags` or `useValidTools` to `false` to disable that respective validation system. The corresponding list (`validToolTags` or `validTools`) will then be ignored.

### **Q: What's the difference between the sneak modes?**

A: `"ALWAYS"` works regardless of sneaking, `"ONLY_SNEAKING"` only works while sneaking (for precise control), and `"ONLY_STANDING"` only works while not sneaking (so you can disable it by sneaking).

### **Q: Can I set an invalid sneak mode?**

A: If you set an invalid sneak mode in the config, the mod will fall back to `"ALWAYS"` behavior.

## Licence

This project is licensed under the Creative Commons Licence - see the [LICENCE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request on [GitHub](https://github.com/Jackclarke95/auto-replanter-1.21.1).

## Support

If you encounter any issues or have suggestions, please:

1. Check the configuration file for proper setup
2. Ensure the correct validation booleans (`useValidToolTags` and `useValidTools`) are enabled
3. Verify your `sneakMode` setting is one of the valid options
4. Report bugs on the [GitHub Issues](https://github.com/Jackclarke95/auto-replanter-1.21.1/issues) page
5. Join the discussion on [CurseForge](your-curseforge-link-here)

---

**Author**: jackclarke95  
**Version**: 1.1.0  
**Minecraft**: 1.21.1  
**Mod Loader**: Fabric
