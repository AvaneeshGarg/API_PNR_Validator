import React, { useState } from "react";
import api from "../api";
import type { AnomalyResult } from "../types";

interface ApiResponse {
  anomalies?: AnomalyResult[];
  message?: string;
  status?: string;
}

function FileUpload(): React.ReactElement {
  const [file, setFile] = useState<File | null>(null);
  const [response, setResponse] = useState<ApiResponse | null>(null);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>): void => {
    const selectedFile = e.target.files?.[0] || null;
    setFile(selectedFile);
  };

  const handleUpload = async (): Promise<void> => {
    if (!file) {
      alert("Please select a file");
      return;
    }

    const formData = new FormData();
    formData.append("file", file);

    try {
      const res = await api.post<ApiResponse>(
        "/api/upload/detect",
        formData,
        {
          headers: { "Content-Type": "multipart/form-data" },
        }
      );
      setResponse(res.data);
    } catch (error: any) {
      console.error("Upload error:", error);

      if (error.response) {
        console.error("Response data:", error.response.data);
        console.error("Response status:", error.response.status);
        alert(
          `Upload failed: ${error.response.status} - ${
            error.response.data?.message || "Server error"
          }`
        );
      } else if (error.request) {
        console.error("No response received:", error.request);
        alert(
          "Upload failed: Cannot connect to server. Please check if the backend is running on port 8080."
        );
      } else {
        console.error("Request setup error:", error.message);
        alert(`Upload failed: ${error.message}`);
      }
    }
  };

  return (
    <div className="container-fluid">
      <div className="row justify-content-center">
        <div className="col-md-6">
          <div className="mb-3">
            <label htmlFor="fileInput" className="form-label">
              Select JSON File
            </label>
            <input
              type="file"
              id="fileInput"
              accept=".json"
              onChange={handleFileChange}
              className="form-control"
            />
          </div>
          <button
            onClick={handleUpload}
            className="btn btn-primary"
            disabled={!file}
          >
            Upload and Detect
          </button>
          {response && (
            <div className="mt-4">
              <h5>Detection Result:</h5>
              <pre className="bg-light p-3 rounded">
                {JSON.stringify(response, null, 2)}
              </pre>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default FileUpload;
