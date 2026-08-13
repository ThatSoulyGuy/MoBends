package goblinbob.mobends.lib.flux;

import java.util.HashSet;
import java.util.Set;

public class Computed<T> implements IObservable<T>, IObserver
{

    private boolean dirty = true;
    private T value;
    private IComputedExpression<T> expression;
    private Set<IObserver<T>> observers;

    public Computed(IComputedExpression<T> expression)
    {
        this.expression = expression;
        this.observers = new HashSet<>();
        ComputedDependencyHelper.dirtyComputedSet.add(this);
    }

    @Override
    public T getValue()
    {
        ComputedDependencyHelper.linkDependency(this);

        if (dirty)
        {
            ComputedDependencyHelper.evaluatedStack.push(this);

            T newValue = expression.compute();

            for (IObserver<T> observer : observers)
            {
                observer.onChanged(newValue);
            }

            value = newValue;
            dirty = false;

            ComputedDependencyHelper.evaluatedStack.pop();
        }
        return value;
    }

    @Override
    public Set<IObserver<T>> getObservers()
    {
        return observers;
    }

    @Override
    public void onChanged(Object newValue)
    {
        dirty = true;
        ComputedDependencyHelper.dirtyComputedSet.add(this);
    }

}
