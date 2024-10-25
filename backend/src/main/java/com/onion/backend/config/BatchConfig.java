package com.onion.backend.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    public BatchConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    @Bean
    public Job exampleJob() {
        return new JobBuilder("exampleJob", jobRepository)
                .start(exampleStep())
                .build();
    }

    @Bean
    public Step exampleStep() {
        return new StepBuilder("exampleStep", jobRepository)
                .<String, String>chunk(10, transactionManager)
                .reader(exampleReader())
                .processor(exampleProcessor())
                .writer(exampleWriter())
                .build();
    }

    @Bean
    public ItemReader<String> exampleReader() {
        return new ItemReader<String>() {
            private int count = 0;
            private final String[] data = {"One", "Two", "Three", "Four", "Five"};

            @Override
            public String read() {
                if (count < data.length) {
                    return data[count++];
                }
                return null; // null을 반환하여 읽기 종료
            }
        };
    }

    @Bean
    public ItemProcessor<String, String> exampleProcessor() {
        return item -> "Processed " + item;
    }

    @Bean
    public ItemWriter<String> exampleWriter() {
        return items -> items.forEach(System.out::println);
    }
}
