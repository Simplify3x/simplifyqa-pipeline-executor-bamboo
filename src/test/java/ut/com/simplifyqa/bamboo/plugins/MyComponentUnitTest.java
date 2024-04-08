package ut.com.simplifyqa.bamboo.plugins;

import static org.junit.Assert.assertEquals;

import com.simplifyqa.bamboo.plugins.api.ExecutionServices;
import com.simplifyqa.bamboo.plugins.impl.ExecutionServicesImpl;
import org.junit.Test;

public class MyComponentUnitTest {

  @Test
  public void testMyName() {
    ExecutionServices component = new ExecutionServicesImpl(null);
    assertEquals("names do not match!", "myComponent", component.getName());
  }
}
