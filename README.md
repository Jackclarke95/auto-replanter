# Auto Replanter

A Minecraft Fabric mod that automatically replants crops when harvested, eliminating the tedious task of manually replanting seeds after breaking crops.

## Demo

![Auto Replanter Showcase](Auto%20Replanter%20Showcase.gif)

## Features

- **Automatic Crop Replanting**: Crops are instantly replanted at age 0 when broken
- **Dual Tool Validation System**: Choose between tag-based and specific item-based tool requirements
- **Configurable Tool Requirements**: Choose whether specific tools are required for auto-replanting
- **Smart Tool Damage**: Tools take durability damage with full enchantment support (such as Unbreaking)
- **Sneak Mode Control**: Configure auto-replanting to work always, only while sneaking, or only while standing
- **Highly Configurable**: Extensive configuration options to customise behaviour
- **Mod Compatibility**: Works with any modded crops that extend Minecraft's `CropBlock`
- **Companion Enchantment**: Optional companion [enchantment](https://modrinth.com/datapack/auto-replanter-enchantment) available for adding auto replanting as an enchantment

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/)
2. Download the mod jar
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
  "sneakMode": "ALWAYS",
  "customBlockReplacements": [
    {
      "target": "farmersdelight:brown_mushroom_colony",
      "replacement": "minecraft:brown_mushroom",
      "damageTool": true
    },
    {
      "target": "farmersdelight:red_mushroom_colony",
      "replacement": "minecraft:red_mushroom",
      "damageTool": true
    }
  ],
  "useCustomBlockReplacements": true
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
| `customBlockReplacements` | Array | See below | List of custom block replacement rules for non-crop blocks |
| `useCustomBlockReplacements` | Boolean | `true` | Whether to enable custom block replacement functionality |

### Custom Block Replacements

The mod supports custom block replacement rules that allow you to automatically replace non-crop blocks with other blocks when broken. This is useful for blocks like mushroom colonies that should replant a mushroom when harvested.

Each custom block replacement rule has the following structure:

```json
{
  "target": "farmersdelight:brown_mushroom_colony",
  "replacement": "minecraft:brown_mushroom", 
  "damageTool": true
}
```

| Property | Type | Description |
|----------|------|-------------|
| `target` | String | The block ID to be replaced (what you're breaking) |
| `replacement` | String | The block ID to place after breaking |
| `damageTool` | Boolean | Whether breaking this specific block should damage your tool |

**Example Configuration:**

```json
"customBlockReplacements": [
  {
    "target": "farmersdelight:brown_mushroom_colony",
    "replacement": "minecraft:brown_mushroom",
    "damageTool": true
  },
  {
    "target": "farmersdelight:red_mushroom_colony", 
    "replacement": "minecraft:red_mushroom",
    "damageTool": false
  }
]
```

**How It Works:**

- When you break a `farmersdelight:brown_mushroom_colony` with a valid tool, it will automatically place a `minecraft:brown_mushroom` in its place
- The original block's drops are spawned, minus one of the replacement item (to simulate using it for replanting)
- Tool damage is applied based on the `damageTool` setting for each rule
- All other configuration options (tool requirements, sneak mode, etc.) still apply

**Notes:**

- Custom block replacements work independently of crop blocks
- The `useCustomBlockReplacements` setting must be `true` for this feature to work
- Block IDs must be valid and exist in your game (including mod blocks)
- If the replacement block ID is invalid, that rule will be ignored

## Auto Replanter Enchantment Support

There is an additional, optional datapack to add enchantment support. If a tool has the **Auto Replanter Enchantment** (regardless of its type or whether it matches your configured tags or item lists), it will always be considered a valid tool for auto-replanting. This means you can enchant any tool with the Auto Replanter enchantment and it will work with the mod, even if it is not listed in `validToolTags` or `validTools`. The tools that can be enchanted are defined in the Auto Replanter Enchantment data pack.

If this datapack is enabled, the mod will automatically detect it with no config required. To enforce the enchantmentand ignore any valid tools as defined using the config settings described above, simply set `useValidToolTags` and `useValidTools` to `false` in the config.

The Auto Replanter Enchantment datapack is available on [Modrinth](https://modrinth.com/project/auto-replanter-enchantment)

**Note:**  

The other configuration settings—such as `requireTool`, `sneakMode`, and tool durability—still apply.  

- If `requireTool` is set to `false`, any tool (or even no tool) will work, regardless of enchantment.  

- If `requireTool` is `true`, a tool with the Auto Replanter enchantment will always be valid, even if it doesn't match your tags or item lists.  

- The sneak mode and tool damage settings are still respected.

This allows for more flexibility: you can give the enchantment to custom tools, modded tools, or even vanilla tools not normally allowed by your config, and they will work seamlessly with auto-replanting.

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

### Custom Block Harvesting

Use custom block replacements to automatically replant modded blocks like mushroom colonies:

```json
"customBlockReplacements": [
  {
    "target": "farmersdelight:brown_mushroom_colony",
    "replacement": "minecraft:brown_mushroom",
    "damageTool": true
  }
]
```

Using the default values, when you break a brown mushroom colony from Farmer's Delight (having met the valid criteria to trigger automatic replanting), it will automatically place a brown mushroom and drop the colony's normal loot (minus one brown mushroom used for replanting).

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

### **Q: Can I use custom block replacements with any modded blocks?**

A: Yes, as long as you specify valid block IDs for both the target and replacement blocks. The blocks must exist in your game for the replacement to work.

### **Q: Do custom block replacements work with crops?**

A: Custom block replacements are designed for non-crop blocks. Regular crops are handled by the mod's built-in crop replanting system. If a crop block is also defined in the custom block replacement config, crop breaking logic will take precedence.

### **Q: Can I disable tool damage for specific custom blocks?**

A: Yes! Set `"damageTool": false` in the custom block replacement rule to prevent tool damage for that specific block. Note that crop blocks take precedence over custom blocks, so if a crop block is defined in the custom blocks, its `damageTool` value will be ignored in favour of the crop block logic.

## Licence

This project is licensed under the Creative Commons Licence - see the [LICENCE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request on [GitHub](https://github.com/Jackclarke95/auto-replanter?tab=contributing-ov-file).

## Support

If you encounter any issues or have suggestions, please:

1. Check the configuration file for proper setup
2. Ensure the correct validation booleans (`useValidToolTags` and `useValidTools`) are enabled
3. Verify your `sneakMode` setting is one of the valid options
4. Report bugs on the [GitHub Issues](https://github.com/Jackclarke95/auto-replanter/issues) page

---

**Author**: jackclarke95  
**Version**: 1.2.0  
**Minecraft**: 1.21.1  
**Mod Loader**: Fabric
