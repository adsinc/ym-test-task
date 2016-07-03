package accounting;

/**
 * Event accounting object
 */
public interface AccountingObject {

    void registerEvent();

    long lastMinuteCount();

    long lastHourCount();

    long lastDayCount();
}
