package org.openstack.atlas.api.mgmt.resources;

import org.openstack.atlas.api.helpers.ResponseFactory;
import org.openstack.atlas.api.mgmt.resources.providers.ManagementDependencyProvider;
import org.openstack.atlas.docs.loadbalancers.api.management.v1.AccountBillings;
import org.openstack.atlas.docs.loadbalancers.api.v1.AccountBilling;
import org.openstack.atlas.util.converters.exceptions.ConverterException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.Calendar;
import java.util.Collection;

import static org.openstack.atlas.util.converters.DateTimeConverters.isoTocal;

public class AccountsResource extends ManagementDependencyProvider {

    private AccountResource accountResource;

    public void setAccountResource(AccountResource accountResource) {
        this.accountResource = accountResource;
    }

    @Path("{id:[1-9][0-9]*}")
    public AccountResource retrieveAccountResource(@PathParam("id") int id) {
        accountResource.setId(id);
        return accountResource;
    }

    @GET
    @Path("billing")
    public Response retrieveAccountBilling(@QueryParam("startTime") String startTimeString, @QueryParam("endTime") String endTimeString) {
        if (!isUserInRole("cp,ops,support,billing")) {
            return ResponseFactory.accessDenied();
        }

        if (startTimeString == null || endTimeString == null) {
            return ResponseFactory.getResponseWithStatus(Response.Status.BAD_REQUEST, "Must provide startTime and endTime query parameters");
        }

        Collection<org.openstack.atlas.service.domain.pojos.AccountBilling> dAccountBillings;
        AccountBillings rAccountBillings = new AccountBillings();
        Calendar startTime;
        Calendar endTime;

        try {
            startTime = isoTocal(startTimeString);
            endTime = isoTocal(endTimeString);

            final long timeDiff = endTime.getTimeInMillis() - startTime.getTimeInMillis();
            final int millisecondsInADay = 86400000;

            if (timeDiff > millisecondsInADay) {
                return ResponseFactory.getResponseWithStatus(Response.Status.BAD_REQUEST, "Time range cannot be greater than one day.");
            }

            if (timeDiff < 0) {
                return ResponseFactory.getResponseWithStatus(Response.Status.BAD_REQUEST, "");
            }

        } catch (ConverterException ce) {
            return ResponseFactory.getResponseWithStatus(Response.Status.BAD_REQUEST, "Date parameter(s) must follow ISO-8601 format.");
        }

        try {
            dAccountBillings = getLoadBalancerRepository().getAccountBillingForAllAccounts(startTime, endTime);
            for (org.openstack.atlas.service.domain.pojos.AccountBilling dAccountBilling : dAccountBillings) {
                rAccountBillings.getAccountBillings().add(getDozerMapper().map(dAccountBilling, AccountBilling.class));
            }
            return Response.status(200).entity(rAccountBillings).build();
        } catch (Exception ex) {
            return ResponseFactory.getErrorResponse(ex, null, null);
        }
    }
}
