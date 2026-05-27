# Legal Review Follow-Ups

The vehicle-sale document text was carried forward from the original
implementation. The modernization intentionally does not reinterpret legal
language.

Items requiring legal or product-owner confirmation before wording changes:

- Confirm spelling, accents, punctuation, and grammatical agreement in the
  existing contract and authentication clauses.
- Confirm whether the sale price must be entered as words, numbers, or both.
  The current generator inserts the value supplied by the form.
- Confirm when `institucion` is required for `calidad_de` values such as
  `Prenda`. It remains optional because the original form did not capture it.
- Confirm whether DUI and NIT terminology or formats need adjustment for
  current Salvadoran identification practices.
