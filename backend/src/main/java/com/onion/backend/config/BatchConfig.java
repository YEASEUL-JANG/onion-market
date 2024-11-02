package com.onion.backend.config;

import com.onion.backend.dto.AdViewHistoryResDto;
import com.onion.backend.service.AdvertisementService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Configuration
@EnableBatchProcessing
@EnableScheduling
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final AdvertisementService advertisementService;
    private final JobLauncher jobLauncher;


    public BatchConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                       AdvertisementService advertisementService, JobLauncher jobLauncher) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.advertisementService = advertisementService;
        this.jobLauncher = jobLauncher;
    }
    // Job 정의
    @Bean
    public Job adViewStatJob() {
        return new JobBuilder("adViewStatJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(adViewStatStep())
                .build();
    }

    // Step 정의
    @Bean
    public Step adViewStatStep() {
        return new StepBuilder("adViewStatStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    // 필요한 데이터를 불러와서 insertAdViewStat 메서드에 전달
                    List<AdViewHistoryResDto> results = advertisementService.getAdViewHistoryGroupedByAdId();
                    advertisementService.insertAdViewStat(results);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    // 매일 자정에 배치 작업 실행
    @Scheduled(cron = "0 0 0 * * ?")
    public void performAdViewStatJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis()) // JobParameters에 고유 ID 추가
                .toJobParameters();
        jobLauncher.run(adViewStatJob(), jobParameters);
    }
}
