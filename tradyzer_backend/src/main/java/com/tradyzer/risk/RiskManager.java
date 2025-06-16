package com.tradyzer.risk;

import com.tradyzer.entity.Portfolio;
import com.tradyzer.entity.PortfolioHolding;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Component
public class RiskManager {

    // حدود المخاطر
    private final double MAX_POSITION_SIZE_PERCENT = 10.0; // 10% max per position
    private final double MAX_DAILY_LOSS_PERCENT = 5.0;    // 5% max daily loss
    private final double MAX_PORTFOLIO_RISK = 20.0;       // 20% max total risk

    public RiskAssessment assessPortfolioRisk(Portfolio portfolio) {
        RiskAssessment assessment = new RiskAssessment();

        // حساب التنويع
        double diversificationScore = calculateDiversification(portfolio);
        assessment.setDiversificationScore(diversificationScore);

        // حساب التعرض للمخاطر
        double exposureRisk = calculateExposureRisk(portfolio);
        assessment.setExposureRisk(exposureRisk);

        // حساب نسبة الخسارة
        double drawdown = calculateDrawdown(portfolio);
        assessment.setCurrentDrawdown(drawdown);

        // تقييم عام للمخاطر
        double overallRisk = (exposureRisk + (100 - diversificationScore) + drawdown) / 3;
        assessment.setOverallRiskScore(overallRisk);

        // توصيات
        generateRecommendations(assessment, portfolio);

        return assessment;
    }

    public boolean canExecuteTrade(Portfolio portfolio, BigDecimal tradeAmount, String symbol) {
        // التحقق من حجم المركز
        BigDecimal portfolioValue = portfolio.getTotalValue();
        BigDecimal positionPercent = tradeAmount.divide(portfolioValue, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100));

        if (positionPercent.doubleValue() > MAX_POSITION_SIZE_PERCENT) {
            return false; // المركز كبير جداً
        }

        // التحقق من الخسارة اليومية
        if (portfolio.getProfitLossPercentage() < -MAX_DAILY_LOSS_PERCENT) {
            return false; // تجاوز حد الخسارة اليومية
        }

        return true;
    }

    public BigDecimal calculateSafePositionSize(Portfolio portfolio, BigDecimal currentPrice, double riskPercent) {
        BigDecimal portfolioValue = portfolio.getTotalValue();
        BigDecimal riskAmount = portfolioValue.multiply(new BigDecimal(riskPercent / 100));

        // Position size = Risk Amount / Price
        return riskAmount.divide(currentPrice, 8, RoundingMode.HALF_UP);
    }

    private double calculateDiversification(Portfolio portfolio) {
        if (portfolio.getHoldings().isEmpty()) {
            return 100; // محفظة فارغة = تنويع كامل
        }

        // حساب توزيع الأصول
        Map<String, Double> assetWeights = new HashMap<>();
        BigDecimal totalValue = portfolio.getTotalValue();

        for (PortfolioHolding holding : portfolio.getHoldings()) {
            double weight = holding.getCurrentValue()
                    .divide(totalValue, 4, RoundingMode.HALF_UP)
                    .doubleValue() * 100;
            assetWeights.put(holding.getSymbol(), weight);
        }

        // حساب مؤشر هيرفندال
        double hhi = assetWeights.values().stream()
                .mapToDouble(w -> w * w)
                .sum();

        // تحويل إلى نقاط (0-100)
        return Math.max(0, 100 - (hhi / 100));
    }

    private double calculateExposureRisk(Portfolio portfolio) {
        if (portfolio.getCurrentBalance().compareTo(portfolio.getTotalValue()) == 0) {
            return 0; // كل الأموال نقدية = لا توجد مخاطر
        }

        BigDecimal invested = portfolio.getTotalValue().subtract(portfolio.getCurrentBalance());
        double exposurePercent = invested.divide(portfolio.getTotalValue(), 4, RoundingMode.HALF_UP)
                .doubleValue() * 100;

        return exposurePercent;
    }

    private double calculateDrawdown(Portfolio portfolio) {
        if (portfolio.getProfitLossPercentage() == null || portfolio.getProfitLossPercentage() >= 0) {
            return 0;
        }

        return Math.abs(portfolio.getProfitLossPercentage());
    }

    private void generateRecommendations(RiskAssessment assessment, Portfolio portfolio) {
        if (assessment.getDiversificationScore() < 60) {
            assessment.addRecommendation("⚠️ محفظتك غير متنوعة بشكل كافٍ. فكر في توزيع استثماراتك على عملات أكثر.");
        }

        if (assessment.getExposureRisk() > 80) {
            assessment.addRecommendation("⚠️ معظم رأس مالك مستثمر. احتفظ ببعض النقد للفرص الجديدة.");
        }

        if (assessment.getCurrentDrawdown() > 10) {
            assessment.addRecommendation("🔴 خسائرك تتجاوز 10%. فكر في تقليل المخاطر أو إيقاف التداول مؤقتاً.");
        }

        if (assessment.getOverallRiskScore() > 70) {
            assessment.addRecommendation("🚨 مستوى المخاطر مرتفع جداً! راجع استراتيجيتك.");
        } else if (assessment.getOverallRiskScore() < 30) {
            assessment.addRecommendation("✅ مستوى المخاطر منخفض. يمكنك زيادة الاستثمار إذا أردت.");
        }
    }
}