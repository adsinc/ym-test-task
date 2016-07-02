package accounting;

import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import static java.time.temporal.ChronoUnit.*;

public class AccountingObjectImpl implements AccountingObject {

    private final TemporalUnit groupByUnit;
    private final Map<Long, Long> events = new ConcurrentHashMap<>();

    public AccountingObjectImpl(TemporalUnit groupByUnit) {
        this.groupByUnit = groupByUnit;
    }

    public AccountingObjectImpl() {
        this(MILLIS);
    }

    @Override
    public void registerEvent() {
        long now = Instant.now().truncatedTo(groupByUnit).toEpochMilli();
        events.merge(now, 1L, (x, y) -> x + y);
    }

    @Override
    public long lastMinuteCount() {
        return lastTemporalUnitCount(MINUTES);
    }

    @Override
    public long lastHourCount() {
        return lastTemporalUnitCount(HOURS);
    }

    @Override
    public long lastDayCount() {
        return lastTemporalUnitCount(DAYS);
    }

    private long lastTemporalUnitCount(TemporalUnit temporalUnit) {
        long lowerAmount = Instant.now()
                .truncatedTo(groupByUnit)
                .minus(1, temporalUnit)
                .toEpochMilli();
        return events.keySet().parallelStream()
                .filter(millis -> millis > lowerAmount)
                .map(events::get)
                .reduce((x, y) -> x + y)
                .orElse(0L);
    }

    public static void main(String[] args) throws InterruptedException {
        AccountingObject ac = new AccountingObjectImpl();
        Thread t1 = new Thread(() -> IntStream.range(0, 1_000_000).forEach(x -> ac.registerEvent()));
        Thread t2 = new Thread(() -> IntStream.range(0, 1_000_000).forEach(x -> ac.registerEvent()));
        Thread r = new Thread(() -> IntStream.range(0, 1_000_000).forEach(x -> ac.lastDayCount()));

        t1.start();
        t2.start();
        r.start();

        t1.join();
        t2.join();
        r.join();

        System.out.println(ac.lastDayCount());
    }
}
