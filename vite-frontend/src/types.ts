export interface DataRow {
  name: string;
  date: string;
  iata: string;
  seatNumber: string;
  cabinClass: string;
}

export interface AnomalyResult {
  NAME: string;
  DATE: string;
  IATA_code: string;
  Age: number;
  SeatNumber: string;
  CabinClass: string;
  currentDateTime: string;
  ruleBasedAnomaly: boolean;
  ollamaAnomaly: boolean;
  overallAnomaly: boolean;
  anomalyType: string;
  confidence: number;
  aiReasoning: string;
  aiConcerns: string[];
  recommendation: string;
  IATA_valid: boolean;
}
