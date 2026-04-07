package cfg;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.EnumMap;
import Exception.cfg.ConfigException;
import model.Section;

public class Config {

    // dodaj godziny startu
    // dodaj ile ma byc roznych ShiftPool
    // Co można poprawić, ale nie teraz:
    //
    // Wydzielenie strategii fitness jako interfejs — przydatne gdy będziesz chciał
    // testować różne funkcje oceny
    // Interfejs dla operatorów ewolucyjnych (mutacja, krzyżowanie) — przydatne gdy
    // będziesz eksperymentował z różnymi podejściami

    // dodaj mutationRate
    // zrekonfiguruj odchylenia, zbyt duzo skrajnych

    private static Config instance;

    // ==================== Ustawienia sklepu ====================
    private final Map<DayOfWeek, DayHours> hours;
    private final Map<DayOfWeek, Integer> staffingPercent;
    private final Map<DayOfWeek, PeakHours> peakHours;
    private final List<Integer> shiftLengths;
    private final int maxWorkingDaysInARow;

    // ==================== Dane generowania ====================
    private final YearMonth yearMonth;
    private final List<LocalDate> holidays;
    private final List<LocalDate> tradingSundays;
    private final List<Section> sections;
    private final boolean grantFreeWeekend;

    private Config(Map<DayOfWeek, DayHours> hours,
            Map<DayOfWeek, Integer> staffingPercent,
            Map<DayOfWeek, PeakHours> peakHours,
            List<Integer> shiftLengths,
            int maxWorkingDaysInARow,
            YearMonth yearMonth,
            List<LocalDate> holidays,
            List<LocalDate> tradingSundays,
            List<Section> sections,
            boolean grantFreeWeekend) {
        this.hours = new EnumMap<>(hours);
        this.staffingPercent = new EnumMap<>(staffingPercent);
        this.peakHours = new EnumMap<>(peakHours);
        this.shiftLengths = List.copyOf(shiftLengths);
        this.maxWorkingDaysInARow = maxWorkingDaysInARow;
        this.yearMonth = yearMonth;
        this.holidays = List.copyOf(holidays);
        this.tradingSundays = List.copyOf(tradingSundays);
        this.sections = List.copyOf(sections);
        this.grantFreeWeekend = grantFreeWeekend;
    }

    // ==================== Inicjalizacja ====================

    public static void init(Map<DayOfWeek, DayHours> hours,
            Map<DayOfWeek, Integer> staffingPercent,
            Map<DayOfWeek, PeakHours> peakHours,
            List<Integer> shiftLengths,
            int maxWorkingDaysInARow,
            YearMonth yearMonth,
            List<LocalDate> holidays,
            List<LocalDate> tradingSundays,
            List<Section> sections,
            boolean grantFreeWeekend) throws ConfigException {
        if (instance != null) {
            throw new ConfigException("Config już został zainicjalizowany");
        }
        validate(hours, staffingPercent, peakHours, shiftLengths, maxWorkingDaysInARow,
                yearMonth, holidays, tradingSundays, sections);
        instance = new Config(hours, staffingPercent, peakHours, shiftLengths,
                maxWorkingDaysInARow, yearMonth, holidays, tradingSundays, sections, grantFreeWeekend);
    }

    public static Config getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Config nie został zainicjalizowany.");
        }
        return instance;
    }

    public static void reset() {
        instance = null;
    }

    // ==================== Dni zamknięte ====================

    public boolean isClosedDay(LocalDate date) {
        if (holidays.contains(date))
            return true;
        return date.getDayOfWeek() == DayOfWeek.SUNDAY
                && !tradingSundays.contains(date);
    }

    private int countDaysClosed() {
        int daysClosed = 0;
        for (int i = 0; i < yearMonth.lengthOfMonth(); i++) {
            LocalDate date = yearMonth.atDay(i + 1);
            if (isClosedDay(date)) {
                daysClosed++;
            }
        }
        return daysClosed;
    }

    // ==================== Gettery — ustawienia sklepu ====================

    public DayHours getHours(DayOfWeek day) {
        return hours.get(day);
    }

    public LocalTime getOpenHour(DayOfWeek day) {
        return hours.get(day).open();
    }

    public int getOpenHourInt(DayOfWeek day) {
        return hours.get(day).open().getHour();
    }

    public LocalTime getCloseHour(DayOfWeek day) {
        return hours.get(day).close();
    }

    public int getCloseHourInt(DayOfWeek day) {
        return hours.get(day).close().getHour();
    }

    public int getStaffingPercent(DayOfWeek day) {
        return staffingPercent.get(day);
    }

    public List<Integer> getShiftLengths() {
        return shiftLengths;
    }

    public int getMaxWorkingDaysInARow() {
        return maxWorkingDaysInARow;
    }

    // ==================== Gettery — dane generowania ====================

    public int[] getPeakHoursArray(DayOfWeek day) {
        DayHours dh = hours.get(day);
        int openHour = dh.open().getHour();
        int closeHour = dh.close().getHour();
        int length = closeHour - openHour;

        int[] peak = new int[length];

        PeakHours ph = peakHours.get(day);
        int peakStart = ph.start().getHour();
        int peakEnd = ph.end().getHour();

        for (int i = 0; i < length; i++) {
            int hour = openHour + i;
            if (hour >= peakStart && hour < peakEnd) {
                peak[i] = 1;
            }
        }
        return peak;
    }

    public YearMonth getYearMonth() {
        return yearMonth;
    }

    public int getDaysInMonth() {
        return yearMonth.lengthOfMonth();
    }

    public PeakHours getPeakHour(DayOfWeek day) {
        return peakHours.get(day);
    }

    public DayOfWeek getFirstDayOfWeek() {
        return yearMonth.atDay(1).getDayOfWeek();
    }

    public List<LocalDate> getHolidays() {
        return holidays;
    }

    public List<LocalDate> getTradingSundays() {
        return tradingSundays;
    }

    public List<Section> getSections() {
        return sections;
    }

    public int getClosedDaysSize() {
        return countDaysClosed();
    }

    public boolean isGrantFreeWeekend() {
        return grantFreeWeekend;
    }

    public record DayHours(LocalTime open, LocalTime close) {
    }

    public record PeakHours(LocalTime start, LocalTime end) {
    }

    // ==================== Walidacja ====================

    private static void validate(Map<DayOfWeek, DayHours> hours,
            Map<DayOfWeek, Integer> staffingPercent,
            Map<DayOfWeek, PeakHours> peakHours,
            List<Integer> shiftLengths,
            int maxWorkingDaysInARow,
            YearMonth yearMonth,
            List<LocalDate> holidays,
            List<LocalDate> tradingSundays,
            List<Section> sections) throws ConfigException {
        if (maxWorkingDaysInARow <= 0) {
            throw new ConfigException("max_working_days_in_row musi być > 0, podano: " + maxWorkingDaysInARow);
        }

        if (shiftLengths == null || shiftLengths.isEmpty()) {
            throw new ConfigException("shift_lengths nie może być puste");
        }
        for (int len : shiftLengths) {
            if (len <= 0 || len > 12) {
                throw new ConfigException("shift_lengths: nieprawidłowa wartość: " + len + " (wymagane 1-12)");
            }
        }

        for (DayOfWeek day : DayOfWeek.values()) {
            String dayName = day.name().toLowerCase();

            if (!hours.containsKey(day)) {
                throw new ConfigException("Brak godzin otwarcia dla: " + dayName);
            }
            DayHours dh = hours.get(day);
            if (!dh.close().isAfter(dh.open())) {
                throw new ConfigException(dayName + ": godzina zamknięcia (" + dh.close()
                        + ") musi być po godzinie otwarcia (" + dh.open() + ")");
            }

            if (!staffingPercent.containsKey(day)) {
                throw new ConfigException("Brak staffing_percent dla: " + dayName);
            }
            int percent = staffingPercent.get(day);
            if (percent < 0 || percent > 100) {
                throw new ConfigException(dayName + ": staffing_percent musi być 0-100, podano: " + percent);
            }

            if (!peakHours.containsKey(day)) {
                throw new ConfigException("Brak peak_hours dla: " + dayName);
            }
            Config.PeakHours ph = peakHours.get(day);
            if (ph.start().isBefore(dh.open()) || ph.end().isAfter(dh.close())) {
                throw new ConfigException(dayName + ": peak_hours (" + ph.start()
                        + "-" + ph.end() + ") musi być pomiędzy " + dh.open() + " a " + dh.close());
            }
            if (!ph.end().isAfter(ph.start())) {
                throw new ConfigException(dayName + ": peak_hours end musi być po start");
            }
        }

        if (yearMonth == null) {
            throw new ConfigException("yearMonth nie może być null");
        }

        for (LocalDate holiday : holidays) {
            if (!yearMonth.equals(YearMonth.from(holiday))) {
                throw new ConfigException("Święto " + holiday + " nie należy do miesiąca " + yearMonth);
            }
        }

        for (LocalDate sunday : tradingSundays) {
            if (sunday.getDayOfWeek() != DayOfWeek.SUNDAY) {
                throw new ConfigException(sunday + " nie jest niedzielą");
            }
            if (!yearMonth.equals(YearMonth.from(sunday))) {
                throw new ConfigException("Niedziela handlowa " + sunday + " nie należy do miesiąca " + yearMonth);
            }
        }

        if (sections == null || sections.isEmpty()) {
            throw new ConfigException("Musi być przynajmniej jeden dział");
        }
    }

    // ==================== Testy ====================

    public static void initForTest(List<Integer> shiftLengths, int maxWorkingDaysInARow,
            Map<DayOfWeek, DayHours> hours,
            Map<DayOfWeek, Integer> staffingPercent,
            YearMonth yearMonth,
            List<Section> sections) {
        Map<DayOfWeek, PeakHours> peakHours = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek day : DayOfWeek.values()) {
            DayHours dh = hours.get(day);
            LocalTime peakStart = dh.open().plusHours(3);
            LocalTime peakEnd = peakStart.plusHours(3);
            peakHours.put(day, new PeakHours(peakStart, peakEnd));
        }
        instance = new Config(hours, staffingPercent, peakHours, shiftLengths,
                maxWorkingDaysInARow, yearMonth, List.of(), List.of(), sections, false);
    }
}
