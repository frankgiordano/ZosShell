package com.commands;

import org.beryx.textio.TextTerminal;
import zosjobs.GetJobs;
import zosjobs.input.GetJobParams;
import zosjobs.response.Job;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

public class JobLog {

    protected final TextTerminal<?> terminal;
    protected final GetJobs getJobs;
    private final boolean isAll;

    public JobLog(TextTerminal<?> terminal, GetJobs getJobs, boolean isAll) {
        this.terminal = terminal;
        this.getJobs = getJobs;
        this.isAll = isAll;
    }

    protected List<String> getJobLog(String param) throws Exception {
        var jobParams = new GetJobParams.Builder("*").prefix(param).build();
        final var jobs = getJobs.getJobsCommon(jobParams);
        if (jobs.isEmpty()) {
            terminal.println(jobParams.getPrefix().orElse("n\\a") + " does not exist, try again...");
            return new ArrayList<>();
        }
        // select the active one first not found then get the highest job number
        Optional<Job> job = jobs.stream().filter(j -> "ACTIVE".equalsIgnoreCase(j.getStatus().orElse(""))).findAny();
        final var files = getJobs.getSpoolFilesForJob(job.orElse(jobs.get(0)));

        if (!isAll) {
            return Arrays.asList(getJobs.getSpoolContent(files.get(0)).split("\n"));
        }

        ExecutorService pool = Executors.newFixedThreadPool(1);
        Future<List<String>> result = pool.submit(new GetAllJobLog(getJobs, files));
        try {
            return result.get(60, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new Exception("timeout");
        }
    }

}