package io.github.goonle.budgettracker.importer;

import java.time.LocalDate;

public record ParsedRow(
        int lineNo,           // physical line in the file (header = 1, first data row = 2)
        String rawLine,       // exact text of the line, for the duplicate check
        LocalDate date,
        long amountCents,     // -2000 = -$20.00
        String rawType,       // "Visa Purchase", "Eft-Pos", ...
        String merchantText,  // "Pak N Save F"; null for transfers
        String cardLast4,     // "1111"; null if the row has no card
        String error          // null if OK, otherwise why the line couldn't be parsed
) {
    public boolean isOk() {
        return error == null;
    }
}
