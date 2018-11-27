/*
 * Copyright 2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance
 * with the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package com.amazonaws.serverless.sample.springboot.controller;



import com.amazonaws.serverless.sample.springboot.service.WalkService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;


@RestController
@EnableWebMvc
public class WalkController {

    @Autowired
    private WalkService walkService;

    @GetMapping("/server")
    public String welcome(){
        return "Welcome";
    }

    @GetMapping("/server/current/{userID}")
    public Integer getCurrentDayCount(@PathVariable(value = "userID") Long userId) throws ExecutionException, InterruptedException, SQLException {
        return walkService.getDayCount(userId, 1);
    }

    @GetMapping("/server/single/{userID}/{day}")
    public Integer getDayCount(@PathVariable(value = "userID") Long userId,
                               @PathVariable(value = "day") int day) throws ExecutionException, InterruptedException, SQLException {
        return walkService.getDayCount(userId,day);
    }


    @GetMapping("/server/range/{userID}/{startDay}/{numDays}")
    public List<Integer> getDayRangeCount(@PathVariable(value = "userID") Long userId,
                                          @PathVariable(value = "startDay") int startDay,
                                          @PathVariable(value = "numDays") int numDays) throws ExecutionException, InterruptedException, SQLException {
        return walkService.getDayRangeCount(userId, startDay, startDay + numDays - 1);
    }

    @PostMapping("/server/{userId}/{day}/{timeInterval}/{stepCount}")
    public Integer addStepCount(@PathVariable(value = "userId") Long userId,
                               @PathVariable(value = "day") int day,
                               @PathVariable(value = "timeInterval") int timeInterval,
                               @PathVariable(value = "stepCount") int stepCount) throws ExecutionException, InterruptedException, SQLException {

        return walkService.addStepCount(userId,day,timeInterval,stepCount);
    }

}
