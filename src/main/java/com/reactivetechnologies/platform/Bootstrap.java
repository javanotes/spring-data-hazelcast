/* ============================================================================
*
* FILE: Bootstrap.java
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
package com.reactivetechnologies.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Bootstrap {

  @Bean
  public ChannelMultiplexerFactoryBean channelMultiplexerFactoryBean()
  {
    ChannelMultiplexerFactoryBean bean = new ChannelMultiplexerFactoryBean();
    return bean;
  }
  
  /* -----------------------------------------------------------
  //This is the main link to flow an incoming message
  //from inbound to outbound
  @Bean
  public ChannelMultiplexerBean channelMultiplexerBean() throws Exception
  {
    ChannelMultiplexerBean bean = channelMultiplexerFactoryBean().getObject();
    bean.setChannel(inbound());
    return bean;
  }
      
  //Default channel linking. Create others as necessary or override default behavior
  @Bean
  public AbstractInboundInterceptor<?, ? extends Serializable> inbound()
  {
    DefaultInboundInterceptorBean in = new DefaultInboundInterceptorBean();
    //link the outbound channel
    in.setOutChannel(outbound());
    return in;
  }
  
  //Outbound feeder configuration
  @Bean
  public AbstractOutboundChannel outbound()
  {
    DefaultOutboundChannelBean out = new DefaultOutboundChannelBean();
    //out.addFeeder(out);out.addFeeder(out);out.addFeeder(out);
    //out.setStrategy(strategy);
    return out;
  }
  ------------------------------------------------------------------ */
    
  public static void main(String[] args) {
    SpringApplication app = new SpringApplication(Bootstrap.class);
    app.run(args);
    
  }

}
