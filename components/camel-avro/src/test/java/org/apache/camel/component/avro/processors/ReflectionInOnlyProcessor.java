package org.apache.camel.component.avro.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.avro.test.TestReflection;

public class ReflectionInOnlyProcessor implements Processor {

	private TestReflection testReflection;

	public ReflectionInOnlyProcessor(TestReflection testReflection) {
		this.testReflection = testReflection; 
	}
	
	@Override
	public void process(Exchange exchange) throws Exception {
        Object body = exchange.getIn().getBody();
        if(body instanceof String) {
        	testReflection.setName(String.valueOf(body));
        }
    }
	
	public TestReflection getTestReflection() {
		return testReflection;
	}

	public void setTestReflection(TestReflection testReflection) {
		this.testReflection = testReflection;
	}

}
