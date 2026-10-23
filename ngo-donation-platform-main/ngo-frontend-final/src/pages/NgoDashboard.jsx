import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api, { getApiError } from "../api/axios";
import AppLayout from "../components/AppLayout";
import "../App.css";

const emptyForm = { title: "", description: "", type: "MONEY", targetAmount: "", itemRequired: "", requiredQuantity: "" };
const money = (value) => new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR", maximumFractionDigits: 0 }).format(value || 0);

function NgoDashboard() {
    const navigate = useNavigate();
    const ngoId = Number(localStorage.getItem("id"));
    const ngoName = localStorage.getItem("name") || "your organisation";
    const [needs, setNeeds] = useState([]);
    const [form, setForm] = useState(emptyForm);
    const [editingId, setEditingId] = useState(null);
    const [busy, setBusy] = useState(false);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => { loadNeeds(); }, []);

    const loadNeeds = async () => {
        try {
            const response = await api.get("/needs");
            setNeeds(response.data.filter((need) => Number(need.ngo?.id) === ngoId));
            setError("");
        } catch (requestError) {
            setError(getApiError(requestError, "Unable to load your campaigns."));
        } finally { setLoading(false); }
    };

    const change = (event) => setForm({ ...form, [event.target.name]: event.target.value });

    const submit = async (event) => {
        event.preventDefault();
        if (form.type === "MONEY" && Number(form.targetAmount) <= 0) return setError("Enter a target amount greater than zero.");
        if (form.type === "ITEM" && (!form.itemRequired.trim() || Number(form.requiredQuantity) <= 0)) return setError("Enter an item name and required quantity.");
        const payload = {
            title: form.title.trim(), description: form.description.trim(), type: form.type,
            targetAmount: form.type === "MONEY" ? Number(form.targetAmount) : null,
            itemRequired: form.type === "ITEM" ? form.itemRequired.trim() : null,
            requiredQuantity: form.type === "ITEM" ? Number(form.requiredQuantity) : null,
        };
        setBusy(true); setError("");
        try {
            if (editingId) await api.put(`/needs/${editingId}`, payload);
            else await api.post("/needs", payload);
            setForm(emptyForm); setEditingId(null); await loadNeeds();
        } catch (requestError) { setError(getApiError(requestError, "Unable to save campaign.")); }
        finally { setBusy(false); }
    };

    const edit = (need) => {
        setEditingId(need.id);
        setForm({ title: need.title || "", description: need.description || "", type: need.type, targetAmount: need.targetAmount ?? "", itemRequired: need.itemRequired || "", requiredQuantity: need.requiredQuantity ?? "" });
        window.scrollTo({ top: 0, behavior: "smooth" });
    };

    const deleteNeed = async (id) => {
        if (!window.confirm("Delete this campaign? Campaigns with contribution history must be kept for records.")) return;
        try { await api.delete(`/needs/${id}`); await loadNeeds(); }
        catch (requestError) { setError(getApiError(requestError, "Unable to delete this campaign.")); }
    };

    const cancelEdit = () => { setEditingId(null); setForm(emptyForm); setError(""); };
    const totalRaised = needs.filter((need) => need.type === "MONEY").reduce((sum, need) => sum + Number(need.currentAmount || 0), 0);
    const activeCount = needs.filter((need) => need.status !== "COMPLETED").length;
    const itemCount = needs.filter((need) => need.type === "ITEM").reduce((sum, need) => sum + Number(need.receivedQuantity || 0), 0);

    return <AppLayout>
        <div className="container dashboard-container">
            <section className="hero-banner ngo-hero">
                <div className="hero-content"><span className="hero-kicker"><span /> NGO WORKSPACE</span><h1>Good work deserves<br /><em>to be seen.</em></h1><p>Welcome back, {ngoName}. Share your needs, keep supporters informed, and turn generosity into meaningful progress.</p><button className="hero-button" onClick={() => document.getElementById("campaign-form")?.scrollIntoView({ behavior: "smooth", block: "center" })}>＋ Create a campaign <span>→</span></button></div>
                <div className="hero-illustration" aria-hidden="true"><div className="illustration-ring ring-one" /><div className="illustration-ring ring-two" /><div className="illustration-leaf leaf-one">✦</div><div className="illustration-leaf leaf-two">✿</div><div className="impact-note"><span className="impact-note-icon">♥</span><div><strong>Community impact</strong><small>One campaign at a time</small></div></div></div>
            </section>

            <section className="metric-grid" aria-label="Campaign overview">
                <article className="metric-tile"><span className="metric-icon green-icon">▤</span><div><small>Total campaigns</small><strong>{needs.length}</strong><span className="metric-caption">Published by your NGO</span></div></article>
                <article className="metric-tile"><span className="metric-icon gold-icon">₹</span><div><small>Funds raised</small><strong>{money(totalRaised)}</strong><span className="metric-caption">Across money campaigns</span></div></article>
                <article className="metric-tile"><span className="metric-icon blue-icon">↗</span><div><small>Active campaigns</small><strong>{activeCount}</strong><span className="metric-caption">Still accepting support</span></div></article>
                <article className="metric-tile"><span className="metric-icon coral-icon">♡</span><div><small>Items received</small><strong>{itemCount}</strong><span className="metric-caption">Units pledged to date</span></div></article>
            </section>

            <section className="panel campaign-form-panel" id="campaign-form">
                <div className="section-heading"><div><span className="eyebrow">CAMPAIGN STUDIO</span><h2>{editingId ? "Edit your campaign" : "What does your community need?"}</h2><p>Be specific. Clear needs help supporters understand where their contribution goes.</p></div><span className="section-icon">✎</span></div>
                {error && <div className="notice notice-error" role="alert">{error}</div>}
                <form onSubmit={submit}>
                    <div className="field-grid">
                        <label className="form-field">Campaign title<input name="title" placeholder="e.g. School Supplies for 50 Children" value={form.title} onChange={change} maxLength={140} required /></label>
                        <label className="form-field">Type of need<select name="type" value={form.type} onChange={change} disabled={Boolean(editingId && needs.find((need) => need.id === editingId)?.status !== "PENDING")}><option value="MONEY">Money campaign</option><option value="ITEM">Items / supplies</option></select></label>
                        <label className="form-field field-full">Campaign description<textarea name="description" placeholder="Explain who this will help and why it matters…" value={form.description} onChange={change} rows={3} maxLength={1000} required /></label>
                        {form.type === "MONEY" ? <label className="form-field">Fundraising target (₹)<input type="number" name="targetAmount" min="0.01" step="0.01" placeholder="e.g. 25000" value={form.targetAmount} onChange={change} required /></label> : <><label className="form-field">Item needed<input name="itemRequired" placeholder="e.g. Food kit, notebook, backpack" value={form.itemRequired} onChange={change} required /></label><label className="form-field">Quantity needed<input type="number" name="requiredQuantity" min="1" step="1" placeholder="e.g. 100" value={form.requiredQuantity} onChange={change} required /></label></>}
                    </div>
                    <div className="form-footer"><p>Campaign progress updates automatically as contributions are recorded.</p><div className="form-actions"><button type="submit" disabled={busy}>{busy ? "Saving…" : editingId ? "Save changes" : "Publish campaign"} <span>→</span></button>{editingId && <button className="button-secondary" type="button" onClick={cancelEdit}>Cancel</button>}</div></div>
                </form>
            </section>

            <section className="campaign-section">
                <div className="section-heading section-heading-list"><div><span className="eyebrow">YOUR IMPACT</span><h2>Your campaigns <span className="count-bubble">{needs.length}</span></h2><p>Keep track of your published needs and the support they receive.</p></div><button className="button-secondary" onClick={loadNeeds}>↻ Refresh</button></div>
                {error && !loading && <div className="notice notice-error">{error}</div>}
                {loading ? <div className="panel loading-panel">Loading your campaigns…</div> : needs.length === 0 ? <div className="panel empty-state"><span className="empty-icon">✦</span><h3>Your first campaign starts here</h3><p>Publish a need above to let potential donors know how they can help.</p><button onClick={() => document.getElementById("campaign-form")?.scrollIntoView({ behavior: "smooth" })}>Create first campaign</button></div> : <div className="campaign-grid">
                    {needs.map((need) => {
                        const isMoney = need.type === "MONEY";
                        const current = Number(isMoney ? need.currentAmount || 0 : need.receivedQuantity || 0);
                        const target = Number(isMoney ? need.targetAmount || 0 : need.requiredQuantity || 0);
                        const progress = target > 0 ? Math.min(100, current / target * 100) : 0;
                        const completed = need.status === "COMPLETED";
                        return <article className="campaign-card" key={need.id}>
                            <div className={`campaign-art ${isMoney ? "art-money" : "art-items"}`}><span>{isMoney ? "₹" : "✿"}</span><small>{isMoney ? "FUNDRAISING" : "SUPPLY DRIVE"}</small><span className="campaign-art-mark">♥</span></div>
                            <div className="campaign-card-body"><div className="need-heading"><span className="type-tag">{isMoney ? "Money campaign" : "Item campaign"}</span><span className={`status ${completed ? "complete" : ""}`}>{completed ? "Completed" : "Active"}</span></div><h3>{need.title}</h3><p className="campaign-description">{need.description}</p>
                                <div className="campaign-progress-copy"><strong>{isMoney ? money(current) : `${current} ${need.itemRequired || "items"}`}</strong><span>of {isMoney ? money(target) : `${target} ${need.itemRequired || "items"}`}</span></div><div className="progress"><div style={{ width: `${progress}%` }} /></div><div className="progress-meta"><span>{progress.toFixed(1)}% of goal</span><span>{isMoney ? "Funds raised" : "Items received"}</span></div>
                                <div className="campaign-card-actions"><button className="button-secondary" onClick={() => edit(need)}>Edit details</button><button className="button-quiet-danger" onClick={() => deleteNeed(need.id)}>Delete</button></div>
                            </div>
                        </article>;
                    })}
                </div>}
            </section>
        </div>
    </AppLayout>;
}

export default NgoDashboard;
