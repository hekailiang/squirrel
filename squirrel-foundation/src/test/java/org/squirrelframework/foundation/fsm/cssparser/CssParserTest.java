package org.squirrelframework.foundation.fsm.cssparser;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CssParserTest {
    
    private SimpleCssParser parser;
    
    @Before
    public void setup() {
        parser = SimpleCssParser.newParser();
    }

    @Test
    public void testMultipleProperties() {
            List<CssRule> rules = parser.parse("product-row { background: #ABC123; border: " +
            		"1px black solid; border: none;background:   url(http://www.domain.com/image.jpg);}");
            
            CssRule rule = rules.get(0);
            Assert.assertEquals("product-row", rule.getSelector());

            Assert.assertEquals("background", rule.getProperties().get(0).getName());
            Assert.assertEquals("#ABC123", rule.getProperties().get(0).getValue());

            Assert.assertEquals("border", rule.getProperties().get(1).getName());
            Assert.assertEquals("1px black solid", rule.getProperties().get(1).getValue());

            Assert.assertEquals("border", rule.getProperties().get(2).getName());
            Assert.assertEquals("none", rule.getProperties().get(2).getValue());

            Assert.assertEquals("background", rule.getProperties().get(3).getName());
            Assert.assertEquals("url(http://www.domain.com/image.jpg)", rule.getProperties().get(3).getValue());
    }
    
    @Test
    public void testMultipleRulesAndComments() throws Exception {

        List<CssRule> rules = parser.parse("alpha { width: 100px/*comment1*/; /*comment2*/text-decoration: " +
        		"/*comment3*/ underlined; } epsilon/*comment4*/, zeta{ height: 34px; } ");

        Assert.assertEquals(2, rules.size());

        CssRule rule = rules.get(0);
        Assert.assertEquals("alpha", rule.getSelector());

        Assert.assertEquals("width", rule.getProperties().get(0).getName());
        Assert.assertEquals("100px", rule.getProperties().get(0).getValue());
        Assert.assertEquals("text-decoration", rule.getProperties().get(1).getName());
        Assert.assertEquals("underlined", rule.getProperties().get(1).getValue());

        rule = rules.get(1);
        Assert.assertEquals("epsilon, zeta", rule.getSelector());

        Assert.assertEquals("height", rule.getProperties().get(0).getName());
        Assert.assertEquals("34px", rule.getProperties().get(0).getValue());

    }
}
