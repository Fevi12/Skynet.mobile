import unittest
from skynet_core import Engine, Event, NodeStatus, Mode
from skynet_core.engine import ActionPlan, ApprovedDispatcher

class SkynetTests(unittest.TestCase):
    def test_decision_engine(self):
        e = Engine()
        e.register(NodeStatus("LINUX-1", "linux", "core"))
        e.ingest(Event("telemetry", "LINUX-1", {
            "load": .95, "latency_ms": 150,
            "alert_score": .9
        }))
        e.assess()
        self.assertEqual(e.state.mode, Mode.DEFENSIVE)

    def test_unknown_node(self):
        e = Engine()
        e.ingest(Event("telemetry", "UNKNOWN", {"load": .9}))
        self.assertEqual(len(e.state.nodes), 0)

    def test_approval_required(self):
        d = ApprovedDispatcher()
        d.register("SAFE_TASK", lambda target: "OK")
        p = ActionPlan("SAFE_TASK", "local", "test", 10)
        self.assertIn("BLOCKED", d.dispatch(p))
        self.assertEqual(d.dispatch(p, approved=True), "OK")

    def test_invalid_values(self):
        e = Engine()
        with self.assertRaises(ValueError):
            e.register(NodeStatus("BAD", "linux", "core", load=5))

if __name__ == "__main__":
    unittest.main()
