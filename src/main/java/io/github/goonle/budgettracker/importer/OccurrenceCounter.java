package io.github.goonle.budgettracker.importer;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/** Numbers identical lines within one file: 1, 2, 3, ... (see "Import and duplicate rules" in docs/plan.md). */
public final class OccurrenceCounter {

    private OccurrenceCounter() {
        // static helpers only
    }

    /**
     * Same order as the input. Each row gets its line hash and how many times
     * that hash has appeared so far in the list (first time = 1).
     */
    public static List<NumberedRow> number(List<ParsedRow> rows) {
        Map<String, Integer> seen = new HashMap<>();
        List<NumberedRow> result = new ArrayList<>();
        
        for(ParsedRow row : rows) {
            String hash = LineHasher.hash(row.rawLine());
            int occurrence = seen.getOrDefault(hash, 0) + 1;
            seen.put(hash, occurrence);
            result.add(new NumberedRow(row, hash, occurrence));
        }
        return result;

    }
}
