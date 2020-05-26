/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.unstoppabledomains.resolution;

import com.unstoppabledomains.exceptions.NSExceptionCode;
import com.unstoppabledomains.exceptions.NSExceptionParams;
import com.unstoppabledomains.exceptions.NamingServiceException;

public class Resolution {
   private NamingService[] services;
   private String providerUrl;

    public Resolution(String blockchainProviderUrl) {
        this.providerUrl = blockchainProviderUrl;
        this.services = this.buildServices(providerUrl, false);
    }

    public String addr(String domain, String ticker) throws NamingServiceException {
        NamingService service = this.findService(domain);
        return service.addr(domain, ticker);
    }

    public String namehash(String domain) throws NamingServiceException {
        NamingService service = this.findService(domain);
        return service.namehash(domain);
    }
    
    public String ipfsHash(String domain) throws NamingServiceException {
        NamingService service = this.findService(domain);
        return service.ipfsHash(domain);
    }

    public String email(String domain) throws NamingServiceException {
        NamingService service = this.findService(domain);
        return service.email(domain);
    }

    public String owner(String domain) throws NamingServiceException {
        NamingService service = this.findService(domain);
        return service.owner(domain);
    }

    private NamingService findService(String domain) throws NamingServiceException {
        for (NamingService service : this.services) {
            if (service.isSupported(domain)) return service;
        }
        throw new NamingServiceException(NSExceptionCode.UnsupportedDomain, new NSExceptionParams("d", domain));
    }

    private NamingService[] buildServices(String providerUrl, Boolean verbose) {
        NamingService[] services = new NamingService[1];
        services[0] = new CNS(providerUrl, verbose);
        return services;
    }
}
