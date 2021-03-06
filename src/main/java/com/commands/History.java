package com.commands;

import com.Constants;
import com.data.CircularLinkedList;
import com.google.common.base.Strings;
import com.utility.Util;
import org.beryx.textio.TextTerminal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class History {

    private final TextTerminal<?> terminal;
    private final List<String> commandLst = new LinkedList<>();
    private final CircularLinkedList<String> circularLinkedList = new CircularLinkedList<>();

    public History(TextTerminal<?> terminal) {
        this.terminal = terminal;
    }

    public void listUpCommands(String prompt) {
        terminal.resetLine();
        terminal.printf(prompt + " " + circularLinkedList.back().trim());
    }

    public void listDownCommands(String prompt) {
        terminal.resetLine();
        terminal.printf(prompt + " " + circularLinkedList.forward().trim());
    }

    public void addHistory(String[] params) {
        final var str = new StringBuilder();
        Arrays.stream(params).forEach(p -> {
            str.append(p);
            str.append(" ");
        });
        var command = str.toString();
        if (!command.startsWith("history")) {
            if (commandLst.size() == Constants.HISTORY_SIZE) {
                commandLst.remove(0);
            }
            if (commandLst.isEmpty() || !getLastHistory().equals(command)) {
                commandLst.add(command);
                circularLinkedList.add(command);
            } else {
                return;
            }
        }
        // reset the currNode pointer used to handle history scrolling..
        circularLinkedList.currNode = null;
    }

    public void displayHistory() {
        displayAll();
    }

    public void displayHistory(String param) {
        int num;
        try {
            num = Integer.parseInt(param);
        } catch (NumberFormatException e) {
            terminal.println(Constants.INVALID_NUMBER);
            return;
        }

        if (this.commandLst.isEmpty()) {
            terminal.println(Constants.NO_HISTORY);
            return;
        }
        final var size = commandLst.size();
        if (num > size) {
            displayAll();
        } else {
            var startIndex = size - num;
            for (var i = startIndex; i < size; i++) {
                display(i);
            }
        }
    }

    public String[] filterCommand(String prompt, String[] command) {
        if (!prompt.equals(command[0])) {
            return command;
        }

        List<String> list = new ArrayList<>(Arrays.asList(command));
        // remove multiple spaces entered by end user and nulls
        list.removeAll(Arrays.asList("", null));

        String[] newCommand;
        if (list.get(0).equals(Util.getPrompt())) {
            newCommand = new String[list.size() - 1];
            // remove prompt before sending new command
            for (int i = 1, j = 0; i < list.size(); i++, j++) {
                newCommand[j] = list.get(i);
            }
        } else {
            // prompt not seen maybe removed by end user when scrolling through history
            newCommand = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                newCommand[i] = list.get(i);
            }
        }

        return newCommand;
    }

    public String getHistoryByIndex(int index) {
        if (index > commandLst.size() - 1 || commandLst.isEmpty()) {
            terminal.println(Constants.NO_HISTORY);
            return null;
        }
        return commandLst.get(index);
    }

    public String getLastHistory() {
        if (commandLst.isEmpty()) {
            terminal.println(Constants.NO_HISTORY);
            return null;
        }
        return commandLst.get(commandLst.size() - 1);
    }

    public String getLastHistoryByValue(String str) {
        final var lst = commandLst.stream().filter(c -> c.startsWith(str.toLowerCase())).collect(Collectors.toList());
        if (lst.isEmpty()) {
            terminal.println(Constants.NO_HISTORY);
            return null;
        }
        return lst.get(lst.size() - 1);
    }

    private void displayAll() {
        IntStream.range(0, commandLst.size()).forEach(this::display);
    }

    private void display(int i) {
        final var orderNum = Strings.padStart(String.valueOf(i + 1), 4, ' ');
        final var historyRow = orderNum + Constants.ARROW + commandLst.get(i);
        terminal.println(historyRow);
    }

}
