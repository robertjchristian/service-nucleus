package com.liaison.framework.dynamic;

import com.liaison.framework.util.ServiceUtils;

/**
 * Dynamic Services Web Page Builder
 * <p/>
 * <P>Quick and dirty templating for the dynamic services landing page
 *
 * @author Robert.Christian
 * @version 1.0
 */
public class DynamicServicesWebPageBuilder {

    /**
     * Builds a landing page for Dynamic Services
     *
     * @return String representing an HTML response
     */
    public static String buildHTMLPageFromBindings(String rawBindingConfiguration, DynamicBindings dynamicBindings) {

        // read landing page template
        StringBuilder template = new StringBuilder(ServiceUtils.readFileFromClassPath("/dyn/services-landing.template.html"));

        // swap raw pretty config token
        ServiceUtils.replaceAll(template, "{{rawConfigJSON}}", ServiceUtils.prettifyJSON(rawBindingConfiguration));

        // build and swap parsed services section
        StringBuilder serviceDetail = new StringBuilder();
        for (DynamicBinding db : dynamicBindings.bindings) {
            serviceDetail.append(bindingToHTML((db)));
        }

        ServiceUtils.replaceAll(template, "{{parsedConfiguration}}", serviceDetail.toString());

        return template.toString();

    }

    private static String bindingToHTML(DynamicBinding db) {

        String parsed = ServiceUtils.readFileFromClassPath("/dyn/services-detail.template.html");
        parsed = parsed.replace("{{binding-title}}", db.serviceName);
        parsed = parsed.replace("{{written-by}}", db.about.author);
        parsed = parsed.replace("{{description}}", db.about.description);
        parsed = parsed.replace("{{base-uri}}", db.baseURI);
        parsed = parsed.replace("{{script-location}}", db.scriptLocation);

        StringBuilder operationSB = new StringBuilder();
        for (Operation o : db.operations) {
            String operationTemplate = ServiceUtils.readFileFromClassPath("/dyn/operations-detail.template.html");
            operationTemplate = operationTemplate.replace("{{operation-url}}", o.operationUrl);
            String allowedMethods = ServiceUtils.formatArrayAsString(o.allowedMethods, " | ");
            operationTemplate = operationTemplate.replace("{{allowed-http-methods}}", allowedMethods);
            operationSB.append(operationTemplate);
        }

        parsed = parsed.replace("{{operation-rows}}", operationSB.toString());

        return parsed;

    }

}
