/* ============================================================================
*
* FILE: AbstractIncomingInterceptor.java
*
The MIT License (MIT)

Copyright (c) 2016 Sutanu Dalui

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*
* ============================================================================
*/
package com.uthtechnologies.fuzon.interceptor;

import java.io.Serializable;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.hazelcast.core.EntryEvent;
import com.uthtechnologies.fuzon.message.Event;
import com.uthtechnologies.fuzon.springdata.handlers.LocalPutMapEntryCallback;
/**
 * 
 *
 * @param <V> intercepted message type
 * @param <T> transformed message type
 */
@Component
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public abstract class AbstractInboundInterceptor<V extends Serializable, T extends Serializable>
    implements LocalPutMapEntryCallback<Event<V>>, InboundInterceptor<V, T> {

  private static final Logger log = LoggerFactory.getLogger(AbstractInboundInterceptor.class);
  
  protected AbstractOutboundChannel outChannel;
  
  public AbstractOutboundChannel getOutChannel() {
    return outChannel;
  }

  public void setOutChannel(AbstractOutboundChannel outChannel) {
    this.outChannel = outChannel;
  }

  @PostConstruct
  protected void init()
  {
    log.info("-- New Inbound channel created "+this+", linked to outbound channel "+outChannel);
  }
  
  @Override
  public void entryAdded(EntryEvent<Serializable, Event<V>> event) {
    Serializable t = intercept(event.getKey(), event.getValue().getPayload(), event.getOldValue().getPayload());
    outChannel.feed(t);
    
  }

  @Override
  public void entryUpdated(EntryEvent<Serializable, Event<V>> event) {
    Serializable t = intercept(event.getKey(), event.getValue().getPayload(), event.getOldValue().getPayload());
    outChannel.feed(t);
    
  }

}
