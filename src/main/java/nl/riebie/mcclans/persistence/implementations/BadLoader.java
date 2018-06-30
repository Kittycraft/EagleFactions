package nl.riebie.mcclans.persistence.implementations;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import nl.riebie.mcclans.persistence.FileUtils;
import nl.riebie.mcclans.persistence.exceptions.GetDataVersionFailedException;
import nl.riebie.mcclans.persistence.exceptions.WrappedDataException;
import nl.riebie.mcclans.persistence.interfaces.DataLoader;
import nl.riebie.mcclans.persistence.pojo.VersionPojo;
import nl.riebie.mcclans.persistence.upgrade.interfaces.DataUpgrade;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class BadLoader extends DataLoader
{

    private static final File recentDataFolder = new File(EagleFactions.getPlugin().getDataFolder(), "recent");
    private static final File loadDataFolder = new File(EagleFactions.getPlugin().getDataFolder(), "load");

    private Gson gson = new Gson();

    private JsonReader reader;

    public static boolean recentFilesPresent()
    {
        File dataFile = new File(recentDataFolder, "quickSave.json");

        if (dataFile.exists())
        {
            return true;
        } else
        {
            return false;
        }
    }

    public static boolean loadFilesPresent()
    {
        loadDataFolder.mkdirs();
        File loadClansFile = new File(loadDataFolder, "quickSave.json");
        if (loadClansFile.exists())
        {
            return true;
        } else
        {
            return false;
        }
    }

    @Override
    protected boolean initialize()
    {
        recentDataFolder.mkdirs();
        loadDataFolder.mkdirs();

        File loadClansFile = new File(loadDataFolder, "quickSave.json");

        File clansFile = new File(recentDataFolder, "quickSave.json");

        if (loadFilesPresent())
        {
            EagleFactions.getLogger().info("Found data in 'load' folder. Loading this data instead", true);
            try
            {
                FileUtils.moveFile(loadClansFile, clansFile);

                loadClansFile.delete();
            } catch (Exception e)
            {
                throw new WrappedDataException(e);
            }
        }

        boolean recentFilesPresent = recentFilesPresent();
        if (recentFilesPresent)
        {
            try
            {
                reader = new JsonReader(new FileReader(clansFile));
            } catch (FileNotFoundException e)
            {
                throw new WrappedDataException(e);
            }
        }

        return recentFilesPresent;
    }

    @Override
    protected int getDataVersion()
    {
        try
        {
            File clansFile = new File(recentDataFolder, "quickSave.json");

            JsonReader clansReader = new JsonReader(new FileReader(clansFile));

            VersionPojo data = gson.fromJson(clansReader, VersionPojo.class);

            clansReader.close();

            if (data.dataVersion == -1)
            {
                throw new GetDataVersionFailedException("dataVersion still has the default value of -1");
            }

            return data.dataVersion;
        } catch (IOException e)
        {
            throw new WrappedDataException(e);
        }
    }

    @Override
    protected List<DataUpgrade> getDataUpgrades(List<DataUpgrade> dataUpgrades)
    {
        return dataUpgrades;
    }

    @Override
    protected void loadFactions()
    {

    }

    @Override
    protected void loadGroups()
    {

    }

    @Override
    protected void loadPlayers()
    {

    }

    @Override
    protected void loadRelations()
    {

    }

    @Override
    protected void loadClaims()
    {

    }

    @Override
    public boolean load()
    {
        FactionsCache.getInstance().setFactionsCache(gson.fromJson(reader, new TypeToken<FactionsCache>()
        {
        }.getType()));
        return true;
    }
}
