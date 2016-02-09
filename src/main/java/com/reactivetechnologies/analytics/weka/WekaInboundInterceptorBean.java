/* ============================================================================
*
* FILE: WekaInterceptorBean.java
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
package com.reactivetechnologies.analytics.weka;

import java.io.Serializable;
import java.io.Writer;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.internal.ascii.rest.RestValue;
import com.hazelcast.util.StringUtil;
import com.reactivetechnologies.analytics.interceptor.AbstractInboundInterceptor;
import com.reactivetechnologies.analytics.weka.impl.TrainModel;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import com.thoughtworks.xstream.io.json.JsonWriter;

public class WekaInboundInterceptorBean extends AbstractInboundInterceptor<RestValue, TrainModel> {

  private static final Logger log = LoggerFactory.getLogger(WekaInboundInterceptorBean.class);
      
  @PostConstruct
  void created() throws Exception
  {
    if (log.isDebugEnabled()) {
      log.debug(
          "Ready to intercept for WEKA. Listening on IMAP::" + keyspace());
      log.debug("Downstream connector [" + getOutChannel() + "] ");
      XStream x = new XStream(new JsonHierarchicalStreamDriver() {
        public HierarchicalStreamWriter createWriter(Writer writer) {
          return new JsonWriter(writer, JsonWriter.DROP_ROOT_MODE);
        }
      });
      x.setMode(XStream.NO_REFERENCES);
      RestValue event = new RestValue();
      log.debug("[" + name() + "] consuming Hazelcast event of type:\n"
          + x.toXML(event));
    }
    
  }
  
  @Override
  public String keyspace() {
    return "WEKA";
  }

  
  @Override
  public String name() {
    return "Weka-Inbound";
  }
 
  @Override
  public Class<TrainModel> outType() {
    return TrainModel.class;
  }
    
  @Override
  public TrainModel intercept(Serializable key, RestValue _new,
      RestValue _old) {
    if (log.isDebugEnabled()) {
      log.debug("-- Begin message --");
      log.debug(StringUtil.bytesToString(_new.getValue()));
      log.debug("-- End message --");
    }
    log.warn("UNDER CONSTRUCTION. NO MODEL BEING PASSED");
    return new TrainModel();
  }
  @Override
  public Class<RestValue> inType() {
    return RestValue.class;
  }

  

  

  
}
