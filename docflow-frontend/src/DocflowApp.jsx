import { useState, useEffect, useCallback } from "react";

const API = "https://docflow-backend-fhv4.onrender.com/api";

const G = `
  @import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@300;400;500;600;700;800&family=JetBrains+Mono:wght@400;500&display=swap');
  *,*::before,*::after{box-sizing:border-box;margin:0;padding:0}
  :root{
    --bg:#f4f6fb;--surface:#ffffff;--surface2:#f1f3f9;
    --border:#e4e8f0;--border2:#cdd2e8;
    --text:#0d1130;--text2:#4a5175;--muted:#8891b0;
    --accent:#2563eb;--accent-light:#eff4ff;--accent-dark:#1d4ed8;
    --success:#059669;--success-light:#ecfdf5;
    --danger:#dc2626;--danger-light:#fef2f2;
    --warn:#d97706;--warn-light:#fffbeb;
    --p1:#7c3aed;--p1l:#f5f3ff;--p2:#2563eb;--p2l:#eff4ff;--p3:#059669;--p3l:#ecfdf5;
    --font:'Plus Jakarta Sans',sans-serif;--mono:'JetBrains Mono',monospace;
    --shadow:0 1px 3px #0d113008,0 4px 16px #0d113008;
    --shadow-lg:0 4px 24px #0d113014,0 1px 4px #0d113008;
    --r:10px;
  }
  body{background:var(--bg);color:var(--text);font-family:var(--font);min-height:100vh;-webkit-font-smoothing:antialiased}
  ::-webkit-scrollbar{width:5px}::-webkit-scrollbar-track{background:transparent}::-webkit-scrollbar-thumb{background:var(--border2);border-radius:99px}
  @keyframes fadeUp{from{opacity:0;transform:translateY(12px)}to{opacity:1;transform:translateY(0)}}
  @keyframes fadeIn{from{opacity:0}to{opacity:1}}
  @keyframes slideR{from{opacity:0;transform:translateX(-8px)}to{opacity:1;transform:translateX(0)}}
  @keyframes spin{to{transform:rotate(360deg)}}
`;

const authH = t => ({ Authorization: `Bearer ${t}`, "Content-Type": "application/json" });

async function apiFetch(path, opts = {}, token = null) {
  const h = {};
  if (token) h.Authorization = `Bearer ${token}`;
  if (!(opts.body instanceof FormData)) h["Content-Type"] = "application/json";
  const r = await fetch(API + path, { headers: h, ...opts });
  return r.json();
}

// ── ATOMS ──────────────────────────────────────────────
function Spin({ size = 18 }) {
  return <div style={{ width: size, height: size, border: "2px solid var(--border2)", borderTopColor: "var(--accent)", borderRadius: "50%", animation: "spin .7s linear infinite", display: "inline-block", flexShrink: 0 }} />;
}

function Badge({ children, color = "var(--accent)", bg = "var(--accent-light)", border = "#2563eb30" }) {
  return <span style={{ display: "inline-flex", alignItems: "center", gap: 3, background: bg, color, border: `1px solid ${border}`, borderRadius: 5, padding: "2px 8px", fontSize: 11, fontFamily: "var(--mono)", fontWeight: 600, letterSpacing: ".04em", whiteSpace: "nowrap" }}>{children}</span>;
}

function StatusBadge({ status }) {
  const map = { APPROVED: ["var(--success)", "var(--success-light)", "#05966930"], REJECTED: ["var(--danger)", "var(--danger-light)", "#dc262630"], REVIEW: ["var(--warn)", "var(--warn-light)", "#d9770630"] };
  const [c, bg, b] = map[status] || ["var(--muted)", "var(--surface2)", "var(--border)"];
  return <Badge color={c} bg={bg} border={b}>{status}</Badge>;
}

function PBadge({ p }) {
  const m = { 1: ["var(--p1)", "var(--p1l)", "#7c3aed30", "HIGH"], 2: ["var(--p2)", "var(--p2l)", "#2563eb30", "MEDIUM"], 3: ["var(--p3)", "var(--p3l)", "#05966930", "LOW"] };
  const [c, bg, b, label] = m[p] || m[3];
  return <Badge color={c} bg={bg} border={b}>P{p} {label}</Badge>;
}

function Btn({ children, onClick, variant = "primary", loading, disabled, full, size = "md", style: s }) {
  const vs = { primary: { bg: "var(--accent)", color: "#fff", border: "none" }, outline: { bg: "transparent", color: "var(--accent)", border: "1px solid var(--accent)" }, ghost: { bg: "transparent", color: "var(--text2)", border: "1px solid var(--border)" }, danger: { bg: "var(--danger)", color: "#fff", border: "none" }, success: { bg: "var(--success)", color: "#fff", border: "none" } };
  const v = vs[variant] || vs.primary;
  const pad = size === "sm" ? "6px 12px" : size === "lg" ? "12px 28px" : "9px 20px";
  return (
    <button onClick={onClick} disabled={disabled || loading}
      style={{ background: v.bg, color: v.color, border: v.border, borderRadius: 8, padding: pad, fontFamily: "var(--font)", fontWeight: 600, fontSize: size === "sm" ? 12 : size === "lg" ? 15 : 13, cursor: disabled || loading ? "not-allowed" : "pointer", display: "inline-flex", alignItems: "center", justifyContent: "center", gap: 7, opacity: disabled ? .5 : 1, transition: "opacity .15s", width: full ? "100%" : "auto", ...s }}
      onMouseEnter={e => { if (!disabled && !loading) e.currentTarget.style.opacity = ".85"; }}
      onMouseLeave={e => { e.currentTarget.style.opacity = "1"; }}>
      {loading && <Spin size={13} />}{children}
    </button>
  );
}

function Field({ label, hint, children }) {
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 5 }}>
      {label && <label style={{ fontSize: 12, fontWeight: 600, color: "var(--text2)" }}>{label}</label>}
      {children}
      {hint && <div style={{ fontSize: 11, color: "var(--muted)", fontFamily: "var(--mono)" }}>{hint}</div>}
    </div>
  );
}

function Input({ label, hint, type = "text", value, onChange, placeholder, required, disabled, icon }) {
  const [focus, setFocus] = useState(false);
  return (
    <Field label={label} hint={hint}>
      <div style={{ position: "relative" }}>
        {icon && <span style={{ position: "absolute", left: 10, top: "50%", transform: "translateY(-50%)", fontSize: 13, color: "var(--muted)", pointerEvents: "none" }}>{icon}</span>}
        <input type={type} value={value} onChange={onChange} placeholder={placeholder} required={required} disabled={disabled}
          onFocus={() => setFocus(true)} onBlur={() => setFocus(false)}
          style={{ width: "100%", background: "var(--surface)", border: `1.5px solid ${focus ? "var(--accent)" : "var(--border)"}`, borderRadius: 8, padding: `9px 12px 9px ${icon ? "32px" : "12px"}`, color: "var(--text)", fontFamily: "var(--font)", fontSize: 13, outline: "none", transition: "border-color .15s", opacity: disabled ? .6 : 1 }} />
      </div>
    </Field>
  );
}

function Select({ label, hint, value, onChange, options }) {
  return (
    <Field label={label} hint={hint}>
      <select value={value} onChange={onChange} style={{ background: "var(--surface)", border: "1.5px solid var(--border)", borderRadius: 8, padding: "9px 12px", color: "var(--text)", fontFamily: "var(--font)", fontSize: 13, outline: "none", cursor: "pointer" }}>
        {options.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
      </select>
    </Field>
  );
}

function Card({ children, style: s }) {
  return <div style={{ background: "var(--surface)", border: "1px solid var(--border)", borderRadius: "var(--r)", boxShadow: "var(--shadow)", ...s }}>{children}</div>;
}

function Empty({ icon, title, sub, action }) {
  return (
    <div style={{ display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", padding: "48px 24px", gap: 10, textAlign: "center" }}>
      <div style={{ fontSize: 38, opacity: .2 }}>{icon}</div>
      <div style={{ fontWeight: 700, fontSize: 15 }}>{title}</div>
      {sub && <div style={{ fontSize: 13, color: "var(--muted)", maxWidth: 300 }}>{sub}</div>}
      {action}
    </div>
  );
}

function Toast({ msg, type = "info", onClose }) {
  useEffect(() => { const t = setTimeout(onClose, 4000); return () => clearTimeout(t); }, [onClose]);
  const c = { info: "var(--accent)", success: "var(--success)", error: "var(--danger)", warn: "var(--warn)" };
  return (
    <div style={{ position: "fixed", bottom: 24, right: 24, zIndex: 9999, background: "var(--surface)", border: "1px solid var(--border)", borderLeft: `3px solid ${c[type]}`, borderRadius: 10, padding: "13px 18px", minWidth: 260, maxWidth: 380, boxShadow: "var(--shadow-lg)", animation: "fadeUp .3s ease", display: "flex", alignItems: "center", gap: 10 }}>
      <span style={{ fontSize: 16 }}>{type === "success" ? "✓" : type === "error" ? "✕" : "ℹ"}</span>
      <span style={{ fontSize: 13, fontWeight: 500, flex: 1 }}>{msg}</span>
      <button onClick={onClose} style={{ background: "none", border: "none", cursor: "pointer", color: "var(--muted)", fontSize: 18 }}>×</button>
    </div>
  );
}

// ── SIDEBAR ────────────────────────────────────────────
function Sidebar({ page, setPage, user, onLogout, hasRules }) {
  const nav = [
    { id: "setup", icon: "⚙", label: "Rule Setup", warn: !hasRules },
    { id: "dashboard", icon: "◻", label: "Dashboard" },
    { id: "upload", icon: "↑", label: "Upload Document" },
    { id: "pipeline", icon: "⬡", label: "Priority Pipeline" },
    { id: "decisions", icon: "◆", label: "Decisions" },
    { id: "forward", icon: "→", label: "Forward Document" },
    { id: "audit", icon: "≡", label: "Audit Trail" },
  ];
  return (
    <aside style={{ width: 228, background: "var(--surface)", borderRight: "1px solid var(--border)", display: "flex", flexDirection: "column", height: "100vh", position: "sticky", top: 0, flexShrink: 0 }}>
      <div style={{ padding: "20px 18px 16px", borderBottom: "1px solid var(--border)" }}>
        <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
          <img src="/docflow-icon.jpeg" width={36} height={36} alt="DocFlow" />
          <div style={{ fontWeight: 800, fontSize: 17, letterSpacing: "-.03em" }}>Flow<span style={{ color: "var(--accent)" }}>Doc</span></div>
        </div>
        <div style={{ fontSize: 10, color: "var(--muted)", fontFamily: "var(--mono)", marginTop: 2, letterSpacing: ".08em" }}>INTELLIGENT WORKFLOW</div>
      </div>

      {!hasRules && (
        <div style={{ margin: "10px 10px 0", background: "var(--warn-light)", border: "1px solid #d9770630", borderRadius: 8, padding: "8px 11px", fontSize: 11, color: "var(--warn)", fontWeight: 600 }}>
          ⚠ Set up rules first before uploading documents
        </div>
      )}

      <nav style={{ flex: 1, padding: "10px 8px", overflowY: "auto" }}>
        {nav.map(item => {
          const active = page === item.id;
          return (
            <button key={item.id} onClick={() => setPage(item.id)}
              style={{ width: "100%", display: "flex", alignItems: "center", gap: 9, padding: "9px 11px", background: active ? "var(--accent-light)" : "transparent", border: "none", borderRadius: 8, color: active ? "var(--accent)" : "var(--text2)", fontFamily: "var(--font)", fontWeight: active ? 700 : 500, fontSize: 13, cursor: "pointer", transition: "all .12s", textAlign: "left", marginBottom: 1 }}
              onMouseEnter={e => { if (!active) { e.currentTarget.style.background = "var(--surface2)"; e.currentTarget.style.color = "var(--text)"; } }}
              onMouseLeave={e => { if (!active) { e.currentTarget.style.background = "transparent"; e.currentTarget.style.color = "var(--text2)"; } }}>
              <span style={{ fontSize: 14, width: 18, textAlign: "center", flexShrink: 0 }}>{item.icon}</span>
              <span style={{ flex: 1 }}>{item.label}</span>
              {item.warn && <span style={{ background: "var(--danger)", color: "#fff", fontSize: 9, fontWeight: 800, padding: "1px 5px", borderRadius: 99 }}>!</span>}
            </button>
          );
        })}
      </nav>

      <div style={{ padding: "12px 14px", borderTop: "1px solid var(--border)" }}>
        <div style={{ display: "flex", alignItems: "center", gap: 9, marginBottom: 10 }}>
          <div style={{ width: 30, height: 30, borderRadius: "50%", background: "var(--accent-light)", display: "flex", alignItems: "center", justifyContent: "center", fontWeight: 800, fontSize: 12, color: "var(--accent)", flexShrink: 0 }}>
            {(user?.name || "U")[0].toUpperCase()}
          </div>
          <div style={{ flex: 1, minWidth: 0 }}>
            <div style={{ fontSize: 12, fontWeight: 700, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{user?.name || "User"}</div>
            <div style={{ fontSize: 10, color: "var(--muted)", fontFamily: "var(--mono)" }}>{user?.role || "STAFF"}</div>
          </div>
        </div>
        <Btn variant="ghost" onClick={onLogout} full size="sm">Sign out</Btn>
        <div style={{ textAlign: "center", marginTop: 8, fontSize: 10, color: "var(--muted)", letterSpacing: ".03em" }}>
          © 2026 <span style={{ fontWeight: 700 }}>Aravind R</span>
        </div>
      </div>
    </aside>
  );
}

function PageWrap({ title, subtitle, action, children }) {
  return (
    <div style={{ flex: 1, overflowY: "auto", padding: "30px 34px", animation: "fadeUp .3s ease" }}>
      <div style={{ display: "flex", alignItems: "flex-start", justifyContent: "space-between", marginBottom: 24 }}>
        <div>
          <h1 style={{ fontSize: 21, fontWeight: 800, letterSpacing: "-.03em", marginBottom: 3 }}>{title}</h1>
          {subtitle && <p style={{ fontSize: 13, color: "var(--muted)" }}>{subtitle}</p>}
        </div>
        {action}
      </div>
      {children}
    </div>
  );
}

// ── LOGIN ──────────────────────────────────────────────
function LoginPage({ onLogin, onGoRegister }) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function submit() {
    if (!email || !password) { setError("Please fill all fields"); return; }
    setLoading(true); setError("");
    try {
      const d = await apiFetch("/auth/login", { method: "POST", body: JSON.stringify({ email, password }) });
      if (d.success) onLogin(d.data, email);
      else setError(d.error || "Invalid credentials");
    } catch { setError("Cannot connect to server. Make sure Spring Boot is running on port 8080."); }
    setLoading(false);
  }

  return (
    <div style={{ minHeight: "100vh", display: "flex", background: "var(--bg)" }}>
      {/* Left */}
      <div style={{ flex: 1, background: "linear-gradient(145deg, #1e3a8a 0%, #2563eb 60%, #60a5fa 100%)", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", padding: 52, position: "relative", overflow: "hidden" }}>
        <div style={{ position: "absolute", inset: 0, backgroundImage: "radial-gradient(circle at 15% 85%, #ffffff15 0%, transparent 45%), radial-gradient(circle at 85% 15%, #ffffff08 0%, transparent 45%)" }} />
        <div style={{ position: "relative", zIndex: 1, color: "#fff", maxWidth: 380 }}>
          <div style={{ fontSize: 44, fontWeight: 800, letterSpacing: "-.04em", marginBottom: 14 }}>Flow<span style={{ opacity: .65 }}>Doc</span></div>
          <div style={{ fontSize: 16, opacity: .85, lineHeight: 1.75, marginBottom: 36 }}>
            Universal document intelligence system for any organization — process, route, and track all your documents automatically.
          </div>
          <div style={{ display: "grid", gap: 14 }}>
            {[
              { icon: "⚙", title: "Your rules, your logic", desc: "Define approval conditions that match exactly what your process requires" },
              { icon: "⬡", title: "Priority-based routing", desc: "Documents are evaluated and sorted by priority level and department" },
              { icon: "♦", title: "Clear decisions with reasons", desc: "Every approval or rejection includes the exact reason why" },
              { icon: "≡", title: "Full audit trail", desc: "Every action is logged — who did what, when, and why" },
            ].map(f => (
              <div key={f.title} style={{ display: "flex", alignItems: "flex-start", gap: 12 }}>
                <div style={{ width: 36, height: 36, borderRadius: 9, background: "rgba(255,255,255,0.15)", display: "flex", alignItems: "center", justifyContent: "center", fontSize: 15, flexShrink: 0, backdropFilter: "blur(4px)" }}>{f.icon}</div>
                <div>
                  <div style={{ fontWeight: 700, fontSize: 13, color: "#fff", marginBottom: 2 }}>{f.title}</div>
                  <div style={{ fontSize: 12, opacity: .75, lineHeight: 1.5, color: "#fff" }}>{f.desc}</div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Right */}
      <div style={{ width: 460, display: "flex", alignItems: "center", justifyContent: "center", padding: 48 }}>
        <div style={{ width: "100%", animation: "fadeUp .4s ease" }}>
          <h2 style={{ fontSize: 25, fontWeight: 800, letterSpacing: "-.03em", marginBottom: 5 }}>Welcome back</h2>
          <p style={{ fontSize: 13, color: "var(--muted)", marginBottom: 28 }}>Sign in to your FlowDoc workspace</p>

          <div style={{ display: "flex", flexDirection: "column", gap: 14 }}>
            <Input label="Email" type="email" value={email} onChange={e => setEmail(e.target.value)} placeholder="you@organization.com" icon="✉" required />
            <Input label="Password" type="password" value={password} onChange={e => setPassword(e.target.value)} placeholder="••••••••" icon="🔒" required />
            {error && <div style={{ background: "var(--danger-light)", border: "1px solid #dc262630", borderRadius: 8, padding: "10px 14px", fontSize: 12, color: "var(--danger)", fontWeight: 500 }}>{error}</div>}
            <Btn onClick={submit} loading={loading} full size="lg" style={{ marginTop: 4 }}>Sign In →</Btn>
          </div>

          <div style={{ textAlign: "center", marginTop: 20, fontSize: 13, color: "var(--muted)" }}>
            No account? <span onClick={onGoRegister} style={{ color: "var(--accent)", cursor: "pointer", fontWeight: 600 }}>Create one →</span>
          </div>
        </div>
      </div>
      <div style={{ position: "fixed", bottom: 14, right: 24, fontSize: 11, color: "var(--muted)", letterSpacing: ".02em" }}>
        © 2026 Designed & Developed by <span style={{ fontWeight: 700, color: "var(--text2)" }}>Aravind R</span>
      </div>
    </div>
  );
}

// ── REGISTER ───────────────────────────────────────────
function RegisterPage({ onGoLogin }) {
  const [form, setForm] = useState({ name: "", email: "", password: "", role: "STAFF" });
  const [loading, setLoading] = useState(false);
  const [msg, setMsg] = useState(null);
  const set = (k, v) => setForm(f => ({ ...f, [k]: v }));

  async function submit() {
    if (!form.name || !form.email || !form.password) { setMsg({ type: "error", text: "All fields required" }); return; }
    setLoading(true); setMsg(null);
    try {
      const d = await apiFetch("/auth/register", { method: "POST", body: JSON.stringify(form) });
      setMsg(d.success ? { type: "success", text: "Account created! You can now sign in." } : { type: "error", text: d.error || "Registration failed" });
    } catch { setMsg({ type: "error", text: "Cannot connect to server" }); }
    setLoading(false);
  }

  return (
    <div style={{ minHeight: "100vh", display: "flex", alignItems: "center", justifyContent: "center", background: "var(--bg)", padding: 24 }}>
      <div style={{ width: "100%", maxWidth: 460, animation: "fadeUp .4s ease" }}>
        <div style={{ textAlign: "center", marginBottom: 24 }}>
          <div style={{ fontSize: 26, fontWeight: 800, letterSpacing: "-.04em", marginBottom: 4 }}>Flow<span style={{ color: "var(--accent)" }}>Doc</span></div>
          <p style={{ fontSize: 13, color: "var(--muted)" }}>Create your workspace account</p>
        </div>
        <Card style={{ padding: 26 }}>
          <div style={{ display: "flex", flexDirection: "column", gap: 14 }}>
            <Input label="Full Name" value={form.name} onChange={e => set("name", e.target.value)} placeholder="name" required />
            <Input label="Email" type="email" value={form.email} onChange={e => set("email", e.target.value)} placeholder="admin@college.edu" required />
            <Input label="Password" type="password" value={form.password} onChange={e => set("password", e.target.value)} placeholder="minimum 6 characters" required />
            <Select label="Role" value={form.role} onChange={e => set("role", e.target.value)}
              hint="Admin can create rules, manage users, and view full audit logs"
              options={[{ value: "ADMIN", label: "Admin — full access" }, { value: "STAFF", label: "Staff — upload & forward" }, { value: "VIEWER", label: "Viewer — read only" }]} />
            {msg && <div style={{ background: msg.type === "success" ? "var(--success-light)" : "var(--danger-light)", border: `1px solid ${msg.type === "success" ? "#05966930" : "#dc262630"}`, borderRadius: 8, padding: "10px 14px", fontSize: 12, color: msg.type === "success" ? "var(--success)" : "var(--danger)", fontWeight: 500 }}>{msg.text}</div>}
            <Btn onClick={submit} loading={loading} full size="lg">Create Account →</Btn>
          </div>
        </Card>
        <div style={{ textAlign: "center", marginTop: 16, fontSize: 13, color: "var(--muted)" }}>
          Already have an account? <span onClick={onGoLogin} style={{ color: "var(--accent)", cursor: "pointer", fontWeight: 600 }}>Sign in</span>
        </div>
      </div>
    </div>
  );
}

// ── RULE SETUP ─────────────────────────────────────────
function RuleSetupPage({ token, setToast, onRulesReady }) {
  const [rules, setRules] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({ ruleName: "", conditionDescription: "", thresholdValue: "0.80", priority: "1" });
  const sf = (k, v) => setForm(f => ({ ...f, [k]: v }));

  async function load() {
    setLoading(true);
    const r = await apiFetch("/rules", {}, token).catch(() => null);
    if (r?.success) setRules(r.data || []);
    setLoading(false);
  }

  useEffect(() => { load(); }, []);

  async function addRule() {
    if (!form.ruleName || !form.thresholdValue) { setToast({ msg: "Rule name and threshold required", type: "error" }); return; }
    setSaving(true);
    const r = await fetch(`${API}/rules`, { method: "POST", headers: authH(token), body: JSON.stringify({ ruleName: form.ruleName, conditionDescription: form.conditionDescription, thresholdValue: parseFloat(form.thresholdValue), priority: parseInt(form.priority), active: true }) }).then(x => x.json()).catch(() => null);
    if (r?.success) { setToast({ msg: "Rule added!", type: "success" }); setForm({ ruleName: "", conditionDescription: "", thresholdValue: "0.80", priority: "1" }); load(); }
    else setToast({ msg: "Failed to add rule", type: "error" });
    setSaving(false);
  }

  async function del(id) { await fetch(`${API}/rules/${id}`, { method: "DELETE", headers: authH(token) }); load(); }
  async function toggle(id) { await fetch(`${API}/rules/${id}/toggle`, { method: "PUT", headers: authH(token) }); load(); }

  const pColors = { 1: ["var(--p1)", "var(--p1l)"], 2: ["var(--p2)", "var(--p2l)"], 3: ["var(--p3)", "var(--p3l)"] };

  return (
    <PageWrap title="Rule Setup" subtitle="Rules determine if a document is approved or rejected. They are evaluated in priority order (1 first)."
      action={rules.length > 0 && <Btn variant="success" onClick={onRulesReady}>Continue to Dashboard →</Btn>}>

      {/* How it works */}
      <div style={{ background: "var(--accent-light)", border: "1px solid #2563eb20", borderRadius: 10, padding: "14px 18px", marginBottom: 22, display: "grid", gridTemplateColumns: "repeat(3,1fr)", gap: 14 }}>
        {[{ n: "1", t: "Create rules", d: "Define a condition — e.g. minimum OCR confidence score needed to pass" }, { n: "2", t: "Set priority", d: "Priority 1 is checked first. All rules must pass for approval." }, { n: "3", t: "Auto decision", d: "System approves or rejects and shows exact reason to the user." }].map(s => (
          <div key={s.n} style={{ display: "flex", gap: 10 }}>
            <div style={{ width: 26, height: 26, borderRadius: "50%", background: "var(--accent)", color: "#fff", fontWeight: 800, fontSize: 12, display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0 }}>{s.n}</div>
            <div><div style={{ fontWeight: 700, fontSize: 12, color: "var(--accent)", marginBottom: 2 }}>{s.t}</div><div style={{ fontSize: 11, color: "var(--text2)", lineHeight: 1.5 }}>{s.d}</div></div>
          </div>
        ))}
      </div>

      <div style={{ display: "grid", gridTemplateColumns: "420px 1fr", gap: 18, alignItems: "start" }}>
        {/* Add form */}
        <Card style={{ padding: 20 }}>
          <div style={{ fontWeight: 700, fontSize: 14, marginBottom: 16 }}>➕ Add New Rule</div>
          <div style={{ display: "flex", flexDirection: "column", gap: 13 }}>
            <Input label="Rule Name *" value={form.ruleName} onChange={e => sf("ruleName", e.target.value)} placeholder="e.g. Minimum Document Quality Check" />
            <Input label="Description (what does this rule check?)" value={form.conditionDescription} onChange={e => sf("conditionDescription", e.target.value)} placeholder="e.g. OCR confidence must exceed the threshold to be approved" />
            <Field label="Threshold — documents below this % will be REJECTED" hint={`Currently set to ${(parseFloat(form.thresholdValue || 0) * 100).toFixed(0)}% — documents must score above this to pass`}>
              <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
                <input type="range" min="0" max="1" step="0.01" value={form.thresholdValue} onChange={e => sf("thresholdValue", e.target.value)} style={{ flex: 1, accentColor: "var(--accent)" }} />
                <span style={{ fontWeight: 800, fontSize: 18, color: "var(--accent)", fontFamily: "var(--mono)", minWidth: 44 }}>{(parseFloat(form.thresholdValue || 0) * 100).toFixed(0)}%</span>
              </div>
            </Field>
            <Select label="Priority" value={form.priority} onChange={e => sf("priority", e.target.value)}
              hint="Priority 1 rules are checked before priority 2, 3..."
              options={[{ value: "1", label: "Priority 1 — HIGH (most important, checked first)" }, { value: "2", label: "Priority 2 — MEDIUM" }, { value: "3", label: "Priority 3 — LOW (checked last)" }]} />
            <Btn onClick={addRule} loading={saving} full>Add Rule</Btn>
          </div>
        </Card>

        {/* Rules list */}
        <Card style={{ padding: 20 }}>
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 16 }}>
            <div style={{ fontWeight: 700, fontSize: 14 }}>Active Rules — in Priority Order</div>
            <Badge color="var(--text2)" bg="var(--surface2)" border="var(--border)">{rules.length} rules</Badge>
          </div>
          {loading ? <div style={{ display: "flex", justifyContent: "center", padding: 28 }}><Spin size={24} /></div>
            : rules.length === 0 ? <Empty icon="⚙" title="No rules yet" sub="Add your first rule. At least one rule is needed before uploading documents." />
            : (
              <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
                {rules.map((rule, i) => {
                  const [pc, pbg] = pColors[rule.priority] || pColors[3];
                  return (
                    <div key={rule.ruleId} style={{ border: "1px solid var(--border)", borderRadius: 9, padding: "12px 14px", background: rule.active ? "var(--surface)" : "var(--surface2)", opacity: rule.active ? 1 : .55, animation: `slideR .25s ease ${i * .05}s both` }}>
                      <div style={{ display: "flex", gap: 10, alignItems: "flex-start" }}>
                        <div style={{ width: 28, height: 28, borderRadius: 7, background: pbg, color: pc, fontWeight: 800, fontSize: 13, display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0 }}>{rule.priority}</div>
                        <div style={{ flex: 1 }}>
                          <div style={{ display: "flex", alignItems: "center", gap: 7, marginBottom: 3 }}>
                            <span style={{ fontWeight: 700, fontSize: 13 }}>{rule.ruleName}</span>
                            {!rule.active && <Badge color="var(--muted)" bg="var(--surface2)" border="var(--border)">DISABLED</Badge>}
                          </div>
                          <div style={{ fontSize: 11, color: "var(--muted)", marginBottom: 5 }}>{rule.conditionDescription || "No description provided"}</div>
                          <span style={{ fontSize: 11, fontFamily: "var(--mono)", background: rule.active ? "var(--danger-light)" : "var(--surface2)", color: rule.active ? "var(--danger)" : "var(--muted)", padding: "2px 8px", borderRadius: 4, fontWeight: 600 }}>
                            Reject if below {(rule.thresholdValue * 100).toFixed(0)}% confidence
                          </span>
                        </div>
                        <div style={{ display: "flex", gap: 6, flexShrink: 0 }}>
                          <Btn variant="ghost" size="sm" onClick={() => toggle(rule.ruleId)}>{rule.active ? "Disable" : "Enable"}</Btn>
                          <Btn variant="danger" size="sm" onClick={() => del(rule.ruleId)}>✕</Btn>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
        </Card>
      </div>
    </PageWrap>
  );
}

// ── DASHBOARD ──────────────────────────────────────────
function DashboardPage({ token, user, setPage }) {
  const [decisions, setDecisions] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    apiFetch("/decisions", {}, token).then(d => { if (d.success) setDecisions(d.data || []); setLoading(false); }).catch(() => setLoading(false));
  }, [token]);

  const total = decisions.length;
  const approved = decisions.filter(d => d.decisionType === "APPROVED").length;
  const rejected = decisions.filter(d => d.decisionType === "REJECTED").length;
  const review = decisions.filter(d => d.decisionType === "REVIEW").length;

  return (
    <PageWrap title={`Good ${new Date().getHours() < 12 ? "morning" : "afternoon"}, ${(user?.name || "there").split(" ")[0]}`}
      subtitle={new Date().toLocaleDateString("en-IN", { weekday: "long", year: "numeric", month: "long", day: "numeric" })}>

      <div style={{ display: "grid", gridTemplateColumns: "repeat(4,1fr)", gap: 14, marginBottom: 22 }}>
        {[{ l: "Total Documents", v: total, c: "var(--accent)", icon: "📄" }, { l: "Approved", v: approved, c: "var(--success)", icon: "✅" }, { l: "Rejected", v: rejected, c: "var(--danger)", icon: "❌" }, { l: "Under Review", v: review, c: "var(--warn)", icon: "⚠️" }].map(s => (
          <Card key={s.l} style={{ padding: "18px 20px" }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", marginBottom: 10 }}><span style={{ fontSize: 20 }}>{s.icon}</span></div>
            <div style={{ fontSize: 30, fontWeight: 800, color: s.c, letterSpacing: "-.03em", marginBottom: 2 }}>{loading ? <Spin size={22} /> : s.v}</div>
            <div style={{ fontSize: 12, color: "var(--muted)", fontWeight: 500 }}>{s.l}</div>
          </Card>
        ))}
      </div>

      <div style={{ display: "grid", gridTemplateColumns: "2fr 1fr 1fr", gap: 14, marginBottom: 22 }}>
        <div onClick={() => setPage("upload")} style={{ background: "linear-gradient(140deg, #1e3a8a, #2563eb)", borderRadius: "var(--r)", padding: "20px 22px", cursor: "pointer", color: "#fff", transition: "transform .15s, box-shadow .15s", boxShadow: "var(--shadow)" }}
          onMouseEnter={e => { e.currentTarget.style.transform = "translateY(-2px)"; e.currentTarget.style.boxShadow = "var(--shadow-lg)"; }}
          onMouseLeave={e => { e.currentTarget.style.transform = "translateY(0)"; e.currentTarget.style.boxShadow = "var(--shadow)"; }}>
          <div style={{ fontSize: 22, marginBottom: 8 }}>↑</div>
          <div style={{ fontWeight: 700, fontSize: 14, marginBottom: 3 }}>Upload Document</div>
          <div style={{ fontSize: 12, opacity: .8 }}>Auto-classified, rule-evaluated and routed by priority</div>
        </div>
        {[{ l: "Priority Pipeline", icon: "⬡", p: "pipeline" }, { l: "Audit Trail", icon: "≡", p: "audit" }].map(a => (
          <Card key={a.p} style={{ padding: "20px 22px", cursor: "pointer" }} onClick={() => setPage(a.p)}>
            <div style={{ fontSize: 20, marginBottom: 8 }}>{a.icon}</div>
            <div style={{ fontWeight: 700, fontSize: 13 }}>{a.l}</div>
          </Card>
        ))}
      </div>

      <Card>
        <div style={{ padding: "14px 18px", borderBottom: "1px solid var(--border)", fontWeight: 700, fontSize: 13 }}>Recent Decisions</div>
        {loading ? <div style={{ display: "flex", justifyContent: "center", padding: 28 }}><Spin size={24} /></div>
          : decisions.length === 0 ? <Empty icon="📄" title="No documents yet" sub="Upload your first document to get started" action={<Btn onClick={() => setPage("upload")} size="sm">Upload Document</Btn>} />
          : decisions.slice(0, 8).map((d, i) => (
            <div key={d.decisionId || i} style={{ display: "flex", alignItems: "center", justifyContent: "space-between", padding: "11px 18px", borderBottom: i < 7 ? "1px solid var(--border)" : "none", animation: `fadeIn .3s ease ${i * .04}s both` }}>
              <div style={{ display: "flex", alignItems: "center", gap: 11 }}>
                <div style={{ width: 34, height: 34, borderRadius: 8, background: "var(--surface2)", display: "flex", alignItems: "center", justifyContent: "center", fontSize: 15, flexShrink: 0 }}>📄</div>
                <div>
                  <div style={{ fontWeight: 600, fontSize: 13 }}>Document #{d.document?.documentId || "—"}</div>
                  <div style={{ fontSize: 11, color: "var(--muted)", fontFamily: "var(--mono)" }}>{d.decisionSource || "SYSTEM"}</div>
                </div>
              </div>
              <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
                {d.document?.priority && <PBadge p={d.document.priority} />}
                <StatusBadge status={d.decisionType} />
                <span style={{ fontSize: 11, color: "var(--muted)", fontFamily: "var(--mono)" }}>{d.decisionTime ? new Date(d.decisionTime).toLocaleDateString("en-IN") : "—"}</span>
              </div>
            </div>
          ))}
      </Card>
    </PageWrap>
  );
}

// ── UPLOAD ─────────────────────────────────────────────
function UploadPage({ token, setToast, setPage, setLastUpload }) {
  const [file, setFile] = useState(null);
  const [dragging, setDragging] = useState(false);
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);

  function onDrop(e) { e.preventDefault(); setDragging(false); const f = e.dataTransfer.files[0]; if (f) setFile(f); }

  async function upload() {
    if (!file) { setToast({ msg: "Please select a file first", type: "warn" }); return; }

    // Check if rules exist before uploading
    const rulesCheck = await apiFetch("/rules", {}, token).catch(() => null);
    if (!rulesCheck?.success || !rulesCheck?.data?.length) {
      setToast({ msg: "⚠ No rules configured! Please set up rules first.", type: "error" });
      setResult({ error: true });
      return;
    }

    setLoading(true); setResult(null);
    try {
      const fd = new FormData(); fd.append("file", file);
      const r = await fetch(`${API}/upload`, { method: "POST", headers: { Authorization: `Bearer ${token}` }, body: fd });
      const d = await r.json();
      if (d.success) { setResult(d.data); setLastUpload(d.data); setToast({ msg: "Document processed!", type: "success" }); }
      else setToast({ msg: d.error || "Upload failed", type: "error" });
    } catch { setToast({ msg: "Cannot connect to server", type: "error" }); }
    setLoading(false);
  }

  const approved = result?.decision === "APPROVED";
  const rejected = result?.decision === "REJECTED";

  return (
    <PageWrap title="Upload Document" subtitle="Any document type — the system classifies it, applies your rules in priority order, and routes it automatically">
      <div style={{ maxWidth: 620 }}>
        <div onDragOver={e => { e.preventDefault(); setDragging(true); }} onDragLeave={() => setDragging(false)} onDrop={onDrop} onClick={() => document.getElementById("fup").click()}
          style={{ border: `2px dashed ${dragging ? "var(--accent)" : file ? "var(--success)" : "var(--border2)"}`, borderRadius: 12, padding: "38px 24px", textAlign: "center", cursor: "pointer", transition: "all .2s", marginBottom: 14, background: dragging ? "var(--accent-light)" : file ? "var(--success-light)" : "var(--surface)" }}>
          <input id="fup" type="file" style={{ display: "none" }} onChange={e => setFile(e.target.files[0])} />
          {file ? (
            <><div style={{ fontSize: 32, marginBottom: 8 }}>✓</div><div style={{ fontWeight: 700, fontSize: 14, color: "var(--success)", marginBottom: 3 }}>{file.name}</div><div style={{ fontSize: 12, color: "var(--muted)", fontFamily: "var(--mono)" }}>{(file.size / 1024).toFixed(1)} KB · click to change</div></>
          ) : (
            <><div style={{ fontSize: 38, marginBottom: 10, opacity: .25 }}>↑</div><div style={{ fontWeight: 700, fontSize: 14, marginBottom: 5 }}>{dragging ? "Drop it here" : "Drag & drop your document"}</div><div style={{ fontSize: 12, color: "var(--muted)" }}>or click to browse — PDF, DOCX, PNG, JPG</div></>
          )}
        </div>

        <Btn onClick={upload} loading={loading} disabled={!file} full size="lg" style={{ marginBottom: 18 }}>
          {loading ? "Processing document..." : "Upload & Process →"}
        </Btn>

        {result?.error && (
          <Card style={{ overflow: "hidden", animation: "fadeUp .4s ease" }}>
            <div style={{ padding: "13px 18px", background: "var(--danger-light)", display: "flex", alignItems: "center", gap: 10 }}>
              <span style={{ fontSize: 22 }}>⚠</span>
              <div>
                <div style={{ fontWeight: 800, fontSize: 15, color: "var(--danger)" }}>No Rules Configured!</div>
                <div style={{ fontSize: 12, color: "var(--text2)" }}>You must set up rules before uploading documents</div>
              </div>
            </div>
            <div style={{ padding: "14px 18px" }}>
              <div style={{ fontSize: 13, color: "var(--text2)", marginBottom: 12 }}>
                Go to Rule Setup and add at least one rule before uploading documents.
              </div>
              <Btn variant="primary" size="sm" onClick={() => setPage("setup")}>Go to Rule Setup →</Btn>
            </div>
          </Card>
        )}

        {result && !result.error && (
          <Card style={{ overflow: "hidden", animation: "fadeUp .4s ease" }}>
            <div style={{ padding: "13px 18px", background: approved ? "var(--success-light)" : rejected ? "var(--danger-light)" : "var(--warn-light)", display: "flex", alignItems: "center", gap: 10 }}>
              <span style={{ fontSize: 18 }}>{approved ? "✅" : rejected ? "❌" : "⚠️"}</span>
              <div>
                <div style={{ fontWeight: 700, fontSize: 14, color: approved ? "var(--success)" : rejected ? "var(--danger)" : "var(--warn)" }}>
                  Document {approved ? "Approved" : rejected ? "Rejected" : "Under Review"}
                </div>
                <div style={{ fontSize: 11, color: "var(--muted)" }}>Doc #{result.documentId} · Processing complete</div>
              </div>
            </div>
            <div style={{ padding: "16px 18px" }}>
              <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gap: 10, marginBottom: 14 }}>
                {[{ l: "Document ID", v: `#${result.documentId}` }, { l: "Decision", v: result.decision }, { l: "Status", v: result.status }].map(r => (
                  <div key={r.l} style={{ background: "var(--surface2)", borderRadius: 7, padding: "9px 11px" }}>
                    <div style={{ fontSize: 10, color: "var(--muted)", fontFamily: "var(--mono)", marginBottom: 2 }}>{r.l}</div>
                    <div style={{ fontSize: 13, fontWeight: 700, fontFamily: "var(--mono)" }}>{r.v}</div>
                  </div>
                ))}
              </div>
              {rejected && (
                <div style={{ background: "var(--danger-light)", border: "1px solid #dc262620", borderRadius: 8, padding: "11px 14px", marginBottom: 14 }}>
                  <div style={{ fontSize: 12, fontWeight: 700, color: "var(--danger)", marginBottom: 4 }}>❌ Reason for Rejection</div>
                  <div style={{ fontSize: 13, color: "var(--text)", lineHeight: 1.6 }}>
                    The document did not meet the minimum quality threshold defined in your rules. The OCR confidence score was below the required level. Please resubmit a clearer, higher-quality version of the document.
                  </div>
                </div>
              )}
              <div style={{ display: "flex", gap: 8 }}>
                <Btn variant="outline" size="sm" onClick={() => setPage("decisions")}>View Decision</Btn>
                <Btn variant="ghost" size="sm" onClick={() => setPage("forward")}>Forward Document</Btn>
                {rejected && <Btn variant="ghost" size="sm" onClick={() => { setFile(null); setResult(null); }}>Try Again</Btn>}
              </div>
            </div>
          </Card>
        )}
      </div>
    </PageWrap>
  );
}

// ── PRIORITY PIPELINE ──────────────────────────────────
function PipelinePage({ token }) {
  const [decisions, setDecisions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [groupBy, setGroupBy] = useState("priority");

  useEffect(() => {
    apiFetch("/decisions", {}, token).then(d => { if (d.success) setDecisions(d.data || []); setLoading(false); }).catch(() => setLoading(false));
  }, [token]);

  const groups = (() => {
    if (groupBy === "priority") return { "Priority 1 — HIGH": decisions.filter(d => (d.document?.priority || 3) === 1), "Priority 2 — MEDIUM": decisions.filter(d => (d.document?.priority || 3) === 2), "Priority 3 — LOW": decisions.filter(d => (d.document?.priority || 3) === 3) };
    if (groupBy === "department") { const g = {}; decisions.forEach(d => { const k = d.document?.department || "Unassigned"; (g[k] = g[k] || []).push(d); }); return g; }
    return { "APPROVED": decisions.filter(d => d.decisionType === "APPROVED"), "REJECTED": decisions.filter(d => d.decisionType === "REJECTED"), "REVIEW": decisions.filter(d => d.decisionType === "REVIEW") };
  })();

  const pCols = { "Priority 1 — HIGH": ["var(--p1)", "var(--p1l)", "#7c3aed18"], "Priority 2 — MEDIUM": ["var(--p2)", "var(--p2l)", "#2563eb18"], "Priority 3 — LOW": ["var(--p3)", "var(--p3l)", "#05966918"], "APPROVED": ["var(--success)", "var(--success-light)", "#05966918"], "REJECTED": ["var(--danger)", "var(--danger-light)", "#dc262618"], "REVIEW": ["var(--warn)", "var(--warn-light)", "#d9770618"] };

  return (
    <PageWrap title="Priority Pipeline" subtitle="Documents sorted by priority level, department, or decision status"
      action={
        <div style={{ display: "flex", gap: 6 }}>
          {["priority", "department", "status"].map(g => (
            <button key={g} onClick={() => setGroupBy(g)} style={{ padding: "7px 14px", border: "1px solid", borderColor: groupBy === g ? "var(--accent)" : "var(--border)", borderRadius: 7, background: groupBy === g ? "var(--accent-light)" : "var(--surface)", color: groupBy === g ? "var(--accent)" : "var(--text2)", fontFamily: "var(--font)", fontWeight: 600, fontSize: 12, cursor: "pointer" }}>
              {g.charAt(0).toUpperCase() + g.slice(1)}
            </button>
          ))}
        </div>
      }>
      {loading ? <div style={{ display: "flex", justifyContent: "center", padding: 48 }}><Spin size={28} /></div>
        : decisions.length === 0 ? <Empty icon="⬡" title="No documents yet" sub="Upload documents to see them in the pipeline" />
        : (
          <div style={{ display: "grid", gridTemplateColumns: `repeat(${Math.min(Object.keys(groups).length, 3)},1fr)`, gap: 14, alignItems: "start" }}>
            {Object.entries(groups).map(([group, docs]) => {
              const [pc, pbg, pborder] = pCols[group] || ["var(--text2)", "var(--surface2)", "var(--border)"];
              return (
                <div key={group} style={{ background: "var(--surface)", border: `1px solid ${pborder}`, borderRadius: "var(--r)", overflow: "hidden", boxShadow: "var(--shadow)" }}>
                  <div style={{ padding: "11px 14px", background: pbg, borderBottom: "1px solid var(--border)", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                    <div style={{ fontWeight: 700, fontSize: 12, color: pc }}>{group}</div>
                    <Badge color={pc} bg={pbg} border={pborder}>{docs.length}</Badge>
                  </div>
                  <div style={{ padding: 10, display: "flex", flexDirection: "column", gap: 7, maxHeight: 580, overflowY: "auto" }}>
                    {docs.length === 0 ? <div style={{ textAlign: "center", padding: "20px 10px", color: "var(--muted)", fontSize: 12 }}>No documents</div>
                      : docs.map((d, i) => (
                        <div key={d.decisionId || i} style={{ background: "var(--bg)", border: "1px solid var(--border)", borderRadius: 8, padding: "10px 12px", animation: `fadeIn .3s ease ${i * .04}s both` }}>
                          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", marginBottom: 5 }}>
                            <span style={{ fontWeight: 700, fontSize: 13 }}>Doc #{d.document?.documentId || "—"}</span>
                            <StatusBadge status={d.decisionType} />
                          </div>
                          <div style={{ fontSize: 11, color: "var(--muted)", fontFamily: "var(--mono)", marginBottom: 5 }}>
                            {d.document?.documentType || "GENERAL"} · {d.document?.department || "No dept assigned"}
                          </div>
                          {d.document?.folderPath && <div style={{ fontSize: 10, color: "var(--muted)", fontFamily: "var(--mono)", background: "var(--surface)", padding: "2px 7px", borderRadius: 4, display: "inline-block" }}>📁 {d.document.folderPath}</div>}
                          {d.decisionType === "REJECTED" && (
                            <div style={{ marginTop: 6, fontSize: 11, color: "var(--danger)", background: "var(--danger-light)", padding: "4px 8px", borderRadius: 5 }}>
                              ❌ {(d.decisionSource || "").replace("FAILED_RULE: ", "") || "Threshold not met"}
                            </div>
                          )}
                        </div>
                      ))}
                  </div>
                </div>
              );
            })}
          </div>
        )}
    </PageWrap>
  );
}

// ── DECISIONS ──────────────────────────────────────────
function DecisionsPage({ token }) {
  const [decisions, setDecisions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState("ALL");
  const [search, setSearch] = useState("");

  useEffect(() => {
    apiFetch("/decisions", {}, token).then(d => { if (d.success) setDecisions(d.data || []); setLoading(false); }).catch(() => setLoading(false));
  }, [token]);

  const filtered = decisions.filter(d => {
    if (filter !== "ALL" && d.decisionType !== filter) return false;
    if (search && !`${d.document?.documentId}`.includes(search) && !(d.decisionSource || "").toLowerCase().includes(search.toLowerCase())) return false;
    return true;
  });

  return (
    <PageWrap title="Decision Results" subtitle="All automated decisions — each rejection shows the exact reason">
      <div style={{ display: "flex", gap: 10, marginBottom: 18, alignItems: "center", flexWrap: "wrap" }}>
        <div style={{ display: "flex", gap: 6 }}>
          {["ALL", "APPROVED", "REJECTED", "REVIEW"].map(f => (
            <button key={f} onClick={() => setFilter(f)} style={{ padding: "6px 13px", border: "1px solid", borderColor: filter === f ? "var(--accent)" : "var(--border)", borderRadius: 7, background: filter === f ? "var(--accent-light)" : "var(--surface)", color: filter === f ? "var(--accent)" : "var(--text2)", fontFamily: "var(--font)", fontWeight: 600, fontSize: 12, cursor: "pointer" }}>{f}</button>
          ))}
        </div>
        <input value={search} onChange={e => setSearch(e.target.value)} placeholder="Search by document ID..."
          style={{ flex: 1, minWidth: 180, background: "var(--surface)", border: "1px solid var(--border)", borderRadius: 8, padding: "7px 12px", fontFamily: "var(--font)", fontSize: 12, outline: "none", color: "var(--text)" }} />
        <span style={{ fontSize: 12, color: "var(--muted)", fontFamily: "var(--mono)" }}>{filtered.length} results</span>
      </div>

      {loading ? <div style={{ display: "flex", justifyContent: "center", padding: 40 }}><Spin size={26} /></div>
        : filtered.length === 0 ? <Empty icon="◆" title="No decisions found" sub="Try a different filter or upload a document" />
        : (
          <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
            {filtered.map((d, i) => (
              <Card key={d.decisionId || i} style={{ overflow: "hidden", animation: `slideR .25s ease ${i * .03}s both`, borderLeft: `3px solid ${d.decisionType === "APPROVED" ? "var(--success)" : d.decisionType === "REJECTED" ? "var(--danger)" : "var(--warn)"}` }}>
                <div style={{ padding: "14px 18px" }}>
                  <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start" }}>
                    <div style={{ flex: 1 }}>
                      <div style={{ display: "flex", alignItems: "center", gap: 9, marginBottom: 9, flexWrap: "wrap" }}>
                        <span style={{ fontWeight: 700, fontSize: 15 }}>Document #{d.document?.documentId || "—"}</span>
                        <StatusBadge status={d.decisionType} />
                        {d.document?.priority && <PBadge p={d.document.priority} />}
                        {d.document?.department && <Badge color="var(--text2)" bg="var(--surface2)" border="var(--border)">📁 {d.document.department}</Badge>}
                      </div>
                      <div style={{ display: "flex", gap: 20, marginBottom: 10, flexWrap: "wrap" }}>
                        {[{ l: "Decision #", v: d.decisionId }, { l: "Evaluated by", v: d.evaluatedBy || "SYSTEM" }, { l: "Version", v: `v${d.versionNumber || 1}` }, { l: "Folder", v: d.document?.folderPath || "—" }].map(r => (
                          <div key={r.l}><div style={{ fontSize: 10, color: "var(--muted)", fontFamily: "var(--mono)" }}>{r.l}</div><div style={{ fontSize: 12, fontFamily: "var(--mono)", fontWeight: 600 }}>{r.v}</div></div>
                        ))}
                      </div>
                      {d.decisionType === "REJECTED" && (
                        <div style={{ background: "var(--danger-light)", border: "1px solid #dc262618", borderRadius: 7, padding: "10px 13px" }}>
                          <div style={{ fontSize: 11, fontWeight: 700, color: "var(--danger)", marginBottom: 4 }}>❌ Reason for Rejection</div>
                          <div style={{ fontSize: 12, color: "var(--text)", fontFamily: "var(--mono)" }}>
                            {(d.decisionSource || "").replace("FAILED_RULE: ", "") || "Did not meet required quality threshold"}
                          </div>
                          {d.decisionReason && <div style={{ fontSize: 12, color: "var(--text2)", marginTop: 4 }}>{d.decisionReason}</div>}
                        </div>
                      )}
                    </div>
                    <div style={{ fontSize: 11, color: "var(--muted)", fontFamily: "var(--mono)", marginLeft: 16, flexShrink: 0, textAlign: "right" }}>
                      {d.decisionTime ? new Date(d.decisionTime).toLocaleString("en-IN") : "—"}
                    </div>
                  </div>
                </div>
              </Card>
            ))}
          </div>
        )}
    </PageWrap>
  );
}

// ── FORWARD ────────────────────────────────────────────
function ForwardPage({ token, setToast, lastUpload }) {
  const [docId, setDocId] = useState(lastUpload?.documentId?.toString() || "");
  const [dept, setDept] = useState("");
  const [note, setNote] = useState("");
  const [loading, setLoading] = useState(false);
  const [history, setHistory] = useState([]);

  async function loadHistory() {
    if (!docId) return;
    const r = await apiFetch(`/forward/${docId}`, {}, token).catch(() => null);
    if (r?.success) setHistory(r.data || []);
  }

  useEffect(() => { if (docId) loadHistory(); }, [docId]);

  async function forward() {
    if (!docId || !dept.trim()) { setToast({ msg: "Document ID and destination are required", type: "error" }); return; }
    setLoading(true);
    const r = await fetch(`${API}/forward`, { method: "POST", headers: authH(token), body: JSON.stringify({ documentId: Number(docId), forwardedTo: dept.trim(), forwardType: "DEPARTMENT", note }) }).then(x => x.json()).catch(() => null);
    if (r?.success) { setToast({ msg: `Forwarded to ${dept}!`, type: "success" }); setDept(""); setNote(""); loadHistory(); }
    else setToast({ msg: r?.error || "Forward failed", type: "error" });
    setLoading(false);
  }

  return (
    <PageWrap title="Forward Document" subtitle="Send any document to any department or person — type the destination freely, no fixed list">
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 18, alignItems: "start" }}>
        <Card style={{ padding: 20 }}>
          <div style={{ fontWeight: 700, fontSize: 14, marginBottom: 16 }}>Send Document</div>
          <div style={{ display: "flex", flexDirection: "column", gap: 13 }}>
            <Input label="Document ID *" value={docId} onChange={e => setDocId(e.target.value)} placeholder="e.g. 5" hint={lastUpload ? `Last uploaded: Doc #${lastUpload.documentId}` : "Enter the document ID from your upload"} />
            <Input label="Send To *" value={dept} onChange={e => setDept(e.target.value)} placeholder="Type freely — e.g. Scholarship Committee, Ward 4, Finance Dept, Dr. Kumar..." icon="→"
              hint="No fixed list — you can send to any department, person or team" />
            <Input label="Note (optional)" value={note} onChange={e => setNote(e.target.value)} placeholder="Reason for forwarding or additional message..." />
            <Btn onClick={forward} loading={loading} full>Forward Document →</Btn>
          </div>
        </Card>

        <Card style={{ padding: 20 }}>
          <div style={{ fontWeight: 700, fontSize: 14, marginBottom: 16 }}>Forward History — Doc #{docId || "—"}</div>
          {!docId ? <Empty icon="→" title="Enter a Document ID" sub="Type a document ID on the left to see where it has been forwarded" />
            : history.length === 0 ? <Empty icon="→" title="Not forwarded yet" sub="This document hasn't been sent anywhere yet" />
            : history.map((h, i) => (
              <div key={i} style={{ background: "var(--surface2)", borderRadius: 8, padding: "11px 13px", marginBottom: 8, animation: `fadeIn .25s ease ${i * .05}s both` }}>
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start" }}>
                  <div>
                    <div style={{ fontWeight: 700, fontSize: 13, marginBottom: 2 }}>→ {h.forwardedTo}</div>
                    <div style={{ fontSize: 11, color: "var(--muted)", fontFamily: "var(--mono)" }}>by {h.forwardedBy}</div>
                    {h.note && <div style={{ fontSize: 12, color: "var(--text2)", marginTop: 4, fontStyle: "italic" }}>"{h.note}"</div>}
                  </div>
                  <div style={{ fontSize: 10, color: "var(--muted)", fontFamily: "var(--mono)", textAlign: "right" }}>
                    {h.forwardedAt ? new Date(h.forwardedAt).toLocaleString("en-IN") : "—"}
                  </div>
                </div>
              </div>
            ))}
        </Card>
      </div>
    </PageWrap>
  );
}

// ── AUDIT ──────────────────────────────────────────────
function AuditPage({ token }) {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [docFilter, setDocFilter] = useState("");

  async function load() {
    setLoading(true);
    const r = await apiFetch(docFilter ? `/audit/${docFilter}` : "/audit", {}, token).catch(() => null);
    if (r?.success) setLogs(r.data || []);
    setLoading(false);
  }

  useEffect(() => { load(); }, [token]);

  const am = { UPLOADED: ["↑", "var(--accent)", "var(--accent-light)"], APPROVED: ["✓", "var(--success)", "var(--success-light)"], REJECTED: ["✕", "var(--danger)", "var(--danger-light)"], FORWARDED: ["→", "var(--p1)", "var(--p1l)"], EMAIL_SENT: ["✉", "var(--warn)", "var(--warn-light)"], ROLE_CHANGED: ["⚙", "var(--text2)", "var(--surface2)"] };

  return (
    <PageWrap title="Audit Trail" subtitle="Complete history of every action in the system — upload, approve, reject, forward, email">
      <div style={{ display: "flex", gap: 10, marginBottom: 18, alignItems: "flex-end" }}>
        <div style={{ flex: 1, maxWidth: 280 }}>
          <Input label="Filter by Document ID" value={docFilter} onChange={e => setDocFilter(e.target.value)} placeholder="Leave blank for all logs" hint="Enter document ID to see only its history" />
        </div>
        <Btn onClick={load}>Apply</Btn>
        <Btn variant="ghost" onClick={() => { setDocFilter(""); setTimeout(load, 0); }}>Clear</Btn>
      </div>

      {loading ? <div style={{ display: "flex", justifyContent: "center", padding: 40 }}><Spin size={26} /></div>
        : logs.length === 0 ? <Empty icon="≡" title="No audit logs found" sub="Actions will appear here as the system processes documents" />
        : (
          <Card>
            {logs.map((log, i) => {
              const [icon, color, bg] = am[log.action] || ["•", "var(--muted)", "var(--surface2)"];
              return (
                <div key={log.id || i} style={{ display: "flex", alignItems: "flex-start", gap: 13, padding: "12px 18px", borderBottom: i < logs.length - 1 ? "1px solid var(--border)" : "none", animation: `fadeIn .25s ease ${Math.min(i, 10) * .03}s both` }}>
                  <div style={{ width: 30, height: 30, borderRadius: 8, background: bg, color, display: "flex", alignItems: "center", justifyContent: "center", fontSize: 13, fontWeight: 700, flexShrink: 0 }}>{icon}</div>
                  <div style={{ flex: 1 }}>
                    <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 3 }}>
                      <Badge color={color} bg={bg} border={`${color}30`}>{log.action}</Badge>
                      {log.documentId && <span style={{ fontSize: 11, color: "var(--muted)", fontFamily: "var(--mono)" }}>Doc #{log.documentId}</span>}
                    </div>
                    <div style={{ fontSize: 13, color: "var(--text)", marginBottom: 2 }}>{log.details || "—"}</div>
                    <div style={{ fontSize: 11, color: "var(--muted)", fontFamily: "var(--mono)" }}>by {log.performedBy || "SYSTEM"} · {log.actionTime ? new Date(log.actionTime).toLocaleString("en-IN") : "—"}</div>
                  </div>
                </div>
              );
            })}
          </Card>
        )}
    </PageWrap>
  );
}

// ── ROOT APP ───────────────────────────────────────────
export default function App() {
  const [screen, setScreen] = useState("login");
  const [token, setToken] = useState(null);
  const [user, setUser] = useState(null);
  const [page, setPage] = useState("setup");
  const [toast, setToast] = useState(null);
  const [lastUpload, setLastUpload] = useState(null);
  const [hasRules, setHasRules] = useState(false);

  async function handleLogin(tok, email) {
    setToken(tok);
    const u = await apiFetch("/users/me", {}, tok).catch(() => null);
    setUser(u?.success ? u.data : { email, name: email.split("@")[0], role: "STAFF" });
    const r = await apiFetch("/rules", {}, tok).catch(() => null);
    const count = r?.data?.length || 0;
    setHasRules(count > 0);
    setPage(count > 0 ? "dashboard" : "setup");
    setScreen("app");
  }

  function handleLogout() { setToken(null); setUser(null); setScreen("login"); setPage("setup"); setHasRules(false); }
  const showToast = useCallback(t => setToast(t), []);

  return (
    <>
      <style>{G}</style>
      {screen === "login" && <LoginPage onLogin={handleLogin} onGoRegister={() => setScreen("register")} />}
      {screen === "register" && <RegisterPage onGoLogin={() => setScreen("login")} />}
      {screen === "app" && (
        <div style={{ display: "flex", minHeight: "100vh" }}>
          <Sidebar page={page} setPage={setPage} user={user} onLogout={handleLogout} hasRules={hasRules} />
          <main style={{ flex: 1, overflowY: "auto" }}>
            {page === "setup"     && <RuleSetupPage  token={token} setToast={showToast} onRulesReady={() => { setHasRules(true); setPage("dashboard"); }} />}
            {page === "dashboard" && <DashboardPage  token={token} user={user} setPage={setPage} />}
            {page === "upload"    && <UploadPage     token={token} setToast={showToast} setPage={setPage} setLastUpload={setLastUpload} />}
            {page === "pipeline"  && <PipelinePage   token={token} />}
            {page === "decisions" && <DecisionsPage  token={token} />}
            {page === "forward"   && <ForwardPage    token={token} setToast={showToast} lastUpload={lastUpload} />}
            {page === "audit"     && <AuditPage      token={token} />}
          </main>
        </div>
      )}
      {toast && <Toast msg={toast.msg} type={toast.type} onClose={() => setToast(null)} />}
    </>
  );
}