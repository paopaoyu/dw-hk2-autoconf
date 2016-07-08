/**
 * 
 */
package org.mcdan.dropwizard.bundles.hk2autoconfig;

import javax.annotation.PostConstruct;

import org.mcdan.dropwizard.bundles.hk2autoconfig.test.service.ServiceConfig;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;

/**
 * @author Administrator
 *
 */
public class ServiceConfiguration extends Configuration {

	
	@JsonProperty
	private ServiceConfig serviceConfig;
	
	@PostConstruct
	private void init(){
		System.out.println("serviceConfig value:"+serviceConfig.getItValue());
	}
	

	public void setServiceConfig(ServiceConfig serviceConfig) {
		this.serviceConfig = serviceConfig;
	}
	
	
	
}
