package net.jackclarke95.autoreplanter;

import java.util.List;

/**
 * Configuration class for the Auto Replanter mod.
 * <p>
 * This class contains all the configurable options that control the behavior
 * of automatic crop replanting. The configuration is loaded from and saved to
 * a JSON file in the Minecraft config directory.
 * </p>
 * 
 * @author jackclarke95
 * @since 1.0.0
 */
public class AutoReplanterConfig {
    /**
     * List of item tags that are considered valid tools for automatic replanting.
     * <p>
     * Tags should be specified in the format "namespace:path" (e.g.,
     * "minecraft:hoes")
     * or "namespace:category/subcategory" (e.g., "farmersdelight:tools/knives").
     * </p>
     * 
     * @see #requireTool
     */
    public List<String> validToolTags = List.of(
            "minecraft:hoes",
            "farmersdelight:tools/knives");

    /**
     * Whether automatic replanting is enabled.
     * <p>
     * When set to {@code false}, the mod will restore vanilla harvesting behavior
     * and crops will not be automatically replanted.
     * </p>
     * 
     * @default true
     */
    public boolean enableAutoReplanting = true;

    /**
     * Whether tools should take durability damage when used for automatic
     * replanting.
     * <p>
     * This setting only applies when {@link #requireTool} is {@code true} and
     * a valid tool is being used. The actual damage behavior can be further
     * controlled by {@link #onlyDamageOnMatureCrop}.
     * </p>
     * 
     * @default true
     * @see #requireTool
     * @see #onlyDamageOnMatureCrop
     */
    public boolean damageTools = true;

    /**
     * Whether a valid tool is required for automatic replanting to occur.
     * <p>
     * When set to {@code true}, crops will only be automatically replanted
     * when broken with a tool that matches one of the {@link #validToolTags}.
     * When set to {@code false}, crops will be replanted regardless of the
     * tool used (or if no tool is used at all).
     * </p>
     * 
     * @default true
     * @see #validToolTags
     * @see #damageTools
     */
    public boolean requireTool = true;

    /**
     * Whether tools should only take damage when harvesting mature crops.
     * <p>
     * When set to {@code true}, tools will only lose durability when breaking
     * crops that are fully grown and would normally drop items. When set to
     * {@code false}, tools will take damage every time a crop is broken and
     * replanted, regardless of the crop's growth stage.
     * </p>
     * <p>
     * This setting has no effect if {@link #requireTool} is {@code false},
     * as tool damage only applies when valid tools are required.
     * </p>
     * 
     * @default true
     * @see #damageTools
     * @see #requireTool
     */
    public boolean onlyDamageOnMatureCrop = true;
}