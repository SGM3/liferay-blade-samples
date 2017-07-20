# User Facet Search Portlet

This application provides an example on how to utilize the new search API to
implements a search within Liferay Portal.

This app utilizes the `PortletSharedSearchContributor` to configure the criteria
of a search and `PortletSharedSearchRequest` object to preform the search.

Apply/add the following changes to the portlet in charge of handling the search
request:

    @Component(
        immediate = true,
        // ...
        service = {Portlet.class, PortletSharedSearchContributor.class}
    )
    public class UserFacetedSearchPortlet
        extends MVCPortlet implements PortletSharedSearchContributor {
            @Override
            public void contribute(
                PortletSharedSearchSettings portletSharedSearchSettings) {
                // ...

            }
    }

To perform the query, use `@Reference` to grab the `PortletSharedSearchRequest`
component from the OSGi runtime in the portlet used to display the results. This
does not necessary need to be the same portlet as the
`PortletSharedSearchContributor`:

        @Reference
        private PortletSharedSearchRequest _portletSharedSearchRequest;


This example uses a single portlet to both handle the search criteria and submit
the actual search request that fetches the results.

**NOTE**: The example was made when the new API was embedded in the `portal-search-web` module.
The modules at the current state needs to be modified to expose the used packages.