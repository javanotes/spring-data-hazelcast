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
package com.uthtechnologies.fuzon.weka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import com.uthtechnologies.fuzon.ChannelMultiplexerBean;
import com.uthtechnologies.fuzon.ChannelMultiplexerFactoryBean;
import com.uthtechnologies.fuzon.defaults.DefaultOutboundChannelBean;
import com.uthtechnologies.fuzon.message.WekaEvent;
import com.uthtechnologies.fuzon.weka.impl.IncrementalClassifierBean;

import weka.classifiers.Classifier;

@Configuration
public class ClassifierConfigurator {

  @Value("${weka.classifier}")
  private String wekaClassifier;
  
  @Value("${weka.classifier.options}")
  private String options;
  
  @Bean
  public IncrementalClassifierBean regressionBean() throws Exception
  {
    return new IncrementalClassifierBean(Classifier.forName(wekaClassifier, 
        StringUtils.delimitedListToStringArray(options, ",")), 1000);
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
  
  /**
   * Weka interceptors. Clients should submit via {@linkplain WekaEvent} with the payload
   * as a JSON string
   */
  @Bean
  public WekaInboundInterceptorBean inboundWeka()
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
