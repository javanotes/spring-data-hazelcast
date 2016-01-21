/* ============================================================================
*
* FILE: PartitionMigrationCallback.java
*
* MODULE DESCRIPTION:
* See class description
*
* Copyright (C) 2015 
*
* All rights reserved
*
* ============================================================================
*/
package org.springframework.data.hz;

import java.io.Serializable;

import com.hazelcast.map.EntryProcessor;
/**
 * Partition migration callback on all entries of a given map
 *
 * @param <V>
 */
public interface PartitionMigrationCallback<V> extends EntryProcessor<Serializable, V>{

  /**
   * Gets the Map for which migrated elements will have a callback
   * @return
   */
  String keyspace();
  
}
