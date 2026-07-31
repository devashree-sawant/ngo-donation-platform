import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axios";
import "../App.css";

function DonorDashboard() {

    const navigate = useNavigate();
    const [needs, setNeeds] = useState([]);

    useEffect(() => {
        loadNeeds();
    }, []);

    const loadNeeds = async () => {
        try {

            const res = await api.get("/needs");

            setNeeds(
                res.data.filter(n => n.type === "MONEY")
            );

        } catch {

            alert("Unable to load campaigns");

        }
    };

    return (

        <div className="container">

            <div className="top">

                <h1>Donor Dashboard</h1>

                <button
                    className="logout"
                    onClick={() => {

                        localStorage.clear();
                        navigate("/");

                    }}
                >
                    Logout
                </button>

            </div>

            <div className="card">

                <h2>Support a Campaign ❤️</h2>

                <p>
                    Choose a campaign below and make a contribution.
                </p>

            </div>

            <div className="grid">

                {

                    needs.map((need) => {

                        const progress =
                            need.targetAmount
                                ? ((need.currentAmount / need.targetAmount) * 100).toFixed(1)
                                : 0;

                        return (

                            <div
                                className="need"
                                key={need.id}
                            >

                                <h3>

                                    {need.title}

                                </h3>

                                <p>

                                    {need.description}

                                </p>

                                <p>

                                    <b>NGO:</b>

                                    {" "}

                                    {need.ngo?.name}

                                </p>

                                <p className="amount">

                                    ₹{need.currentAmount}

                                    {" / "}

                                    ₹{need.targetAmount}

                                </p>

                                <div className="progress">

                                    <div
                                        style={{
                                            width: `${progress}%`
                                        }}
                                    />

                                </div>

                                <p>

                                    {progress}% Funded

                                </p>

                                <br/>

                                <button
                                    onClick={() =>
                                        navigate("/donate", {
                                            state: { need }
                                        })
                                    }
                                >

                                    Donate ❤️

                                </button>

                            </div>

                        );

                    })

                }

            </div>

        </div>

    );

}

export default DonorDashboard;