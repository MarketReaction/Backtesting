package uk.co.jassoft.markets.learningmodel;

import uk.co.jassoft.markets.datamodel.learningmodel.LearningModelRecord;

/**
 * Created by jonshaw on 01/09/15.
 */
public class LearningModelBuilder {
    public static String[] makeHeader () {
        return new String[]{"LearningModelId","Exchange","Company","PreviousQuoteDirection","PreviousSentimentDirection","LastSentimentDifferenceFromAverage","LastSentiment","ResultingQuoteChange"};
    }

    public static String[] makeRow(LearningModelRecord learningModelRecord) {
        return new String[]{learningModelRecord.getId(),
                learningModelRecord.getExchange(),
                learningModelRecord.getCompany(),
                learningModelRecord.getPreviousQuoteDirection().name(),
                learningModelRecord.getPreviousSentimentDirection().name(),
                String.valueOf(learningModelRecord.getLastSentimentDifferenceFromAverage()),
                String.valueOf(learningModelRecord.getLastSentiment()),
                String.valueOf(learningModelRecord.getResultingQuoteChange())
        };
    }
}
