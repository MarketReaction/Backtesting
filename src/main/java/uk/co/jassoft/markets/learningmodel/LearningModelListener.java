package uk.co.jassoft.markets.learningmodel;

import uk.co.jassoft.markets.datamodel.company.Company;
import uk.co.jassoft.markets.datamodel.company.quote.Quote;
import uk.co.jassoft.markets.datamodel.company.sentiment.StorySentiment;
import uk.co.jassoft.markets.datamodel.learningmodel.LearningModelRecord;
import uk.co.jassoft.markets.datamodel.learningmodel.LearningModelRecordBuilder;
import uk.co.jassoft.markets.exceptions.quote.QuotePriceCalculationException;
import uk.co.jassoft.markets.exceptions.sentiment.SentimentException;
import uk.co.jassoft.markets.repository.CompanyRepository;
import uk.co.jassoft.markets.repository.LearningModelRepository;
import uk.co.jassoft.markets.repository.QuoteRepository;
import uk.co.jassoft.markets.repository.StorySentimentRepository;
import uk.co.jassoft.markets.utils.QuoteUtils;
import uk.co.jassoft.markets.utils.SentimentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.List;

/**
 * Created by jonshaw on 03/09/15.
 */
@Component
public class LearningModelListener {

    private static final Logger LOG = LoggerFactory.getLogger(LearningModelListener.class);

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private StorySentimentRepository storySentimentRepository;

    @Autowired
    private LearningModelRepository learningModelRepository;

    @JmsListener(destination = "QuoteUpdated", concurrency = "5")
    public void onMessage(Message message) {
        if (message instanceof TextMessage) {
            final TextMessage textMessage = (TextMessage) message;

            try {
                message.acknowledge();

                final Quote quote = quoteRepository.findOne(textMessage.getText());

                List<Quote> quotes;

                // TODO behave differently if intraday to be able to calculate time since sentiment change to quote change
                if(quote.isIntraday()) {
                    quotes = quoteRepository.findByCompanyAndIntradayAndDateLessThan(quote.getCompany(), false, quote.getDate(), new PageRequest(0, 7, new Sort(Sort.Direction.ASC, "date")));
                }
                else {
                    quotes = quoteRepository.findByCompanyAndIntradayAndDateLessThan(quote.getCompany(), false, quote.getDate(), new PageRequest(0, 7, new Sort(Sort.Direction.ASC, "date")));
                }

                final Company company = companyRepository.findOne(quote.getCompany());

                final List<StorySentiment> storySentiments = storySentimentRepository.findByCompany(company.getId());

                LearningModelRecord learningModelRecord = LearningModelRecordBuilder.aLearningModelRecord()
                        .withExchange(company.getExchange())
                        .withCompany(company.getId())
                        .withPreviousQuoteDirection(QuoteUtils.getPreviousPriceDirection(quotes))
                        .withPreviousSentimentDirection(SentimentUtil.getPreviousSentimentDirection(storySentiments, quote.getDate()))
                        .withLastSentimentDifferenceFromAverage(SentimentUtil.getLastSentimentDifferenceFromAverage(storySentiments, quote.getDate()))
                        .withLastSentiment(SentimentUtil.getLastSentiment(storySentiments))
                        .withQuoteChangeDate(quote.getDate())
                        .build();

                double change = QuoteUtils.getPriceChange(quotes);

                learningModelRecord.setResultingQuoteChange(change);

                learningModelRepository.save(learningModelRecord);
            }
            catch (QuotePriceCalculationException | SentimentException exception) {
                LOG.info(exception.getLocalizedMessage());
            }
            catch (final Exception exception) {
                LOG.error(exception.getLocalizedMessage(), exception);

                throw new RuntimeException(exception);
            }
        }
    }
}