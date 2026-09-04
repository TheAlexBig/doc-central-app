# Legal Review Follow-Ups

The vehicle-sale document text was carried forward from the original
implementation. The modernization intentionally does not reinterpret legal
language.

The mutual-agreement workflow was derived from two supplied private mutual
agreements with notarial authentication. Their spelling and grammar were
normalized during implementation, but the resulting clauses still require
review by the responsible Salvadoran notary before production use.

Items requiring legal or product-owner confirmation before wording changes:

- Confirm spelling, accents, punctuation, and grammatical agreement in the
  existing contract and authentication clauses.
- Confirm whether the sale price must be entered as words, numbers, or both.
  The current generator inserts the value supplied by the form.
- Confirm when `institucion` is required for `calidad_de` values such as
  `Prenda`. It remains optional because the original form did not capture it.
- Confirm whether DUI and NIT terminology or formats need adjustment for
  current Salvadoran identification practices.

Mutual-agreement items requiring confirmation:

- Confirm the final wording of the funds, maturity, default, jurisdiction,
  tax, anti-money-laundering, and anti-usury clauses.
- Confirm whether the private agreement must always be reproduced literally
  inside the authentication act.
- Confirm whether bank and account are always mandatory or whether cash and
  other payment methods must be supported.
- Confirm wording for one payment versus installments, including whether each
  installment includes capital and interest.
- Confirm when interest, default interest, a bill of exchange, its due date,
  and administrative expenses are permitted or required.
- Confirm whether the creditor must expressly accept the debtor's special
  domicile and how the number of useful pages must be stated.
