/* ============================================================================
*
* FILE: ModelJdbcRepository.java
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
package com.reactivetechnologies.platform.datagrid.store;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.repository.CrudRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

import com.reactivetechnologies.platform.analytics.core.RegressionModel;

/**
 * A very basic jdbc store for saving the ensembled output.
 * No connection pooling
 */
public class ModelJdbcRepository
    implements CrudRepository<RegressionModel, Long> {

  private static final Logger log = LoggerFactory.getLogger(ModelJdbcRepository.class);
  @Autowired
  private JdbcTemplate jdbcTemplate;
  
  @PostConstruct
  void init()
  {
    String ddl = "create table if not exists RD_MODEL_SNAPSHOT ("
        + "CREATED_TS TIMESTAMP,"
        + "MODEL MEDIUMTEXT,"
        + "GEN_ID BIGINT(20) PRIMARY KEY"
        + ")";
    
    jdbcTemplate.execute(ddl);
    log.info("Init scripts ran..");
  }
  @Override
  public <S extends RegressionModel> S save(S entity) {
    Assert.notNull(entity);
    String dml = "insert into RD_MODEL_SNAPSHOT (CREATED_TS,MODEL,GEN_ID) values (now(),?,?) ";
    log.debug("Firing insert: "+dml);
    try {
      jdbcTemplate.update(dml, entity.serializeClassifierAsJson(), entity.getLongId());
    }
    catch(DuplicateKeyException dup)
    {
      log.warn("Model already exists in database. Ignoring save failure");
    }
    catch (Exception e) {
      throw new DataAccessException("Cannot serialize ensemble classifier to disk", e) {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;
      };
    }
    return entity;
  }

  @Override
  public <S extends RegressionModel> Iterable<S> save(Iterable<S> entities) {
    // NOOP
    return null;
  }

  @Override
  public RegressionModel findOne(Long id) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean exists(Long id) {
    return false;
  }

  @Override
  public Iterable<RegressionModel> findAll() {
    // NOOP
    return null;
  }

  @Override
  public Iterable<RegressionModel> findAll(Iterable<Long> ids) {
    // NOOP
    return null;
  }

  @Override
  public long count() {
    // NOOP
    return 0;
  }

  @Override
  public void delete(Long id) {
    // TODO Auto-generated method stub

  }

  @Override
  public void delete(RegressionModel entity) {
    entity.generateId();
    delete(entity.getLongId());
  }

  @Override
  public void delete(Iterable<? extends RegressionModel> entities) {
    // NOOP

  }

  @Override
  public void deleteAll() {
    // NOOP

  }

}
