package me.staticjava;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.data.DataException;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by StaticJava.
 */
public class SchematicLoader extends JavaPlugin {

    public WorldEditPlugin worldEditPlugin = null;
    public MultiverseCore mvCore = null;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        String worldName = getConfig().getString("world.name");
        File currentDirectory = new File(new File(".").getAbsolutePath());
        Path mainPath = null;

        try {
            mainPath = Paths.get(currentDirectory.getCanonicalPath(), getConfig().getString("dir"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        World.Environment environment = World.Environment.valueOf(getConfig().getString("world.environment").toUpperCase());
        WorldType worldType = WorldType.getByName(getConfig().getString("world.type").toUpperCase());

        worldEditPlugin = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        mvCore = (MultiverseCore) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core");

        if (worldEditPlugin == null) {
            getLogger().severe("WorldEdit is not installed!");
            Bukkit.getServer().getPluginManager().disablePlugin(this);
        }

        if (mvCore == null) {
            getLogger().severe("Multiverse-Core is not installed!");
            Bukkit.getServer().getPluginManager().disablePlugin(this);
        }

        mvCore.getMVWorldManager().addWorld(worldName, environment, null, worldType, false, null);

        for (String key : getConfig().getConfigurationSection("schematics").getKeys(false)) {
            List<String> locs = getConfig().getStringList("lobbySpawns." + key);
            double x = Double.parseDouble(locs.get(0).split(":")[1]);
            double y = Double.parseDouble(locs.get(1).split(":")[1]);
            double z = Double.parseDouble(locs.get(2).split(":")[1]);
            org.bukkit.util.Vector vector = new org.bukkit.util.Vector(x, y, z);

            Path path = Paths.get(mainPath.toString(), getConfig().getString("schematics." + key));
            try {
                loadArea(Bukkit.getWorld(worldName), path.toFile(), BukkitUtil.toVector(vector));
            } catch (DataException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (MaxChangedBlocksException e) {
                e.printStackTrace();
            }
        }

        getLogger().info("Loaded all schematics.");
        getLogger().info("SchematicLoader has been enabled.");
    }

    @Override
    public void onDisable() {
        saveConfig();
        mvCore.getMVWorldManager().deleteWorld(getConfig().getString("world.name"));
        getLogger().info("SchematicLoader has been disabled.");
    }

    public void loadArea(World world, File file, Vector origin) throws DataException, IOException, MaxChangedBlocksException {
        EditSession es = new EditSession(new BukkitWorld(world), 999999999);
        CuboidClipboard cc = CuboidClipboard.loadSchematic(file);
        cc.paste(es, origin, false);
    }
}
