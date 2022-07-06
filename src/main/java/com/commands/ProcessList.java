package com.commands;

import com.Constants;
import com.dto.ResponseStatus;
import com.utility.Util;
import org.beryx.textio.TextTerminal;
import zowe.client.sdk.zosjobs.GetJobs;
import zowe.client.sdk.zosjobs.input.GetJobParams;
import zowe.client.sdk.zosjobs.response.Job;

import java.util.Comparator;
import java.util.List;

public class ProcessList {

    private final TextTerminal<?> terminal;
    private final GetJobs getJobs;
    private final GetJobParams.Builder getJobParams = new GetJobParams.Builder("*");

    public ProcessList(TextTerminal<?> terminal, GetJobs getJobs) {
        this.terminal = terminal;
        this.getJobs = getJobs;
    }

    public ResponseStatus ps(String jobOrTask) {
        List<Job> jobs;
        try {
            if (jobOrTask != null) {
                getJobParams.prefix(jobOrTask).build();
            }
            var params = getJobParams.build();
            jobs = getJobs.getJobsCommon(params);
        } catch (Exception e) {
            return new ResponseStatus(Util.getErrorMsg(e + ""), false);
        }
        jobs.sort(Comparator.comparing((Job j) -> j.getJobName().orElse(""))
                .thenComparing(j -> j.getStatus().orElse(""))
                .thenComparing(j -> j.getJobId().orElse("")));
        if (jobs.isEmpty()) {
            return new ResponseStatus(Constants.NO_PROCESS_FOUND, false);
        }
        jobs.forEach(job -> {
            var jobName = job.getJobName().orElse("");
            var jobId = job.getJobId().orElse("");
            var jobStatus = job.getStatus().orElse("");
            terminal.println(String.format("%-8s %-8s %-8s", jobName, jobId, jobStatus));
        });
        return new ResponseStatus("", true);
    }

}
