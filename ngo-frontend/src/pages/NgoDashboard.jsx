import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axios";
import "../App.css";

function NgoDashboard() {

    const navigate = useNavigate();
    const ngoId = Number(localStorage.getItem("id"));

    const [needs, setNeeds] = useState([]);
    const [title, setTitle] = useState("");
    const [description, setDescription] = useState("");
    const [targetAmount, setTargetAmount] = useState("");

    useEffect(() => {
        loadNeeds();
    }, []);

    const loadNeeds = async () => {
        try {
            const res = await api.get("/needs");
            setNeeds(res.data.filter(n => n.ngo?.id === ngoId));
        } catch {
            alert("Unable to load needs");
        }
    };

    const createNeed = async () => {

        if (!title || !description || !targetAmount) {
            alert("Fill all fields");
            return;
        }

        try {

            await api.post("/needs", {
                title,
                description,
                type: "MONEY",
                targetAmount: Number(targetAmount),
                currentAmount: 0,
                status: "PENDING",
                ngo: { id: ngoId }
            });

            setTitle("");
            setDescription("");
            setTargetAmount("");

            loadNeeds();

        } catch {
            alert("Unable to create campaign");
        }
    };

    const deleteNeed = async (id) => {
        if (!window.confirm("Delete this campaign?")) return;

        await api.delete(`/needs/${id}`);
        loadNeeds();
    };

    return (

        <div className="container">

            <div className="top">

                <h1>NGO Dashboard</h1>

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

                <h2>Create Campaign</h2>

                <input
                    placeholder="Campaign Title"
                    value={title}
                    onChange={e => setTitle(e.target.value)}
                />

                <textarea
                    placeholder="Campaign Description"
                    value={description}
                    onChange={e => setDescription(e.target.value)}
                />

                <input
                    type="number"
                    placeholder="Target Amount"
                    value={targetAmount}
                    onChange={e => setTargetAmount(e.target.value)}
                />

                <button onClick={createNeed}>
                    Create Campaign
                </button>

            </div>

            <h2 style={{marginBottom:"20px"}}>Your Campaigns</h2>

            <div className="grid">

                {needs.map(need => {

                    const progress =
                        need.targetAmount
                            ? ((need.currentAmount / need.targetAmount) * 100).toFixed(1)
                            : 0;

                    return (

                        <div className="need" key={need.id}>

                            <h3>{need.title}</h3>

                            <p>{need.description}</p>

                            <p className="amount">
                                ₹{need.currentAmount} / ₹{need.targetAmount}
                            </p>

                            <div className="progress">
                                <div style={{ width: `${progress}%` }} />
                            </div>

                            <p>{progress}% Funded</p>

                            <br />

                            <span className="status">
                                {need.status}
                            </span>

                            <br /><br />

                            <button
                                className="delete"
                                onClick={() => deleteNeed(need.id)}
                            >
                                Delete
                            </button>

                        </div>

                    );

                })}

            </div>

        </div>

    );

}

export default NgoDashboard;