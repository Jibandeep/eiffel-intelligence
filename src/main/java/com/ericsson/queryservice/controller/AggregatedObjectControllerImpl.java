/*
    Copyright 2017 Ericsson AB.
    For a full list of individual contributors, please see the commit history.
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package com.ericsson.queryservice.controller;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestParam;

import com.ericsson.queryservice.ProcessAggregatedObject;
import com.ericsson.queryservice.controller.model.QueryResponse;

@Component
@CrossOrigin
public class AggregatedObjectControllerImpl implements AggregatedObjectController {

    static Logger log = (Logger) LoggerFactory.getLogger(AggregatedObjectControllerImpl.class);

    @Autowired
    private ProcessAggregatedObject processAggregatedObject;

    public ResponseEntity<QueryResponse> getQueryAggregatedObject(@RequestParam("ID") String id) {
        ArrayList<String> response = processAggregatedObject.processQueryAggregatedObject(id);
        log.info("The response is : " + response.toString());
        return new ResponseEntity(response.toString(), HttpStatus.OK);
    }

}
