package com.sap.fontus.gdpr.broadleaf;

import com.sap.fontus.asm.FunctionCall;
import com.sap.fontus.config.Configuration;
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
import com.sap.fontus.taintaware.shared.IASBasicMetadata;
import com.sap.fontus.taintaware.shared.IASTaintSource;
import com.sap.fontus.taintaware.shared.IASTaintSourceRegistry;
import com.sap.fontus.taintaware.unified.IASString;
import com.sap.fontus.taintaware.unified.IASTaintHandler;
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
            try {

                ReflectedHttpServletRequest request = new ReflectedHttpServletRequest(parent);
                IASString uri = request.getRequestURI();
                String path = uri.getString();
                Collection<AllowedPurpose> purposeses = Utils.getPurposesFromRequest(request);
                System.out.println(purposeses);

                ReflectedSession session = request.getSession();
                Object springSecurityContext = session.getAttribute(new IASString("SPRING_SECURITY_CONTEXT"));

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
                        if ((o != null) && (o instanceof ProductPurpose)) {
                            productPurpose = (ProductPurpose) o;
                            System.out.println(productPurpose);
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

    public static Object checkCustomer(Object object, Object instance, String sinkFunction, String sinkName) {
        // object is the customer
        // instance is the CustomerServiceImpl
        if (object.getClass().getName().equals("org.broadleafcommerce.profile.core.domain.CustomerImpl")) {
            try {
                Method getAddresses = object.getClass().getMethod("getCustomerAddresses");
                Object addresses = getAddresses.invoke(object);
                System.out.println(addresses);
                if (addresses instanceof List) {
                    List<Object> castedAddresses = (List) addresses;
                    for (Object o : castedAddresses) {
                        System.out.println(o);
                    }
                }
                return null;
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

    public static Object checkAddresses(Object object, Object instance, String sinkFunction, String sinkName) {
        if (object.getClass().getName().equals("org.broadleafcommerce.profile.core.domain.CustomerImpl")) {
            try {
                Method getAddresses = object.getClass().getMethod("getCustomerAddresses");
                Object addresses = getAddresses.invoke(object);
                System.out.println(addresses);
                if (addresses instanceof List) {
                    List<Object> castedAddresses = (List) addresses;
                    for (Object o : castedAddresses) {
                        System.out.println(o);
                    }
                }
                return null;
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
}
