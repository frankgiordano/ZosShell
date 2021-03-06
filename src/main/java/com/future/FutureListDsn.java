package com.future;

import zowe.client.sdk.zosfiles.ZosDsnList;
import zowe.client.sdk.zosfiles.input.ListParams;
import zowe.client.sdk.zosfiles.response.Dataset;

import java.util.List;
import java.util.concurrent.Callable;

public class FutureListDsn implements Callable<List<Dataset>> {

    private final ZosDsnList zosDsnList;
    private final String dataSet;
    private final ListParams params;

    public FutureListDsn(ZosDsnList zosDsnList, String dataSet, ListParams params) {
        this.zosDsnList = zosDsnList;
        this.dataSet = dataSet;
        this.params = params;
    }

    @Override
    public List<Dataset> call() throws Exception {
        return zosDsnList.listDsn(dataSet, params);
    }

}
