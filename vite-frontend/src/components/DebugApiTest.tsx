import React, { useState } from "react";

const DebugApiTest: React.FC = () => {
  const [testResult, setTestResult] = useState<string>("");
  const [isLoading, setIsLoading] = useState<boolean>(false);

  const testBackendConnection = async () => {
    setIsLoading(true);
    setTestResult("Testing connection...");

    try {
      // Test 1: Basic connectivity
      const statusResponse = await fetch(
        "http://localhost:8080/api/ollama/status"
      );
      if (!statusResponse.ok) {
        throw new Error(`Status check failed: ${statusResponse.status}`);
      }
      const statusData = await statusResponse.json();

      // Test 2: Detection API with simple data
      const testData = {
        name: "John Doe",
        date: "1990-01-01",
        iata: "US",
        seatNumber: "12A",
        cabinClass: "A",
      };

      const detectResponse = await fetch("http://localhost:8080/api/detect", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(testData),
      });

      if (!detectResponse.ok) {
        throw new Error(
          `Detection failed: ${detectResponse.status} - ${detectResponse.statusText}`
        );
      }

      const detectData = await detectResponse.json();

      setTestResult(`✅ SUCCESS!

🔍 Status Check:
${JSON.stringify(statusData, null, 2)}

🎯 Detection Test:
${JSON.stringify(detectData, null, 2)}`);
    } catch (error: any) {
      setTestResult(`❌ ERROR:
${error.message}

Debug Info:
- Make sure backend is running on http://localhost:8080
- Check browser console for CORS errors
- Verify mvn spring-boot:run completed successfully`);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="container mt-4">
      <div className="card">
        <div className="card-header">
          <h5>Backend Connection Debug Tool</h5>
        </div>
        <div className="card-body">
          <button
            className="btn btn-primary"
            onClick={testBackendConnection}
            disabled={isLoading}
          >
            {isLoading ? "Testing..." : "Test Backend Connection"}
          </button>

          {testResult && (
            <div className="mt-3">
              <h6>Test Results:</h6>
              <pre
                className="bg-light p-3 border rounded"
                style={{
                  whiteSpace: "pre-wrap",
                  maxHeight: "500px",
                  overflowY: "auto",
                }}
              >
                {testResult}
              </pre>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default DebugApiTest;
