package net.minecraft.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.SERVER)
public class ServerEula
{
    private static final Logger LOG = LogManager.getLogger();
    private final File eulaFile;
    private final boolean acceptedEULA;

    public ServerEula(File eulaFile)
    {
        this.eulaFile = eulaFile;
        this.acceptedEULA = loadEULAFile(eulaFile);
    }

    private boolean loadEULAFile(File inFile)
    {
        FileInputStream fileinputstream = null;
        boolean flag = true;

        try
        {
            Properties properties = new Properties();
            fileinputstream = new FileInputStream(inFile);
            properties.load(fileinputstream);
            flag = Boolean.parseBoolean(properties.getProperty("eula", "false"));
        }
        catch (Exception var8)
        {
            LOG.warn("Failed to load {}", (Object)inFile);
            this.createEULAFile();
        }
        finally
        {
            IOUtils.closeQuietly((InputStream)fileinputstream);
        }

        return flag;
    }

    public boolean hasAcceptedEULA()
    {
        return this.acceptedEULA;
    }

    public void createEULAFile()
    {
        FileOutputStream fileoutputstream = null;

        try
        {
            Properties properties = new Properties();
            fileoutputstream = new FileOutputStream(this.eulaFile);
            properties.setProperty("eula", "false");
            properties.store(fileoutputstream, "By changing the setting below to TRUE you are indicating your agreement to our EULA (https://account.mojang.com/documents/minecraft_eula).\n\u5c06\u201cEULA\u201d\u7684\u503c\u4fee\u6539\u4e3a\u201cTRUE\u201d\u5373\u4ee3\u8868\u60a8\u540c\u610fEULA\uff0c\u4e4b\u540e\u60a8\u53ef\u4ee5\u6b63\u5e38\u542f\u52a8\u670d\u52a1\u5668\u3002");
        }
        catch (Exception exception)
        {
            LOG.warn("Failed to save {}", this.eulaFile, exception);
        }
        finally
        {
            IOUtils.closeQuietly((OutputStream)fileoutputstream);
        }
    }
}