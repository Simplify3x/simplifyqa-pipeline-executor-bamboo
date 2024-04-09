package it.com.simplifyqa.bamboo.plugins;

import static org.junit.Assert.assertEquals;

import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.sal.api.ApplicationProperties;
import com.simplifyqa.bamboo.plugins.api.ExecutionServices;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AtlassianPluginsTestRunner.class)
public class MyComponentWiredTest {

  private final ApplicationProperties applicationProperties;
  private final ExecutionServices ExecutionServices;

  public MyComponentWiredTest(
    ApplicationProperties applicationProperties,
    ExecutionServices ExecutionServices
  ) {
    this.applicationProperties = applicationProperties;
    this.ExecutionServices = ExecutionServices;
  }
  // @Test
  // public void testMyName() {
  //   assertEquals(
  //     "names do not match!",
  //     "myComponent:" + applicationProperties.getDisplayName(),
  //     ExecutionServices.getName()
  //   );
  // }
}
