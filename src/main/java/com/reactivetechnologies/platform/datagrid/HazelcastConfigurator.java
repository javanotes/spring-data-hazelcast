/* ============================================================================
*
* FILE: HazelcastConfiguration.java
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
package com.reactivetechnologies.platform.datagrid;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.keyvalue.core.KeyValueTemplate;
import org.springframework.data.repository.CrudRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.reactivetechnologies.platform.analytics.core.RegressionModel;
import com.reactivetechnologies.platform.datagrid.core.HazelcastClusterServiceFactoryBean;
import com.reactivetechnologies.platform.datagrid.store.ModelJdbcRepository;

@Configuration
public class HazelcastConfigurator {

  @Value("${keyval.hazelcast.cfg: }")
  private String configXml;
  @Value("${keyval.entity.base:com.uthtechnologies.springdata.hz}")
  private String entityPkg;
  
  @Bean
  public static PropertySourcesPlaceholderConfigurer propertyConfigIn() 
  {
    return new PropertySourcesPlaceholderConfigurer();
  }
  @Bean
  public HazelcastClusterServiceFactoryBean hzServiceFactoryBean()
  {
    HazelcastClusterServiceFactoryBean bean = new HazelcastClusterServiceFactoryBean();
    bean.setConfigXml(configXml);
    bean.setEntityBasePkg(entityPkg);
    return bean;
    
  }
  
  @Bean
  @Primary
  public HazelcastKeyValueAdapterBean hzKeyValueAdaptor() throws Exception
  {
    HazelcastKeyValueAdapterBean bean = new HazelcastKeyValueAdapterBean(hzServiceFactoryBean().getObject());
    return bean;
    
  }
  @Bean
  @Qualifier("hzKeyValueAdaptorJoinImmediate")
  public HazelcastKeyValueAdapterBean hzKeyValueAdaptorJoinImmediate() throws Exception
  {
    HazelcastKeyValueAdapterBean bean = new HazelcastKeyValueAdapterBean(true, hzServiceFactoryBean().getObject());
    return bean;
    
  }
  @Bean
  @Primary
  public KeyValueTemplate kvtemplate() throws Exception
  {
    KeyValueTemplate kv = new KeyValueTemplate(hzKeyValueAdaptor());
    return kv;
    
  }
  @Bean
  @Qualifier("kvTemplateJoinImmediate")
  public KeyValueTemplate kvtemplateAndJoin() throws Exception
  {
    KeyValueTemplate kv = new KeyValueTemplate(hzKeyValueAdaptorJoinImmediate());
    return kv;
    
  }
  @ConfigurationProperties(prefix = "spring.datasource")
  @Bean
  public DataSource unpooled()
  {
    return new DriverManagerDataSource();
  }
  @Bean
  public JdbcTemplate jdbcTemplateUnpooled()
  {
    JdbcTemplate tmpl = new JdbcTemplate(unpooled());
    return tmpl;
  }
  @Bean
  public CrudRepository<RegressionModel, Long> repository()
  {
    return new ModelJdbcRepository();
  }
}
