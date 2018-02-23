
package com.awc.paymentbatch.paymentbatch;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.sql.DataSource;


import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.H2PagingQueryProvider;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import com.awc.domain.Customer;
import com.awc.domain.CustomerRowMapper;


@Configuration
public class JobConfiguration {


	
	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Autowired
	public DataSource dataSource;

	@Bean
	public JdbcPagingItemReader<Customer> pagingItemReader() {
		System.out.println("*********************Reader****");

		JdbcPagingItemReader<Customer> reader = new JdbcPagingItemReader<>();

		reader.setDataSource(this.dataSource);
		reader.setFetchSize(10);
		reader.setRowMapper(new CustomerRowMapper());

		H2PagingQueryProvider queryProvider = new H2PagingQueryProvider();
		queryProvider.setSelectClause("id, firstName, lastName");
		queryProvider.setFromClause("from customer");
		//queryProvider.setWhereClause("batch_id=");
		Map<String, Order> sortKeys = new HashMap<>(1);

		sortKeys.put("id", Order.ASCENDING);

		queryProvider.setSortKeys(sortKeys);

		reader.setQueryProvider(queryProvider);
		System.out.println("size"+reader.toString());
		return reader;
	}
	@Bean
	public JobLauncher jobLauncher(JobRepository jobRepo){
	    SimpleJobLauncher simpleJobLauncher = new SimpleJobLauncher();
	    simpleJobLauncher.setJobRepository(jobRepo);
	    return simpleJobLauncher;
	}
	@Bean
	public ItemProcessor itemProcessor() {
		return new ItemProcessor<Customer, Customer>() {
			
			@Override
			public Customer process(Customer item) throws Exception {
				System.out.println(Thread.currentThread().getName()+"*********************Entry-Processor****"+item);

				Thread.sleep(new Random().nextInt(10000));
				System.out.println(Thread.currentThread().getName()+"*********************Exit-Processor****"+item);
                 if(item.getId()==12) {
                	 throw new RuntimeException();
                 }
				return new Customer(item.getId(),
						item.getFirstName().toUpperCase(),
						item.getLastName().toUpperCase());
			}
		};
	}

	@Bean
	public AsyncItemProcessor asyncItemProcessor() throws Exception {
		System.out.println("*********************AyncProcessor****");

		AsyncItemProcessor<Customer, Customer> asyncItemProcessor = new AsyncItemProcessor();

		asyncItemProcessor.setDelegate(itemProcessor());
		asyncItemProcessor.setTaskExecutor(new SimpleAsyncTaskExecutor());
		asyncItemProcessor.afterPropertiesSet();

		return asyncItemProcessor;
	}

	@Bean
	public JdbcBatchItemWriter customerItemWriter() {
		JdbcBatchItemWriter<Customer> itemWriter = new JdbcBatchItemWriter<>();

		itemWriter.setDataSource(this.dataSource);
		itemWriter.setSql("update customer set retry_count=retry_count+1 where id=:id ");
		itemWriter.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider());
		itemWriter.afterPropertiesSet();

		return itemWriter;
	}

	@Bean
	public AsyncItemWriter asyncItemWriter() throws Exception {
		AsyncItemWriter<Customer> asyncItemWriter = new AsyncItemWriter<>();

		asyncItemWriter.setDelegate(customerItemWriter());
		asyncItemWriter.afterPropertiesSet();

		return asyncItemWriter;
	}

	@Bean
	public Step step1() throws Exception {
		return stepBuilderFactory.get("step")
				.chunk(10)
				.reader(pagingItemReader())
				.processor(asyncItemProcessor())
				.writer(asyncItemWriter())
				.build();
	}

	@Bean
	public Job job() throws Exception {
		return jobBuilderFactory.get("paymentBatchJob")
				.start(step1())
				.build();
	}
}
