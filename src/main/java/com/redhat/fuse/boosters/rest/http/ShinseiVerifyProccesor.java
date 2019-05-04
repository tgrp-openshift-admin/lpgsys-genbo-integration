package com.redhat.fuse.boosters.rest.http;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.ArrayList;
import java.util.Arrays;

import com.tokaicom.genbo.*;

import org.kie.api.command.Command;
import org.kie.api.command.KieCommands;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.KieServiceResponse.ResponseType;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.RuleServicesClient;
import org.kie.server.client.KieServicesClient;
import org.kie.api.KieServices;

public class ShinseiVerifyProccesor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        // 通信先のDM設定
        String SERVERURL = "https://dm-kieserver-lpgsys.754d.tgrp.openshiftapps.com/services/rest/server";
        String USER ="executionUser";
        String PASSWORD = "gqQSPS4!";
        String KSESSION = "ksession-dtables";
        MarshallingFormat FORMAT = MarshallingFormat.JSON;
        String CONTAINER = "genbo_1.0.1";
        String OUTIDENTIFIER = "resultfact";

        //KieServicesConfiguration を作成
        KieServicesConfiguration config = KieServicesFactory.newRestConfiguration(SERVERURL, USER, PASSWORD);
        config.setMarshallingFormat(FORMAT);
        KieServicesClient kieServicesClient = KieServicesFactory.newKieServicesClient(config);
        RuleServicesClient rulesClient = kieServicesClient.getServicesClient(RuleServicesClient.class);
        KieCommands commandsFactory = KieServices.Factory.get().getCommands();

        //BODYからFactを取得
        Shinsei shinsei = exchange.getIn().getBody(Shinsei.class);
        
        //結果用のFact作成
        Result result = new Result();
        result.setErrormsg(new ArrayList<String>());
        
        //FactをInsert
        Command<?> insert1 = commandsFactory.newInsert(shinsei);
        Command<?> insert2 = commandsFactory.newInsert(result,OUTIDENTIFIER); //応答に欲しいFactにはOutIdentifierをつける

        Command<?> fireAllRules = commandsFactory.newFireAllRules();
        Command<?> batchCommand = commandsFactory.newBatchExecution(Arrays.asList(insert1,insert2, fireAllRules),KSESSION);

        //ルールを実行
        ServiceResponse<ExecutionResults> executeResponse = rulesClient.executeCommandsWithResults(CONTAINER, batchCommand);

        //成功時
        if(executeResponse.getType() == ResponseType.SUCCESS) {
            System.out.println("Commands executed with success! Response: ");
            result = (Result) executeResponse.getResult().getValue(OUTIDENTIFIER); //応答からオブジェクトを得るためにOutIdentifierを指定
            System.out.println(executeResponse.getResult());
          }
        //失敗時
        else {
            System.out.println("Error executing rules. Message: ");
            result.getErrormsg().add("ルールの実行に失敗しました");
            System.out.println(executeResponse.getMsg());
        }
        exchange.getIn().setBody(result);
    }

}