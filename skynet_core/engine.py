"""SKYNET event-driven orchestration engine."""
from dataclasses import dataclass, field, asdict
from datetime import datetime, timezone
from enum import Enum
from pathlib import Path
from typing import Callable
import sqlite3
import json

class Mode(str, Enum):
    NOMINAL = "NOMINAL"
    ELEVATED = "ELEVATED"
    DEFENSIVE = "DEFENSIVE"

@dataclass
class NodeStatus:
    node_id: str
    platform: str
    role: str
    load: float = 0.0
    latency_ms: float = 0.0
    alert_score: float = 0.0
    online: bool = True

    def validate(self):
        if not self.node_id.strip():
            raise ValueError("Invalid node ID")
        for key in ("load", "alert_score"):
            if not 0 <= getattr(self, key) <= 1:
                raise ValueError(key + " outside allowed range")
        if self.latency_ms < 0:
            raise ValueError("Invalid latency")
        if not isinstance(self.online, bool):
            raise ValueError("Invalid online state")

@dataclass(frozen=True)
class Event:
    event_type: str
    node_id: str
    payload: dict

@dataclass(frozen=True)
class ActionPlan:
    action: str
    target: str
    reason: str
    priority: int
    requires_approval: bool = True

@dataclass
class WorldState:
    tick: int = 0
    mode: Mode = Mode.NOMINAL
    risk_score: float = 0
    nodes: dict = field(default_factory=dict)
    plans: list = field(default_factory=list)

class Memory:
    def __init__(self, path="skynet_memory.db"):
        self.db = sqlite3.connect(path)
        self.db.execute("""
        CREATE TABLE IF NOT EXISTS events(
          id INTEGER PRIMARY KEY,
          timestamp TEXT,
          category TEXT,
          payload TEXT
        )""")
        self.db.commit()

    def save(self, category, payload):
        self.db.execute(
          "INSERT INTO events(timestamp,category,payload) VALUES(?,?,?)",
          (datetime.now(timezone.utc).isoformat(),
           category, json.dumps(payload, ensure_ascii=False))
        )
        self.db.commit()

class Engine:
    def __init__(self, memory=None):
        self.state = WorldState()
        self.memory = memory or Memory()

    def register(self, node):
        node.validate()
        self.state.nodes[node.node_id] = node
        self.memory.save("NODE_REGISTERED", asdict(node))

    def ingest(self, event):
        if event.event_type != "telemetry":
            return
        node = self.state.nodes.get(event.node_id)
        if node is None:
            self.memory.save("UNKNOWN_NODE", {"node": event.node_id})
            return
        allowed = {"load", "latency_ms", "alert_score", "online"}
        for key, value in event.payload.items():
            if key not in allowed:
                raise ValueError("Unsupported telemetry: " + key)
            setattr(node, key, value)
        node.validate()
        self.memory.save("TELEMETRY", {
            "node": event.node_id, **event.payload
        })

    def assess(self):
        nodes = [n for n in self.state.nodes.values() if n.online]
        if nodes:
            overload = sum(n.load >= .85 for n in nodes)
            latency = sum(n.latency_ms >= 120 for n in nodes)
            alerts = sum(n.alert_score >= .60 for n in nodes)
            risk = min(1, (
                overload*.20 + latency*.15 + alerts*.35
            ) / len(nodes))
        else:
            risk = 0

        self.state.risk_score = round(risk, 2)
        self.state.mode = (
            Mode.DEFENSIVE if risk >= .70 else
            Mode.ELEVATED if risk >= .30 else
            Mode.NOMINAL
        )

        plans = []
        if self.state.mode == Mode.DEFENSIVE:
            plans.append(ActionPlan(
                "PAUSE_NONCRITICAL_WORK", "scheduler",
                "Defensive threshold reached", 100
            ))

        for n in nodes:
            if n.load >= .85:
                plans.append(ActionPlan(
                    "REVIEW_LOAD", n.node_id, "High load", 70
                ))
            if n.latency_ms >= 120:
                plans.append(ActionPlan(
                    "REVIEW_CONNECTIVITY", n.node_id,
                    "High latency", 60
                ))
            if n.alert_score >= .60:
                plans.append(ActionPlan(
                    "SECURITY_REVIEW", n.node_id,
                    "Elevated alert score", 90
                ))

        self.state.plans = sorted(
            plans, key=lambda p: p.priority, reverse=True
        )
        self.state.tick += 1
        self.memory.save("ASSESSMENT", self.status())
        return self.state.plans

    def status(self):
        return {
            "tick": self.state.tick,
            "mode": self.state.mode.value,
            "risk": self.state.risk_score,
            "nodes": [asdict(n) for n in self.state.nodes.values()],
            "plans": [asdict(p) for p in self.state.plans]
        }

class ApprovedDispatcher:
    """Allow-listed local handlers; never executes arbitrary shell commands."""
    def __init__(self):
        self.handlers: dict[str, Callable] = {}

    def register(self, action, handler):
        if not action or action in self.handlers:
            raise ValueError("Duplicate or invalid action")
        self.handlers[action] = handler

    def dispatch(self, plan, approved=False):
        if plan.requires_approval and not approved:
            return "BLOCKED: approval required"
        handler = self.handlers.get(plan.action)
        if handler is None:
            return "BLOCKED: action not registered"
        return handler(plan.target)
