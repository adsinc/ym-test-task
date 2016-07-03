package accounting;

import org.junit.Before;
import org.junit.Test;

import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class AccountingObjectImplTest {

    private AccountingObject accountingObject;

    @Before
    public void before() {
        accountingObject = new AccountingObjectImpl();
    }

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
        IntStream.range(0, threadNumber).mapToObj(i -> new Thread(() ->
                IntStream.range(0, eventsPerThread).forEach(k -> accountingObject.registerEvent())
        )).map(t -> {
            t.start();
            return t;
        }).forEach(this::joinThread);
        assertEquals(accountingObject.lastMinuteCount(), threadNumber * eventsPerThread);
        assertEquals(accountingObject.lastHourCount(), threadNumber * eventsPerThread);
        assertEquals(accountingObject.lastDayCount(), threadNumber * eventsPerThread);
    }

    @Test
    public void test() throws Exception {

    }

    private void joinThread(Thread thread) {
        try {
            thread.join();
        } catch (InterruptedException e) {
            fail("Test threads was interrupted");
        }
    }
}