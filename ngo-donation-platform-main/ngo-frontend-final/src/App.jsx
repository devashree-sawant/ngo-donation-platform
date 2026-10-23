import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import Login from "./pages/Login";
import NgoDashboard from "./pages/NgoDashboard";
import DonorDashboard from "./pages/DonorDashboard";
import Needs from "./pages/Needs";
import Donate from "./pages/Donate";
import Certificate from "./pages/Certificate";
import Profile from "./pages/Profile";

function RequireRole({ role, children }) {
    const token = localStorage.getItem("token");
    const currentRole = localStorage.getItem("role");
    if (!token) return <Navigate to="/" replace />;
    if (currentRole !== role) return <Navigate to={currentRole === "NGO" ? "/ngo" : "/donor"} replace />;
    return children;
}

function RequireAuth({ children }) {
    if (!localStorage.getItem("token")) return <Navigate to="/" replace />;
    return children;
}

function App() {
    return <BrowserRouter>
        <Routes>
            <Route path="/" element={<Login />} />
            <Route path="/needs" element={<Needs />} />
            <Route path="/ngo" element={<RequireRole role="NGO"><NgoDashboard /></RequireRole>} />
            <Route path="/donor" element={<RequireRole role="DONOR"><DonorDashboard /></RequireRole>} />
            <Route path="/donate" element={<RequireRole role="DONOR"><Donate /></RequireRole>} />
            <Route path="/profile" element={<RequireAuth><Profile /></RequireAuth>} />
            <Route path="/certificate/:id" element={<RequireRole role="DONOR"><Certificate /></RequireRole>} />
            <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
    </BrowserRouter>;
}

export default App;
