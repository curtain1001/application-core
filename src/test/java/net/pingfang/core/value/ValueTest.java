package net.pingfang.core.value;

import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;

public class ValueTest {


    @Test
    public void test(){
        Assert.assertEquals(1,Value.simple(1).asInt());
        Assert.assertEquals(1L,Value.simple(1).asLong());
        assertTrue(Value.simple(true).asBoolean());
        assertTrue(Value.simple("true").asBoolean());


    }
}
