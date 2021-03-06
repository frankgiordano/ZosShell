package com.commands;

import com.Constants;
import com.dto.ResponseStatus;
import com.google.common.base.Strings;
import com.utility.DirectorySetup;
import com.utility.Util;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import zowe.client.sdk.utility.UtilIO;
import zowe.client.sdk.zosfiles.ZosDsnDownload;
import zowe.client.sdk.zosfiles.input.DownloadParams;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Download {

    public static final String DIRECTORY_PATH_WINDOWS = Constants.PATH_FILE_DIRECTORY_WINDOWS + "\\";
    public static final String DIRECTORY_PATH_MAC = Constants.PATH_FILE_DIRECTORY_MAC + "/";
    private final ZosDsnDownload download;
    private DownloadParams dlParams;
    private final boolean isBinary;

    public Download(ZosDsnDownload download, boolean isBinary) {
        this.download = download;
        this.isBinary = isBinary;
    }

    public ResponseStatus download(String dataSet, String member) {
        var message = Strings.padStart(member, 8, ' ') + Constants.ARROW;

        DirectorySetup dirSetup = new DirectorySetup();
        try {
            dirSetup.initialize(dataSet, member);
        } catch (Exception e) {
            return new ResponseStatus(message + Constants.OS_ERROR, false);
        }

        try {
            String textContent;
            InputStream binaryContent;

            if (!isBinary) {
                dlParams = new DownloadParams.Builder().build();
                textContent = getTextContent(dataSet, member);
                if (textContent == null) {
                    return new ResponseStatus(message + Constants.DOWNLOAD_FAIL, false);
                }
                Util.writeTextFile(textContent, dirSetup.getDirectoryPath(), dirSetup.getFileNamePath());
            } else {
                dlParams = new DownloadParams.Builder().binary(true).build();
                binaryContent = getBinaryContent(dataSet, member);
                if (binaryContent == null) {
                    return new ResponseStatus(message + Constants.DOWNLOAD_FAIL, false);
                }
                writeBinaryFile(binaryContent, dirSetup.getDirectoryPath(), dirSetup.getFileNamePath());
            }
            message += "downloaded to " + dirSetup.getFileNamePath();
        } catch (Exception e) {
            if (e.getMessage().contains(Constants.CONNECTION_REFUSED)) {
                return new ResponseStatus(message + Constants.CONNECTION_REFUSED, false);
            }
            return new ResponseStatus(message + e.getMessage(), false);
        }
        return new ResponseStatus(message, true);
    }

    private void writeBinaryFile(InputStream input, String directoryPath, String fileNamePath) throws IOException {
        Files.createDirectories(Paths.get(directoryPath));
        FileUtils.copyInputStreamToFile(input, new File(fileNamePath));
    }

    private String getTextContent(String dataSet, String param) throws Exception {
        var inputStream = getInputStream(dataSet, param);
        return getTextStreamData(inputStream);
    }

    private InputStream getBinaryContent(String dataSet, String param) throws Exception {
        return getInputStream(dataSet, param);
    }

    public InputStream getInputStream(String dataSet, String param) throws Exception {
        InputStream inputStream;
        if (dlParams == null) {
            dlParams = new DownloadParams.Builder().build();
        }
        if (Util.isDataSet(param)) {
            inputStream = download.downloadDsn(String.format("%s", param), dlParams);
        } else {
            inputStream = download.downloadDsn(String.format("%s(%s)", dataSet, param), dlParams);
        }
        return inputStream;
    }

    private String getTextStreamData(InputStream inputStream) throws IOException {
        if (inputStream != null) {
            final var writer = new StringWriter();
            IOUtils.copy(inputStream, writer, UtilIO.UTF8);
            final var content = writer.toString();
            inputStream.close();
            return content;
        }
        return null;
    }

}
