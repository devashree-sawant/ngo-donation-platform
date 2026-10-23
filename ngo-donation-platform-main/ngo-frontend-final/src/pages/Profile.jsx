import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import api, { getApiError } from "../api/axios";
import AppLayout from "../components/AppLayout";
import "../App.css";

const money = (value) => new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR", maximumFractionDigits: 2 }).format(value || 0);

function Profile() {
    const navigate = useNavigate();
    const role = localStorage.getItem("role");
    const id = localStorage.getItem("id");
    const isNgo = role === "NGO";
    const resource = isNgo ? "ngos" : "donors";
    const [profile, setProfile] = useState({ name: "", email: "", phone: "", address: "", description: "" });
    const [metrics, setMetrics] = useState({ campaigns: 0, contributions: 0, donated: 0 });
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [message, setMessage] = useState("");
    const [error, setError] = useState("");

    useEffect(() => {
        if (!id || !role) {
            navigate("/", { replace: true });
            return;
        }
        const load = async () => {
            setLoading(true);
            try {
                const response = await api.get(`/${resource}/${id}`);
                const data = response.data;
                setProfile({ name: data.name || "", email: data.email || "", phone: data.phone || "", address: data.address || "", description: data.description || "" });
                if (isNgo) {
                    try {
                        const needsResponse = await api.get("/needs");
                        const mine = needsResponse.data.filter((need) => Number(need.ngo?.id) === Number(id));
                        setMetrics({ campaigns: mine.length, contributions: 0, donated: mine.filter((n) => n.type === "MONEY").reduce((sum, n) => sum + Number(n.currentAmount || 0), 0) });
                    } catch (metricsError) { console.error("NGO profile metrics could not be loaded", metricsError); }
                } else {
                    try {
                        const historyResponse = await api.get(`/donors/${id}/contributions`);
                        const history = historyResponse.data;
                        setMetrics({ campaigns: 0, contributions: history.length, donated: history.filter((item) => item.type === "MONEY").reduce((sum, item) => sum + Number(item.amount || 0), 0) });
                    } catch (metricsError) { console.error("Donor profile metrics could not be loaded", metricsError); }
                }
            } catch (requestError) {
                setError(getApiError(requestError, "We couldn't load your profile."));
            } finally {
                setLoading(false);
            }
        };
        load();
    }, [id, role, resource, isNgo, navigate]);

    const initials = useMemo(() => profile.name.split(/\s+/).filter(Boolean).slice(0, 2).map((part) => part[0]).join("").toUpperCase() || "U", [profile.name]);

    const change = (event) => setProfile((current) => ({ ...current, [event.target.name]: event.target.value }));

    const submit = async (event) => {
        event.preventDefault();
        setSaving(true); setMessage(""); setError("");
        try {
            const payload = { name: profile.name.trim(), email: profile.email, phone: profile.phone.trim(), address: profile.address.trim() };
            if (isNgo) payload.description = profile.description.trim();
            await api.put(`/${resource}/${id}`, payload);
            localStorage.setItem("name", payload.name);
            setMessage("Your profile has been updated.");
        } catch (requestError) {
            setError(getApiError(requestError, "We couldn't save your profile."));
        } finally {
            setSaving(false);
        }
    };

    return <AppLayout>
        <div className="container profile-page">
            <div className="page-intro">
                <div><span className="eyebrow">YOUR ACCOUNT</span><h1>Profile & settings</h1><p>Keep your details up to date so your community knows who they're working with.</p></div>
                <button className="button-secondary" onClick={() => navigate(isNgo ? "/ngo" : "/donor")}>← Back to dashboard</button>
            </div>

            {loading ? <div className="panel loading-panel">Loading your profile…</div> : error && !profile.email ? <div className="notice notice-error">{error}</div> : <div className="profile-layout">
                <aside className="profile-summary panel">
                    <div className="profile-avatar">{initials}</div>
                    <span className="role-pill">{isNgo ? "NGO PARTNER" : "COMMUNITY DONOR"}</span>
                    <h2>{profile.name || "Your name"}</h2>
                    <p>{profile.email}</p>
                    <div className="profile-summary-rule" />
                    <div className="profile-summary-line"><span>Account type</span><strong>{isNgo ? "NGO" : "Donor"}</strong></div>
                    <div className="profile-summary-line"><span>Member ID</span><strong>#{id}</strong></div>
                    <p className="profile-summary-note">Your account details are used to manage your campaigns and contributions.</p>
                </aside>

                <section className="panel profile-form-panel">
                    <div className="section-heading"><div><span className="eyebrow">PERSONAL DETAILS</span><h2>Edit your information</h2><p>Your email is your sign-in ID and can't be changed here.</p></div><span className="section-icon">✎</span></div>
                    {message && <div className="notice notice-success">{message}</div>}
                    {error && <div className="notice notice-error">{error}</div>}
                    <form onSubmit={submit}>
                        <div className="field-grid">
                            <label className="form-field">{isNgo ? "NGO / organisation name" : "Full name"}<input name="name" value={profile.name} onChange={change} required maxLength={120} /></label>
                            <label className="form-field">Email address<input value={profile.email} readOnly disabled /></label>
                            <label className="form-field">Phone number<input name="phone" value={profile.phone} onChange={change} placeholder="Add a contact number" maxLength={30} /></label>
                            <label className="form-field">Location / address<input name="address" value={profile.address} onChange={change} placeholder="City, area or address" maxLength={250} /></label>
                            {isNgo && <label className="form-field field-full">About your NGO<textarea name="description" value={profile.description} onChange={change} placeholder="Tell donors about your mission and work" rows={4} maxLength={1000} /></label>}
                        </div>
                        <div className="form-footer"><p>Your changes are saved to your account.</p><button type="submit" disabled={saving}>{saving ? "Saving changes…" : "Save profile changes"}</button></div>
                    </form>
                </section>
            </div>}

            {!loading && !error && <section className="profile-metrics">
                {isNgo ? <>
                    <article className="metric-tile"><span className="metric-icon">▤</span><div><small>Campaigns created</small><strong>{metrics.campaigns}</strong></div></article>
                    <article className="metric-tile"><span className="metric-icon">₹</span><div><small>Funds raised</small><strong>{money(metrics.donated)}</strong></div></article>
                </> : <>
                    <article className="metric-tile"><span className="metric-icon">♡</span><div><small>Contributions made</small><strong>{metrics.contributions}</strong></div></article>
                    <article className="metric-tile"><span className="metric-icon">₹</span><div><small>Total money donated</small><strong>{money(metrics.donated)}</strong></div></article>
                </>}
            </section>}
        </div>
    </AppLayout>;
}

export default Profile;
