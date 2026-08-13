package goblinbob.mobends.lib.flux;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class ObservableTest
{
    @Test
    public void initialValue()
    {
        Observable<Integer> obs = new Observable<>(42);
        assertEquals(42, obs.getValue());
    }

    @Test
    public void setValue()
    {
        Observable<String> obs = new Observable<>("initial");
        obs.next("updated");
        assertEquals("updated", obs.getValue());
    }

    @Test
    public void subscribeNotifiesOnChange()
    {
        Observable<Integer> obs = new Observable<>(0);
        AtomicInteger notifyCount = new AtomicInteger(0);

        obs.subscribe(value -> notifyCount.incrementAndGet());

        obs.next(1);
        assertEquals(1, notifyCount.get());

        obs.next(2);
        assertEquals(2, notifyCount.get());
    }

    @Test
    public void subscribeReceivesNewValue()
    {
        Observable<String> obs = new Observable<>("initial");
        AtomicInteger callCount = new AtomicInteger(0);
        String[] receivedValue = new String[1];

        obs.subscribe(value -> {
            callCount.incrementAndGet();
            receivedValue[0] = value;
        });

        obs.next("updated");
        assertEquals("updated", receivedValue[0]);
    }

    @Test
    public void unsubscribeStopsNotifications()
    {
        Observable<Integer> obs = new Observable<>(0);
        AtomicInteger notifyCount = new AtomicInteger(0);

        Subscription<Integer> sub = obs.subscribe(value -> notifyCount.incrementAndGet());

        obs.next(1);
        assertEquals(1, notifyCount.get());

        sub.unsubscribe();

        obs.next(2);
        assertEquals(1, notifyCount.get());
    }

    @Test
    public void multipleSubscribers()
    {
        Observable<Integer> obs = new Observable<>(0);
        AtomicInteger count1 = new AtomicInteger(0);
        AtomicInteger count2 = new AtomicInteger(0);

        obs.subscribe(value -> count1.incrementAndGet());
        obs.subscribe(value -> count2.incrementAndGet());

        obs.next(1);
        assertEquals(1, count1.get());
        assertEquals(1, count2.get());
    }

    @Test
    public void nullValue()
    {
        Observable<String> obs = new Observable<>(null);
        assertNull(obs.getValue());

        obs.next("value");
        assertEquals("value", obs.getValue());

        obs.next(null);
        assertNull(obs.getValue());
    }
}
