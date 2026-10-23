import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import api, { getApiError } from "../api/axios";
import AppLayout from "../components/AppLayout";
import "../App.css";

const money = (value) => new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR", maximumFractionDigits: 0 }).format(value || 0);

function Needs() {
    const [needs, setNeeds] = useState([]);
    const [search, setSearch] = useState("");
    const [type, setType] = useState("ALL");
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const navigate = useNavigate();

    useEffect(() => {
        api.get("/needs").then((response) => setNeeds(response.data)).catch((requestError) => setError(getApiError(requestError, "Unable to load campaigns."))).finally(() => setLoading(false));
    }, []);

    const filtered = useMemo(() => needs.filter((need) => {
        const matchesType = type === "ALL" || need.type === type;
        const label = `${need.title || ""} ${need.description || ""} ${need.ngo?.name || ""}`.toLowerCase();
        return matchesType && label.includes(search.toLowerCase());
    }), [needs, type, search]);

    return <AppLayout>
        <div className="container dashboard-container public-campaigns">
            <section className="hero-banner public-hero"><div className="hero-content"><span className="hero-kicker"><span /> DISCOVER THE GOOD AROUND YOU</span><h1>Find a cause.<br /><em>Make a difference.</em></h1><p>Explore specific needs shared by NGOs and see how your money or essential items can help move a campaign forward.</p>{localStorage.getItem("token") ? <button className="hero-button" onClick={() => navigate(localStorage.getItem("role") === "NGO" ? "/ngo" : "/donor")}>Go to your dashboard <span>→</span></button> : <button className="hero-button" onClick={() => navigate("/")}>Join the community <span>→</span></button>}</div><div className="hero-illustration public-art" aria-hidden="true"><div className="public-sun">✦</div><div className="public-heart">♥</div><div className="public-card-float">Kindness in action <span>↗</span></div></div></section>
            <section className="campaign-section">
                <div className="section-heading section-heading-list"><div><span className="eyebrow">COMMUNITY CAMPAIGNS</span><h2>Needs looking for a helping hand</h2><p>Choose a campaign that speaks to you. Each one shows its goal and current progress.</p></div><span className="count-bubble">{filtered.length}</span></div>
                <div className="filter-bar"><label className="search-box"><span>⌕</span><input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search by campaign or NGO…" aria-label="Search campaigns" /></label><div className="filter-pills"><button className={type === "ALL" ? "filter-pill active" : "filter-pill"} onClick={() => setType("ALL")}>All needs</button><button className={type === "MONEY" ? "filter-pill active" : "filter-pill"} onClick={() => setType("MONEY")}>Money</button><button className={type === "ITEM" ? "filter-pill active" : "filter-pill"} onClick={() => setType("ITEM")}>Items</button></div></div>
                {error && <div className="notice notice-error">{error}</div>}
                {loading ? <div className="panel loading-panel">Loading community campaigns…</div> : filtered.length === 0 ? <div className="panel empty-state"><span className="empty-icon">♡</span><h3>No campaigns found</h3><p>There are no campaigns matching these filters right now.</p><button className="button-secondary" onClick={() => { setSearch(""); setType("ALL"); }}>Clear filters</button></div> : <div className="campaign-grid">
                    {filtered.map((need) => {
                        const isMoney = need.type === "MONEY";
                        const current = Number(isMoney ? need.currentAmount || 0 : need.receivedQuantity || 0);
                        const target = Number(isMoney ? need.targetAmount || 0 : need.requiredQuantity || 0);
                        const progress = target > 0 ? Math.min(100, current / target * 100) : 0;
                        const completed = need.status === "COMPLETED";
                        return <article className="campaign-card" key={need.id}><div className={`campaign-art ${isMoney ? "art-money" : "art-items"}`}><span>{isMoney ? "₹" : "✿"}</span><small>{isMoney ? "FUNDRAISING CAMPAIGN" : "COMMUNITY SUPPLY DRIVE"}<span className="campaign-art-mark">♥</span></small></div><div className="campaign-card-body"><div className="need-heading"><span className="type-tag">{isMoney ? "Money need" : "Item need"}</span><span className={`status ${completed ? "complete" : ""}`}>{completed ? "Goal reached" : "Open"}</span></div><h3>{need.title}</h3><p className="campaign-description">{need.description}</p><p className="ngo-label">Organised by <strong>{need.ngo?.name || "Community NGO"}</strong></p><div className="campaign-progress-copy"><strong>{isMoney ? money(current) : `${current} ${need.itemRequired || "items"}`}</strong><span>of {isMoney ? money(target) : `${target} ${need.itemRequired || "items"}`}</span></div><div className="progress"><div style={{ width: `${progress}%` }} /></div><div className="progress-meta"><span>{progress.toFixed(0)}% of goal</span><span>{isMoney ? "funded" : "collected"}</span></div>
                            {localStorage.getItem("role") === "DONOR" ? <button className="campaign-cta" disabled={completed} onClick={() => navigate("/donate", { state: { need } })}>{completed ? "Campaign completed" : "Contribute now"}<span>→</span></button> : !localStorage.getItem("token") ? <button className="campaign-cta" onClick={() => navigate("/")}>Sign in to contribute <span>→</span></button> : <button className="campaign-cta" onClick={() => navigate("/ngo")}>Open your dashboard <span>→</span></button>}
                        </div></article>;
                    })}
                </div>}
            </section>
        </div>
    </AppLayout>;
}

export default Needs;
