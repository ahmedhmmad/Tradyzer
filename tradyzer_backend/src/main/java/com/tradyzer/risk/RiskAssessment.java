package com.tradyzer.risk;

import java.util.ArrayList;
import java.util.List;

public class RiskAssessment {
    private double overallRiskScore; // 0-100
    private double diversificationScore; // 0-100
    private double exposureRisk; // 0-100
    private double currentDrawdown; // percentage
    private List<String> recommendations = new ArrayList<>();

    // Getters and Setters
    public double getOverallRiskScore() { return overallRiskScore; }
    public void setOverallRiskScore(double overallRiskScore) { this.overallRiskScore = overallRiskScore; }

    public double getDiversificationScore() { return diversificationScore; }
    public void setDiversificationScore(double diversificationScore) { this.diversificationScore = diversificationScore; }

    public double getExposureRisk() { return exposureRisk; }
    public void setExposureRisk(double exposureRisk) { this.exposureRisk = exposureRisk; }

    public double getCurrentDrawdown() { return currentDrawdown; }
    public void setCurrentDrawdown(double currentDrawdown) { this.currentDrawdown = currentDrawdown; }

    public List<String> getRecommendations() { return recommendations; }
    public void addRecommendation(String recommendation) { this.recommendations.add(recommendation); }

    public String getRiskLevel() {
        if (overallRiskScore < 30) return "LOW";
        if (overallRiskScore < 60) return "MEDIUM";
        if (overallRiskScore < 80) return "HIGH";
        return "VERY HIGH";
    }
}