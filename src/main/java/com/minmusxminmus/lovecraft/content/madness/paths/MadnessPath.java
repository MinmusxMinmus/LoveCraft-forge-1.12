package com.minmusxminmus.lovecraft.content.madness.paths;

import com.minmusxminmus.lovecraft.LoveCraft;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public abstract class MadnessPath extends IForgeRegistryEntry.Impl<MadnessPath> {
    public static IForgeRegistry<MadnessPath> MADNESS_REGISTRY = null;

    /**
     * The path's unique identifier string. Usually follows the format "pathXXXX", with XXXX being the identifying word.
     */
    public abstract String getIdentifier();

    /**
     * Convenience method to retrieve LoveCraft's default paths.
     */
    public static Collection<MadnessPath> getDefaultPaths() {
        Collection<MadnessPath> paths = new HashSet<>();
        Collections.addAll(paths, new MadnessPathDeep(), new MadnessPathDream(), new MadnessPathLore(), new MadnessPathScience());
        return paths;
    }

    /**
     * Returns all available madness paths. If no madness path registry is found, it will instead return the default paths.
     */
    public static Collection<MadnessPath> getAllPaths() {
        if (MADNESS_REGISTRY == null) {
            LoveCraft.LOGGER.warn("Unable to find Madness path registry. No alternative paths will be available.");
            return getDefaultPaths();
        }
        return MADNESS_REGISTRY.getValuesCollection();
    }


}
