package pe.com.unifiedgo.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.openbravo.client.kernel.BaseComponentProvider;
import org.openbravo.client.kernel.Component;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.client.kernel.KernelConstants;

@ApplicationScoped
@ComponentProvider.Qualifier("SCR_MODULE_PROVIDER")
public class SCRComponentProvider extends BaseComponentProvider {

  public Component getComponent(String componentId, Map<String, Object> parameters) {
    throw new IllegalArgumentException("Component id " + componentId + " not supported.");
  }

  public List<ComponentResource> getGlobalComponentResources() {
    final List<ComponentResource> globalResources = new ArrayList<ComponentResource>();
    globalResources.add(createStaticResource(
        "web/pe.com.unifiedgo.core/js/execCheckInvToDiscountComission.js", true));
    globalResources.add(createStaticResource(
        "web/pe.com.unifiedgo.core/js/execUncheckInvToDiscountComission.js", true));
    globalResources.add(createStaticResource(
        "web/pe.com.unifiedgo.core/js/execCheckInvToRefundComission.js", true));
    globalResources.add(createStaticResource(
        "web/pe.com.unifiedgo.core/js/execUncheckInvToRefundComission.js", true));

    globalResources
        .add(createStaticResource("web/pe.com.unifiedgo.core/js/bpinfo-toolbar-button.js", false));
    globalResources.add(createStyleSheetResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_VERSION_PARAMETER + "/pe.com.unifiedgo.core/bpinfo-styles.css",
        false));

    globalResources.add(
        createStaticResource("web/pe.com.unifiedgo.core/js/clntinfo-toolbar-button.js", false));
    globalResources.add(createStyleSheetResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_VERSION_PARAMETER + "/pe.com.unifiedgo.core/clntinfo-styles.css",
        false));

    globalResources.add(
        createStaticResource("web/pe.com.unifiedgo.core/js/provinfo-toolbar-button.js", false));
    globalResources.add(createStyleSheetResource(
        "web/org.openbravo.userinterface.smartclient/openbravo/skins/"
            + KernelConstants.SKIN_VERSION_PARAMETER + "/pe.com.unifiedgo.core/provinfo-styles.css",
        false));

    return globalResources;
  }

}