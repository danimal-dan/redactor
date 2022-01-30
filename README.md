# Redactor

Provides field-level security using Spring AOP.

Upcoming features:

- [ ] Wire up Spring Security in Aspect to provide actual values to `hasAuthorityCallback`
- [ ] Provide mechanism to override default `hasAuthorityCallback` initialization
- [ ] Currently, we redact properties on the way out, but we need a mechanism to re-populate redacted data when the
  payload is sent back, say for an update.
    - Interface - `<T> T redactor.rehydrate(T redactedObjectWithUpdates, T priorState)`
        - Any redacted field values must be copied from `priorState` to corresponding field
          on `redactedObjectWithUpdates`.
        - Always reanalyze `@RedactAuthorize` annotations instead of trusting `redacted=true` values
          on `redactedObjectWithUpdates`
- [ ] Jackson Module - When data is redacted, the application may want to prevent serialization of the property
  altogether. We need to provide a Jackson module to do this.
    - without Module `User { name: "test", password: { value: null, redacted: true }`
    - with Module `User { name: "test" }`
