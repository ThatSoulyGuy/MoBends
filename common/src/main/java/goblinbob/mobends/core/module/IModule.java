package goblinbob.mobends.core.module;

public interface IModule
{
    void init();

    default void refresh()
    {
        onRefresh();
    }

    void onRefresh();

    /**
     * Factory interface for creating modules.
     */
    interface Factory
    {
        IModule create();
    }
}
