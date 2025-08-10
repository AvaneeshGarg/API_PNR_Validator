import React, { useState } from "react";
import api from "../api";
import type { DataRow, AnomalyResult } from "../types";

const SingleEntryForm: React.FC = () => {
  const [formData, setFormData] = useState<DataRow>({
    name: "",
    date: "",
    iata: "",
    seatNumber: "",
    cabinClass: "",
  });

  const [result, setResult] = useState<AnomalyResult | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async () => {
    console.log("Submitting form data:", formData);
    setIsLoading(true);
    setResult(null);

    try {
      const res = await api.post<AnomalyResult>("/api/detect", formData, {
        headers: { "Content-Type": "application/json" },
      });
      console.log("Success response:", res.data);
      setResult(res.data);
    } catch (err: any) {
      console.error("Detection failed:", err);

      if (err.response) {
        // Server responded with error status
        console.error("Response data:", err.response.data);
        console.error("Response status:", err.response.status);
        alert(
          `Detection failed: ${err.response.status} - ${
            err.response.data?.message || "Server error"
          }`
        );
      } else if (err.request) {
        // Request was made but no response received
        console.error("No response received:", err.request);
        alert(
          "Detection failed: Cannot connect to server. Please check if the backend is running on port 8080."
        );
      } else {
        // Something else happened
        console.error("Request setup error:", err.message);
        alert(`Detection failed: ${err.message}`);
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleFormSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    handleSubmit();
  };

  return (
    <div className="container-fluid">
      <div className="row justify-content-center">
        <div className="row g-3">
          <div className="col-md-12">
            <form
              onSubmit={handleFormSubmit}
              className="row g-3 needs-validation"
              noValidate
            >
              <div className="col-md-6">
                <label htmlFor="name" className="form-label">
                  Name
                </label>
                <input
                  type="text"
                  id="name"
                  name="name"
                  placeholder="Name"
                  value={formData.name}
                  onChange={handleChange}
                  className="form-control"
                  required
                />
              </div>
              <div className="col-md-3">
                <label htmlFor="date" className="form-label">
                  Date
                </label>
                <input
                  type="date"
                  id="date"
                  name="date"
                  placeholder="YYYY-MM-DD"
                  value={formData.date}
                  onChange={handleChange}
                  className="form-control"
                  required
                />
              </div>
              <div className="col-md-3">
                <label htmlFor="iata" className="form-label">
                  IATA Code
                </label>
                <input
                  type="text"
                  id="iata"
                  name="iata"
                  placeholder="IATA CODE"
                  value={formData.iata}
                  onChange={handleChange}
                  className="form-control"
                  required
                />
              </div>
              <div className="col-md-3">
                <label htmlFor="seatNumber" className="form-label">
                  Seat Number
                </label>
                <input
                  type="text"
                  id="seatNumber"
                  name="seatNumber"
                  placeholder="Seat Number"
                  value={formData.seatNumber}
                  onChange={handleChange}
                  className="form-control"
                  required
                />
              </div>
              <div className="col-md-3">
                <label htmlFor="cabinClass" className="form-label">
                  Cabin Class
                </label>
                <input
                  type="text"
                  id="cabinClass"
                  name="cabinClass"
                  placeholder="Cabin Class"
                  value={formData.cabinClass}
                  onChange={handleChange}
                  className="form-control"
                  required
                />
              </div>
              <div className="col-md-12">
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={isLoading}
                >
                  {isLoading ? (
                    <>
                      <span
                        className="spinner-border spinner-border-sm me-2"
                        role="status"
                        aria-hidden="true"
                      ></span>
                      Analyzing with AI... (may take up to 2 minutes)
                    </>
                  ) : (
                    "Detect Anomaly"
                  )}
                </button>
              </div>
            </form>
          </div>

          {isLoading && (
            <div className="mt-4 text-center">
              <div className="spinner-border text-primary" role="status">
                <span className="visually-hidden">Loading...</span>
              </div>
              <div className="mt-2">
                <strong>AI Analysis in Progress...</strong>
                <br />
                <small className="text-muted">
                  Processing with Ollama gemma3:4b model. This may take 1-2
                  minutes.
                </small>
              </div>
            </div>
          )}

          {result && (
            <div className="mt-4">
              <h5>Detection Result:</h5>
              <pre
                className="bg-light p-3 rounded"
                style={{ background: "#f0f0f0", padding: "10px" }}
              >
                {JSON.stringify(result, null, 2)}
              </pre>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default SingleEntryForm;
