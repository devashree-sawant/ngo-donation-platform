import { useLocation, useNavigate } from "react-router-dom";
import { useState } from "react";
import api, { getApiError } from "../api/axios";
import AppLayout from "../components/AppLayout";
import "../App.css";

const money = (value) => new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR", maximumFractionDigits: 0 }).format(value || 0);

function Donate() {
    const navigate = useNavigate();
    const { state } = useLocation();
    const need = state?.need;
    const [amount, setAmount] = useState("");
    const [quantity, setQuantity] = useState("");
    const [busy, setBusy] = useState(false);
    const [error, setError] = useState("");

    if (!need) return <AppLayout><div className="container"><div className="panel empty-state"><span className="empty-icon">♡</span><h3>No campaign selected</h3><p>Choose a campaign before making a contribution.</p><button onClick={() => navigate("/donor")}>Back to campaigns</button></div></div></AppLayout>;

    const isMoney = need.type === "MONEY";
    const current = Number(isMoney ? need.currentAmount || 0 : need.receivedQuantity || 0);
    const target = Number(isMoney ? need.targetAmount || 0 : need.requiredQuantity || 0);
    const progress = target > 0 ? Math.min(100, current / target * 100) : 0;
    const remaining = Math.max(0, target - current);

    const submit = async (event) => {
        event.preventDefault(); setError("");
        if (isMoney && (!amount || !Number.isFinite(Number(amount)) || Number(amount) <= 0)) return setError("Enter a donation amount greater than zero.");
        if (!isMoney && (!quantity || !Number.isInteger(Number(quantity)) || Number(quantity) <= 0)) return setError("Enter a whole quantity greater than zero.");
        if (isMoney && Number(amount) > remaining) return setError(`The remaining goal is ${money(remaining)}. Please enter that amount or less.`);
        if (!isMoney && Number(quantity) > remaining) return setError(`The remaining requirement is ${remaining} ${need.itemRequired || "items"}. Please enter that quantity or less.`);
        setBusy(true);
        try {
            const contribution = isMoney
                ? { type: "MONEY", amount: Number(amount), donor: { id: Number(localStorage.getItem("id")) }, need: { id: need.id } }
                : { type: "ITEM", itemName: need.itemRequired, quantity: Number(quantity), donor: { id: Number(localStorage.getItem("id")) }, need: { id: need.id } };
            await api.post("/contributions", contribution);
            navigate("/donor", { replace: true, state: { toast: "Thank you! Your contribution has been recorded." } });
        } catch (requestError) { setError(getApiError(requestError, "Contribution failed. Please try again.")); }
        finally { setBusy(false); }
    };

    return <AppLayout>
        <div className="container donate-container">
            <div className="page-intro"><div><span className="eyebrow">MAKE AN IMPACT</span><h1>Every little bit helps.</h1><p>Your contribution goes directly toward a specific need shared by this NGO.</p></div><button className="button-secondary" onClick={() => navigate("/donor")}>← Back to campaigns</button></div>
            <div className="donate-layout">
                <article className="donate-campaign panel"><div className={`campaign-art donate-art ${isMoney ? "art-money" : "art-items"}`}><span>{isMoney ? "₹" : "✿"}</span><small>{isMoney ? "FUNDRAISING CAMPAIGN" : "COMMUNITY SUPPLY DRIVE"}</small><span className="campaign-art-mark">♥</span></div><div className="donate-campaign-body"><div className="need-heading"><span className="type-tag">{isMoney ? "Money need" : "Item need"}</span><span className={`status ${need.status === "COMPLETED" ? "complete" : ""}`}>{need.status === "COMPLETED" ? "Completed" : "Open for support"}</span></div><h2>{need.title}</h2><p>{need.description}</p><p className="ngo-label">Organised by <strong>{need.ngo?.name || "Community NGO"}</strong></p><div className="campaign-progress-copy"><strong>{isMoney ? money(current) : `${current} ${need.itemRequired}`}</strong><span>of {isMoney ? money(target) : `${target} ${need.itemRequired}`}</span></div><div className="progress"><div style={{ width: `${progress}%` }} /></div><div className="progress-meta"><span>{progress.toFixed(1)}% complete</span><span>{isMoney ? `${money(remaining)} left` : `${remaining} left`}</span></div></div></article>
                <section className="panel donate-form-panel"><div className="section-heading"><div><span className="eyebrow">YOUR CONTRIBUTION</span><h2>{isMoney ? "Choose an amount" : "Pledge useful items"}</h2><p>{isMoney ? "Select a suggested amount or enter your own." : `Help provide ${need.itemRequired || "essential items"} to people who need them.`}</p></div><span className="section-icon">♡</span></div>
                    {error && <div className="notice notice-error" role="alert">{error}</div>}
                    <form onSubmit={submit}>
                        {isMoney ? <><div className="amount-suggestions">{[250, 500, 1000, 2000].filter((value) => value <= remaining).map((value) => <button type="button" className={Number(amount) === value ? "selected" : ""} key={value} onClick={() => setAmount(String(value))}>₹{value.toLocaleString("en-IN")}</button>)}</div><label className="form-field">Donation amount (₹)<input type="number" min="0.01" max={remaining} step="0.01" placeholder="Enter amount" value={amount} onChange={(event) => setAmount(event.target.value)} required /></label><p className="donation-helper">You can contribute any amount up to the remaining goal of {money(remaining)}.</p></> : <><label className="form-field">Quantity of {need.itemRequired}<input type="number" min="1" max={remaining} step="1" placeholder={`Up to ${remaining} items`} value={quantity} onChange={(event) => setQuantity(event.target.value)} required /></label><p className="donation-helper">The campaign needs {remaining} more {need.itemRequired || "items"}.</p></>}
                        <div className="donate-assurance"><span>✓</span><p>Your contribution will be recorded in your history so you can revisit its certificate later.</p></div><button className="campaign-cta donate-submit" type="submit" disabled={busy || need.status === "COMPLETED"}>{busy ? "Recording contribution…" : isMoney ? `Confirm donation${amount ? ` · ${money(Number(amount))}` : ""}` : "Confirm item pledge"}<span>→</span></button>
                    </form>
                </section>
            </div>
        </div>
    </AppLayout>;
}

export default Donate;
