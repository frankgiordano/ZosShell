package com.commands;

import com.Constants;
import com.utility.Util;
import org.apache.commons.io.IOUtils;
import org.beryx.textio.TextTerminal;
import zowe.client.sdk.utility.UtilIO;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Arrays;

public class Concatenate {

    private final TextTerminal<?> terminal;
    private final Download download;

    public Concatenate(TextTerminal<?> terminal, Download download) {
        this.terminal = terminal;
        this.download = download;
    }

    public void cat(String dataSet, String param) {
        InputStream inputStream;
        try {
            inputStream = download.getInputStream(dataSet, param);
            display(inputStream);
        } catch (Exception e) {
            if (e.getMessage().contains(Constants.CONNECTION_REFUSED)) {
                terminal.println(Constants.SEVERE_ERROR);
                return;
            }
            Util.printError(terminal, e.getMessage());
        }
    }

    private void display(InputStream inputStream) throws IOException {
        if (inputStream != null) {
            var writer = new StringWriter();
            IOUtils.copy(inputStream, writer, UtilIO.UTF8);
            var content = writer.toString().split("\\n");
            Arrays.stream(content).forEach(terminal::println);
            inputStream.close();
        }
    }

}
