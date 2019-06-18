package com.poiji.config;

import com.poiji.option.PoijiOptions;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Created by brunslo on 15/06/2019.
 */
public final class LogCasting implements Casting {
    private final List<CastingError> castingErrors = new ArrayList<CastingError>();

    private void log(String value, Object defaultValue, String sheetName, int row, int col, Throwable exception) {
        castingErrors.add(new CastingError(value, defaultValue, sheetName, row, col, exception));
    }

    private int primitiveIntegerValue(String value, String sheetName, int row, int col) {
        try {
            return new Integer(value);
        } catch (NumberFormatException nfe) {
            int defaultValue = 0;

            log(value, defaultValue, sheetName, row, col, nfe);

            return defaultValue;
        }
    }

    private Integer integerValue(String value, String sheetName, int row, int col, PoijiOptions options) {
        try {
            return new Integer(value);
        } catch (NumberFormatException nfe) {
            Integer defaultValue = Boolean.TRUE.equals(options.preferNullOverDefault()) ? null : 0;

            log(value, defaultValue, sheetName, row, col, nfe);

            return defaultValue;
        }
    }

    private long primitiveLongValue(String value, String sheetName, int row, int col) {
        try {
            return new Long(value);
        } catch (NumberFormatException nfe) {
            long defaultValue = 0L;

            log(value, defaultValue, sheetName, row, col, nfe);

            return defaultValue;
        }
    }

    private Long longValue(String value, String sheetName, int row, int col, PoijiOptions options) {
        try {
            return new Long(value);
        } catch (NumberFormatException nfe) {
            Long defaultValue = Boolean.TRUE.equals(options.preferNullOverDefault()) ? null : 0L;

            log(value, defaultValue, sheetName, row, col, nfe);

            return defaultValue;
        }
    }

    private double primitiveDoubleValue(String value, String sheetName, int row, int col) {
        try {
            return new Double(value);
        } catch (NumberFormatException nfe) {
            double defaultValue = 0d;

            log(value, defaultValue, sheetName, row, col, nfe);

            return defaultValue;
        }
    }

    private Double doubleValue(String value, String sheetName, int row, int col, PoijiOptions options) {
        try {
            return new Double(value);
        } catch (NumberFormatException nfe) {
            Double defaultValue = Boolean.TRUE.equals(options.preferNullOverDefault()) ? null : 0d;

            log(value, defaultValue, sheetName, row, col, nfe);

            return defaultValue;
        }
    }

    private float primitiveFloatValue(String value, String sheetName, int row, int col) {
        try {
            return new Float(value);
        } catch (NumberFormatException nfe) {
            float defaultValue = 0f;

            log(value, defaultValue, sheetName, row, col, nfe);

            return defaultValue;
        }
    }

    private Float floatValue(String value, String sheetName, int row, int col, PoijiOptions options) {
        try {
            return new Float(value);
        } catch (NumberFormatException nfe) {
            Float defaultValue = Boolean.TRUE.equals(options.preferNullOverDefault()) ? null : 0f;

            log(value, defaultValue, sheetName, row, col, nfe);

            return defaultValue;
        }
    }

    private Date defaultDate(PoijiOptions options) {
        if (Boolean.TRUE.equals(options.preferNullOverDefault())) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime();
    }

    private LocalDate defaultLocalDate(PoijiOptions options) {
        if (Boolean.TRUE.equals(options.preferNullOverDefault())) {
            return null;
        }
        return LocalDate.now();
    }

    private BigDecimal bigDecimalValue(String value, String sheetName, int row, int col, PoijiOptions options) {
        try {
            String clean = value != null ? value.replace(",", ".") : "";
            return new BigDecimal(clean);
        } catch (NumberFormatException nfe) {
            BigDecimal defaultValue = Boolean.TRUE.equals(options.preferNullOverDefault()) ? null : BigDecimal.ZERO;

            log(value, defaultValue, sheetName, row, col, nfe);

            return defaultValue;
        }
    }

    private Date dateValue(String value, String sheetName, int row, int col, PoijiOptions options) {

        //ISSUE #57
        //if a date regex has been specified then it wont be null
        //so then make sure the string matches the pattern
        //if it doesn't, fall back to default
        //else continue to turn string into java date

        //the reason for this is sometime Java will manage to parse a string to a date object
        //without any exceptions but since the string was not an exact match you get a very strange date
        if (options.getDateRegex() != null && !value.matches(options.getDateRegex())) {
            return defaultDate(options);
        } else {
            try {
                final SimpleDateFormat sdf = new SimpleDateFormat(options.datePattern());
                sdf.setLenient(options.getDateLenient());
                return sdf.parse(value);
            } catch (ParseException e) {
                Date defaultValue = defaultDate(options);

                log(value, defaultValue, sheetName, row, col, e);

                return defaultValue;
            }
        }
    }

    private LocalDate localDateValue(String value, String sheetName, int row, int col, PoijiOptions options) {

        //ISSUE #57
        //if a date regex has been specified then it wont be null
        //so then make sure the string matches the pattern
        //if it doesn't, fall back to default
        //else continue to turn string into java date

        //the reason for this is sometime java will manage to parse a string to a date object
        //without any exceptions but since the string was not an exact match you get a very strange date
        if (options.getDateRegex() != null && !value.matches(options.getDateRegex())) {
            return defaultLocalDate(options);
        } else {
            try {
                return LocalDate.parse(value, options.dateTimeFormatter());
            } catch (DateTimeParseException e) {
                LocalDate defaultValue = defaultLocalDate(options);

                log(value, defaultValue, sheetName, row, col, e);

                return defaultValue;
            }
        }
    }

    private Object enumValue(String value, String sheetName, int row, int col, Class type) {
        Optional<Object> object = Arrays
                .stream(type.getEnumConstants())
                .filter(o -> ((Enum) o).name().equals(value))
                .findFirst();

        if (object.isPresent()) {
            return object.get();
        } else {
            Object defaultValue = null;

            IllegalArgumentException exception = new IllegalArgumentException("No enumeration " + type.getSimpleName() + "." + value);

            log(value, defaultValue, sheetName, row, col, exception);

            return defaultValue;
        }
    }

    public Object castValue(Class<?> fieldType, String value, PoijiOptions options) {
        return castValue(fieldType, value, -1, -1, options);
    }

    public Object castValue(Class<?> fieldType, String value, int row, int col, PoijiOptions options) {

        String sheetName = options.getSheetName() == null ? "" : options.getSheetName();

        if (options.trimCellValue()) {
            value = value.trim();
        }

        Object o = value;

        if (fieldType.getName().equals("int")) {
            o = primitiveIntegerValue(value, sheetName, row, col);

        } else if (fieldType.getName().equals("java.lang.Integer")) {
            o = integerValue(value, sheetName, row, col, options);

        } else if (fieldType.getName().equals("java.math.BigDecimal")) {
            o = bigDecimalValue(value, sheetName, row, col, options);

        } else if (fieldType.getName().equals("long")) {
            o = primitiveLongValue(value, sheetName, row, col);

        } else if (fieldType.getName().equals("java.lang.Long")) {
            o = longValue(value, sheetName, row, col, options);

        } else if (fieldType.getName().equals("double")) {
            o = primitiveDoubleValue(value, sheetName, row, col);

        } else if (fieldType.getName().equals("java.lang.Double")) {
            o = doubleValue(value, sheetName, row, col, options);

        } else if (fieldType.getName().equals("float")) {
            o = primitiveFloatValue(value, sheetName, row, col);

        } else if (fieldType.getName().equals("java.lang.Float")) {
            o = floatValue(value, sheetName, row, col, options);

        } else if (fieldType.getName().equals("boolean") || fieldType.getName().equals("java.lang.Boolean")) {
            o = Boolean.valueOf(value);

        } else if (fieldType.getName().equals("java.util.Date")) {
            o = dateValue(value, sheetName, row, col, options);

        } else if (fieldType.getName().equals("java.time.LocalDate")) {
            o = localDateValue(value, sheetName, row, col, options);

        } else if (fieldType.isEnum()) {
            o = enumValue(value, sheetName, row, col, fieldType);

        } else {
            if (value.isEmpty()) {
                if (Boolean.TRUE.equals(options.preferNullOverDefault())) {
                    o = null;
                } else {
                    o = value;
                }
            } else {
                o = value;
            }
        }
        return o;
    }

    public List<CastingError> getCastingErrors() {
        return Collections.unmodifiableList(castingErrors);
    }

    public static final class CastingError {
        private String value;

        private Object defaultValue;

        private String sheetName;

        private int row;

        private int column;

        private Throwable exception;

        CastingError(String value, Object defaultValue, String sheetName, int row, int column, Throwable exception) {
            this.value = value;
            this.defaultValue = defaultValue;
            this.sheetName = sheetName;
            this.row = row;
            this.column = column;
            this.exception = exception;
        }

        public String getValue() {
            return value;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }

        public String getSheetName() {
            return sheetName;
        }

        public int getRow() {
            return row;
        }

        public int getColumn() {
            return column;
        }

        public Throwable getException() {
            return exception;
        }
    }
}
