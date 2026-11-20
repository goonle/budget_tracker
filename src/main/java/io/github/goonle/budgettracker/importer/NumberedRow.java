package io.github.goonle.budgettracker.importer;

/** A parsed line plus what the duplicate check needs: its hash and which occurrence it is in the file. */
public record NumberedRow(
        ParsedRow row,
        String lineHash,      // LineHasher.hash(row.rawLine())
        int occurrenceNo      // 1 = first time this line appears in the file, 2 = second, ...
) {}
