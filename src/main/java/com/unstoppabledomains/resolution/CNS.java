
package com.unstoppabledomains.resolution;

import com.unstoppabledomains.exceptions.ContractCallException;
import com.unstoppabledomains.exceptions.NSExceptionCode;
import com.unstoppabledomains.exceptions.NSExceptionParams;
import com.unstoppabledomains.exceptions.NamingServiceException;
import com.unstoppabledomains.resolution.contracts.cns.ProxyReader;
import com.unstoppabledomains.util.Utilities;

import java.math.BigInteger;
import java.net.UnknownHostException;

public class CNS implements NamingService {

  private static final String PROXY_READER_ADDRESS = "0x7ea9Ee21077F84339eDa9C80048ec6db678642B1";

  private final ProxyReader proxyReaderContract;

  public CNS(String blockchainProviderUrl) {
    this.proxyReaderContract = new ProxyReader(blockchainProviderUrl, PROXY_READER_ADDRESS);
  }

  public Boolean isSupported(String domain) {
    String[] split = domain.split("\\.");
    return (split.length != 0 && split[split.length - 1].equals("crypto"));
  }

  public String getAddress(String domain, String ticker) throws NamingServiceException {
    String owner = getOwner(domain);
    if (Utilities.isEmptyResponse(owner))
      throw new NamingServiceException(NSExceptionCode.UnregisteredDomain,
          new NSExceptionParams("d|c|n", domain, ticker, "CNS"));
    String key = "crypto." + ticker.toUpperCase() + ".address";
    String address = resolveKey(key, domain);
    if (Utilities.isEmptyResponse(address))
      throw new NamingServiceException(NSExceptionCode.UnknownCurrency,
          new NSExceptionParams("d|c|n", domain, ticker, "CNS"));
    return address;
  }

  public  String getIpfsHash(String domain) throws NamingServiceException {
    String key = "ipfs.html.value";
    String hash = resolveKey(key, domain);

    if (hash == null) {
      throw new NamingServiceException(NSExceptionCode.UnspecifiedResolver,
              new NSExceptionParams("d|r", domain, key));
    }
    if ("".equals(hash)) {
      throw new NamingServiceException(NSExceptionCode.RecordNotFound,
              new NSExceptionParams("d|r", domain, key));
    }
    return hash;
  }

  public  String getEmail(String domain) throws NamingServiceException {
    String key = "whois.getEmail.value";
    String email = resolveKey(key, domain);
    if (Utilities.isEmptyResponse(email))
      throw new NamingServiceException(NSExceptionCode.RecordNotFound,
          new NSExceptionParams("d|r", domain, key));
    return email;
  }

  public  String getOwner(String domain) throws NamingServiceException {
    try {
      BigInteger tokenID = tokenID(domain);
      String owner = owner(tokenID);
      if (Utilities.isEmptyResponse(owner)) {
        throw new NamingServiceException(NSExceptionCode.UnregisteredDomain, new NSExceptionParams("d|n", domain, "CNS"));
      }
      return owner;
    } catch (Exception e) {
      throw configureNamingServiceException(e,
          new NSExceptionParams("d|n", domain, "CNS"));
    }
  }

  protected  String resolveKey(String key, String domain) throws NamingServiceException {
    try {
      BigInteger tokenID = tokenID(domain);
      return resolveKey(key, tokenID);
    } catch (Exception e) {
      throw configureNamingServiceException(e,
          new NSExceptionParams("d|n", domain, "CNS"));
    }
  }

  private NamingServiceException configureNamingServiceException(Exception e, NSExceptionParams params) {
    if (e instanceof NamingServiceException) {
      return (NamingServiceException) e;
    }
    if (e instanceof UnknownHostException) {
      return new NamingServiceException(NSExceptionCode.BlockchainIsDown, params, e);
    } else if (e instanceof ContractCallException) {
      return new NamingServiceException(NSExceptionCode.RecordNotFound, params, e);
    }
    return new NamingServiceException(NSExceptionCode.UnknownError, params, e);
  }

  private String resolveKey(String key, BigInteger tokenID) throws Exception {
    return proxyReaderContract.getRecord(key, tokenID);
  }

  private String owner(BigInteger tokenID) throws NamingServiceException {
    String owner = proxyReaderContract.getOwner(tokenID);
    if (Utilities.isEmptyResponse(owner)) {
      throw new NamingServiceException(NSExceptionCode.UnregisteredDomain);
    }
    return owner;
  }

  private BigInteger tokenID(String domain) throws NamingServiceException {
    String hash = getNamehash(domain);
    return new BigInteger(hash.substring(2), 16);
  }

  @Override
  public String getNamehash(String domain) throws NamingServiceException {
    return Namehash.nameHash(domain);
  }
}
