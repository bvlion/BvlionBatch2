package net.ambitious.bvlion.batch2.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;

@Slf4j
@RestController
public class IndexController {
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String index() {
		return FastDateFormat.getInstance("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance());
	}
}