package goblinbob.mobends.api.resource;

public interface ILocalization
{
    String get(String key);

    String get(String key, Object... args);

    boolean has(String key);
}
