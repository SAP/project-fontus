package com.sap.fontus.gdpr.broadleaf;

import com.sap.fontus.asm.FunctionCall;
import com.sap.fontus.config.Configuration;
import com.sap.fontus.config.Sink;
import com.sap.fontus.config.Source;
import com.sap.fontus.gdpr.cookie.ConsentCookie;
import com.sap.fontus.gdpr.cookie.ConsentCookieMetadata;
import com.sap.fontus.gdpr.metadata.*;
import com.sap.fontus.gdpr.Utils;
import com.sap.fontus.gdpr.metadata.simple.*;
import com.sap.fontus.gdpr.openmrs.OpenMrsTaintHandler;
import com.sap.fontus.gdpr.servlet.ReflectedCookie;
import com.sap.fontus.gdpr.servlet.ReflectedHttpServletRequest;
import com.sap.fontus.gdpr.servlet.ReflectedSession;
import com.sap.fontus.taintaware.IASTaintAware;
import com.sap.fontus.taintaware.shared.*;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.IASTaintHandler;
import com.sap.fontus.taintaware.unified.IASTaintInformationable;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;


//TODO delete
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BroadleafTaintHandler extends IASTaintHandler {

    private static final String productPurposeAttributeName = BroadleafTaintHandler.class.getName() + ".PRODUCTPURPOSE";
    private static final String addressServiceAttributeName = BroadleafTaintHandler.class.getName() + ".ADDRESSSERVICE";

    /**
     * Extracts the TCF consent string from a cookie and attaches it as the taint metadata
     * @param taintAware The Taint Aware String-like object
     * @param parent The object on which this method is being called
     * @param parameters The parameters used to make the method call
     * @param sourceId The ID of the source function (internal)
     * @return A possibly tainted version of the input object
     */
    private static IASTaintAware setTaint(IASTaintAware taintAware, Object parent, Object[] parameters, int sourceId) {
        // General debug info
        IASTaintHandler.printObjectInfo(taintAware, parent, parameters, sourceId);
        IASTaintSource taintSource = IASTaintSourceRegistry.getInstance().get(sourceId);
        Source source = null;
        GdprMetadata metadata = null;

        if (taintSource != null) {
            source = Configuration.getConfiguration().getSourceConfig().getSourceWithName(taintSource.getName());
            System.out.println("source from config: " + source);
        }

        if ((parent != null) && (source != null)) {

            // check if CONTEXT exists, most stuff not working without!

            try {

                ReflectedHttpServletRequest request = new ReflectedHttpServletRequest(parent);
                IASString uri = request.getRequestURI();
                String path = uri.getString();
                Collection<AllowedPurpose> purposeses = Utils.getPurposesFromRequest(request);
                System.out.println(purposeses);

                ReflectedSession session = request.getSession();
                Object springSecurityContext = session.getAttribute(new IASString("SPRING_SECURITY_CONTEXT"));

                Object obj100 = request.getAttribute(new IASString("org.springframework.web.servlet.DispatcherServlet.CONTEXT"));
                if (obj100 != null) {
                    Method m100 = obj100.getClass().getMethod("getBean", IASString.class);

                    try {
                        Object bean = m100.invoke(obj100, new IASString("blCustomerAddressService"));
                        session.setAttribute(addressServiceAttributeName, bean);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                //Admin part
                if (path.contains("/admin/")) {

                    //Authenticed admin
                    if (springSecurityContext != null) {
                        long id = getIdFromSecurityContext(springSecurityContext);

                        //TODO what is the data subject? Admin can register new admins or create new customers!
                        //Call customer service?
                        Object obj = request.getAttribute(new IASString("org.springframework.web.servlet.DispatcherServlet.CONTEXT"));
                        Method m = obj.getClass().getMethod("getBean", IASString.class);

                        try {
                            Object bean = m.invoke(obj, new IASString("blCustomerService"));
                            Method m2 = bean.getClass().getMethod("readCustomerById", Long.class);
                            m2.invoke(bean, 200);
                        } catch (Exception ex2) {
                            ex2.printStackTrace();
                        }
                    //Not authenticated admin
                    } else {
                    }


                //API part
                } else if (path.contains("/api/")) {
                    //TODO purpose check for 3rd party code
                    System.out.println("Path is api");

                //Customer case
                } else {
                    //Authenticated customer
                    if (springSecurityContext != null) {
                        long id = getIdFromSecurityContext(springSecurityContext);
                        DataSubject ds = new SimpleDataSubject(String.valueOf(id));
                        metadata = new SimpleGdprMetadata(Utils.getPurposesFromRequest(request), ProtectionLevel.Normal, ds, new SimpleDataId(), true, true, Identifiability.NotExplicit);
                    //Not authenticated customer
                    } else {
                        Object anonymousCustomer = session.getAttribute(new IASString("_blc_anonymousCustomer"));
                        Object customerMerged = session.getAttribute(new IASString("_blc_anonymousCustomerMerged"));
                        if (anonymousCustomer != null) {
                            Method getId = anonymousCustomer.getClass().getMethod("getId");
                            long id = (long) getId.invoke(anonymousCustomer);
                            if (path.equals("/register")) {
                                DataSubject ds = new SimpleDataSubject(String.valueOf(id));
                                metadata = new SimpleGdprMetadata(Utils.getPurposesFromRequest(request), ProtectionLevel.Normal, ds, new SimpleDataId(), true, true, Identifiability.NotExplicit);
                            }
                        } else if (customerMerged != null) {
                            //TODO stuff with merged customer?
                        }
                    }

                    if (path.contains("/checkout")) {
                        ProductPurpose productPurpose = null;
                        Object o = session.getAttribute(productPurposeAttributeName);
                        long id = getIdFromSecurityContext(springSecurityContext);

                        if ((o != null) && (o instanceof ProductPurpose)) {
                            getTaintMetadataFromExistingUser(request, id, (ProductPurpose) o);
                            productPurpose = (ProductPurpose) o;
                            if (metadata != null) {
                                for (AllowedPurpose p : productPurpose.getPurposes()) {
                                    metadata.getAllowedPurposes().add(p);
                                }
                            }
                        }
                    }

                    if (path.contains("/cart/add")) {
                        long productId = 10;
                        IASString[] stringId;
                        try {
                            stringId = request.getParameterValues(new IASString("productId"));
                            productId = Long.valueOf(stringId[0].toString());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        IASString[] quantity;
                        try {
                            quantity = request.getParameterValues(new IASString("quantity"));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        Object obj = request.getAttribute(new IASString("org.springframework.web.servlet.DispatcherServlet.CONTEXT"));
                        Method m = obj.getClass().getMethod("getBean", IASString.class);

                        try {
                            Object bean = m.invoke(obj, new IASString("blCatalogService"));
                            Method m2 = bean.getClass().getMethod("findProductById", Long.class);
                            Object product = m2.invoke(bean, productId);

                            SimpleAllowedPurpose p = processProductPurpose(product);
                            if (metadata != null) {
                                metadata.getAllowedPurposes().add(p);
                            }

                            ProductPurpose productPurpose = null;
                            Object o = session.getAttribute(productPurposeAttributeName);
                            if ((o != null) && (o instanceof ProductPurpose)) {
                                productPurpose = (ProductPurpose) o;
                            } else {
                                productPurpose = ProductPurpose.getInstance();
                                session.setAttribute(productPurposeAttributeName, productPurpose);
                            }
                            productPurpose.addPurpose(p);

                        } catch (Exception ex2) {
                            ex2.printStackTrace();
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            // Add taint information if match was found
            if (metadata != null) {
                System.out.println("Adding Taint metadata to string '" + taintAware.toString() + "': " + metadata);
                taintAware.setTaint(new GdprTaintMetadata(sourceId, metadata));
            } else {
                System.out.println("Null metadata, not tainting!");
            }
        }
        return taintAware;
    }

        /**
         * The taint method can be used as a taintHandler for a given taint source
         * @param object The object to be tainted
         * @param sourceId The ID of the taint source function
         * @return The tainted object
         *
         * This snippet of XML can be added to the source:
         *
         * <tainthandler>
         *     <opcode>184</opcode>
         *     <owner>com/sap/fontus/gdpr/broadleaf/BroadleafTaintHandler</owner>
         *     <name>taint</name>
         *     <descriptor>(Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;I)Ljava/lang/Object;</descriptor>
         *     <interface>false</interface>
         * </tainthandler>iali
         *
         */
    public static Object taint(Object object, Object parent, Object[] parameters, int sourceId) {
        if (object instanceof IASTaintAware) {
            return setTaint((IASTaintAware) object, parent, parameters, sourceId);
        }
        return IASTaintHandler.traverseObject(object, taintAware -> setTaint(taintAware, parent, parameters, sourceId));
    }

    public static Object monitorOutgoingTraffic(Object object, Object instance, String sinkFunction, String sinkName) {
        Sink sink = Configuration.getConfiguration().getSinkConfig().getSinkForFqn(sinkFunction);
        List<String> sinkPurposes = sink.getDataProtection().getPurposes();
        List<String> sinkVendors = sink.getDataProtection().getVendors();
        if (object.getClass().getName().equals("com.broadleafcommerce.rest.api.wrapper.CustomerWrapper")) {
            try {
                Method getId = object.getClass().getMethod("getId");
                Method getFirstName = object.getClass().getMethod("getFirstName");
                Method getLastName = object.getClass().getMethod("getLastName");
                Method getEmailAddress = object.getClass().getMethod("getEmailAddress");

                long id = (long) getId.invoke(object);
                UUID firstName = getDataId((IASString) getFirstName.invoke(object));
                UUID lastName = getDataId((IASString) getLastName.invoke(object));
                UUID mailAddresse = getDataId((IASString) getEmailAddress.invoke(object));

                System.out.println("Data with id " + firstName.toString() + " was sent to " + sinkVendors.get(0));
                System.out.println("Data with id " + lastName.toString() + " was sent to " + sinkVendors.get(0));
                System.out.println("Data with id " + mailAddresse.toString() + " was sent to " + sinkVendors.get(0));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (object instanceof List) {
            List<Object> listOfObjects = (List) object;
            for (Object obj : listOfObjects) {
                if (obj.getClass().getName().equals("com.broadleafcommerce.rest.api.wrapper.CustomerAddressWrapper")) {
                    try {
                        Method getId = obj.getClass().getMethod("getId");
                        Method getAddressName = obj.getClass().getMethod("getAddressName");
                        Method getAddressWrapper = obj.getClass().getMethod("getAddress");

                        long id = (long) getId.invoke(obj);
                        UUID addressName = getDataId((IASString) getAddressName.invoke(obj));
                        Object addressWrapper = getAddressWrapper.invoke(obj);

                        System.out.println("Data with id " + addressName.toString() + " was sent to " + sinkVendors.get(0));

                        if (addressWrapper.getClass().getName().equals("com.broadleafcommerce.rest.api.wrapper.AddressWrapper")) {
                            Method getAddressId = addressWrapper.getClass().getMethod("getId");
                            Method getFirstName = addressWrapper.getClass().getMethod("getFirstName");
                            Method getLastName = addressWrapper.getClass().getMethod("getLastName");
                            Method getAddressLine1 = addressWrapper.getClass().getMethod("getAddressLine1");
                            Method getAddressLine2 = addressWrapper.getClass().getMethod("getAddressLine2");
                            Method getAddressLine3 = addressWrapper.getClass().getMethod("getAddressLine3");
                            Method getCity = addressWrapper.getClass().getMethod("getCity");
                            Method getIsoCountrySubdivision = addressWrapper.getClass().getMethod("getIsoCountrySubdivision");
                            Method getStateProvinceRegion = addressWrapper.getClass().getMethod("getStateProvinceRegion");
                            Method getPostalCode = addressWrapper.getClass().getMethod("getPostalCode");
                            Method getCompanyName = addressWrapper.getClass().getMethod("getCompanyName");

                            long addressId = (long) getAddressId.invoke(addressWrapper);
                            UUID firstName = getDataId((IASString) getFirstName.invoke(addressWrapper));
                            UUID lastName = getDataId((IASString) getLastName.invoke(addressWrapper));
                            UUID addressLine1 = getDataId((IASString) getAddressLine1.invoke(addressWrapper));
                            UUID addressLine2 = getDataId((IASString) getAddressLine2.invoke(addressWrapper));
                            UUID addressLine3 = getDataId((IASString) getAddressLine3.invoke(addressWrapper));
                            UUID city = getDataId((IASString) getCity.invoke(addressWrapper));
                            UUID postalCode = getDataId((IASString) getPostalCode.invoke(addressWrapper));
                            UUID companyName = getDataId((IASString) getCompanyName.invoke(addressWrapper));
                            UUID isoCountrySubdivision = getDataId((IASString) getIsoCountrySubdivision.invoke(addressWrapper));
                            UUID stateProvinceRegion = getDataId((IASString) getStateProvinceRegion.invoke(addressWrapper));

                            System.out.println("Data with id " + firstName.toString() + " was sent to " + sinkVendors.get(0));
                            System.out.println("Data with id " + lastName.toString() + " was sent to " + sinkVendors.get(0));
                            System.out.println("Data with id " + addressLine1.toString() + " was sent to " + sinkVendors.get(0));
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        return object;
    }

    public static Object checkCustomer(Object object, Object instance, String sinkFunction, String sinkName) {
        // object is the customer
        // instance is the CustomerServiceImpl
        if (object.getClass().getName().equals("org.broadleafcommerce.profile.core.domain.CustomerImpl")) {
            try {
                Sink sink = Configuration.getConfiguration().getSinkConfig().getSinkForFqn(sinkFunction);
                List<String> sinkPurposes = sink.getDataProtection().getPurposes();
                List<String> sinkVendors = sink.getDataProtection().getVendors();

                Method getLastName = object.getClass().getMethod("getLastName");
                IASString lastname = (IASString) getLastName.invoke(object);
                GdprMetadata md = null;

                if (lastname.isTainted() && (lastname.getTaintInformation() != null)) {
                    for (IASTaintRange range : lastname.getTaintInformation().getTaintRanges(lastname.getString().length())) {
                        if (range.getMetadata() instanceof GdprTaintMetadata) {
                            md = ((GdprTaintMetadata) range.getMetadata()).getMetadata();
                            break;
                        }
                    }
                }

                Collection<AllowedPurpose> allowedPurposes = md.getAllowedPurposes();

                for (AllowedPurpose purpose : allowedPurposes) {
                    for (String sinkPurpose : sinkPurposes) {
                        if (purpose.getAllowedPurpose().getName().equalsIgnoreCase(sinkPurpose)) {
                            Set<Vendor> allowedVendors = purpose.getAllowedVendors();
                            for (Vendor vendor : allowedVendors) {
                                for (String sinkVendor : sinkVendors) {
                                    if (vendor.getName().equalsIgnoreCase(sinkVendor)) {
                                        return object;
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex3) {
                ex3.printStackTrace();
            }
            return null;
        } else if (object instanceof IASTaintAware) {
            return handleTaint((IASTaintAware)object, instance, sinkFunction, sinkName);
        } else {
            return traverseObject(object, (taintAware) -> {
                return handleTaint(taintAware, instance, sinkFunction, sinkName);
            });
        }
    }

    public static Object checkAddresses(Object object, Object instance, String sinkFunction, String sinkName) {
        if (object instanceof List) {
            try {
                Method getActiveAddresses = instance.getClass().getMethod("readActiveCustomerAddressesByCustomerId", Long.class);
                long id = 300;
                Object addresses = getActiveAddresses.invoke(instance, id);
                System.out.println(addresses);
                Sink sink = Configuration.getConfiguration().getSinkConfig().getSinkForFqn(sinkFunction);
                sink.getDataProtection().getPurposes();
                sink.getDataProtection().getVendors();
                List<Object> castedAddresses = (List) object;
                List<Object> correctAddresses = new ArrayList<>();
                for (Object o : castedAddresses) {
                    Method getAddress = o.getClass().getMethod("getAddress");
                    Object address = getAddress.invoke(o);
                    Method getAddressLine1 = address.getClass().getMethod("getAddressLine1");
                    IASString addressLine = (IASString) getAddressLine1.invoke(address);

                    System.out.println(o);
                    //if check and than add
                }
                return correctAddresses;
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        } else if (object instanceof IASTaintAware) {
            return handleTaint((IASTaintAware)object, instance, sinkFunction, sinkName);
        } else {
            return traverseObject(object, (taintAware) -> {
                return handleTaint(taintAware, instance, sinkFunction, sinkName);
            });
        }
    }

    private static SimpleAllowedPurpose processProductPurpose(Object product) {
        // Maybe it's possible to read taint of products at checkout and set taint equally for address and so on
        Purpose p = null;
        p = new SimplePurpose(10, "Shopping");
        try {
            Method getUrl = product.getClass().getMethod("getUrl");
            Object url = getUrl.invoke(product);
            System.out.println(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Method getCategory = product.getClass().getMethod("getCategory");
            Object category = getCategory.invoke(product);

            Method getName = category.getClass().getMethod("getName");
            IASString categoryName = (IASString) getName.invoke(category);
            System.out.println(categoryName);
            switch (categoryName.toString()) {
                case "Hot Sauces": p = new SimplePurpose(20, "Groceries");
                    return new SimpleAllowedPurpose(p, Collections.singleton(new SimpleVendor(10, "acyou")));
                case "Merchandise": p = new SimplePurpose(30, "Clothing");
                    return new SimpleAllowedPurpose(p, Collections.singleton(new SimpleVendor(1, "acme")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return new SimpleAllowedPurpose(p, Collections.singleton(new SimpleVendor(20, "Placeholder")));
    }

    private static long getIdFromSecurityContext(Object springSecurityContext) {
        try {
            Method getAuthentication = springSecurityContext.getClass().getMethod("getAuthentication");
            Object authentication = getAuthentication.invoke(springSecurityContext);
            Method getPrincipal = authentication.getClass().getMethod("getPrincipal");
            Method isAuthenticated = authentication.getClass().getMethod("isAuthenticated");
            Object principal = getPrincipal.invoke(authentication);
            isAuthenticated.invoke(authentication);
            Method getId = principal.getClass().getMethod("getId");
            Method getUsername = principal.getClass().getMethod("getUsername");
            Method getAuthorities = principal.getClass().getMethod("getAuthorities");
            long id = (long) getId.invoke(principal);
            String username = ((IASString) getUsername.invoke(principal)).getString();
            getAuthorities.invoke(principal);
            return id;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    private static void getTaintMetadataFromExistingUser(ReflectedHttpServletRequest request, long userId, ProductPurpose productPurpose) {
        GdprMetadata md = null;
        if (userId > -1) {
            try {
                Object obj = request.getAttribute(new IASString("org.springframework.web.servlet.DispatcherServlet.CONTEXT"));
                if (obj != null) {
                    Method m = obj.getClass().getMethod("getBean", IASString.class);

                    Object bean = m.invoke(obj, new IASString("blCustomerService"));
                    Method m2 = bean.getClass().getMethod("readCustomerById", Long.class);

                    Object user = m2.invoke(bean, userId);

                    Method getEmail = user.getClass().getMethod("getEmailAddress");
                    Method setEmail = user.getClass().getMethod("setEmailAddress", IASString.class);

                    Method getLastName = user.getClass().getMethod("getLastName");
                    Method setLastName = user.getClass().getMethod("setLastName", IASString.class);

                    IASString lastname = (IASString) getLastName.invoke(user);

                    IASTaintInformationable infos = lastname.getTaintInformation();
                    IASTaintRanges ranges = lastname.getTaintInformation().getTaintRanges(lastname.length());

                    GdprTaintMetadata currentData = null;
                    for (IASTaintRange range : ranges) {
                        IASTaintMetadata metadata = range.getMetadata();
                        if (metadata instanceof GdprTaintMetadata) {
                            md = ((GdprTaintMetadata) metadata).getMetadata();
                            currentData = (GdprTaintMetadata) metadata;
                            break;
                        }
                    }

                    for (AllowedPurpose p : productPurpose.getPurposes()) {
                        Collection<AllowedPurpose> allowedPurposes = md.getAllowedPurposes();
                        boolean contains = false;
                        for (AllowedPurpose p2 : allowedPurposes) {
                            if (p2.getAllowedPurpose().getName().equalsIgnoreCase(p.getAllowedPurpose().getName())) {
                                contains = true;
                            }
                        }
                        if (!contains) {
                            md.getAllowedPurposes().add(p);
                        }
                    }
                    Class customerInterface =  Class.forName("org.broadleafcommerce.profile.core.domain.Customer", false, Thread.currentThread().getContextClassLoader());
                    Method saveCustomer = bean.getClass().getMethod("saveCustomer", customerInterface);


                    IASString descentLastName = new IASString("descentLastName");
                    setLastName.invoke(user, descentLastName);

                    saveCustomer.invoke(bean, user);


                    IASString newLastName = new IASString(lastname.getString());
                    newLastName.setTaint(new GdprTaintMetadata(currentData.getSource().getId(), md));
                    setLastName.invoke(user, newLastName);

                    saveCustomer.invoke(bean, user);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private static UUID getDataId(IASString iasString) {
        if (iasString.isTainted() && (iasString.getTaintInformation() != null)) {
            for (IASTaintRange range : iasString.getTaintInformation().getTaintRanges(iasString.getString().length())) {
                if (range.getMetadata() instanceof GdprTaintMetadata) {
                    return ((GdprTaintMetadata) range.getMetadata()).getMetadata().getId().getUUID();
                }
            }
            return null;
        } else {
            return null;
        }
    }
}
