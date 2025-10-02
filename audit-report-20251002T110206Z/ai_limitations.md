# Cursor/AI Limitations & Mitigation (auto audit)
- Static analysis cannot guarantee runtime state: visibility toggled at runtime (View.setVisibility) may hide views even if XML says VISIBLE.
  * Mitigation: use Layout Inspector + runtime logs.
- Heuristics may list false positives for unused resources (some drawables loaded dynamically by name).
  * Mitigation: backup then remove; prefer grep for dynamic references (e.g. getDrawable(context.resources.getIdentifier(...))).
- Complex Kotlin usage (reflection, generated code, databinding, kapt-generated ids) may confuse simple regex search.
  * Mitigation: run Android Studio Inspections and use the IDE's "Find Usages".
- Detection of logic bugs (race conditions, coroutine misuse) requires manual test scenarios and code review.
  * Mitigation: add unit & instrumentation tests; use strict lint/detekt rules.
