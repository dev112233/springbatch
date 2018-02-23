package com.awc.paymentbatch.paymentbatch;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.awc.Greeting;

@RestController
public class GreetingController {

    @Autowired
    JobLauncher jobLauncher;

    @Autowired
    Job job;

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping("/greeting")
    public Greeting greeting(@RequestParam(value="name", defaultValue="World") String name) {
        return new Greeting(counter.incrementAndGet(),
                            String.format(template, name));
    }
    @RequestMapping("/batchID")
    public String runBatch(@RequestParam(value="id", defaultValue="World") String batchId) {
    	String status ="Batch run successfull";
    	try {
    		JobParametersBuilder jobBuilder= new JobParametersBuilder();

            jobBuilder.addString("batchID", batchId);
            System.out.println("batch ID "+batchId);
	        JobParameters jobParameters =jobBuilder.toJobParameters();
			jobLauncher.run(job, jobParameters);
			
		} catch (JobExecutionAlreadyRunningException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			status = "batch already running";
		} catch (JobRestartException e) {
			// TODO Auto-generated catch block
			status = "JobRestartException";
			e.printStackTrace();
		} catch (JobInstanceAlreadyCompleteException e) {
			// TODO Auto-generated catch block
			status = "Job already run";
			e.printStackTrace();
		} catch (JobParametersInvalidException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return status;
    }
    @RequestMapping("/hi")
    public String hi() {
        return "batch complete";
    }
   
}