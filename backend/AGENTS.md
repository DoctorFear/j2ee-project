# Java Spring Boot Backend — AI Coding Standards

## 1. Core Principles

Write production-quality Java Spring Boot backend code that is correct, readable, simple, consistent, and maintainable.

**Code is written for humans to understand and maintain, not merely for machines to execute.**

Prioritize:

1. Correctness and intended behavior.
2. Clarity and readability.
3. Simplicity and maintainability.
4. Consistency with existing project conventions.
5. Performance when justified.
6. Conciseness.

Never sacrifice correctness, clarity, or maintainability to reduce code length.

Avoid unnecessary complexity, premature optimization, speculative abstractions, and overengineering.

## 2. Code Formatting

Prefer clear, structured, vertically organized code over dense or compressed formatting.

- Use 4 spaces for indentation.
- Follow consistent Java naming and brace conventions.
- Prefer a maximum line length of 100–120 characters unless the project specifies otherwise.
- Keep simple expressions on one line when readable.
- Split complex expressions, method signatures, builders, and method chains across multiple lines.
- Separate distinct logical operations with blank lines.
- Keep related statements together.
- Avoid excessive line breaks, unnecessary alignment, and deeply nested expressions.
- Never combine unrelated operations on the same line.
- Apply consistent formatting throughout modified code.

### Java Records — Mandatory

For every non-empty Java record:

- Place each component on its own line.
- Keep the opening parenthesis on the declaration line.
- Put the closing parenthesis on a separate line.
- Keep the opening brace on the same line as the closing parenthesis.
- Apply these rules even when the record has only one component.
- Keep empty records compact.

### Java Enums — Mandatory

For every non-empty Java enum:

- Place each constant on its own line.
- Include a trailing comma after every constant, including the last.
- Place the terminating semicolon on a separate line.
- Preserve this structure when enums contain fields, constructors, or methods.

### Method Chains and Builders

- Prefer vertical formatting for multi-step chains and builders.
- Make nested operations visually distinguishable.
- Introduce intermediate variables only when they improve clarity.
- Avoid compressing complex operations into single expressions.

Formatting must reveal logical structure without introducing unnecessary visual noise.

## 3. Clean Code and Design

### Responsibilities

- Give each class and method a clear, focused responsibility.
- Keep business logic separate from infrastructure concerns.
- Extract complex logic when doing so improves readability.
- Avoid excessive decomposition into trivial helper methods.

### Simplicity

- Prefer straightforward, explicit solutions.
- Avoid unnecessary classes, interfaces, layers, and design patterns.
- Apply SOLID principles pragmatically.
- Prefer composition over inheritance when appropriate.
- Introduce abstractions only when they solve an actual problem.

### Control Flow

- Prefer early returns when they reduce nesting.
- Keep conditions and execution paths easy to follow.
- Replace complex conditions with meaningful names when useful.
- Avoid unnecessary temporary variables and nested expressions.

### Duplication

- Eliminate meaningful duplication of business logic.
- Reuse existing implementations when appropriate.
- Do not abstract unrelated code merely because it looks similar.

### Naming

- Use descriptive, domain-specific names.
- Follow standard Java naming conventions.
- Maintain consistent business terminology.
- Avoid ambiguous abbreviations, generic identifiers, and misleading names.
- Prefer names that express intent rather than implementation details.

## 4. Spring Boot Architecture

### Controller

- Handle HTTP mapping, request validation, and response construction.
- Delegate business logic to the appropriate services.
- Avoid direct persistence operations.
- Preserve established API response conventions.

### Service

- Implement business rules in the appropriate service.
- Keep operations focused, explicit, and understandable.
- Define transaction boundaries intentionally.
- Avoid unnecessary service abstractions.

### Repository

- Encapsulate persistence operations.
- Write clear, maintainable queries.
- Avoid unnecessary database operations.
- Apply sorting, locking, and specialized queries only when justified.

### Entity

- Follow JPA requirements and existing project conventions.
- Maintain clear relationship ownership and encapsulation.
- Keep persistence entities separate from public API contracts.
- Avoid unnecessary bidirectional relationships and Lombok annotations.
- Do not use `@Data` on JPA entities by default.
- Protect database-generated identifiers from unintended modification.
- Prevent avoidable lazy-loading and N+1 query problems.

### DTO and API Contracts

- Prefer Java records for suitable immutable request and response DTOs.
- Apply Jakarta Bean Validation at appropriate boundaries.
- Do not expose JPA entities directly through public APIs unless explicitly required.
- Preserve existing API structures, validation behavior, and error contracts.

### Exception Handling

- Use meaningful domain-specific exceptions.
- Prefer centralized handling when appropriate.
- Never silently swallow errors.
- Avoid unnecessarily broad exception handling.
- Never expose sensitive implementation details in API responses.

### Dependency Injection

- Prefer constructor injection.
- Keep dependencies explicit and minimal.
- Avoid field injection.

## 5. Microservices

- Respect service ownership and architectural boundaries.
- Keep business logic within the responsible service.
- Never directly access another service's private database.
- Maintain consistent communication contracts.
- Reuse established communication patterns.
- Avoid unnecessary inter-service coupling.
- Do not introduce infrastructure, shared modules, or communication mechanisms without a concrete requirement.
- Preserve existing authentication, gateway, and service communication behavior.

## 6. Reliability and Security

- Validate untrusted input at appropriate boundaries.
- Handle expected failures explicitly.
- Preserve data integrity and transactional correctness.
- Consider concurrency only where shared state or business requirements demand it.
- Avoid unnecessary database queries and external calls.
- Never log credentials, access tokens, passwords, or sensitive information.
- Avoid introducing security vulnerabilities or exposing internal system details.

## 7. Testing

- Preserve observable behavior unless a change is explicitly requested.
- Add or update tests when functionality changes.
- Cover important business rules, failure scenarios, and edge cases.
- Prefer behavior-focused tests over implementation-dependent tests.
- Keep tests clear, deterministic, and maintainable.
- Avoid unnecessary mocking.
- Never modify unrelated tests to conceal defects.
- Never claim tests passed unless they were actually executed.

## 8. Project Consistency and Change Scope

Before changing code:

1. Inspect the relevant implementation and dependencies.
2. Understand existing architecture, conventions, and behavior.
3. Identify the smallest appropriate change.
4. Reuse established patterns and existing functionality.
5. Implement without introducing unrelated modifications.
6. Verify compatibility with affected components.

During implementation:

- Preserve existing behavior unless explicitly instructed otherwise.
- Keep changes localized to the requested functionality.
- Avoid unrelated renaming, restructuring, and reformatting.
- Do not invent requirements or assume hypothetical future needs.
- Avoid introducing dependencies without justification.
- Refactor only when necessary for correctness, clarity, or maintainability.
- Keep comments focused on non-obvious intent, constraints, and business rules.
- Do not add comments that merely restate the code.

## 9. AI Agent Execution Rules

When implementing or refactoring code:

- Understand the task and relevant context before making changes.
- Follow explicit requirements without inventing additional scope.
- Prefer the simplest correct implementation.
- Make design decisions consistent with the existing codebase.
- Apply mandatory formatting to newly created or modified code.
- Favor maintainability over cleverness.
- Avoid unnecessary abstractions and speculative engineering.
- Do not silently change existing contracts or behavior.
- Do not claim to have verified anything that was not actually checked.

When requirements are incomplete, distinguish confirmed requirements from assumptions. Ask for clarification only when the missing information materially affects correctness or safety; otherwise, proceed conservatively without inventing behavior.

## 10. Final Verification

Before completing a task, verify:

- **Correctness:** Does the implementation satisfy the requirements without unintended behavior changes?
- **Readability:** Is the logic, naming, and formatting clear?
- **Simplicity:** Can unnecessary complexity or abstraction be removed?
- **Architecture:** Does the implementation respect existing responsibilities and boundaries?
- **Consistency:** Does the code follow project conventions and mandatory formatting rules?
- **Scope:** Were unrelated modifications avoided?
- **Reliability:** Are validation, errors, transactions, and important edge cases handled appropriately?
- **Testing:** Were relevant tests executed, and are any limitations reported accurately?

Report implementation results, relevant verification outcomes, and unresolved issues concisely.

## 11. Rule Precedence

When instructions conflict, follow this priority:

1. Explicit task requirements, subject to correctness and security constraints.
2. Correctness and preservation of existing functional contracts.
3. Mandatory Java record and enum formatting rules.
4. Existing architecture and project-specific conventions.
5. Readability and maintainability.
6. Simplicity and justified performance.
7. Conciseness.

An enforced project formatter takes precedence over conflicting formatting preferences. If it conflicts with a mandatory formatting rule, report the conflict rather than repeatedly fighting the formatter.

**Final principle: Produce the simplest correct implementation that another developer can easily understand, review, extend, and maintain.**