import { useLocation, useNavigate } from "react-router-dom";
import { useState } from "react";
import api from "../api/axios";
import "../App.css";

function Donate() {

    const navigate = useNavigate();
    const location = useLocation();

    const need = location.state?.need;

    const [amount, setAmount] = useState("");

    if (!need) {
        return (
            <div className="container">
                <div className="card">
                    <h2>No Campaign Selected</h2>
                    <button onClick={() => navigate("/donor")}>
                        Back
                    </button>
                </div>
            </div>
        );
    }

    const donate = async () => {

        if (!amount || Number(amount) <= 0) {
            alert("Enter a valid amount");
            return;
        }

        try {

            await api.post("/contributions", {

                type: "MONEY",

                amount: Number(amount),

                donor: {
                    id: Number(localStorage.getItem("id"))
                },

                need: {
                    id: need.id
                }

            });

            alert("Donation Successful ❤️");

            navigate("/donor", { replace: true });

        } catch (err) {

            console.log(err);

            alert("Donation Failed");

        }

    };

    const progress =
        need.targetAmount
            ? ((need.currentAmount / need.targetAmount) * 100).toFixed(1)
            : 0;

    return (

        <div className="container">

            <div className="card">

                <h1>Donate</h1>

                <h2>{need.title}</h2>

                <p>{need.description}</p>

                <br />

                <p>

                    <b>NGO:</b> {need.ngo?.name}

                </p>

                <br />

                <p className="amount">

                    ₹{need.currentAmount} / ₹{need.targetAmount}

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

                <br />

                <input

                    type="number"

                    placeholder="Enter Donation Amount"

                    value={amount}

                    onChange={(e) => setAmount(e.target.value)}

                />

                <button
                    onClick={donate}
                    style={{ marginRight: "10px" }}
                >
                    Donate Now
                </button>

                <button
                    className="logout"
                    onClick={() => navigate("/donor")}
                >
                    Cancel
                </button>

            </div>

        </div>

    );

}

export default Donate;