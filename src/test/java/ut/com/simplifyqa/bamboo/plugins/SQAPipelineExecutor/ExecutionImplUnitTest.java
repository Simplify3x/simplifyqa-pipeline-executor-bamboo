package ut.com.simplifyqa.bamboo.plugins.SQAPipelineExecutor;

import com.simplifyqa.bamboo.plugins.SQAPipelineExecutor.api.Execution;
import com.simplifyqa.bamboo.plugins.SQAPipelineExecutor.impl.ExecutionImpl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExecutionImplUnitTest {
    @Test
    public void testMyName() {
        Execution component = new ExecutionImpl(null);
        assertEquals("names do not match!", "myComponent", component.getName());
    }
}