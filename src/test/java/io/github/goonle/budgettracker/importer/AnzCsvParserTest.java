package io.github.goonle.budgettracker.importer;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.StringReader;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnzCsvParserTest {

    private static final String HEADER =
            "Type,Details,Particulars,Code,Reference,Amount,Date,ForeignCurrencyAmount,ConversionCharge";

    private final AnzCsvParser parser = new AnzCsvParser();

    /** Builds a file: header + the given data lines + trailing newline, then parses it. */
    private List<ParsedRow> parseLines(String... dataLines) {
        String csv = HEADER + "\n" + String.join("\n", dataLines) + "\n";
        return parser.parse(new BufferedReader(new StringReader(csv)));
    }

    @Test
    void bankName_always_returnsAnz() {
        // when
        String name = parser.bankName();

        // then
        assertThat(name).isEqualTo("ANZ");
    }

    @Test
    void parse_visaPurchase_takesMerchantFromCodeAndCardFromDetails() {
        // given
        String line = "Visa Purchase,4835-****-****-1111  Df,,Pak N Save F,,-72.23,09/11/2026,,";

        // when
        List<ParsedRow> rows = parseLines(line);

        // then
        assertThat(rows).hasSize(1);
        ParsedRow row = rows.get(0);
        assertThat(row.isOk()).isTrue();
        assertThat(row.lineNo()).isEqualTo(2);
        assertThat(row.rawLine()).isEqualTo(line);
        assertThat(row.rawType()).isEqualTo("Visa Purchase");
        assertThat(row.merchantText()).isEqualTo("Pak N Save F");
        assertThat(row.cardLast4()).isEqualTo("1111");
        assertThat(row.amountCents()).isEqualTo(-7223L);
        assertThat(row.date()).isEqualTo(LocalDate.of(2026, 11, 9));
    }

    @Test
    void parse_eftPos_takesMerchantFromDetailsAndCardFromCode() {
        // given
        String line = "Eft-Pos,Authentic Factory Ou,4835********,2222   C,261112171201,-25.00,12/11/2026,,";

        // when
        ParsedRow row = parseLines(line).get(0);

        // then
        assertThat(row.isOk()).isTrue();
        assertThat(row.merchantText()).isEqualTo("Authentic Factory Ou");
        assertThat(row.cardLast4()).isEqualTo("2222");
        assertThat(row.amountCents()).isEqualTo(-2500L);
        assertThat(row.date()).isEqualTo(LocalDate.of(2026, 11, 12));
    }

    @Test
    void parse_directDebit_takesMerchantFromDetailsAndHasNoCard() {
        // given
        String line = "Direct Debit,Powershop,,,REF123,-11.29,10/11/2026,,";

        // when
        ParsedRow row = parseLines(line).get(0);

        // then
        assertThat(row.isOk()).isTrue();
        assertThat(row.merchantText()).isEqualTo("Powershop");
        assertThat(row.cardLast4()).isNull();
        assertThat(row.amountCents()).isEqualTo(-1129L);
    }

    @Test
    void parse_transfer_hasNoMerchantNoCardAndPositiveAmount() {
        // given
        String line = "Transfer,00-0000-0000000-00,Credit,Transfer,123456,450.00,12/11/2026,,";

        // when
        ParsedRow row = parseLines(line).get(0);

        // then
        assertThat(row.isOk()).isTrue();
        assertThat(row.rawType()).isEqualTo("Transfer");
        assertThat(row.merchantText()).isNull();
        assertThat(row.cardLast4()).isNull();
        assertThat(row.amountCents()).isEqualTo(45000L);
    }

    @Test
    void parse_quotedFieldWithComma_readsItAsOneField() {
        // given
        String line = "Direct Debit,\"Acme, Ltd\",,,REF999,-5.00,10/11/2026,,";

        // when
        ParsedRow row = parseLines(line).get(0);

        // then
        assertThat(row.isOk()).isTrue();
        assertThat(row.merchantText()).isEqualTo("Acme, Ltd");
        assertThat(row.amountCents()).isEqualTo(-500L);
    }

    @Test
    void parse_unknownType_fallsBackToDetails() {
        // given
        String line = "Bill Payment,Some Biller,,,,-30.00,08/11/2026,,";

        // when
        ParsedRow row = parseLines(line).get(0);

        // then
        assertThat(row.isOk()).isTrue();
        assertThat(row.rawType()).isEqualTo("Bill Payment");
        assertThat(row.merchantText()).isEqualTo("Some Biller");
        assertThat(row.cardLast4()).isNull();
    }

    @Test
    void parse_badAmount_returnsErrorRowInsteadOfThrowing() {
        // given
        String line = "Visa Purchase,4835-****-****-1111  Df,,Broken Row,,abc,09/11/2026,,";

        // when
        ParsedRow row = parseLines(line).get(0);

        // then
        assertThat(row.isOk()).isFalse();
        assertThat(row.error()).contains("amount");
        assertThat(row.lineNo()).isEqualTo(2);
        assertThat(row.rawLine()).isEqualTo(line);
    }

    @Test
    void parse_amountWithThreeDecimals_returnsErrorRow() {
        // given
        String line = "Direct Debit,Powershop,,,REF123,-1.005,10/11/2026,,";

        // when
        ParsedRow row = parseLines(line).get(0);

        // then
        assertThat(row.isOk()).isFalse();
        assertThat(row.error()).contains("amount");
    }

    @Test
    void parse_badDate_returnsErrorRowInsteadOfThrowing() {
        // given
        String line = "Visa Purchase,4835-****-****-2222  Df,,Temu.Com,,-7.55,32/13/2026,,";

        // when
        ParsedRow row = parseLines(line).get(0);

        // then
        assertThat(row.isOk()).isFalse();
        assertThat(row.error()).contains("date");
    }

    @Test
    void parse_tooFewColumns_returnsErrorRow() {
        // given
        String line = "Visa Purchase,4835-****-****-1111  Df";

        // when
        ParsedRow row = parseLines(line).get(0);

        // then
        assertThat(row.isOk()).isFalse();
        assertThat(row.error()).contains("columns");
    }

    @Test
    void parse_identicalLines_keepsBothWithTheirOwnLineNumbers() {
        // given
        String line = "Visa Purchase,4835-****-****-1111  Df,,Pak N Save F,,-72.23,09/11/2026,,";

        // when
        List<ParsedRow> rows = parseLines(line, line);

        // then
        assertThat(rows).extracting(ParsedRow::lineNo).containsExactly(2, 3);
        assertThat(rows).allMatch(ParsedRow::isOk);
    }

    @Test
    void parse_blankLine_isSkipped() {
        // given: header, one row, then an empty line
        String line = "Direct Debit,Powershop,,,REF123,-11.29,10/11/2026,,";

        // when
        List<ParsedRow> rows = parseLines(line, "");

        // then
        assertThat(rows).hasSize(1);
    }

    @Test
    void parse_headerOnly_returnsEmptyList() {
        // given
        BufferedReader reader = new BufferedReader(new StringReader(HEADER + "\n"));

        // when
        List<ParsedRow> rows = parser.parse(reader);

        // then
        assertThat(rows).isEmpty();
    }

    @Test
    void parse_emptyFile_returnsEmptyList() {
        // given
        BufferedReader reader = new BufferedReader(new StringReader(""));

        // when
        List<ParsedRow> rows = parser.parse(reader);

        // then
        assertThat(rows).isEmpty();
    }
}
