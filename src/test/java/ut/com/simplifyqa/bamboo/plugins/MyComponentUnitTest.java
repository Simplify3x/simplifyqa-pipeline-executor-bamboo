package ut.com.simplifyqa.bamboo.plugins;

import org.junit.Test;
import com.simplifyqa.bamboo.plugins.api.MyPluginComponent;
import com.simplifyqa.bamboo.plugins.impl.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}