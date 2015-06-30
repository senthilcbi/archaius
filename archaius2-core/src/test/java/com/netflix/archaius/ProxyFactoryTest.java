package com.netflix.archaius;

import org.junit.Test;

import com.netflix.archaius.annotations.DefaultValue;
import com.netflix.archaius.config.DefaultSettableConfig;
import com.netflix.archaius.config.EmptyConfig;
import com.netflix.archaius.config.MapConfig;
import com.netflix.archaius.config.SettableConfig;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ProxyFactoryTest {
    public static enum TestEnum {
        NONE,
        A, 
        B,
        C
    }
    
    public static interface BaseConfig {
        @DefaultValue("basedefault")
        String getStr();
        
        Boolean getBaseBoolean();
    }
    
    public static interface RootConfig extends BaseConfig {
        @DefaultValue("default")
        @Override
        String getStr();
        
        @DefaultValue("0")
        int getInteger();
        
        @DefaultValue("NONE")
        TestEnum getEnum();
        
        SubConfig getSubConfig();
        
        @DefaultValue("default1:default2")
        SubConfigFromString getSubConfigFromString();
    }
    
    public static interface SubConfig {
        @DefaultValue("default")
        String str();
    }
    
    public static class SubConfigFromString {
        private String[] parts;

        public SubConfigFromString(String value) {
            this.parts = value.split(":");
        }
        
        public String part1() {
            return parts[0];
        }
        
        public String part2() {
            return parts[1];
        }
        
        @Override
        public String toString() {
            return "SubConfigFromString[" + part1() + ", " + part2() + "]";
        }
    }
    
    @Test
    public void testDefaultValues() {
        Config config = EmptyConfig.INSTANCE;

        PropertyFactory factory = DefaultPropertyFactory.from(config);
        ConfigProxyFactory proxy = new ConfigProxyFactory(config.getDecoder(), factory);
        
        RootConfig a = proxy.newProxy(RootConfig.class);
        
        assertThat(a.getStr(),                          equalTo("default"));
        assertThat(a.getInteger(),                      equalTo(0));
        assertThat(a.getEnum(),                         equalTo(TestEnum.NONE));
        assertThat(a.getSubConfig().str(),              equalTo("default"));
        assertThat(a.getSubConfigFromString().part1(),  equalTo("default1"));
        assertThat(a.getSubConfigFromString().part2(),  equalTo("default2"));
        System.out.println(a);
    }
    
    @Test
    public void testAllPropertiesSet() {
        SettableConfig config = new DefaultSettableConfig();
        config.setProperty("prefix.str", "str1");
        config.setProperty("prefix.integer", 1);
        config.setProperty("prefix.enum", TestEnum.A.name());
        config.setProperty("prefix.subConfigFromString", "a:b");
        config.setProperty("prefix.subConfig.str", "str2");
        config.setProperty("prefix.baseBoolean", true);
        
        PropertyFactory factory = DefaultPropertyFactory.from(config);
        ConfigProxyFactory proxy = new ConfigProxyFactory(config.getDecoder(), factory);
        
        RootConfig a = proxy.newProxy(RootConfig.class, "prefix");
        
        assertThat(a.getStr(),      equalTo("str1"));
        assertThat(a.getInteger(),  equalTo(1));
        assertThat(a.getEnum(),     equalTo(TestEnum.A));
        assertThat(a.getSubConfig().str(),      equalTo("str2"));
        assertThat(a.getSubConfigFromString().part1(), equalTo("a"));
        assertThat(a.getSubConfigFromString().part2(), equalTo("b"));

        config.setProperty("prefix.subConfig.str", "str3");
        assertThat(a.getSubConfig().str(),      equalTo("str3"));
        
        System.out.println(a);
    }
}
