import { Link, useLocation, useNavigate } from "react-router-dom";
import "../App.css";

function AppLayout({ children }) {
    const navigate = useNavigate();
    const location = useLocation();
    const role = localStorage.getItem("role");
    const name = localStorage.getItem("name") || (role === "NGO" ? "Your NGO" : "Your account");
    const home = role === "NGO" ? "/ngo" : role === "DONOR" ? "/donor" : "/needs";
    const initials = name.split(/\s+/).filter(Boolean).slice(0, 2).map((part) => part[0]).join("").toUpperCase() || "U";

    const links = role === "NGO"
        ? [{ label: "Dashboard", to: "/ngo" }, { label: "Explore campaigns", to: "/needs" }, { label: "My profile", to: "/profile" }]
        : role === "DONOR"
            ? [{ label: "Explore campaigns", to: "/donor" }, { label: "My contributions", to: "/donor#history" }, { label: "My profile", to: "/profile" }]
            : [{ label: "Explore campaigns", to: "/needs" }];

    const logout = () => {
        localStorage.clear();
        navigate("/", { replace: true });
    };

    return (
        <div className="app-shell">
            <header className="site-header no-print">
                <div className="site-header-inner">
                    <Link className="brand" to={home} aria-label="HopeBridge home">
                        <span className="brand-mark">♥</span>
                        <span className="brand-copy">
                            <strong>HopeBridge</strong>
                            <small>Goodness grows when we share.</small>
                        </span>
                    </Link>

                    <nav className="main-nav" aria-label="Main navigation">
                        {links.map((link) => {
                            const active = link.to.includes("#")
                                ? location.pathname === "/donor" && location.hash === "#history"
                                : location.pathname === link.to;
                            return <Link key={link.to} className={active ? "nav-link active" : "nav-link"} to={link.to}>{link.label}</Link>;
                        })}
                    </nav>

                    <div className="header-account">
                        {role ? <>
                            <button className="account-button" onClick={() => navigate("/profile")} title="View profile">
                                <span className="avatar">{initials}</span>
                                <span className="account-copy"><strong>{name}</strong><small>{role === "NGO" ? "NGO account" : "Donor account"}</small></span>
                                <span className="account-chevron">⌄</span>
                            </button>
                            <button className="logout-button" onClick={logout}>Sign out</button>
                        </> : <Link className="sign-in-link" to="/">Sign in <span>↗</span></Link>}
                    </div>
                </div>
            </header>

            <main className="app-main">{children}</main>

            <footer className="site-footer no-print">
                <div><span className="footer-heart">♥</span> <strong>HopeBridge</strong></div>
                <p>Every contribution can help create a brighter tomorrow.</p>
            </footer>
        </div>
    );
}

export default AppLayout;
