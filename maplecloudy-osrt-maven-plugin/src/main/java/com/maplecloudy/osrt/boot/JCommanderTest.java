package com.maplecloudy.osrt.boot;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import com.google.common.collect.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JCommanderTest {
    @Parameter
    public List<String> parameters = Lists.newArrayList();
 
    @Parameter(names = { "-log", "-verbose" }, description = "Level of verbosity")
    public Integer verbose = 1;
 
    @Parameter(names = "-groups", description = "Comma-separated list of group names to be run")
    public String groups;
 
    @Parameter(names = "-debug", description = "Debug mode")
    public boolean debug = false;

    @DynamicParameter(names = "-D", description = "Dynamic parameters go here")
    public Map<String, String> dynamicParams = new HashMap<String, String>();

}