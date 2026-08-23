# ScenaBL — Claude Code Instructions

## Project

ScenaBL is a student Android application built with Kotlin and Jetpack Compose.

The functional requirements are defined in:

- `ai-context/ScenaBL_Improved_SRS.md`

The technical architecture is defined in:

- `ai-context/ScenaBL_Implementation_Guide.md`

Professor requirements are defined in:

- `ai-context/ProffesorSuggestion.md`

These documents must be treated as project constraints.

## Development Rules

- Do not implement the entire application at once.
- Work incrementally, one development phase at a time.
- Before starting a phase, explain what will be implemented.
- Only modify files necessary for the current phase.
- Do not implement features belonging to future phases.
- After completing a phase, verify that the project still builds.
- Do not make unrelated refactoring or architectural changes.
- Do not change the SRS unless explicitly requested.

## Git Rules

- Never automatically push to GitHub.
- Do not create one giant commit containing the entire application.
- Create one focused commit on the main branch after each completed development phase.
- Before creating a commit, summarize the changes.
- Ask for approval before committing.
- Ask for explicit approval before pushing.
- Never amend or rewrite previous commits unless explicitly requested.

## Phase Workflow

For every phase:

1. Explain the goal of the phase.
2. Inspect the relevant existing code.
3. Implement only this phase.
4. Run/build the project and fix errors caused by the implementation.
5. Summarize the changes.
6. Show which files were created or modified.
7. Ask for approval before committing.
8. Commit only the changes belonging to this phase.
9. Stop and wait for my next instruction (I will push manually when ready).

## Architecture Rules

Follow the architecture defined in
`ai-context/ScenaBL_Implementation_Guide.md`.

The intended architecture is:

UI → ViewModel → Repository → Remote Data Source → Firebase

UI and ViewModels must not directly access Firebase SDK APIs.

Do not introduce additional architectural frameworks unless explicitly requested.

## Scope Control

If a requested feature belongs to a later phase, do not implement it early.

If an architectural decision is unclear or conflicts with the SRS, stop and ask for clarification rather than making a large assumption.

## Development Workflow

- Implement the application incrementally in logical sections.
- Never implement multiple sections in one step unless explicitly requested.
- Before implementing a section, briefly describe the plan.
- After completing a section, build/test it where appropriate.
- Stop and wait for user approval before starting the next section.
- Never commit or push changes unless explicitly instructed by the user.
- Do not make unrelated changes.
- Keep the project buildable after each section whenever reasonably possible.
- Follow `ai-context/ScenaBL_Improved_SRS.md` as the functional specification.
- Follow `ai-context/ScenaBL_Implementation_Guide.md` as the technical implementation plan.


## Implementation Order
1. Project foundation (Verify config, Configure Firebase, Set up dependencies/structure, Create basic MVVM/DI).
2. Data layer (Create models, Firebase remote data sources, repositories, Firestore structure/Rules).
3. Authentication (Registration/login, Roles, Guest mode, Profile).
4. Viewer core functionality (Home, Search/filtering, Title details).
5. Personal functionality (Watchlists, Reservations, Cancellation).
6. Reviews and ratings.
7. Organizer functionality (Dashboard, Title CRUD, Performance CRUD, Reservation overview).
8. UI/UX refinement (Navigation, States, Material 3, Validation).
9. Testing and final verification.