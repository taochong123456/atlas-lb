package org.openstack.atlas.adapter.itest;


import com.zxtm.service.client.VirtualServerProtocol;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openstack.atlas.adapter.exceptions.InsufficientRequestException;
import org.openstack.atlas.adapter.exceptions.RollBackException;
import org.openstack.atlas.adapter.helpers.ZxtmNameBuilder;
import org.openstack.atlas.service.domain.entities.LoadBalancer;
import org.openstack.atlas.service.domain.entities.LoadBalancerProtocol;
import org.rackspace.stingray.client.StingrayRestClient;

public class UpdateProtocolITest extends STMTestBase{


    @BeforeClass
    public static void setupClass() throws InterruptedException {
       Thread.sleep(SLEEP_TIME_BETWEEN_TESTS);
        setupIvars();
        createSimpleLoadBalancer();
    }

    @AfterClass
    public static void tearDownClass()
    {
        try {
            stmAdapter.deleteLoadBalancer(config, lb);
        } catch (InsufficientRequestException e) {
            e.printStackTrace();
        } catch (RollBackException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void updateProtocolToAndFromHTTP() {
        try{
            String vsName;
            vsName = ZxtmNameBuilder.genVSName(lb);
            StingrayRestClient client = new StingrayRestClient();
            Assert.assertEquals(VirtualServerProtocol.http.toString().toUpperCase(), client.getVirtualServer(vsName).getProperties().getBasic().getProtocol());
            lb.setProtocol(LoadBalancerProtocol.HTTPS);
            stmAdapter.updateLoadBalancer(config, lb, new LoadBalancer());
            boolean isConnectionLogging = true;
            lb.setConnectionLogging(isConnectionLogging);
            Assert.assertEquals(LoadBalancerProtocol.HTTPS.toString().toUpperCase(),client.getVirtualServer(vsName).getProperties().getBasic().getProtocol());
            Assert.assertNotNull(client.getVirtualServer(vsName).getProperties().getLog());
            lb.setProtocol(LoadBalancerProtocol.HTTP);
            stmAdapter.updateLoadBalancer(config, lb, new LoadBalancer());
            Assert.assertEquals(LoadBalancerProtocol.HTTP.toString().toUpperCase(), client.getVirtualServer(vsName).getProperties().getBasic().getProtocol());
            Assert.assertTrue(client.getVirtualServer(vsName).getProperties().getLog().getEnabled());

        }catch(Exception e){}

    }






}
