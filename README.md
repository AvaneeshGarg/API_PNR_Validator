A API PNR Validator

A Spring Boot application that validates Passenger Name Record (PNR) data using hybrid rule-based validation and AI-powered anomaly detection with Ollama integration.

## Features

- **Hybrid Anomaly Detection**: Combines rule-based validation with AI-powered analysis
- **Ollama AI Integration**: Uses local Ollama service with Gemma 3 model for intelligent anomaly detection
- **Cultural Sensitivity**: AI prompts include cultural awareness to prevent bias
- **Real-time Validation**: RESTful API for instant passenger data validation
- **MongoDB Integration**: Stores and manages passenger data
- **React Frontend**: Modern web interface for data input and visualization
- **Comprehensive Testing**: JUnit tests with Mockito for reliable validation

## Architecture

### Backend (Spring Boot)

- **Framework**: Spring Boot 3.5.3 with Java 21
- **Database**: MongoDB with embedded MongoDB for testing
- **AI Integration**: Ollama service with local Gemma 3:4b model
- **Validation**: Multi-layered validation system

### Frontend (React + Vite)

- **Framework**: React 18 with TypeScript
- **Build Tool**: Vite for fast development and building
- **HTTP Client**: Axios with 120-second timeout for AI processing
- **UI Components**: Modern React components with loading states

## Prerequisites

- **Java 21** or higher
- **Node.js 18** or higher
- **MongoDB** (local installation or Docker)
- **Ollama** with Gemma 3:4b model
- **Maven 3.6+** for backend builds
- **npm** for frontend dependencies

## Installation

### 1. Clone the Repository

```bash
git clone https://github.com/AvaneeshGarg/API_PNR_Validator.git
cd API_PNR_Validator
```

### 2. Setup Ollama

```bash
# Install Ollama (if not already installed)
curl -fsSL https://ollama.ai/install.sh | sh

# Pull the Gemma 3:4b model
ollama pull gemma3:4b

# Start Ollama service
ollama serve
```

### 3. Setup Backend

```bash
# Install Maven dependencies
mvn clean install

# Run tests
mvn test

# Start the application
mvn spring-boot:run
```

### 4. Setup Frontend

```bash
cd vite-frontend

# Install dependencies
npm install

# Start development server
npm run dev
```

## Configuration

### Backend Configuration (`application.properties`)

```properties
# Server Configuration
server.port=8080

# MongoDB Configuration
spring.data.mongodb.uri=mongodb://localhost:27017/pnr_validator

# Ollama Configuration
ollama.base-url=http://localhost:11434
ollama.model=gemma3:4b
ollama.timeout=120s

# CORS Configuration
cors.allowed-origins=http://localhost:5173,http://localhost:3000
```

### Frontend Configuration

The frontend connects to the backend at `http://localhost:8080` with extended timeout for AI processing.

## API Endpoints

### Anomaly Detection

```http
POST /api/detect-anomaly
Content-Type: application/json

{
  "name": "John Smith",
  "date": "1985-01-01",
  "iata": "US",
  "seatNumber": "12A",
  "cabinClass": "ECONOMY"
}
```

### Health Check

```http
GET /api/health
```

### Test Ollama Connection

```http
GET /api/test-ollama
```

## Testing

### Backend Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=OllamaAnomalyDetectionServiceTest

# Run tests with coverage
mvn test jacoco:report
```

### Frontend Tests

```bash
cd vite-frontend

# Run tests
npm test

# Run tests in watch mode
npm run test:watch
```

## Validation Rules

### Rule-Based Validation

- **Name Validation**: Checks for suspicious/fake names, special characters, length
- **Age Validation**: Validates age range (12-100 years)
- **Date Validation**: Ensures valid birth dates (not future dates)
- **IATA Validation**: Validates against country codes from CSV database
- **Cabin Class**: Validates against approved cabin classes (A-F, ECONOMY, BUSINESS, FIRST)

### AI-Powered Validation

- **Cultural Awareness**: Prevents bias against international names
- **Pattern Recognition**: Detects subtle anomalies in passenger data
- **Confidence Scoring**: Provides confidence levels for AI decisions
- **Contextual Analysis**: Considers multiple data points together

## CI/CD Pipeline

### GitHub Actions Workflows

- **Backend CI**: Runs Java tests with Maven
- **Frontend CI**: Runs Node.js tests and builds
- **CodeQL**: Security analysis for Java code
- **MongoDB Integration**: Tests with containerized MongoDB

## Monitoring & Debugging

### Debug Features

- Request/Response logging
- AI model response tracing
- Performance metrics
- Error handling with fallback mechanisms

### Production Monitoring

- Health check endpoints
- Ollama service status monitoring
- MongoDB connection health
- API response times

## Security Features

- Input validation and sanitization
- CORS protection
- CodeQL security scanning
- Timeout protection for AI requests
- Fallback mechanisms for service failures

## Project Structure

```
deeplearning-extractor/
├── src/main/java/com/coforge/deeplearning_extractor/
│   ├── autoencoder/          # Feature extraction and validation
│   ├── controller/           # REST API controllers
│   ├── ollama/              # Ollama AI integration
│   └── configs/             # Configuration classes
├── src/test/java/           # JUnit tests
├── vite-frontend/           # React frontend
│   ├── src/                 # React components
│   ├── public/              # Static assets
│   └── package.json         # Node.js dependencies
├── .github/workflows/       # CI/CD pipelines
└── pom.xml                  # Maven configuration
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Authors

- **Avaneesh Garg** - _Initial work_ - [AvaneeshGarg](https://github.com/AvaneeshGarg)

## Acknowledgments

- Ollama team for the local AI model infrastructure
- Spring Boot community for the excellent framework
- React and Vite teams for modern frontend tooling
- MongoDB for reliable data storage

## Support

For support, email [your-email@example.com] or create an issue in the GitHub repository.

---

**Built with love using Spring Boot, React, and Ollama AI**
