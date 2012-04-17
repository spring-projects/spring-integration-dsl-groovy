package org.springframework.integration.dsl.groovy

import org.springframework.integration.Message
import org.springframework.integration.handler.MessageProcessor
import org.springframework.integration.support.MessageBuilder

class ClosureInvoker {
	private final closure

	ClosureInvoker(Closure closure){
		this.closure = closure.clone()
	}

	public Object invoke(Object... args) {
		closure.call(args)
	}

	public Object typeSafeInvoke(Object... args) {
		closure.doCall(args)
	}
}

class ClosureInvokingTransformer implements org.springframework.integration.transformer.Transformer {
	private final ClosureInvokingMessageProcessor<Object> messageProcessor
	 

	ClosureInvokingTransformer(Closure closure){
		this.messageProcessor = new ClosureInvokingMessageProcessor<Object>(closure)
		 
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.transformer.Transformer#transform(org.springframework.integration.Message)
	 */
	public Message<?> transform(Message<?> message) {
		Object result = this.messageProcessor.processMessage(message)
		return (result==null) ? null:
		MessageBuilder.withPayload(result).copyHeaders(message.headers).build()
	}
}

/**
 * 
 * @author David Turanski
 *
 * @param <T>
 */

class ClosureInvokingMessageProcessor<T> implements MessageProcessor<T> {
	private Class parameterType
	private Closure closure

	ClosureInvokingMessageProcessor(Closure closure){
		assert closure.getParameterTypes().size() == 1, 'Closure must specify exactly one parameter'
		this.closure = closure.clone()
		this.parameterType = closure.getParameterTypes()[0]
	}
	/* (non-Javadoc)
	 * @see org.springframework.integration.handler.MessageProcessor#processMessage(org.springframework.integration.Message)
	 */
	public T processMessage(Message message) {
		Object result = null
		if (parameterType == Message) {
			result =  this.closure.doCall(message)
		}

		else if (parameterType == Map && !(message.payload instanceof Map)) {
			result =  this.closure.doCall(message.headers)
		}

		else {
			result =  this.closure.doCall(message.payload)
		}
		
		assert !result || T.class.isAssignableFrom(result.class), "expected closure to yield a result of type ${T.class}"
		
		result
		
	}
}
