package com.jassoft.markets.learningmodel;

import com.jassoft.markets.datamodel.company.CompanyBuilder;
import com.jassoft.markets.datamodel.company.quote.QuoteBuilder;
import com.jassoft.markets.repository.CompanyRepository;
import com.jassoft.markets.repository.LearningModelRepository;
import com.jassoft.markets.repository.QuoteRepository;
import com.jassoft.utils.BaseRepositoryTest;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.jms.TextMessage;

import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * Created by jonshaw on 18/03/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringConfiguration.class)
@IntegrationTest
public class LearningModelListenerTest extends BaseRepositoryTest {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private LearningModelRepository learningModelRepository;

    @Autowired
    private LearningModelListener target;

    @Test
    public void onMessage_companyWithNoQuotes_doesNotNotAddToModel() throws Exception {

        String companyId = companyRepository.save(CompanyBuilder.aCompany()
                .build())
                .getId();

        String quoteId = quoteRepository.save(QuoteBuilder.aQuote()
                .withIntraday(false)
                .withDate(new Date())
                .withCompany(companyId)
                .build()).getId();

        TextMessage textMessage = new ActiveMQTextMessage();
        textMessage.setText(quoteId);

        target.onMessage(textMessage);

        assertEquals(0, learningModelRepository.count());

    }
}