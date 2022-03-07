package org.sid.bankspringbatch;

import org.sid.bankspringbatch.dao.BankTransaction;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Configuration
@EnableBatchProcessing
public class SpringBatchConfig {
   @Autowired
   private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    @Autowired
    private ItemReader<BankTransaction> bankTransactionItemReader;
    @Autowired
    private ItemWriter<BankTransaction> bankTransactionItemWriter;
   // @Autowired
    //private ItemProcessor<BankTransaction,BankTransaction> bankTransactionItemProcessor;

    @Bean
    public Job bankJob(){
        Step step1=stepBuilderFactory.get("step-load-data")
                .<BankTransaction,BankTransaction>chunk(100)
                .reader(bankTransactionItemReader)
                .processor(compositetemProcessor())
                .writer(bankTransactionItemWriter)
                .build();

        return jobBuilderFactory.get("bank-data-loader-job")
                .start(step1).build();
    }

   @Bean
    public ItemProcessor<BankTransaction,BankTransaction> compositetemProcessor() {
       List<ItemProcessor<BankTransaction,BankTransaction>> itemProcessors=new ArrayList<>(2);
       itemProcessors.add(itemProcessor1());
       itemProcessors.add(itemProcessor2());

       CompositeItemProcessor<BankTransaction,BankTransaction> compositeItemProcessor=
               new CompositeItemProcessor<>();
       compositeItemProcessor.setDelegates(itemProcessors);

       return  compositeItemProcessor;


    }
    @Bean
    BankTransactionItemProcessor itemProcessor1(){
        return new BankTransactionItemProcessor();
    }
    @Bean
    BankTransactionItemAnalyticsProcessor itemProcessor2(){
        return new BankTransactionItemAnalyticsProcessor();
    }

    @Bean
    public FlatFileItemReader<BankTransaction> getItemReader(@Value("${inputFile}") Resource inputFile){
     FlatFileItemReader<BankTransaction> flatFileItemReader=new FlatFileItemReader<>();
     flatFileItemReader.setName("CSV-READER");
     //header of file it was a names column
     flatFileItemReader.setLinesToSkip(1);
     flatFileItemReader.setResource(inputFile);
     //mapper of own line
     flatFileItemReader.setLineMapper(lineMapper());
     return flatFileItemReader;

    }
    @Bean
    public LineMapper<BankTransaction> lineMapper() {
        DefaultLineMapper<BankTransaction> lineMapper=new DefaultLineMapper<>();
        DelimitedLineTokenizer lineTokenizer=new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames("id","accountID", "strTransactionDate","transactionType","amount");
        lineMapper.setLineTokenizer(lineTokenizer);

        BeanWrapperFieldSetMapper<BankTransaction> fieldSetMapper=new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(BankTransaction.class);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return lineMapper;
    }



}
