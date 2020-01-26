package net.ambitious.bvlion.batch2.web.controller;

import net.ambitious.bvlion.batch2.util.AccessUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index() {
        return AccessUtil.getYmdhms();
    }
}
