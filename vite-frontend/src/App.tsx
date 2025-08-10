import React from "react";
import FileUpload from "./components/FileUpload";
import SingleEntryForm from "./components/SingleEntryForm";
import DebugApiTest from "./components/DebugApiTest";
import coforge from "./assets/coforge.svg";

const App: React.FC = () => {
  return (
    <>
      <nav className="navbar navbar-expand-lg bg-body-tertiary">
        <div className="container-fluid">
          <a className="navbar-brand" href="#">
            <img height="25" width="40" src={coforge}></img>
          </a>
          <button
            className="navbar-toggler"
            type="button"
            data-bs-toggle="collapse"
            data-bs-target="#navbarSupportedContent"
            aria-controls="navbarSupportedContent"
            aria-expanded="false"
            aria-label="Toggle navigation"
          >
            <span className="navbar-toggler-icon"></span>
          </button>
          <div className="flex-fill">
            <div
              className="collapse navbar-collapse"
              id="navbarSupportedContent"
            >
              <ul className="navbar-nav me-auto mb-2 mb-lg-0">
                <li className="nav-item">
                  <a className="nav-link active" aria-current="page" href="#">
                    Home
                  </a>
                </li>
                <li className="nav-item">
                  <a
                    className="nav-link"
                    href="http://localhost:8080/swagger-ui/index.html#/demo-controller/detect"
                  >
                    Documentation
                  </a>
                </li>
                <li className="nav-item">
                  <a className="nav-link" href="#">
                    About
                  </a>
                </li>
              </ul>
              <form className="d-flex" role="search">
                <input
                  className="form-control me-2"
                  type="search"
                  placeholder="Search"
                  aria-label="Search"
                />
              </form>
            </div>
          </div>
        </div>
      </nav>

      <div className="container mt-4">
        <h1 className="text-center mb-4">Anomaly Detection App</h1>
        <hr />
      </div>

      <div className="container-lg">
        <div className="accordion" id="accordionExample">
          <div className="accordion-item">
            <h2 className="accordion-header">
              <button
                className="accordion-button"
                type="button"
                data-bs-toggle="collapse"
                data-bs-target="#collapseOne"
                aria-expanded="true"
                aria-controls="collapseOne"
              >
                JSON file upload
              </button>
            </h2>
            <div
              id="collapseOne"
              className="accordion-collapse collapse show"
              data-bs-parent="#accordionExample"
            >
              <div className="accordion-body">
                <FileUpload />
              </div>
            </div>
          </div>
          <div className="accordion-item">
            <h2 className="accordion-header">
              <button
                className="accordion-button collapsed"
                type="button"
                data-bs-toggle="collapse"
                data-bs-target="#collapseTwo"
                aria-expanded="false"
                aria-controls="collapseTwo"
              >
                Check single entry
              </button>
            </h2>
            <div
              id="collapseTwo"
              className="accordion-collapse collapse"
              data-bs-parent="#accordionExample"
            >
              <div className="accordion-body">
                <SingleEntryForm />
              </div>
            </div>
          </div>
          <div className="accordion-item">
            <h2 className="accordion-header">
              <button
                className="accordion-button collapsed"
                type="button"
                data-bs-toggle="collapse"
                data-bs-target="#collapseThree"
                aria-expanded="false"
                aria-controls="collapseThree"
              >
                Debug Backend Connection
              </button>
            </h2>
            <div
              id="collapseThree"
              className="accordion-collapse collapse"
              data-bs-parent="#accordionExample"
            >
              <div className="accordion-body">
                <DebugApiTest />
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Simple Bootstrap Footer */}
      <footer className="bg-dark text-light mt-5 py-4">
        <div className="container">
          <div className="row">
            <div className="col-md-6">
              <h5>Anomaly Detection App</h5>
              <p className="mb-0">
                Advanced machine learning for data anomaly detection
              </p>
            </div>
            <div className="col-md-6 text-md-end">
              <p className="mb-2">
                <a href="#" className="text-light text-decoration-none me-3">
                  About
                </a>
                <a href="#" className="text-light text-decoration-none me-3">
                  Documentation
                </a>
                <a href="#" className="text-light text-decoration-none">
                  Contact
                </a>
              </p>
              <p className="mb-0 text-muted">
                © {new Date().getFullYear()} Coforge. All rights reserved.
              </p>
            </div>
          </div>
        </div>
      </footer>
    </>
  );
};

export default App;
