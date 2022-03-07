package org.sid.bankspringbatch.web;

import org.sid.bankspringbatch.BankTransactionItemAnalyticsProcessor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class JobRestController {
    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job job;
     @Autowired
     private BankTransactionItemAnalyticsProcessor itemAnalyticsProcessor;
    @GetMapping("/startJob")
    public BatchStatus load() throws Exception{
        Map<String, JobParameter> params=new HashMap<>();

        params.put("time",new JobParameter(System.currentTimeMillis()));
        JobParameters jobparameters=new JobParameters(params);
        JobExecution jobExecution=jobLauncher.run(job,jobparameters);
        while(jobExecution.isRunning()){
            System.out.println("........");
        }
        return jobExecution.getStatus();
    }
    @GetMapping("/analytics")
    public Map<String,Double> analytics() {
        Map<String, Double> map=new HashMap<>();

   map.put("totalCredit",itemAnalyticsProcessor.getTotalCredit());
        map.put("totalDebit",itemAnalyticsProcessor.getTotalDebit());

        return map;
    }
}
