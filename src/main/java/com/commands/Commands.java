package com.commands;

import com.Constants;
import com.dto.JobOutput;
import com.dto.Member;
import com.dto.ResponseStatus;
import com.future.*;
import com.utility.Help;
import com.utility.Util;
import org.beryx.textio.TextTerminal;
import org.beryx.textio.swing.SwingTextTerminal;
import zowe.client.sdk.core.ZOSConnection;
import zowe.client.sdk.zosconsole.IssueCommand;
import zowe.client.sdk.zosfiles.ZosDsn;
import zowe.client.sdk.zosfiles.ZosDsnCopy;
import zowe.client.sdk.zosfiles.ZosDsnDownload;
import zowe.client.sdk.zosfiles.ZosDsnList;
import zowe.client.sdk.zosjobs.GetJobs;
import zowe.client.sdk.zosjobs.SubmitJobs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Commands {

    private final List<ZOSConnection> connections;
    private final TextTerminal<?> terminal;
    private final SwingTextTerminal mainTerminal;
    private long timeOutValue = Constants.FUTURE_TIMEOUT_VALUE;

    public Commands(List<ZOSConnection> connections, TextTerminal<?> terminal, SwingTextTerminal mainTerminal) {
        this.connections = connections;
        this.terminal = terminal;
        this.mainTerminal = mainTerminal;
    }

    public JobOutput browse(ZOSConnection connection, String[] params) {
        if (params.length == 3) {
            if (!"all".equalsIgnoreCase(params[2])) {
                terminal.println(Constants.INVALID_PARAMETER);
                return null;
            }
            return browseAll(connection, params, true);
        }
        return browseAll(connection, params, false);
    }

    private JobOutput browseAll(ZOSConnection connection, String[] params, boolean isAll) {
        BrowseJob browseJob;
        try {
            browseJob = new BrowseJob(terminal, new GetJobs(connection), isAll, timeOutValue);
        } catch (Exception e) {
            Util.printError(terminal, e.getMessage());
            return null;
        }
        StringBuilder output;
        try {
            output = browseJob.browseJob(params[1]);
        } catch (Exception e) {
            if (e.getMessage().contains("timeout")) {
                terminal.println(Constants.BROWSE_TIMEOUT);
                return null;
            }
            Util.printError(terminal, e.getMessage());
            return null;
        }
        terminal.println(output.toString());
        return new JobOutput(params[1], output);
    }

    public void cancel(ZOSConnection connection, String jobOrTask) {
        final var pool = Executors.newFixedThreadPool(1);
        final var submit = pool.submit(new FutureTerminate(new IssueCommand(connection), Terminate.Type.CANCEL, jobOrTask));
        processFuture(pool, submit);
    }

    public void cat(ZOSConnection connection, String dataSet, String member) {
        final var pool = Executors.newFixedThreadPool(1);
        final var submit = pool.submit(new FutureConcatenate(terminal, new Download(new ZosDsnDownload(connection), false), dataSet, member));
        processFuture(pool, submit);
    }

    public String cd(ZOSConnection connection, String currDataSet, String param) {
        ChangeDir changeDir;
        try {
            changeDir = new ChangeDir(terminal, new ZosDsnList(connection));
        } catch (Exception e) {
            Util.printError(terminal, e.getMessage());
            return currDataSet;
        }
        return changeDir.cd(currDataSet, param);
    }

    public ZOSConnection change(ZOSConnection connection, String[] commands) {
        var changeConn = new ChangeConn(terminal, connections);
        return changeConn.changeConnection(connection, commands);
    }

    public void color(String param) {
        var color = new Color(terminal);
        color.color(param);
    }

    public void connections(ZOSConnection connection) {
        var changeConn = new ChangeConn(terminal, connections);
        changeConn.displayConnections(connection);
    }

    public void copy(ZOSConnection connection, String currDataSet, String[] params) {
        if ("*".equals(params[1])) {
            final List<String> members = Util.getMembers(terminal, connection, currDataSet);
            if (members.isEmpty()) {
                return;
            }
            multipleCopy(connection, currDataSet, params[2], members).forEach(i -> terminal.println(i.getMessage()));
            return;
        }
        Copy copy;
        try {
            copy = new Copy(new ZosDsnCopy(connection));
        } catch (Exception e) {
            Util.printError(terminal, e.getMessage());
            return;
        }
        var responseStatus = copy.copy(currDataSet, params);
        terminal.println(responseStatus.getMessage());
    }

    public void count(ZOSConnection connection, String dataSet, String filter) {
        final var pool = Executors.newFixedThreadPool(1);
        final var submit = pool.submit(new FutureCount(new ZosDsnList(connection), dataSet, filter));
        processFuture(pool, submit);
    }

    public void download(ZOSConnection connection, String currDataSet, String member, boolean isBinary) {
        if ("*".equals(member)) {
            final List<String> members = Util.getMembers(terminal, connection, currDataSet);
            if (members.isEmpty()) {
                return;
            }
            multipleDownload(connection, currDataSet, members, isBinary).forEach(i -> terminal.println(i.getMessage()));
            return;
        }
        Download download;
        try {
            download = new Download(new ZosDsnDownload(connection), isBinary);
        } catch (Exception e) {
            Util.printError(terminal, e.getMessage());
            return;
        }
        var result = download.download(currDataSet, member);

        if (!result.isStatus()) {
            terminal.println(Util.getMsgAfterArrow(result.getMessage()));
            terminal.println("cannot open " + member + ", try again...");
        } else {
            terminal.println(result.getMessage());
        }
    }

    public void downloadJob(ZOSConnection currConnection, String param, boolean isAll) {
        DownloadJob downloadJob = new DownloadJob(terminal, new GetJobs(currConnection), isAll, timeOutValue);
        downloadJob.download(param);
    }

    private List<ResponseStatus> multipleDownload(ZOSConnection connection, String dataSet, List<String> members, boolean isBinary) {
        final var pool = Executors.newFixedThreadPool(members.size());
        var futures = new ArrayList<Future<ResponseStatus>>();

        for (final var member : members) {
            futures.add(pool.submit(new FutureDownload(new ZosDsnDownload(connection), dataSet, member, isBinary)));
        }

        var result = getFutureResults(futures);
        pool.shutdownNow();
        return result;
    }

    private List<ResponseStatus> multipleCopy(ZOSConnection connection, String fromDataSetName, String toDataSetName,
                                              List<String> members) {
        if (!Util.isDataSet(toDataSetName)) {
            terminal.println(Constants.INVALID_DATASET);
            return new ArrayList<>();
        }

        final var pool = Executors.newFixedThreadPool(members.size());
        var futures = new ArrayList<Future<ResponseStatus>>();

        for (final var member : members) {
            futures.add(pool.submit(new FutureCopy(new ZosDsnCopy(connection), fromDataSetName, toDataSetName, member)));
        }

        var result = getFutureResults(futures);
        pool.shutdownNow();
        return result;
    }

    private List<ResponseStatus> getFutureResults(List<Future<ResponseStatus>> futures) {
        final var results = new ArrayList<ResponseStatus>();
        for (final var future : futures) {
            try {
                results.add(future.get(timeOutValue, TimeUnit.SECONDS));
            } catch (TimeoutException e) {
                results.add(new ResponseStatus("timeout", false));
            } catch (InterruptedException | ExecutionException e) {
                results.add(new ResponseStatus(Util.getErrorMsg(e + ""), false));
            }
        }
        return results;
    }

    public void files(String dataSet) {
        LocalFiles.listFiles(terminal, dataSet);
    }

    public void help() {
        Help.displayHelp(terminal);
    }

    public void ls(ZOSConnection connection, String member, String dataSet) {
        final var listing = new Listing(terminal, new ZosDsnList(connection), mainTerminal, timeOutValue);
        try {
            listing.ls(member, dataSet, true);
        } catch (TimeoutException e) {
            terminal.println(Constants.TIMEOUT_MESSAGE);
        } catch (ExecutionException | InterruptedException e) {
            terminal.println(Util.getErrorMsg(e + ""));
        }
    }

    public void ls(ZOSConnection connection, String dataSet) {
        final var listing = new Listing(terminal, new ZosDsnList(connection), mainTerminal, timeOutValue);
        try {
            listing.ls(null, dataSet, true);
        } catch (TimeoutException e) {
            terminal.println(Constants.TIMEOUT_MESSAGE);
        } catch (ExecutionException | InterruptedException e) {
            terminal.println(Util.getErrorMsg(e + ""));
        }
    }

    public void lsl(ZOSConnection connection, String dataSet) {
        this.lsl(connection, null, dataSet);
    }

    public void lsl(ZOSConnection connection, String member, String dataSet) {
        final var listing = new Listing(terminal, new ZosDsnList(connection), mainTerminal, timeOutValue);
        try {
            listing.ls(member, dataSet, false);
        } catch (TimeoutException e) {
            terminal.println(Constants.TIMEOUT_MESSAGE);
        } catch (ExecutionException | InterruptedException e) {
            terminal.println(Util.getErrorMsg(e + ""));
        }
    }

    public void mvsCommand(ZOSConnection connection, String command) {
        final var pool = Executors.newFixedThreadPool(1);
        final var submit = pool.submit(new FutureMvs(connection, command));
        processFuture(pool, submit);
    }

    public void ps(ZOSConnection connection) {
        ps(connection, null);
    }

    public void ps(ZOSConnection connection, String jobOrTask) {
        final var pool = Executors.newFixedThreadPool(1);
        final var submit = pool.submit(new FutureProcessList(new GetJobs(connection), jobOrTask));
        processFuture(pool, submit);
    }

    public void rm(ZOSConnection connection, String currDataSet, String param) {
        Delete delete;
        try {
            delete = new Delete(terminal, new ZosDsn(connection), new ZosDsnList(connection));
        } catch (Exception e) {
            Util.printError(terminal, e.getMessage());
            return;
        }
        delete.rm(currDataSet, param);
    }

    public void save(ZOSConnection connection, String dataSet, String[] params) {
        final var member = params[1];
        final var pool = Executors.newFixedThreadPool(1);
        final var submit = pool.submit(new FutureSave(new ZosDsn(connection), dataSet, member));
        processFuture(pool, submit);
    }

    public void searchJobLog(JobOutput job, String text) {
        var search = new SearchJobLog(terminal);
        search.search(job, text);
    }

    public void stop(ZOSConnection connection, String jobOrTask) {
        final var pool = Executors.newFixedThreadPool(1);
        final var submit = pool.submit(new FutureTerminate(new IssueCommand(connection), Terminate.Type.STOP, jobOrTask));
        processFuture(pool, submit);
    }

    public void submit(ZOSConnection connection, String dataSet, String jobName) {
        final var pool = Executors.newFixedThreadPool(1);
        final var submit = pool.submit(new FutureSubmit(new SubmitJobs(connection), dataSet, jobName));
        processFuture(pool, submit);
    }

    public JobOutput tailJob(ZOSConnection connection, String[] params) {
        if (params.length == 4) {
            if (!"all".equalsIgnoreCase(params[3])) {
                terminal.println(Constants.INVALID_PARAMETER);
                return null;
            }
            try {
                Integer.parseInt(params[2]);
            } catch (NumberFormatException e) {
                terminal.println(Constants.INVALID_PARAMETER);
                return null;
            }
            return new JobOutput(params[1], tailAll(connection, params, true));
        }
        if (params.length == 3) {
            if ("all".equalsIgnoreCase(params[2])) {
                return new JobOutput(params[1], tailAll(connection, params, true));
            }
            try {
                Integer.parseInt(params[2]);
            } catch (NumberFormatException e) {
                terminal.println(Constants.INVALID_PARAMETER);
                return null;
            }
        }
        return new JobOutput(params[1], tailAll(connection, params, false));
    }

    private StringBuilder tailAll(ZOSConnection connection, String[] params, boolean isAll) {
        Tail tail;
        try {
            tail = new Tail(terminal, new GetJobs(connection), isAll, timeOutValue);
        } catch (Exception e) {
            Util.printError(terminal, e.getMessage());
            return null;
        }
        return tail.tail(params);
    }

    public void timeOutValue(long value) {
        timeOutValue = value;
        terminal.println("timeout value set to " + timeOutValue + " seconds.");
    }

    public void timeOutValue() {
        terminal.println("timeout value is " + timeOutValue + " seconds.");
    }

    public void touch(ZOSConnection connection, String dataSet, String[] params) {
        final var member = params[1];
        final var pool = Executors.newFixedThreadPool(1);
        final var submit = pool.submit(new FutureTouch(new ZosDsn(connection), new Member(new ZosDsnList(connection)), dataSet, member));
        processFuture(pool, submit);
    }

    public void vi(ZOSConnection connection, String dataSet, String[] params) {
        final var member = params[1];
        final var pool = Executors.newFixedThreadPool(1);
        final var submit = pool.submit(new FutureVi(new Download(new ZosDsnDownload(connection), false), dataSet, member));
        processFuture(pool, submit);
    }

    private void processFuture(ExecutorService pool, Future<ResponseStatus> submit) {
        try {
            final var result = submit.get(timeOutValue, TimeUnit.SECONDS);
            terminal.println(result.getMessage());
        } catch (TimeoutException e) {
            terminal.println(Constants.TIMEOUT_MESSAGE);
        } catch (ExecutionException | InterruptedException e) {
            terminal.println(Util.getErrorMsg(e + ""));
        }
        pool.shutdownNow();
    }

}
