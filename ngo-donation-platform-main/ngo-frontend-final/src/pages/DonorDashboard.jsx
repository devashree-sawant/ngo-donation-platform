import { useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import api, { getApiError } from "../api/axios";
import AppLayout from "../components/AppLayout";
import "../App.css";

const money = (value) => new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR", maximumFractionDigits: 0 }).format(value || 0);

function DonorDashboard() {
    const navigate = useNavigate();
    const location = useLocation();
    const donorId = localStorage.getItem("id");
    const donorName = localStorage.getItem("name") || "friend";
    const [needs, setNeeds] = useState([]);
    const [history, setHistory] = useState([]);
    const [search, setSearch] = useState("");
    const [typeFilter, setTypeFilter] = useState("ALL");
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => { loadData(); }, []);

    const loadData = async () => {
        setLoading(true);
        try {
            const response = await api.get("/needs");
            setNeeds(response.data);
            setError("");
        } catch (requestError) { setError(getApiError(requestError, "Unable to load campaigns.")); }
        try {
            const historyResponse = await api.get(`/donors/${donorId}/contributions`);
            setHistory(historyResponse.data);
        } catch (requestError) { console.error("Contribution history could not be loaded", requestError); }
        finally { setLoading(false); }
    };

    const filteredNeeds = useMemo(() => needs.filter((need) => {
        const matchesType = typeFilter === "ALL" || need.type === typeFilter;
        const text = `${need.title || ""} ${need.description || ""} ${need.ngo?.name || ""}`.toLowerCase();
        return matchesType && text.includes(search.toLowerCase());
    }), [needs, search, typeFilter]);

    const totalDonated = history.filter((item) => item.type === "MONEY").reduce((sum, item) => sum + Number(item.amount || 0), 0);
    const activeCampaigns = needs.filter((need) => need.status !== "COMPLETED").length;
    const totalItems = history.filter((item) => item.type === "ITEM").reduce((sum, item) => sum + Number(item.quantity || 0), 0);

    return <AppLayout>
        <div className="container dashboard-container donor-dashboard">
            {location.state?.toast && <div className="notice notice-success donation-toast" role="status">{location.state.toast}</div>}
            <section className="hero-banner donor-hero">
                <div className="hero-content"><span className="hero-kicker"><span /> YOUR KINDNESS, IN ACTION</span><h1>Hello, {donorName.split(" ")[0]}.<br /><em>You can help.</em></h1><p>Discover community-led campaigns and support causes that matter to you. Every donation brings someone a little closer to what they need.</p><button className="hero-button" onClick={() => document.getElementById("campaigns")?.scrollIntoView({ behavior: "smooth" })}>Explore campaigns <span>↓</span></button></div>
                <div className="hero-illustration donor-art" aria-hidden="true"><div className="sun-disc" /><div className="hand-shape">♥</div><div className="floating-message"><span>✦</span><div><strong>Kindness looks good on you.</strong><small>Thanks for being here.</small></div></div><div className="floating-dot dot-a" /><div className="floating-dot dot-b" /></div>
            </section>

            <section className="metric-grid donor-metrics" aria-label="Your impact at a glance">
                <article className="metric-tile"><span className="metric-icon green-icon">♡</span><div><small>Contributions made</small><strong>{history.length}</strong><span className="metric-caption">Each one makes a difference</span></div></article>
                <article className="metric-tile"><span className="metric-icon gold-icon">₹</span><div><small>Money donated</small><strong>{money(totalDonated)}</strong><span className="metric-caption">Your recorded contributions</span></div></article>
                <article className="metric-tile"><span className="metric-icon blue-icon">✿</span><div><small>Items pledged</small><strong>{totalItems}</strong><span className="metric-caption">Across item campaigns</span></div></article>
                <article className="metric-tile"><span className="metric-icon coral-icon">↗</span><div><small>Campaigns to support</small><strong>{activeCampaigns}</strong><span className="metric-caption">Still accepting contributions</span></div></article>
            </section>

            <section className="campaign-section" id="campaigns">
                <div className="section-heading section-heading-list"><div><span className="eyebrow">FIND YOUR CAUSE</span><h2>Campaigns worth caring about</h2><p>Explore specific needs posted by NGOs and choose how you'd like to help.</p></div><span className="section-aside-note">A little can mean a lot <span>♥</span></span></div>
                <div className="filter-bar"><label className="search-box"><span>⌕</span><input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search campaigns, causes or NGOs…" aria-label="Search campaigns" /></label><div className="filter-pills" aria-label="Filter campaign type"><button className={typeFilter === "ALL" ? "filter-pill active" : "filter-pill"} onClick={() => setTypeFilter("ALL")}>All needs</button><button className={typeFilter === "MONEY" ? "filter-pill active" : "filter-pill"} onClick={() => setTypeFilter("MONEY")}>Money</button><button className={typeFilter === "ITEM" ? "filter-pill active" : "filter-pill"} onClick={() => setTypeFilter("ITEM")}>Items</button></div></div>
                {error && <div className="notice notice-error" role="alert">{error} <button className="button-secondary" onClick={loadData}>Try again</button></div>}
                {loading ? <div className="panel loading-panel">Finding campaigns for you…</div> : filteredNeeds.length === 0 ? <div className="panel empty-state"><span className="empty-icon">♡</span><h3>No campaigns found</h3><p>Try another search or change the selected filter.</p><button className="button-secondary" onClick={() => { setSearch(""); setTypeFilter("ALL"); }}>Clear filters</button></div> : <div className="campaign-grid">
                    {filteredNeeds.map((need) => {
                        const isMoney = need.type === "MONEY";
                        const current = Number(isMoney ? need.currentAmount || 0 : need.receivedQuantity || 0);
                        const target = Number(isMoney ? need.targetAmount || 0 : need.requiredQuantity || 0);
                        const progress = target > 0 ? Math.min(100, current / target * 100) : 0;
                        const completed = need.status === "COMPLETED";
                        return <article className="campaign-card donor-campaign-card" key={need.id}>
                            <div className={`campaign-art ${isMoney ? "art-money" : "art-items"}`}><span>{isMoney ? "₹" : "✿"}</span><small>{isMoney ? "FUNDRAISING CAMPAIGN" : "COMMUNITY SUPPLY DRIVE"}</small><span className="campaign-art-mark">♥</span></div>
                            <div className="campaign-card-body"><div className="need-heading"><span className="type-tag">{isMoney ? "Money need" : "Item need"}</span><span className={`status ${completed ? "complete" : ""}`}>{completed ? "Goal reached" : "Open"}</span></div><h3>{need.title}</h3><p className="campaign-description">{need.description}</p><p className="ngo-label">Organised by <strong>{need.ngo?.name || "Community NGO"}</strong></p>
                                <div className="campaign-progress-copy"><strong>{isMoney ? money(current) : `${current} ${need.itemRequired || "items"}`}</strong><span>of {isMoney ? money(target) : `${target} ${need.itemRequired || "items"}`}</span></div><div className="progress"><div style={{ width: `${progress}%` }} /></div><div className="progress-meta"><span>{progress.toFixed(0)}% of goal</span><span>{isMoney ? "funded" : "collected"}</span></div>
                                <button className="campaign-cta" disabled={completed} onClick={() => navigate("/donate", { state: { need } })}>{completed ? "Campaign completed" : isMoney ? "Contribute funds" : "Pledge items"}<span>→</span></button>
                            </div>
                        </article>;
                    })}
                </div>}
            </section>

            <section className="history-section" id="history">
                <div className="section-heading section-heading-list"><div><span className="eyebrow">YOUR GENEROSITY, RECORDED</span><h2>My contribution history</h2><p>Review your past contributions and open a certificate for your records.</p></div><button className="button-secondary" onClick={loadData}>↻ Refresh history</button></div>
                {history.length === 0 ? <div className="panel empty-state small-empty"><span className="empty-icon">♡</span><h3>Your impact story starts here</h3><p>Once you contribute to a campaign, your record will appear in this section.</p></div> : <div className="history-list">
                    {history.map((contribution) => <article className="history-row" key={contribution.id}><span className="history-mark">{contribution.type === "MONEY" ? "₹" : "✿"}</span><div className="history-detail"><strong>{contribution.need?.title || "Campaign contribution"}</strong><p>{contribution.donationDate || "Date not available"} · {contribution.type === "MONEY" ? money(contribution.amount) : `${contribution.quantity} ${contribution.itemName || "items"}`}</p><small>Reference #{contribution.id}</small></div><button className="button-secondary" onClick={() => navigate(`/certificate/${contribution.id}`)}>View certificate <span>↗</span></button></article>)}
                </div>}
            </section>
        </div>
    </AppLayout>;
}

export default DonorDashboard;
