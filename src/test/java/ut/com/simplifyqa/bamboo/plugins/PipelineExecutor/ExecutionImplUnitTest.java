package ut.com.simplifyqa.bamboo.plugins.PipelineExecutor;

import com.simplifyqa.bamboo.plugins.PipelineExecutor.api.Execution;
import com.simplifyqa.bamboo.plugins.PipelineExecutor.impl.ExecutionImpl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExecutionImplUnitTest {
    @Test
    public void testMyName() {
        Execution component = new ExecutionImpl(null);
        assertEquals("names do not match!", "myComponent", component.getName());
    }
}