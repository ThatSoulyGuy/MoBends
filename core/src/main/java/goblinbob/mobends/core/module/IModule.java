package goblinbob.mobends.core.module;

public interface IModule
{
    void init();

    default void refresh()
    {
        onRefresh();
    }

    void onRefresh();
}
