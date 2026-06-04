# AI Usage

## Tool Used
Claude (Anthropic) via Claude Code CLI

## What AI Was Used For

**1. Fetching exchange rates from Bundesbank API**
Claude helped with the `BundesbankClient` — specifically the SDMX API URL structure, the series key format (`BBEX3/D.{CURRENCY}.EUR.BB.AC.000`), and how to parse the CSV response including handling missing data rows (`.` for weekends/bank holidays).

**2. In-code documentation**
Used Claude to generate Javadoc comments across the controller, service interface, repository, entity, and exception classes.

**3. Writing tests**
Claude generated the initial structure for unit tests covering the service layer, CSV parsing, and controller endpoint contracts.

## Key Decisions I Changed or Overrode

- **Wrong API URL**: Claude initially suggested `api.bundesbank.de` which was unreachable. I found the correct endpoint (`api.statistiken.bundesbank.de`) by checking the Bundesbank website directly.
- **Hardcoded start date**: Claude suggested putting the fetch start date in `application.properties`. I changed it to `LocalDate.now().minusYears(3)` in code — externalising it as config makes no sense for something that isn't environment-specific.
- **GlobalExceptionHandler**: Claude added a `GlobalExceptionHandler` class. I removed it in favour of `@ResponseStatus` directly on the exception — simpler and sufficient for this scope.
- **Interface + impl pattern**: Claude initially wrote a plain `@Service` class. I asked for the interface to be separated as it is better practice.
- **Tests**: Claude generated overly broad tests. I trimmed them to only cover things that could actually break — conversion math, missing CSV rows, 404 responses.

## What I Did Not Use AI For
- Endpoint design and naming
- Business logic in `CurrencyServiceImpl` — conversion formula, stream mappings
- Choice of `BigDecimal` over `double` for monetary values
- Identifying the correct Bundesbank API URL
