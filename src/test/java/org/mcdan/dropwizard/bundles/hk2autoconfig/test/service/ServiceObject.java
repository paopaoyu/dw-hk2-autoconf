package org.mcdan.dropwizard.bundles.hk2autoconfig.test.service;

import javax.inject.Inject;

import org.jvnet.hk2.annotations.Service;

@Service
public class ServiceObject implements ContractInterface {

	@Inject
	private ServiceConfig srvConf;
	
    @Override
    public String getIt() {
        // TODO Auto-generated method stub
        return srvConf.getItValue();
    }

}
