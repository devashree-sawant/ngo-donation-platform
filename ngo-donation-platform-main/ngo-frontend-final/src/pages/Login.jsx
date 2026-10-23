import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api, { getApiError } from "../api/axios";
import "../App.css";
import "./Login.css";

function Login() {
    const navigate = useNavigate();
    const [mode, setMode] = useState("login");
    const [role, setRole] = useState("DONOR");
    const [form, setForm] = useState({ name: "", email: "", password: "", phone: "", address: "", description: "" });
    const [busy, setBusy] = useState(false);
    const [error, setError] = useState("");

    const change = (event) => setForm({ ...form, [event.target.name]: event.target.value });
    const switchMode = (nextMode) => { setMode(nextMode); setError(""); };

    const finishLogin = (data) => {
        localStorage.setItem("token", data.token);
        localStorage.setItem("role", data.role);
        localStorage.setItem("id", data.id);
        localStorage.setItem("name", data.name || "");
        navigate(data.role === "NGO" ? "/ngo" : "/donor", { replace: true });
    };

    const submit = async (event) => {
        event.preventDefault(); setBusy(true); setError("");
        try {
            let response;
            if (mode === "login") {
                response = await api.post("/auth/login", { email: form.email.trim(), password: form.password });
            } else {
                const registration = { name: form.name.trim(), email: form.email.trim(), password: form.password, phone: form.phone.trim(), address: form.address.trim() };
                if (role === "NGO") registration.description = form.description.trim();
                response = await api.post(`/auth/register/${role.toLowerCase()}`, registration);
            }
            finishLogin(response.data);
        } catch (requestError) {
            setError(getApiError(requestError, mode === "login" ? "We couldn't sign you in. Check your details and try again." : "We couldn't create your account."));
        } finally { setBusy(false); }
    };

    return <main className="auth-page">
        <div className="auth-glow auth-glow-one" /><div className="auth-glow auth-glow-two" />
        <div className="auth-layout">
            <section className="auth-story">
                <div className="auth-brand"><span className="auth-brand-mark">♥</span><span><strong>HopeBridge</strong><small>Giving grows when we share.</small></span></div>
                <div className="story-copy"><span className="story-kicker"><span /> GOODNESS, CONNECTED</span><h1>Small acts.<br /><em>Lasting impact.</em></h1><p>Bring generous people and purpose-driven organisations together. Find a cause, support a need, and follow the difference you make.</p></div>
                <div className="story-feature-stack" aria-label="Platform features">
                    <div className="story-feature feature-main"><span className="feature-icon">♡</span><div><strong>Every contribution matters</strong><small>Money and essential items, all in one place.</small></div><span className="feature-arrow">↗</span></div>
                    <div className="story-feature feature-secondary"><span className="feature-icon gold-icon">✦</span><div><strong>Impact you can follow</strong><small>Transparent progress for every campaign.</small></div></div>
                </div>
                <div className="story-bottom"><span>For communities. For kindness. For change.</span><span>♥</span></div>
            </section>

            <section className="auth-panel">
                <div className="auth-panel-heading"><span className="eyebrow">WELCOME TO THE COMMUNITY</span><h2>{mode === "login" ? "Good to see you again." : "Let's make a difference."}</h2><p>{mode === "login" ? "Sign in to continue your impact." : "Create an account to get started."}</p></div>
                <div className="auth-switch" role="tablist" aria-label="Account access">
                    <button type="button" role="tab" aria-selected={mode === "login"} className={mode === "login" ? "selected" : "unselected"} onClick={() => switchMode("login")}>Sign in</button>
                    <button type="button" role="tab" aria-selected={mode === "register"} className={mode === "register" ? "selected" : "unselected"} onClick={() => switchMode("register")}>Create account</button>
                </div>
                {error && <div className="notice notice-error auth-error" role="alert">{error}</div>}
                <form className="auth-form" onSubmit={submit}>
                    {mode === "register" && <>
                        <label className="form-field">I want to join as<select value={role} onChange={(event) => setRole(event.target.value)}><option value="DONOR">A donor</option><option value="NGO">An NGO / organisation</option></select></label>
                        <label className="form-field">{role === "NGO" ? "Organisation name" : "Full name"}<input name="name" placeholder={role === "NGO" ? "e.g. Helping Hands NGO" : "Your full name"} value={form.name} onChange={change} autoComplete="name" required /></label>
                        {role === "NGO" && <label className="form-field">About your organisation<textarea name="description" placeholder="What cause do you work for?" value={form.description} onChange={change} rows={2} /></label>}
                        <div className="auth-two-fields"><label className="form-field">Phone <span>(optional)</span><input name="phone" placeholder="Contact number" value={form.phone} onChange={change} autoComplete="tel" /></label><label className="form-field">Location <span>(optional)</span><input name="address" placeholder="City / area" value={form.address} onChange={change} autoComplete="street-address" /></label></div>
                    </>}
                    <label className="form-field">Email address<input name="email" type="email" placeholder="you@example.com" value={form.email} onChange={change} autoComplete="email" required /></label>
                    <label className="form-field">Password<input name="password" type="password" placeholder={mode === "register" ? "At least 8 characters" : "Enter your password"} value={form.password} onChange={change} autoComplete={mode === "login" ? "current-password" : "new-password"} minLength={mode === "register" ? 8 : undefined} required /></label>
                    <button className="auth-submit" type="submit" disabled={busy}>{busy ? "Please wait…" : mode === "login" ? "Sign in to your account" : `Create ${role === "NGO" ? "NGO" : "donor"} account`} <span>→</span></button>
                </form>
                <p className="auth-privacy"><span>●</span> Your account helps keep your campaigns and contributions organised.</p>
            </section>
        </div>
    </main>;
}

export default Login;
