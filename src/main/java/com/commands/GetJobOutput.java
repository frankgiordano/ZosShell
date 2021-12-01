package com.commands;

import com.Constants;
import com.utility.Util;
import core.ZOSConnection;
import org.beryx.textio.TextTerminal;
import zosjobs.GetJobs;
import zosjobs.input.GetJobParams;
import zosjobs.response.Job;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

public class GetJobOutput {

    private final TextTerminal<?> terminal;
    private final GetJobParams.Builder jobParams = new GetJobParams.Builder("*");
    private final GetJobs getJobs;
    private boolean isAll;

    public GetJobOutput(TextTerminal<?> terminal, ZOSConnection connection, boolean isAll) {
        this.terminal = terminal;
        this.getJobs = new GetJobs(connection);
        this.isAll = isAll;
    }

    public List<String> getLog(String param) {
        try {
            return getJobLog(getJobs, jobParams.prefix(param).build());
        } catch (Exception e) {
            if (e.getMessage().contains("Connection refused")) {
                terminal.println(Constants.SEVERE_ERROR);
                return null;
            }
            Util.printError(terminal, e.getMessage());
            return null;
        }
    }

    public void tail(String[] params) {
        var output = getLog(params[1]);
        if (output == null) return;
        var size = output.size();
        var lines = 0;
        if (params.length == 3) {
            try {
                lines = Integer.parseInt(params[2]);
            } catch (NumberFormatException e) {
                terminal.println(Constants.INVALID_PARAMETER);
                return;
            }
        }

        if (lines > 0) {
            if (lines < size) {
                for (int i = size - lines; i < size; i++)
                    terminal.println(output.get(i));
            } else {
                printAll(output, size);
            }
        } else {
            int LINES_LIMIT = 25;
            if (size > LINES_LIMIT) {
                for (int i = size - LINES_LIMIT; i < size; i++)
                    terminal.println(output.get(i));
            } else {
                printAll(output, size);
            }
        }
    }

    private List<String> getJobLog(GetJobs getJobs, GetJobParams jobParams) throws Exception {
        final var jobs = getJobs.getJobsCommon(jobParams);
        // select the active one first not found then get the highest job number
        Optional<Job> job = jobs.stream().filter(j -> "ACTIVE".equalsIgnoreCase(j.getStatus().orElse(""))).findAny();
        final var files = getJobs.getSpoolFilesForJob(job.orElse(jobs.get(0)));

        if (!isAll) {
            return Arrays.asList(getJobs.getSpoolContent(files.get(0)).split("\n"));
        }

        List<String> results = new ArrayList<>();
        ExecutorService pool = Executors.newFixedThreadPool(1);
        Future<List<String>> result = pool.submit(new GetAllJobOutput(getJobs, files));
        try {
            results = result.get(60, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            terminal.println("timeout, log may be too large to display, try again with \"get\" command...");
        }
        return results;
    }

    private void printAll(List<String> output, int size) {
        for (int i = 0; i < size; i++)
            terminal.println(output.get(i));
    }

}
