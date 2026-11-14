package io.github.goonle.budgettracker.importer;

import java.io.BufferedReader;
import java.util.List;

public interface BankCsvParser {

    /** Shown in the UI, e.g. "ANZ". */
    String bankName();

    /**
     * Reads the whole file, header included (line 1 is skipped).
     * Every data line becomes one ParsedRow, including lines that fail (with error set).
     */
    List<ParsedRow> parse(BufferedReader reader);
}
