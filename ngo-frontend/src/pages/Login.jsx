import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axios";
import "./Login.css";

function Login() {

    const navigate = useNavigate();

    const [email, setEmail] = useState("");

    const [password, setPassword] = useState("");

    const handleLogin = async () => {

        if (!email || !password) {
            alert("Please enter email and password");
            return;
        }

        try {

            const response = await api.post("/auth/login", {
                email,
                password
            });

            localStorage.setItem("token", response.data.token);
            localStorage.setItem("role", response.data.role);
            localStorage.setItem("id", response.data.id);

            if (response.data.role === "NGO") {
                navigate("/ngo");
            } else {
                navigate("/donor");
            }

        } catch (err) {

            console.log(err);

            alert("Invalid Email or Password");

        }

    };

    return (

        <div className="login-page">

            <div className="login-card">

                <div className="logo">

                    ❤️

                </div>

                <h1 className="login-title">
                    NGO Donation Platform
                </h1>

                <p className="login-subtitle">
                    Connecting Donors with NGOs
                </p>

                <input

                    type="email"

                    placeholder="Enter Email"

                    value={email}

                    onChange={(e)=>setEmail(e.target.value)}

                />

                <input

                    type="password"

                    placeholder="Enter Password"

                    value={password}

                    onChange={(e)=>setPassword(e.target.value)}

                />

                <button onClick={handleLogin}>

                    Login

                </button>

            </div>

        </div>

    );

}

export default Login;