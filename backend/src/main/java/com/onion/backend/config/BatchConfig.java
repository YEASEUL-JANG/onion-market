package com.onion.backend.config;

import com.onion.backend.dto.AdViewHistoryResDto;
import com.onion.backend.dto.HotArticleDto;
import com.onion.backend.entity.Article;
import com.onion.backend.repository.ArticleRepository;
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
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

@Configuration
@EnableBatchProcessing
@EnableScheduling
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final AdvertisementService advertisementService;
    private final JobLauncher jobLauncher;
    private final ArticleRepository articleRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String REDIS_KEY = "hot-article:";


    public BatchConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                       AdvertisementService advertisementService, JobLauncher jobLauncher, ArticleRepository articleRepository, RedisTemplate<String, Object> redisTemplate) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.advertisementService = advertisementService;
        this.jobLauncher = jobLauncher;
        this.articleRepository = articleRepository;
        this.redisTemplate = redisTemplate;
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

    // Job 정의
    @Bean
    public Job pickHotArticleJob() {
        return new JobBuilder("pickHotArticleJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(pickHotArticleStep())
                .build();
    }

    // Step 정의
    @Bean
    public Step pickHotArticleStep() {
        return new StepBuilder("pickHotArticleStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    LocalDateTime startDate = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MIN);
                    LocalDateTime endDate = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
                    Article article = articleRepository.findHotArticle(startDate,endDate);
                    if(article!=null){
                        HotArticleDto hotArticleDto = new HotArticleDto();
                        hotArticleDto.setId(article.getId());
                        hotArticleDto.setTitle(article.getTitle());
                        hotArticleDto.setContent(article.getContent());
                        hotArticleDto.setAuthorName(article.getAuthorName());
                        hotArticleDto.setCreatedDate(article.getCreatedDate());
                        hotArticleDto.setUpdatedDate(article.getUpdatedDate());
                        hotArticleDto.setViewCount(article.getViewCount());
                        redisTemplate.opsForHash().put(REDIS_KEY+article.getId(),article.getId(), hotArticleDto);

                    }

                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    // 매일 자정에 배치 작업 실행
    @Scheduled(cron = "0 0 0 * * ?")
    public void performPickHotArticleJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis()) // JobParameters에 고유 ID 추가
                .toJobParameters();
        jobLauncher.run(pickHotArticleJob(), jobParameters);
    }

}
