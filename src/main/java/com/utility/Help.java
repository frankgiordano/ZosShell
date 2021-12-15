package com.utility;

import org.beryx.textio.TextTerminal;

import java.util.List;

public class Help {

    private static final List<String> HELP = List.of(
            "browsejob <arg1> <arg2> - where arg1 is a job name and arg2 is optional",
            "                          if arg2 not specified, display job's JESMSGLG spool output",
            "                          if arg2 is equal to \"all\", display all job's spool output",
            "cancel <arg>            - where arg is a task/job name",
            "cat                     - display contents",
            "cd <arg>                - where arg is a dataset value or empty",
            "change <arg>            - where arg is a number representing a connection",
            "connections             - a list of connection(s)",
            "count members           - return member count in dataset",
            "count datasets          - return dataset count in dataset",
            "cp | copy <arg> arg>    - where arg can be \".\", member, dataset or dataset(member)",
            "download <arg>          - download arg to local c:\\ZosShell\\pwd where arg is member or sequential dataset",
            "end                     - end session closes shell UI window",
            "files                   - list all files under local pwd drive value",
            "h | help                - list commands",
            "history <arg>           - where arg is optional and indicates the number to display from bottom",
            "!n                      - where n is a number, to execute command number n in history list",
            "!string                 - will execute the last history command starting with that \"string\"",
            "ls <arg>                - where arg is a dataset value or empty",
            "ls -l <arg>             - where arg is a dataset value or empty",
            "ps                      - display all processes running",
            "ps <arg>                - where arg is a task/job name",
            "pwd                     - show current working dataset",
            "rm <arg>                - where arg is \"*\", member, dataset, or dataset with member value",
            "save <arg>              - save arg where arg is a file name from files command to the current pwd",
            "search <arg>            - search for arg within a job log from the last browse command performed",
            "stop <arg>              - where arg is a task/job name",
            "submit <arg>            - where arg is a member name",
            "tailjob <arg1> <arg2> <arg3>  - where arg1 is job name and arg2 and arg3 are optional",
            "                                use arg2 to specify either line limit or \"all\" value",
            "                                if \"all\" is specified, display output from all of job's spool content",
            "                                line limit is 25 by default if not specified in arg2",
            "touch <arg>             - create member arg if it does not already exist",
            "uname                   - show current connected host name",
            "vi <arg>                - where arg is a sequential dataset or member name, arg will be downloaded",
            "                          and displayed for editing, use save command to save changes",
            "v | visited             - a list of visited datasets",
            "whoami                  - show current connected user name)");

    public static void displayHelp(TextTerminal<?> terminal) {
        HELP.forEach(terminal::println);
    }

}