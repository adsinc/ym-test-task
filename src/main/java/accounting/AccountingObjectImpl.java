package accounting;

import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static java.time.temporal.ChronoUnit.*;

public class AccountingObjectImpl implements AccountingObject {

    private final TemporalUnit groupByUnit;
    private final Map<Long, Long> events = new ConcurrentHashMap<>();

    AccountingObjectImpl(TemporalUnit groupByUnit) {
        Objects.requireNonNull(groupByUnit);
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
}
