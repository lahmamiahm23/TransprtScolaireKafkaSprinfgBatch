package com.example.transport.config;

import com.example.transport.Repositories.PenaltyRepository;
import com.example.transport.entitie.Penalty;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Collections;

@Configuration
@RequiredArgsConstructor
public class PenaltyExportBatchConfig {

    private final PenaltyRepository penaltyRepository;

    @Bean
    public Job exportPenaltyJob(JobRepository jobRepository, Step step1) {
        return new JobBuilder("exportPenaltyJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(step1)
                .build();
    }

    @Bean
    public Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("csvExportStep", jobRepository)
                .<Penalty, Penalty>chunk(10, transactionManager)
                .reader(penaltyReader())
                .writer(penaltyCsvWriter())
                .build();
    }

    @Bean
    public RepositoryItemReader<Penalty> penaltyReader() {
        return new RepositoryItemReaderBuilder<Penalty>()
                .name("penaltyReader")
                .repository(penaltyRepository)
                .methodName("findAll")
                .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public FlatFileItemWriter<Penalty> penaltyCsvWriter() {
        return new FlatFileItemWriterBuilder<Penalty>()
                .name("penaltyCsvWriter")
                .resource(new FileSystemResource("exports/penalties_report.csv"))
                .delimited()
                .delimiter(",")
                .names("id", "reason", "amount") // On simplifie pour le test
                .headerCallback(writer -> writer.write("ID,REASON,AMOUNT"))
                .build();
    }
}
