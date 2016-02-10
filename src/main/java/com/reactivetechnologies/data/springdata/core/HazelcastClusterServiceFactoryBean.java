/* ============================================================================
*
* FILE: HazelcastClusterServiceFactoryBean.java
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
package com.reactivetechnologies.data.springdata.core;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.util.StringUtils;

public class HazelcastClusterServiceFactoryBean
    implements FactoryBean<HazelcastClusterServiceBean> {

  public HazelcastClusterServiceFactoryBean()
  {
    
  }
  private String entityBasePkg;
  public String getEntityBasePkg() {
    return entityBasePkg;
  }
  public void setEntityBasePkg(String entityBasePkg) {
    this.entityBasePkg = entityBasePkg;
  }
  public String getConfigXml() {
    return configXml;
  }
  public void setConfigXml(String configXml) {
    this.configXml = configXml;
  }

  private String configXml;

  @PostConstruct
  private void setUp()
  {
    if(StringUtils.isEmpty(entityBasePkg))
      throw new BeanCreationException("'entityBasePkg' not specified in factory bean");
  }
  @Override
  public HazelcastClusterServiceBean getObject() throws Exception {
    HazelcastClusterServiceBean bean = new HazelcastClusterServiceBean(configXml, entityBasePkg);
    return bean;
  }

  @Override
  public Class<?> getObjectType() {
    return HazelcastClusterServiceBean.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

}
