package net.jackclarke95.autoreplanter;

import java.util.List;

public class AutoReplanterConfig {
    public List<String> validToolTags = List.of(
            "farmersdelight:tools/knives",
            "minecraft:hoes");

    public boolean enableAutoReplanting = true; // Set to false to restore vanilla harvesting behaviour
    public boolean damageTools = true; // Whether the tool should be damaged on use
    public boolean requireTool = true; // Whether a valid tool is required for automatic replanting
}