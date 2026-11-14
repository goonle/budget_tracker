# Bank CSV formats

Reference for the CSV parsers (epic E2) and their tests.
Real exports live in `sample/`, which is gitignored. Test fixtures in `src/test/resources/csv/` must be anonymized.

## ANZ (New Zealand)

### File

- Encoding: plain ASCII (UTF-8 compatible), no BOM.
- First line is the header. No lines above it.
- No quoted fields in the samples seen so far. Parse with a real CSV library anyway, in case a field contains a comma.
- Rows are **newest first**.

### Columns

```
Type,Details,Particulars,Code,Reference,Amount,Date,ForeignCurrencyAmount,ConversionCharge
```

| Column | Format | Notes |
|---|---|---|
| `Type` | text | Decides where the merchant and card are (see below) |
| `Details`, `Particulars`, `Code`, `Reference` | text, often empty | Meaning depends on `Type` |
| `Amount` | signed decimal, 2 places, no thousands separator | `-20.00` = money out, `450.00` = money in. Parse with `BigDecimal`, store as cents (`long`). |
| `Date` | `dd/MM/yyyy` | e.g. `13/11/2026`. Probably the posting date, not the purchase date. |
| `ForeignCurrencyAmount`, `ConversionCharge` | decimal, usually empty | Only for overseas purchases |

### Where the merchant and card are, by `Type`

| Type | Merchant text | Card last 4 | Example row (anonymized) |
|---|---|---|---|
| `Visa Purchase` | `Code`, cut to 12 chars | `Details`: `4835-****-****-NNNN  Df` | `Visa Purchase,4835-****-****-1111  Df,,Pak N Save F,,-72.23,09/11/2026,,` |
| `Eft-Pos` | `Details`, cut to 20 chars | `Code`: `NNNN   C` | `Eft-Pos,Authentic Factory Ou,4835********,1111   C,261112171201,-25.00,12/11/2026,,` |
| `Direct Debit` | `Details` | none | `Direct Debit,Powershop,,,REF123,-11.29,10/11/2026,,` |
| `Automatic Payment` | `Details` | none | `Automatic Payment,Tuscancity New Accou,,,,-495.00,12/11/2026,,` |
| `Transfer` | none; `Details` is the other account number, `Particulars` is `Credit`/`Debit` | none | `Transfer,00-0000-0000000-00,Credit,Transfer,123456,450.00,12/11/2026,,` |
| anything else | fall back to `Details` | none | Flag the row in the preview as "unknown type" |

Card regexes:
- Visa: `Details` matches `\d{4}-\*{4}-\*{4}-(\d{4})`
- Eft-Pos: `Code` matches `^(\d{4})\s`

### Consequences for the design

- **Merchant names are truncated**, and the cut-off part can differ by store: `Pak N Save F` and `Pak N Save M`. Alias matching uses the **longest matching prefix** (E3), so one alias `Pak N Save` covers both.
- **Visa rows have no unique reference.** Two real identical purchases on the same day produce identical lines. `occurrence_no` (1, 2, …) keeps both, and re-imports still detect them as duplicates.
- **Eft-Pos `Reference`** looks like a timestamp (`yyMMddHHmmss`) and makes each Eft-Pos line unique.
- **Transfers have no merchant.** The preview defaults them to `Not spending → Transfer`. Known account numbers can become aliases later.
- **Cards seen:** usually one per person, so a new last-4 triggers the "assign member" step.
