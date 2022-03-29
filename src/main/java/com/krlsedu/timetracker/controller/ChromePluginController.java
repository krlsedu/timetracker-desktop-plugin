package com.krlsedu.timetracker.controller;

import com.krlsedu.timetracker.core.model.ApplicationDetail;
import com.krlsedu.timetracker.desktop.ApplicationDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
public class ChromePluginController {

    @ResponseStatus(HttpStatus.ACCEPTED)
    @CrossOrigin
    @PostMapping(value = "/chrome-info", consumes = "application/json")
    public void recebeInfoChrome(@RequestBody ApplicationDetail applicationDetail) {
        ApplicationDetailService.setChromeInfos(applicationDetail);
    }
}
