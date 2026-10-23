import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import api, { getApiError } from "../api/axios";
import AppLayout from "../components/AppLayout";
import "../App.css";

const money = (value) => new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR", maximumFractionDigits: 2 }).format(value || 0);

function Certificate() {
    const { id } = useParams();
    const navigate = useNavigate();
    const [certificate, setCertificate] = useState(null);
    const [error, setError] = useState("");

    useEffect(() => {
        api.get(`/contributions/${id}/certificate`).then((response) => setCertificate(response.data)).catch((requestError) => setError(getApiError(requestError, "Certificate could not be loaded.")));
    }, [id]);

    return <AppLayout>
        <div className="container certificate-container">
            <div className="page-intro no-print"><div><span className="eyebrow">A RECORD OF YOUR GENEROSITY</span><h1>Contribution certificate</h1><p>Keep this acknowledgement as a record of your contribution.</p></div><div className="form-actions"><button className="button-secondary" onClick={() => navigate("/donor#history")}>← Back to history</button><button onClick={() => window.print()}>Print / Save PDF ↗</button></div></div>
            {error ? <div className="panel empty-state"><span className="empty-icon">!</span><h3>Certificate unavailable</h3><p>{error}</p><button onClick={() => navigate("/donor")}>Back to dashboard</button></div> : !certificate ? <div className="panel loading-panel">Preparing your certificate…</div> : <article className="certificate-sheet"><div className="certificate-seal">♥</div><p className="certificate-platform">HOPEBRIDGE</p><h1>{certificate.certificateTitle}</h1><div className="certificate-rule" /><p className="certificate-copy">This certificate is presented with gratitude to</p><h2 className="certificate-name">{certificate.donorName}</h2><p className="certificate-copy">in acknowledgement of a contribution supporting</p><h2>{certificate.campaignTitle}</h2><p className="certificate-copy">organised by <strong>{certificate.ngoName}</strong></p><div className="certificate-contribution">{certificate.type === "MONEY" ? <><span>Contribution amount</span><strong>{money(certificate.amount)}</strong></> : <><span>Items contributed</span><strong>{certificate.quantity} {certificate.itemName}</strong></>}</div><p className="certificate-reference">Reference #{certificate.contributionId} <span>•</span> {certificate.donationDate}</p><p className="certificate-disclaimer">{certificate.certificateMessage}</p><div className="certificate-footer"><span>With gratitude,</span><span>HopeBridge <b>♥</b></span></div></article>}
        </div>
    </AppLayout>;
}

export default Certificate;
