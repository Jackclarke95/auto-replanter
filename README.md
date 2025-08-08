# Auto Replanter

A Minecraft Fabric mod that automatically replants crops when harvested, eliminating the tedious task of manually replanting seeds after breaking crops.

## Features

- **Automatic Crop Replanting**: Crops are instantly replanted at age 0 when broken
- **Configurable Tool Requirements**: Choose whether specific tools are required for auto-replanting
- **Smart Tool Damage**: Tools take durability damage with full enchantment support (such as Unbreaking)
- **Mature vs Immature Handling**: Different behaviour for fully grown vs growing crops
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
  "enableAutoReplanting": true,
  "damageTools": true,
  "requireTool": true,
  "onlyDamageOnMatureCrop": true
}
```

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `validToolTags` | Array | `["minecraft:hoes", "farmersdelight:tools/knives"]` | List of item tags that are considered valid tools for auto-replanting |
| `enableAutoReplanting` | Boolean | `true` | Master switch to enable/disable the mod |
| `damageTools` | Boolean | `true` | Whether tools should take durability damage when used |
| `requireTool` | Boolean | `true` | Whether a valid tool is required for auto-replanting to occur |
| `onlyDamageOnMatureCrop` | Boolean | `true` | Whether tools should only take damage when harvesting mature crops |

### Tool Tags

The `validToolTags` option accepts item tags in the following formats:

- Simple tags: `"minecraft:hoes"`
- Category tags: `"farmersdelight:tools/knives"`

You can add tags from any mod to customise which tools trigger auto-replanting.

## Usage Examples

### Default Behaviour

- Use a hoe or knife to break crops
- Mature crops drop their items (minus one seed)
- Crop is automatically replanted
- Tool takes 1 durability damage

### No Tool Required

Set `"requireTool": false` to enable auto-replanting regardless of what tool is used (or no tool at all).

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
- **Compatible with**: Most farming and agriculture mods, specifically those that extend Minecraft'sa `CropBlock` class.

## Technical Details

- Uses Fabric's `PlayerBlockBreakEvents.BEFORE` to intercept crop breaking
- Respects enchantments like Unbreaking
- Server-side only logic (works on both single-player and multiplayer)
- Automatic config generation and validation

## FAQ

**Q: Does this work on servers?**
A: Yes! The mod works on both single-player and multiplayer servers.

**Q: Can I change the config without restarting?**
A: No, you need to restart Minecraft (or the server) for config changes to take effect.

**Q: Does this work with modded crops?**
A: Yes, as long as the modded crops extend Minecraft's `CropBlock` class, then this should work.

**Q: Will tools with Unbreaking last longer?**
A: Yes! The mod uses Minecraft's built-in durability system, so all enchantments work normally.

## Licence

This project is licensed under the MIT Licence - see the [LICENCE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request on [GitHub](your-github-link-here).

## Support

If you encounter any issues or have suggestions, please:

1. Check the configuration file for proper setup
2. Report bugs on the [GitHub Issues](your-github-link-here) page
3. Join the discussion on [CurseForge](your-curseforge-link-here)

---

**Author**: jackclarke95  
**Version**: 1.0.0  
**Minecraft**: 1.21.1  
**Mod Loader**: Fabric
