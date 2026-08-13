package goblinbob.mobends.lib.flux;

@FunctionalInterface
public interface IObserver<T>
{

    void onChanged(T newValue);

}
