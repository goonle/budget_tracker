package io.github.goonle.budgettracker.importer;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LineHasherTest {

    @Test
    void normalize_multipleSpaces_collapseToOne() {
        // given: a line with several spaces in a row
        String line = "Direct Debit,Power     shop,,,REF123,-11.29,10/11/2026,,   ";

        // when
        String result = LineHasher.normalize(line);

        // then: the spaces became one
        assertThat(result).isEqualTo("Direct Debit,Power shop,,,REF123,-11.29,10/11/2026,,");
    }

    @Test
    void hash_sameLineDifferentSpacing_returnsSameHash() {
        // given: two lines that differ only in spacing
        String a = "Direct Debit,Power    shop,,,REF123,-11.29,10/11/2026,,";
        String b = "Direct Debit,Power shop,,,REF123,-11.29,10/11/2026,,";

        // when
        String hashA = LineHasher.hash(a);
        String hashB = LineHasher.hash(b);

        // then: both fingerprints are the same
        assertThat(hashA).isEqualTo(hashB);
    }

    @Test
    void normalize_leadingAndTrailingSpaces_areRemoved() {
        // given
        String line = "   Direct Debit,Powershop,,,REF123,-11.29,10/11/2026,,   ";

        // when
        String result = LineHasher.normalize(line);

        // then
        assertThat(result).isEqualTo("Direct Debit,Powershop,,,REF123,-11.29,10/11/2026,,");
    }

    @Test
    void normalize_carriageReturn_isRemoved() {
        // given: a line from a Windows file ends with \r
        String line = "Direct Debit,Powershop,,,REF123,-11.29,10/11/2026,,\r";

        // when
        String result = LineHasher.normalize(line);

        // then
        assertThat(result).isEqualTo("Direct Debit,Powershop,,,REF123,-11.29,10/11/2026,,");
    }

    @Test
    void hash_knownInput_matchesStandardValue() {
        // given: the official SHA-256 test vector
        String line = "abc";

        // when
        String hash = LineHasher.hash(line);

        // then
        assertThat(hash).isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
    }

    @Test
    void hash_always_returns64LowercaseHexChars() {
        // given
        String line = "Visa Purchase,4835-****-****-1111  Df,,Pak N Save F,,-72.23,09/11/2026,,";

        // when
        String hash = LineHasher.hash(line);

        // then
        assertThat(hash).hasSize(64).matches("[0-9a-f]{64}");
    }

    @Test
    void hash_differentAmount_returnsDifferentHash() {
        // given: same line, one cent different
        String a = "Visa Purchase,4835-****-****-1111  Df,,Pak N Save F,,-72.23,09/11/2026,,";
        String b = "Visa Purchase,4835-****-****-1111  Df,,Pak N Save F,,-72.24,09/11/2026,,";

        // when
        String hashA = LineHasher.hash(a);
        String hashB = LineHasher.hash(b);

        // then
        assertThat(hashA).isNotEqualTo(hashB);
    }
}
