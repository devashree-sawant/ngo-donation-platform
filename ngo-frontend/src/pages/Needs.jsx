import { useEffect, useState } from "react";
import api from "../api/axios";
import "../App.css";

function Needs() {

    const [needs, setNeeds] = useState([]);

    useEffect(() => {
        loadNeeds();
    }, []);

    const loadNeeds = async () => {

        try {

            const res = await api.get("/needs");
            setNeeds(res.data);

        } catch (err) {

            console.log(err);
            alert("Unable to load needs");

        }

    };

    return (

        <div className="container">

            <div className="top">

                <h1>All Campaigns</h1>

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

                                <h3>{need.title}</h3>

                                <p>{need.description}</p>

                                <p>

                                    <b>NGO:</b> {need.ngo?.name}

                                </p>

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

                                <p>{progress}% Funded</p>

                                <br />

                                <span className="status">

                                    {need.status}

                                </span>

                            </div>

                        );

                    })

                }

            </div>

        </div>

    );

}

export default Needs;