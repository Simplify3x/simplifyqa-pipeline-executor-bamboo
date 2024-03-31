package com.simplifyqa.bamboo.plugins.PipelineExecutor.impl;

import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.ApplicationProperties;
import com.simplifyqa.bamboo.plugins.PipelineExecutor.api.Execution;

import javax.inject.Inject;
import javax.inject.Named;

@ExportAsService({ExecutionImpl.class})
@Named("myPluginComponent")
public class ExecutionImpl implements Execution {
    @ComponentImport
    private final ApplicationProperties applicationProperties;

    @Inject
    public ExecutionImpl(final ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    public String getName() {
        if (null != applicationProperties) {
            return "myComponent:" + applicationProperties.getDisplayName();
        }

        return "myComponent";
    }
}