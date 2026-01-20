package goblinbob.mobends.core;

import com.mojang.logging.LogUtils;
import goblinbob.mobends.core.configuration.CoreServerConfig;
import org.slf4j.Logger;

import javax.annotation.Nullable;

/**
 * Server-side Core implementation for Mo' Bends 1.20.1.
 */
public class CoreServer extends Core<CoreServerConfig>
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static CoreServer INSTANCE;

    private CoreServerConfig configuration;

    CoreServer()
    {
        INSTANCE = this;
        this.configuration = new CoreServerConfig();
    }

    @Override
    public CoreServerConfig getConfiguration()
    {
        return configuration;
    }

    @Override
    public void onCommonSetup()
    {
        super.onCommonSetup();
        LOGGER.info("Mo' Bends server core initialized");
    }

    @Nullable
    public static CoreServer getInstance()
    {
        return INSTANCE;
    }
}
