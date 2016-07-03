package accounting;

import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class AccountingObjectImplTest {

    @Test(expected = NullPointerException.class)
    public void testConstructor() throws Exception {
        new AccountingObjectImpl(null);
    }

    @Test
    public void testWithoutEvents() throws Exception {
        AccountingObject obj = new AccountingObjectImpl();
        assertEquals(obj.lastMinuteCount(), 0);
        assertEquals(obj.lastHourCount(), 0);
        assertEquals(obj.lastDayCount(), 0);
    }

    @Test
    public void testWithManyThreads() throws Exception {
        int threadNumber = 5;
        int eventsPerThread = 1_000;
        AccountingObject obj = new AccountingObjectImpl();
        IntStream.range(0, threadNumber).mapToObj(i -> new Thread(() ->
                IntStream.range(0, eventsPerThread).forEach(k -> obj.registerEvent())
        )).map(t -> {
            t.start();
            return t;
        }).forEach(this::joinThread);
        long expected = threadNumber * eventsPerThread;
        assertCounts(obj, expected, expected, expected);
    }

    @Test
    public void testForDifferentPeriods() throws Exception {
        AccountingObjectImpl obj = new AccountingObjectImpl();
        Instant now = Instant.now();
        Instant yesterday = now.minus(1, ChronoUnit.DAYS);

        obj.registerEvent(yesterday.toEpochMilli());
        assertCounts(obj, 0, 0, 0);

        Instant today = yesterday.plusSeconds(1);
        obj.registerEvent(today.toEpochMilli());
        assertCounts(obj, 0, 0, 1);

        Instant lastHour = now.minus(30, ChronoUnit.MINUTES);
        obj.registerEvent(lastHour.toEpochMilli());
        assertCounts(obj, 0, 1, 2);

        Instant lastMinute = now.minusSeconds(10);
        obj.registerEvent(lastMinute.toEpochMilli());
        assertCounts(obj, 1, 2, 3);
    }

    private void assertCounts(AccountingObject obj, long expectedLastMinute,
                              long expectedLastHour, long expectedLastDay) {
        assertEquals(obj.lastMinuteCount(), expectedLastMinute);
        assertEquals(obj.lastHourCount(), expectedLastHour);
        assertEquals(obj.lastDayCount(), expectedLastDay);
    }

    private void joinThread(Thread thread) {
        try {
            thread.join();
        } catch (InterruptedException e) {
            fail("Test threads was interrupted");
        }
    }
}