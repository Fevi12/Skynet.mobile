# SKYNET Hybrid Intelligence Architecture

## Modules
- JARVIS: conversational interface, voice and workflow.
- SKYNET CORE: event ingestion and decision engine.
- WORLD STATE: registered nodes and telemetry.
- MEMORY: SQLite history and audit.
- DECISION: explainable risk scoring and task prioritization.
- RESILIENCE: supervised health checks and recovery proposals.
- DISPATCH: allow-listed actions requiring approval.

## Platforms
Android, Linux, macOS, iOS and isolated Kali Linux sensors.

## Control loop
Observe -> Orient -> Decide -> Approve -> Act -> Audit.

## Security
No arbitrary command execution, unrestricted remote control,
silent self-modification or unauthorized security scanning.
External actions require authenticated integrations and approval.

## Development
Run: python -m unittest discover -s tests -v
