package org.springframework.data.hz.core;
/* ============================================================================
*
* FILE: HzInstanceProxy.java
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
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.ConfigurationException;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICondition;
import com.hazelcast.core.ILock;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.LifecycleListener;
import com.hazelcast.core.Member;
import com.hazelcast.core.MembershipListener;
import com.hazelcast.core.MigrationListener;
import com.hazelcast.map.listener.MapListener;

/**
 * Class to proxy a Hazelcast cluster member. <p><b>Note:</b>
 * If multicast communication doesn't work in your env, try to add this rule to iptables:<p>
 * <i>iptables -A INPUT -m pkttype --pkt-type multicast -j ACCEPT</i>
 * @author esutdal
 *
 */
class HzInstanceProxy {
	
	/*
	 * The broadcast address for an IPv4 host can be obtained by performing a bitwise OR operation 
	 * between the bit complement of the subnet mask and the host's IP address.

	   Example: For broadcasting a packet to an entire IPv4 subnet using the private IP address space 172.16.0.0/12, 
	   which has the subnet mask 255.240.0.0, the broadcast address is 172.16.0.0 | 0.15.255.255 = 172.31.255.255.
	 */
		
	ILock getLock(String name)
	{
		if(isRunning())
		{
			return hazelcast.getLock(name);
		}
		return null;
	}
	
	private HazelcastInstance hazelcast = null;
	<T> IQueue<T> getQueue(String name)
	{
		return hazelcast.getQueue(name);
	}
		
		
	public int noOfMembers(){
		if(isRunning()){
			return hazelcast.getCluster().getMembers().size();
		}
		return 0;
	}
	
	/**
	 * If the particular instance is running an active hazelcast service
	 * @return
	 */
	private boolean isRunning(){
		try {
			return hazelcast != null && hazelcast.getLifecycleService() != null && hazelcast.getLifecycleService().isRunning();
		} catch (Exception e) {
			//log.warn(e);
		}
		return false;
	}
	private ILock clusterSyncLock;	
	/**
	 * 
	 * @throws FileNotFoundException
	 * @throws ConfigurationException 
	 */
	private HzInstanceProxy(Config hzConfig) {
		
    String memberName = System.getProperty("datagrid.instance.id", "datagrid.instance.id");
    
    hzConfig.getMemberAttributeConfig().setStringAttribute("datagrid.instance.id", memberName);
    
    hazelcast = Hazelcast.newHazelcastInstance(hzConfig);
    Set<Member> members = hazelcast.getCluster().getMembers();
    
    int memberIdCnt = 0;
    for(Member m : members)
    {
      
    	if(m.getStringAttribute("datagrid.instance.id").equals(memberName))
    	{
    		memberIdCnt++;
    	}
    	if(memberIdCnt >= 2)
    		throw new ExceptionInInitializerError("Instance ["+memberName+"] already present in cluster");
    }
				
	}
	public HzInstanceProxy()
	{
	  this(new Config());
	}
	public HzInstanceProxy(String configFile)
	{
	  this(new ClasspathXmlConfig(configFile));
	}
	/**
	 * Adds a migration listener
	 * @param listener
	 */
	public void addMigrationListener(MigrationListener listener)
	{
		hazelcast.getPartitionService().addMigrationListener(listener);
	}
	/**
	 * Adds a local map entry listener
	 * @param map
	 * @param el
	 */
	public void addLocalEntryListener(String map, MapListener el)
	{
		if(localEntryListeners.containsKey(el.toString()))
		{
			hazelcast.getMap(map).removeEntryListener(localEntryListeners.get(el.toString()));
		}
		String _id = hazelcast.getMap(map).addLocalEntryListener(el);
		localEntryListeners.put(el.toString(), _id);
	}
	
	private final Map<String, String> localEntryListeners = new HashMap<String, String>();
	/**
	 * Adds a life cycle listener
	 * @param listener
	 */
	public void addLifeCycleListener(LifecycleListener listener)
	{
		hazelcast.getLifecycleService().addLifecycleListener(listener);
	}
	//private PartitionService partitionService = null;
	/**
	 * Initialize with a member event listener, and broadcast message listener			
	 * @param clusterListener
	 * @param msgListener
	 */
	public void init(final MembershipListener clusterListener){
			
		hazelcast.getCluster().addMembershipListener(clusterListener);
		//hazelcast.getTopic(Constants.INSTANCE_SHUTDOWN_Topic).addMessageListener(msgListener);
		//hazelcast.getMap(DATAGRID).addLocalEntryListener(localMapListener);
						
	}
	
			
	public void stop(String message){
		if(hazelcast != null){
			hazelcast.getLifecycleService().shutdown();
		}
		
		
	}
	
	public void remove(Object key, String map)
	{
		if(isRunning()){
			hazelcast.getMap(map).removeAsync(key);
		}
	}
	public Object removeNow(Object key, String map)
  {
    if(isRunning()){
      return hazelcast.getMap(map).remove(key);
    }
    return map;
  }
	
	public <K, V> IMap<K, V> getMap(String map)
	{
		if(isRunning()){
			return hazelcast.getMap(map);
		}
		return null;
	}
	public Object put(Object key, Object value, String map) {
		if(isRunning()){
			return hazelcast.getMap(map).put(key, value);
		}
    return null;
		
	}
	public void set(Object key, Object value, String map) {
    if(isRunning()){
      hazelcast.getMap(map).set(key, value);
    }
    
  }
	public Object get(Object key, String map) {
		if(isRunning())
		{
			return hazelcast.getMap(map).get(key);
		}
		return null;
		
	}
	Set<Entry<Object, Object>> getAll(String map)
	{
		if(isRunning())
		{
			return hazelcast.getMap(map).entrySet();
		}
		return null;
	}
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	public int getPartitionIDForKey(Object key)
	{
		return hazelcast.getPartitionService().getPartition(key).getPartitionId();
	}
	Set<Object> getLocalKeys(String map)
	{
		if(isRunning())
		{
			IMap<Object, Object> imap = hazelcast.getMap(map);
			return imap.localKeySet();
			
		}
		return null;
	}

	private ICondition syncLockCondition;
	public ILock getClusterSyncLock() {
		return clusterSyncLock;
	}


	public ICondition getSyncLockCondition() {
		return syncLockCondition;
	}
}
