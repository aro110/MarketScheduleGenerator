package cfg;

import org.tomlj.Toml;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;
import java.nio.file.Path;
import java.nio.file.Files;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.EnumMap;
import java.util.ArrayList;
import Exception.cfg.ConfigException;

public class Config {

    private static Config instance;

    private final Map<DayOfWeek, DayHours> hours = new EnumMap<>(DayOfWeek.class);
    private final Map<DayOfWeek, Integer> staffingPercent = new EnumMap<>(DayOfWeek.class);
    private final Map<DayOfWeek, LocalTime> peakHours = new EnumMap<>(DayOfWeek.class);
    private final List<Integer> shiftLengths;
    private final int maxWorkingDaysInARow;
    // dodaj godziny startu
    // dodaj ile ma byc roznych ShiftPool
    //Co można poprawić, ale nie teraz:
    //
    //Wydzielenie strategii fitness jako interfejs — przydatne gdy będziesz chciał testować różne funkcje oceny
    //Interfejs dla operatorów ewolucyjnych (mutacja, krzyżowanie) — przydatne gdy będziesz eksperymentował z różnymi podejściami

    private static final String[] DAY_NAMES = {
            "monday", "tuesday", "wednesday", "thursday",
            "friday", "saturday", "sunday"
    };

    private Config(String path) throws ConfigException {
        TomlParseResult toml = parseFile(path);
        validateStructure(toml);

        for (String dayName : DAY_NAMES) {
            DayOfWeek day = DayOfWeek.valueOf(dayName.toUpperCase());

            TomlTable dayTable = toml.getTable("hours." + dayName);
            LocalTime open = dayTable.getLocalTime("open");
            LocalTime close = dayTable.getLocalTime("close");
            validateHours(dayName, open, close);
            hours.put(day, new DayHours(open, close));

            int percent = Math.toIntExact(toml.getLong("staffing_percent." + dayName));
            validatePercent(dayName, percent);
            staffingPercent.put(day, percent);

            LocalTime peak = toml.getLocalTime("peak_hours." + dayName);
            validatePeakHour(dayName, peak, open, close);
            peakHours.put(day, peak);
        }

        shiftLengths = parseShiftLengths(toml);

        maxWorkingDaysInARow = Math.toIntExact(toml.getLong("max_working_days_in_row"));
        validateMaxWorkingDays(maxWorkingDaysInARow);
    }

    public static void init(String path) throws ConfigException {
        if (instance != null) {
            throw new ConfigException("Config już został zainicjalizowany");
        }
        instance = new Config(path);
    }

    public static Config getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Config nie został zainicjalizowany. Wywołaj Config.init(path) najpierw.");
        }
        return instance;
    }

    private TomlParseResult parseFile(String path) throws ConfigException {
        try {
            if (!Files.exists(Path.of(path))) {
                throw new ConfigException("Plik konfiguracyjny nie istnieje: " + path);
            }
            TomlParseResult toml = Toml.parse(Path.of(path));
            if (toml.hasErrors()) {
                throw new ConfigException("Błąd składni TOML: " + toml.errors().getFirst().getMessage());
            }
            return toml;
        } catch (ConfigException e) {
            throw e;
        } catch (Exception e) {
            throw new ConfigException("Nie udało się odczytać pliku: " + e.getMessage());
        }
    }

    private void validateMaxWorkingDays(int maxWorkingDaysInARow) throws ConfigException {
        if (maxWorkingDaysInARow <= 0) {
            throw new ConfigException("max_working_days_in_row musi być >1, podano: " + maxWorkingDaysInARow);
        }
    }

    private void validateStructure(TomlParseResult toml) throws ConfigException {
        List<String> missing = new ArrayList<>();

        for (String section : List.of("hours", "staffing_percent", "peak_hours")) {
            if (toml.getTable(section) == null) {
                missing.add("[" + section + "]");
            }
        }
        if (toml.getArray("shift_lengths") == null) {
            missing.add("shift_lengths");
        }

        if (!missing.isEmpty()) {
            throw new ConfigException("Brakujące sekcje: " + String.join(", ", missing));
        }

        for (String day : DAY_NAMES) {
            if (toml.getTable("hours." + day) == null)
                missing.add("hours." + day);
            if (toml.getLong("staffing_percent." + day) == null)
                missing.add("staffing_percent." + day);
            if (toml.getLocalTime("peak_hours." + day) == null)
                missing.add("peak_hours." + day);
        }

        if (!missing.isEmpty()) {
            throw new ConfigException("Brakujące wpisy dla dni: " + String.join(", ", missing));
        }
    }

    private void validateHours(String day, LocalTime open, LocalTime close) throws ConfigException {
        if (!close.isAfter(open)) {
            throw new ConfigException(day + ": godzina zamknięcia (" + close
                    + ") musi być po godzinie otwarcia (" + open + ")");
        }
    }

    private void validatePercent(String day, int percent) throws ConfigException {
        if (percent < 0 || percent > 100) {
            throw new ConfigException(day + ": staffing_percent musi być 0-100, podano: " + percent);
        }
    }

    private void validatePeakHour(String day, LocalTime peak, LocalTime open, LocalTime close) throws ConfigException {
        if (peak.isBefore(open) || peak.isAfter(close)) {
            throw new ConfigException(day + ": peak_hour (" + peak
                    + ") musi być pomiędzy " + open + " a " + close);
        }
    }

    private List<Integer> parseShiftLengths(TomlParseResult toml) throws ConfigException {
        List<Integer> lengths = toml.getArray("shift_lengths")
                .toList().stream()
                .map(o -> ((Long) o).intValue())
                .toList();

        if (lengths.isEmpty()) {
            throw new ConfigException("shift_lengths nie może być puste");
        }
        for (int len : lengths) {
            if (len <= 0 || len > 12) {
                throw new ConfigException("shift_lengths: nieprawidłowa wartość: " + len + " (wymagane 1-24)");
            }
        }
        return lengths;
    }

    public DayHours getHours(DayOfWeek day) { return hours.get(day); }
    public int getStaffingPercent(DayOfWeek day) { return staffingPercent.get(day); }
    public LocalTime getPeakHour(DayOfWeek day) { return peakHours.get(day); }
    public List<Integer> getShiftLengths() { return shiftLengths; }
    public int getMaxWorkingDaysInARow() { return maxWorkingDaysInARow; }
    public record DayHours(LocalTime open, LocalTime close) {}

}