package io.github.goonle.budgettracker.importer;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AnzCsvParser implements BankCsvParser {

    // Column positions in the ANZ header:
    // Type,Details,Particulars,Code,Reference,Amount,Date,ForeignCurrencyAmount,ConversionCharge
    private static final int TYPE = 0, DETAILS = 1, CODE = 3, AMOUNT = 5, DATE = 6;
    private static final int MIN_COLUMNS = DATE + 1;

    // Type values that change where the merchant and card are (docs/csv-format.md)
    private static final String VISA_PURCHASE = "Visa Purchase";
    private static final String EFT_POS = "Eft-Pos";
    private static final String TRANSFER = "Transfer";

    // STRICT rejects impossible dates like 31/02/2026 ("uuuu" is required in STRICT mode)
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    // "4835-****-****-1111  Df" -> 1111
    private static final Pattern VISA_CARD = Pattern.compile("\\d{4}-\\*{4}-\\*{4}-(\\d{4})");
    // "1111   C" -> 1111
    private static final Pattern EFTPOS_CARD = Pattern.compile("^(\\d{4})\\b");

    @Override
    public String bankName() {
        return "ANZ";
    }

    @Override
    public List<ParsedRow> parse(BufferedReader reader) {
        List<ParsedRow> rows = new ArrayList<>();
        try {
            reader.readLine();                       // line 1: header, skipped
            int lineNo = 1;
            String line;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (line.isBlank()) {
                    continue;                        // trailing empty line at the end of the file
                }
                rows.add(parseLine(lineNo, line));
            }
        } catch (IOException e) {
            // the file itself can't be read: a different problem from one bad row
            throw new UncheckedIOException("Could not read the CSV file", e);
        }
        return rows;
    }

    /** One line -> one ParsedRow. Never throws: problems become an error row. */
    private ParsedRow parseLine(int lineNo, String line) {
        CSVRecord record;
        try {
            record = splitLine(line);
        } catch (IOException | UncheckedIOException | IllegalStateException e) {
            return error(lineNo, line, "Malformed CSV line");
        }
        if (record.size() < MIN_COLUMNS) {
            return error(lineNo, line, "Expected at least " + MIN_COLUMNS + " columns, found " + record.size());
        }

        String rawDate = record.get(DATE).trim();
        LocalDate date;
        try {
            date = LocalDate.parse(rawDate, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return error(lineNo, line, "Invalid date '" + rawDate + "'");
        }

        String rawAmount = record.get(AMOUNT).trim();
        long amountCents;
        try {
            amountCents = new BigDecimal(rawAmount).movePointRight(2).longValueExact();
        } catch (NumberFormatException | ArithmeticException e) {
            // NumberFormat: "abc" or "". Arithmetic: more than 2 decimals, e.g. "1.005"
            return error(lineNo, line, "Invalid amount '" + rawAmount + "'");
        }

        String type = record.get(TYPE).trim();
        String details = emptyToNull(record.get(DETAILS));
        String code = emptyToNull(record.get(CODE));

        String merchant = switch (type) {
            case VISA_PURCHASE -> code;
            case TRANSFER -> null;                   // Details is an account number, not a merchant
            default -> details;                      // Eft-Pos, Direct Debit, Automatic Payment, unknown types
        };
        String cardLast4 = switch (type) {
            case VISA_PURCHASE -> findGroup(VISA_CARD, details);
            case EFT_POS -> findGroup(EFTPOS_CARD, code);
            default -> null;
        };

        return new ParsedRow(lineNo, line, date, amountCents, type, merchant, cardLast4, null);
    }

    /** Splits one line with the CSV quoting rules ("Acme, Ltd" stays one field). */
    private static CSVRecord splitLine(String line) throws IOException {
        try (CSVParser csv = CSVFormat.DEFAULT.parse(new StringReader(line))) {
            return csv.getRecords().get(0);
        }
    }

    private static ParsedRow error(int lineNo, String line, String message) {
        return new ParsedRow(lineNo, line, null, 0, null, null, null, message);
    }

    private static String emptyToNull(String value) {
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /** First capture group of the pattern, or null if the text is null or doesn't match. */
    private static String findGroup(Pattern pattern, String text) {
        if (text == null) {
            return null;
        }
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1) : null;
    }
}
