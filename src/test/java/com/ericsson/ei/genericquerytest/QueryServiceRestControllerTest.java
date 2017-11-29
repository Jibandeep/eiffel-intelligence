/***********************************************************************
 *                                                                     *
 * Copyright Ericsson AB 2017                                          *
 *                                                                     *
 * No part of this software may be reproduced in any form without the  *
 * written permission of the copyright owner.                          *
 *                                                                     *
 ***********************************************************************/
package com.ericsson.ei.genericquerytest;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import org.apache.qpid.util.FileUtils;
import org.bson.Document;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ericsson.ei.genericquery.ProcessQueryParams;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { QueryServiceRestControllerTest.class }, webEnvironment = WebEnvironment.RANDOM_PORT)
public class QueryServiceRestControllerTest {

    @Value("${aggregationCollectionName}")
    private String aggregationCollectionName;

    @Value("${aggregationDataBaseName}")
    private String aggregationDataBaseName;

    @Value("${missedNotificationCollectionName}")
    private String missedNotificationCollectionName;

    @Value("${missedNotificationDataBaseName}")
    private String missedNotificationDataBaseName;

    @Autowired
    private ProcessQueryParams unitUnderTest;

    static Logger log = (Logger) LoggerFactory.getLogger(QueryServiceRestControllerTest.class);

    private static final String inputPath = "src/test/resources/AggregatedObject.json";

    String query = "{\"criteria\" :{\"testCaseExecutions.testCase.verdict\":\"PASSED\", \"testCaseExecutions.testCase.id\":\"TC5\" }, \"options\" :{ \"id\": \"6acc3c87-75e0-4b6d-88f5-b1a5d4e62b44\"} }";

    @BeforeClass
    public static void insertData() {
        String input = FileUtils.readFileAsString(new File(inputPath));

        MongoClient mongoClient = new MongoClient();
        try {
            DB db = mongoClient.getDB("demo");
            DBCollection table = db.getCollection("aggObject");
            DBObject dbObjectInput = (DBObject) JSON.parse(input);
            WriteResult result1 = table.insert(dbObjectInput);
            db = mongoClient.getDB("MissedNotification");
            table = db.getCollection("Notification");
            WriteResult result2 = table.insert(dbObjectInput);
            if (result1.wasAcknowledged() && result2.wasAcknowledged()) {
                System.out.println(" Data Inserted successfully in both the Collections");
            }
        } catch (Exception e) {
            log.info(e.getMessage(), e);
        }

    }

    @Test
    public void filterFormParamTest() throws JSONException, JsonProcessingException, IOException {
        String input = FileUtils.readFileAsString(new File(inputPath));
        JSONObject inputJArr = new JSONObject(input);
        log.info("The input string is : " + inputJArr.toString());
        JsonNode inputCriteria = null;
        JSONArray result = null;

        DB db = new MongoClient().getDB(aggregationDataBaseName);
        Jongo jongo = new Jongo(db);
        MongoCollection aggObjects = jongo.getCollection(aggregationCollectionName);
        MongoCursor<Document> all = aggObjects.find(new ObjectMapper().readTree(query).toString()).as(Document.class);
        Document one = aggObjects.findOne().as(Document.class);
        JSONObject json = new JSONObject(one.toJson());

        log.info("Expect Output for FilterFormParamTest : " + json.toString());
        System.out.println(json.toString());
        JSONObject output = null;
        try {
            inputCriteria = new ObjectMapper().readTree(query);
            result = unitUnderTest.filterFormParam(inputCriteria);
            output = result.getJSONObject(0);
            log.info("Output for FilterFormParamTest is : " + output.toString());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        assertEquals(output.toString(), json.toString());
        // assertThat(output.toString(0), containsString(inputJArr.toString()));
    }

    @Test
    public void filterQueryParamTest() throws JsonProcessingException, IOException, JSONException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName("localhost:8080");
        request.setRequestURI("/ei/query");
        request.setQueryString(
                "testCaseExecutions.testCase.verdict:PASSED,testCaseExecutions.testCase.id:TC5,id:6acc3c87-75e0-4b6d-88f5-b1a5d4e62b44");

        String url = request.getRequestURL() + "?" + request.getQueryString();

        JsonNode inputCriteria = null;
        JSONArray result = null;
        DB db = new MongoClient().getDB(aggregationDataBaseName);
        Jongo jongo = new Jongo(db);
        MongoCollection aggObjects = jongo.getCollection(aggregationCollectionName);
        MongoCursor<Document> all = aggObjects.find(new ObjectMapper().readTree(query).toString()).as(Document.class);
        Document one = aggObjects.findOne().as(Document.class);
        JSONObject json = new JSONObject(one.toJson());
        log.info("Expect Output for FilterQueryParamTest : " + json.toString());
        JSONObject output = null;
        try {
            inputCriteria = new ObjectMapper().readTree(query);
            result = unitUnderTest.filterQueryParam(request);
            output = result.getJSONObject(0);
            log.info("Returned output from ProcessQueryParams : " + output.toString());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        assertThat(url, is(
                "http://localhost:8080/ei/query?testCaseExecutions.testCase.verdict:PASSED,testCaseExecutions.testCase.id:TC5,id:6acc3c87-75e0-4b6d-88f5-b1a5d4e62b44"));

        assertEquals(output.toString(), json.toString());
    }

}
