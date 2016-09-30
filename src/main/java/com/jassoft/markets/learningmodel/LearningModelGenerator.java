package com.jassoft.markets.learningmodel;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.jassoft.markets.datamodel.learningmodel.LearningModelRecord;
import com.jassoft.markets.repository.LearningModelRepository;
import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.*;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by jonshaw on 01/09/15.
 */
//@Component
public class LearningModelGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(LearningModelGenerator.class);

    private final String bucketName = "market-reaction-learning-model";
    private final int batchSize = 100;

    @Autowired
    private LearningModelRepository learningModelRepository;

    @Autowired
    private AmazonS3Client s3Client;

    public void setS3Client(AmazonS3Client s3Client) {
        this.s3Client = s3Client;
    }

    //TODO this currently loads into memory, stream to disk or to AWS
    @Scheduled(cron = "0 0 */5 * * ?")
    public void generateModel() throws IOException {
        try {
            Writer stringWriter = new StringWriter();
            CSVWriter writer = new CSVWriter(stringWriter);

            // Write the Csv Header
            writer.writeNext(LearningModelBuilder.makeHeader());

            int page = 0;

            while(true) {
                LOG.info("Generating Learning Model page {}", page);
                Page<LearningModelRecord> learningModelRecordPage = learningModelRepository.findAll(new PageRequest(page, batchSize));

                List<LearningModelRecord> learningModelRecordList = learningModelRecordPage.getContent();

                if(learningModelRecordList.isEmpty()) {
                    break;
                }

                for (LearningModelRecord learningModelRecord : learningModelRecordList) {
                    writer.writeNext(LearningModelBuilder.makeRow(learningModelRecord));
                }

                page++;
            }

            writer.close();

            byte[] barray = stringWriter.toString().getBytes(Charset.forName("UTF-8"));
            InputStream is = new ByteArrayInputStream(barray);

            ObjectMetadata metadata = new ObjectMetadata();
//            metadata.setContentLength(IOUtils.toByteArray(is).length);

            LOG.info("Writing Learning Model to S3 Bucket: {}", bucketName);

            s3Client.putObject(bucketName, "learningModel.csv", is, metadata);
        }
        catch (Exception exception) {
            LOG.error("Failed to generate Learning Model Export", exception);
        }
    }
}
