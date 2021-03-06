/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.log;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.impl.PropertyPlaceholderDelegateRegistry;
import org.apache.camel.spi.ExchangeFormatter;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.junit.Before;
import org.junit.Test;

/**
 * Custom Exchange Formatter test.
 */
public class LogCustomFormatterTest extends ContextTestSupport {

    private TestExchangeFormatter exchangeFormatter;
    
    @Before @Override
    public void setUp() throws Exception {
        super.setUp();
        // we add an appender explicitly to avoid getting a NOPLogger which permit logging; 
        // otherwise the ExchangeFormatter wouldn't get called
        Logger.getLogger(LogCustomFormatterTest.class).removeAllAppenders();
        Logger.getLogger(LogCustomFormatterTest.class).addAppender(new ConsoleAppender(new SimpleLayout()));
        Logger.getLogger(LogCustomFormatterTest.class).setLevel(Level.TRACE);
    }
    
    @Test
    public void testCustomFormatterInComponent() throws Exception {
        context.stop();
        
        LogComponent log = new LogComponent();
        exchangeFormatter = new TestExchangeFormatter();
        log.setExchangeFormatter(exchangeFormatter);
        context.addComponent("log", log);
        
        context.start();
        
        String endpointUri = "log:" + LogCustomFormatterTest.class.getCanonicalName();
        template.requestBody(endpointUri, "Hello World");
        template.requestBody(endpointUri, "Hello World");
        template.requestBody(endpointUri + "2", "Hello World");
        template.requestBody(endpointUri + "2", "Hello World");
        
        assertEquals(4, exchangeFormatter.getCounter());
    }
    
    @Test
    public void testCustomFormatterInRegistry() throws Exception {
        context.stop();
        
        exchangeFormatter = new TestExchangeFormatter();
        JndiRegistry registry = getRegistryAsJndi();
        registry.bind("logFormatter", exchangeFormatter);
        
        context.start();
        
        String endpointUri = "log:" + LogCustomFormatterTest.class.getCanonicalName();
        template.requestBody(endpointUri, "Hello World");
        template.requestBody(endpointUri, "Hello World");
        template.requestBody(endpointUri + "2", "Hello World");
        template.requestBody(endpointUri + "2", "Hello World");
        
        assertEquals(4, exchangeFormatter.getCounter());
    }

    @Test
    public void testFormatterNotPickedUpWithDifferentKey() throws Exception {
        context.stop();
        
        exchangeFormatter = new TestExchangeFormatter();
        JndiRegistry registry = getRegistryAsJndi();
        registry.bind("anotherFormatter", exchangeFormatter);
        
        context.start();
        
        String endpointUri = "log:" + LogCustomFormatterTest.class.getCanonicalName();
        template.requestBody(endpointUri, "Hello World");
        template.requestBody(endpointUri, "Hello World");
        template.requestBody(endpointUri + "2", "Hello World");
        template.requestBody(endpointUri + "2", "Hello World");
        
        assertEquals(0, exchangeFormatter.getCounter());
    }
    
    private JndiRegistry getRegistryAsJndi() {
        JndiRegistry registry = null;
        if (context.getRegistry() instanceof PropertyPlaceholderDelegateRegistry) {
            registry = (JndiRegistry) ((PropertyPlaceholderDelegateRegistry) context.getRegistry()).getRegistry();
        } else if (context.getRegistry() instanceof JndiRegistry) {
            registry = (JndiRegistry) context.getRegistry();
        } else {
            fail("Could not determine Registry type");
        }
        return registry;
    }
    
    public static class TestExchangeFormatter implements ExchangeFormatter {
        private int counter;
        private boolean addTen;
        
        @Override
        public String format(Exchange exchange) {
            counter += addTen ? 10 : 1;
            return exchange.toString();
        }
        
        public int getCounter() {
            return counter;
        }

        public boolean isAddTen() {
            return addTen;
        }

        public void setAddTen(boolean addTen) {
            this.addTen = addTen;
        }
        
    }
    
}
