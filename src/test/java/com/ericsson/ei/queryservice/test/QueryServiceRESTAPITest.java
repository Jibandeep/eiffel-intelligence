package com.ericsson.ei.queryservice.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.ericsson.queryservice.App;
import com.ericsson.queryservice.controller.AggregatedObjectController;
import com.ericsson.queryservice.controller.AggregatedObjectControllerImpl;
import com.ericsson.queryservice.controller.MissedNotificationControllerImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

@ContextConfiguration(classes = { App.class })
@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest(value = AggregatedObjectController.class, secure = false)
public class QueryServiceRESTAPITest {

    @Autowired
    private MockMvc mockMvc;

    static JSONArray jsonArray = null;

    static Logger log = (Logger) LoggerFactory.getLogger(QueryServiceRESTAPITest.class);

    private static final String aggregatedPath = "src/test/resources/AggregatedObject.json";
    private static final String missedNotificationPath = "src/test/resources/MissedNotification.json";
    private static final String aggregatedOutputPath = "src/test/resources/AggregatedOutput.json";
    private static final String missedNotificationOutputPath = "src/test/resources/MissedNotificationOutput.json";
    private static String aggregatedObject;
    private static String missedNotification;

    ObjectMapper mapper = new ObjectMapper();

    @MockBean
    private AggregatedObjectControllerImpl aggregatedObjectController;

    @MockBean
    private MissedNotificationControllerImpl missedNotificationController;

    @BeforeClass
    public static void init() throws IOException, JSONException {
        aggregatedObject = FileUtils.readFileToString(new File(aggregatedPath));
        missedNotification = FileUtils.readFileToString(new File(missedNotificationPath));
    }

    @Test
    public void getQueryAggregatedObjectTest() throws Exception {
        ArrayList<String> response = new ArrayList<String>();
        response.add(aggregatedObject);
        String expectedOutput = FileUtils.readFileToString(new File(aggregatedOutputPath));
        log.info("The expected output is : " + expectedOutput.toString());

        Mockito.when(aggregatedObjectController.getQueryAggregatedObject(Mockito.anyString()))
                .thenReturn(new ResponseEntity(response, HttpStatus.OK));

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/query/aggregatedObject")
                .accept(MediaType.APPLICATION_JSON).param("ID", "6acc3c87-75e0-4b6d-88f5-b1a5d4e62b43")
                .contentType(MediaType.APPLICATION_JSON);
        MvcResult result = result = mockMvc.perform(requestBuilder).andReturn();

        String output = result.getResponse().getContentAsString().toString();
        // output = output.replaceAll("(\\s\\s\\s\\s)", "");
        // output = output.replaceAll("\\r\\n", "");
        log.info("The Output is : " + output);

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertEquals(expectedOutput, output);
    }

    @Test
    public void getQueryMissedNotificationsTest() throws Exception {
        ArrayList<String> response = new ArrayList<String>();
        response.add(missedNotification);
        String expectedOutput = FileUtils.readFileToString(new File(missedNotificationOutputPath));
        log.info("The expected output is : " + expectedOutput.toString());

        Mockito.when(missedNotificationController.getQueryMissedNotifications(Mockito.anyString()))
                .thenReturn(new ResponseEntity(response, HttpStatus.OK));

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/query/missedNotifications?")
                .accept(MediaType.APPLICATION_JSON).param("SubscriptionName", "Subscription_1");
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        String output = result.getResponse().getContentAsString().toString();
        log.info("The Output is : " + output);

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertEquals(expectedOutput, output);
    }
}
