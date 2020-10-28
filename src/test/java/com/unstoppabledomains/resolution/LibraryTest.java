package com.unstoppabledomains.resolution;

import com.unstoppabledomains.TestUtils;
import com.unstoppabledomains.exceptions.NSExceptionCode;
import com.unstoppabledomains.exceptions.NamingServiceException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LibraryTest {
    private static Resolution resolution;

    @BeforeAll
    public static void init() {
        final String testingProviderUrl = System.getenv("TESTING_PROVIDER_URL");
        resolution = new Resolution(testingProviderUrl);
    }

    @Test
    public void namehash() throws NamingServiceException {
        
        String hash = resolution.namehash("crypto");
        assertEquals("0x0f4a10a4f46c288cea365fcf45cccf0e9d901b945b9829ccdb54c10dc3cb7a6f", hash);
        hash = resolution.namehash("brad.crypto");
        assertEquals("0x756e4e998dbffd803c21d23b06cd855cdc7a4b57706c95964a37e24b47c10fc9", hash);

        hash = resolution.namehash("zil");
        assertEquals("0x9915d0456b878862e822e2361da37232f626a2e47505c8795134a95d36138ed3", hash);
        hash = resolution.namehash("johnnyjumper.zil");
        assertEquals("0x08ab2ffa92966738c881a37d0d97f168d2e076d24639921762d0985ebaa62e31", hash);
    }

    @Test
    public void wrongDomainNamehash() throws Exception {
        TestUtils.checkError(() -> resolution.namehash("unupported"), NSExceptionCode.UnsupportedDomain);
    }

    @Test
    public void addr() throws Exception {
        String addr = resolution.addr("homecakes.crypto", "eth");
        assertEquals("0xe7474D07fD2FA286e7e0aa23cd107F8379085037", addr);

        addr = resolution.addr("brad.crypto", "eth");
        assertEquals("0x8aaD44321A86b170879d7A244c1e8d360c99DdA8", addr, "brad.crypto --> eth");

        addr = resolution.addr("johnnyjumper.zil", "eth");
        assertEquals("0xe7474D07fD2FA286e7e0aa23cd107F8379085037", addr, "johnnyjumper.zil --> eth");
    }


    @Test
    public void wrongDomainAddr() throws Exception {
        TestUtils.checkError(() -> resolution.addr("unregistered.crypto", "eth"), NSExceptionCode.UnregisteredDomain);
        TestUtils.checkError(() -> resolution.addr("unregistered26572654326523456.zil", "eth"), NSExceptionCode.UnregisteredDomain);
    }

    @Test
    public void UnknownCurrency() throws Exception {
        TestUtils.checkError(() -> resolution.addr("brad.crypto", "unknown"), NSExceptionCode.UnknownCurrency);
        TestUtils.checkError(() -> resolution.addr("johnnyjumper.zil", "unknown"), NSExceptionCode.UnknownCurrency);
        TestUtils.checkError(() -> resolution.addr("brad.crypto", "dodge"), NSExceptionCode.UnknownCurrency);
        TestUtils.checkError(() -> resolution.addr("johnnyjumper.zil", "dodge"), NSExceptionCode.UnknownCurrency);
    }

    @Test
    public void ipfsHash() throws NamingServiceException {
        String ipfs = resolution.ipfsHash("brad.crypto");
        assertEquals( "Qme54oEzRkgooJbCDr78vzKAWcv6DDEZqRhhDyDtzgrZP6", ipfs);
        
        ipfs = resolution.ipfsHash("johnnyjumper.zil");
        assertEquals("QmQ38zzQHVfqMoLWq2VeiMLHHYki9XktzXxLYTWXt8cydu", ipfs);
    }

    @Test
    public void emailTest() throws NamingServiceException {
        String email = resolution.email("johnnyjumper.zil");
        assertEquals("jeyhunt@gmail.com", email);
    }

    @Test
    public void ownerTest() throws NamingServiceException{
        String owner = resolution.owner("brad.crypto");
        assertEquals("0x8aad44321a86b170879d7a244c1e8d360c99dda8", owner);

        owner = resolution.owner("johnnyjumper.zil");
        assertEquals("0xcea21f5a6afc11b3a4ef82e986d63b8b050b6910", owner);
    }
    
    @Test
    public void ownerFailTest() throws Exception {
        TestUtils.checkError(() -> resolution.owner("unregistered.crypto"), NSExceptionCode.UnregisteredDomain);
    }

    @Test
    public void noIpfsHash() throws Exception {
        TestUtils.checkError(() -> resolution.ipfsHash("unregstered.crypto"), NSExceptionCode.UnspecifiedResolver);
    }

    @Test
    public void noEmailRecord() throws Exception {
        TestUtils.checkError(() -> resolution.email("brad.crypto"), NSExceptionCode.RecordNotFound);
    }

}
