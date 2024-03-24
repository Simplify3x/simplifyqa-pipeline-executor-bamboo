package it.com.simplifyqa.bamboo.plugins.SQAPipelineExecutor;

import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.sal.api.ApplicationProperties;
import com.simplifyqa.bamboo.plugins.SQAPipelineExecutor.impl.ExecutionImpl;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AtlassianPluginsTestRunner.class)
public class ExecutionImplWiredTest {
    private final ApplicationProperties applicationProperties;
    private final ExecutionImpl execImplObj;

    public ExecutionImplWiredTest(ApplicationProperties applicationProperties, ExecutionImpl execImplObj) {
        this.applicationProperties = applicationProperties;
        this.execImplObj = execImplObj;
    }

    @Test
    public void testMyName() {
        assertEquals("names do not match!", "myComponent:" + applicationProperties.getDisplayName(), execImplObj.getName());
    }
}