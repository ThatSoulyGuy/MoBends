package goblinbob.mobends.api.resource;

public interface IResourcePath
{
    String getNamespace();

    String getPath();

    default String asString()
    {
        return getNamespace() + ":" + getPath();
    }

    IResourcePath withPath(String path);

    Object getNative();
}
