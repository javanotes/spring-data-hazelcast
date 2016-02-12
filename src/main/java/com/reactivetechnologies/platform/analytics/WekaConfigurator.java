/* ============================================================================
*
* FILE: ClassifierConfigurator.java
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
package com.reactivetechnologies.platform.analytics;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.reactivetechnologies.platform.ChannelMultiplexerBean;
import com.reactivetechnologies.platform.ChannelMultiplexerFactoryBean;
import com.reactivetechnologies.platform.analytics.core.IncrementalClassifierBean;
import com.reactivetechnologies.platform.analytics.mapper.DataMapperFactoryBean;
import com.reactivetechnologies.platform.defaults.DefaultOutboundChannelBean;
import com.reactivetechnologies.platform.interceptor.AbstractInboundInterceptor;
import com.reactivetechnologies.platform.message.Event;

import weka.classifiers.Classifier;
import weka.core.Utils;

@Configuration
public class WekaConfigurator {

  @Value("${weka.classifier}")
  private String wekaClassifier;
  
  @Value("${weka.classifier.options}")
  private String options;
  
  @Bean
  public IncrementalClassifierBean regressionBean() throws Exception
  {
    Classifier c = Classifier.forName(wekaClassifier, null);
    c.setOptions(Utils.splitOptions(options));
    return new IncrementalClassifierBean(c, 1000);
  }
    
  @Autowired
  ChannelMultiplexerFactoryBean muxFactory;
  
  
  @Bean
  public ChannelMultiplexerBean wekaChannelMultiplexerBean() throws Exception
  {
    ChannelMultiplexerBean bean = muxFactory.getObject();
    bean.setChannel(inboundWeka());
    return bean;
  }
    
  @Bean
  public DataMapperFactoryBean mapperFactoryBean()
  {
    return new DataMapperFactoryBean();
  }
  
  /**
   * Weka interceptors. Clients should submit via an {@linkplain Event} wrapping the payload JSON
   * 
   */
  @Bean
  public AbstractInboundInterceptor<?, ? extends Serializable> inboundWeka()
  {
    WekaInboundInterceptorBean in = new WekaInboundInterceptorBean();
    //link the outbound channel
    in.setOutChannel(outboundWeka());
    return in;
  }
  @Bean
  public DefaultOutboundChannelBean outboundWeka()
  {
    DefaultOutboundChannelBean out = new DefaultOutboundChannelBean();
    out.addFeeder(outboundWekaInterceptor());
    return out;
  }
  @Bean
  public WekaOutboundInterceptorBean outboundWekaInterceptor()
  {
    return new WekaOutboundInterceptorBean();
  }
}
