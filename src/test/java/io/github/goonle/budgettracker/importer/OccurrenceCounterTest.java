package io.github.goonle.budgettracker.importer;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class OccurrenceCounterTest {

    private static final String COFFEE = "Visa Purchase,4835-****-****-1111  Df,,Coffee Shop,,-5.00,09/11/2026,,";
    private static final String GROCERY = "Visa Purchase,4835-****-****-1111  Df,,Pak N Save F,,-72.23,09/11/2026,,";
    private static final String POWER = "Direct Debit,Powershop,,,REF123,-11.29,10/11/2026,,";

    /** Only lineNo and rawLine matter for counting, so the other fields stay empty. */
    private static ParsedRow row(int lineNo, String rawLine) {
        return new ParsedRow(lineNo, rawLine, null, 0, null, null, null, null);
    }

    @Test
    void number_allDifferentLines_eachGetsOne() {
        // given
        List<ParsedRow> rows = List.of(row(2, COFFEE), row(3, GROCERY), row(4, POWER));

        // when
        List<NumberedRow> result = OccurrenceCounter.number(rows);

        // then
        assertThat(result).extracting(NumberedRow::occurrenceNo).containsExactly(1, 1, 1);
    }

    @Test
    void number_identicalLines_getOneTwoThree() {
        // given: the same coffee bought three times
        List<ParsedRow> rows = List.of(row(2, COFFEE), row(3, COFFEE), row(4, COFFEE));

        // when
        List<NumberedRow> result = OccurrenceCounter.number(rows);

        // then
        assertThat(result).extracting(NumberedRow::occurrenceNo).containsExactly(1, 2, 3);
    }

    @Test
    void number_mixedLines_countsEachLineSeparately() {
        // given: COFFEE, GROCERY, COFFEE
        List<ParsedRow> rows = List.of(row(2, COFFEE), row(3, GROCERY), row(4, COFFEE));

        // when
        List<NumberedRow> result = OccurrenceCounter.number(rows);

        // then: which occurrence numbers do you expect?
        assertThat(result).extracting(NumberedRow::occurrenceNo).containsExactly(1, 1, 2);
    }

    @Test
    void number_linesDifferingOnlyInSpacing_countAsSame() {
        // given: the second coffee line has extra spaces after the card number
        String coffeeWithMoreSpaces = COFFEE.replace("1111  Df", "1111     Df");
        List<ParsedRow> rows = List.of(row(2, COFFEE), row(3, coffeeWithMoreSpaces));

        // when
        List<NumberedRow> result = OccurrenceCounter.number(rows);

        // then
        assertThat(result).extracting(NumberedRow::occurrenceNo).containsExactly(1, 2);
    }

    @Test
    void number_anyRows_keepsInputOrderAndSetsHash() {
        // given
        ParsedRow first = row(2, GROCERY);
        ParsedRow second = row(3, POWER);

        // when
        List<NumberedRow> result = OccurrenceCounter.number(List.of(first, second));

        // then
        assertThat(result).extracting(NumberedRow::row).containsExactly(first, second);
        assertThat(result.get(0).lineHash()).isEqualTo(LineHasher.hash(GROCERY));
        assertThat(result.get(1).lineHash()).isEqualTo(LineHasher.hash(POWER));
    }

    @Test
    void number_emptyList_returnsEmptyList() {
        // when
        List<NumberedRow> result = OccurrenceCounter.number(List.of());

        // then
        assertThat(result).isEmpty();
    }
}
