package pe.com.unifiedgo.accounting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.openbravo.client.kernel.BaseComponentProvider;
import org.openbravo.client.kernel.Component;
import org.openbravo.client.kernel.ComponentProvider;

@ApplicationScoped
@ComponentProvider.Qualifier("SCO_MODULE_PROVIDER")
public class SCOComponentProvider extends BaseComponentProvider {

  public Component getComponent(String componentId, Map<String, Object> parameters) {
    throw new IllegalArgumentException("Component id " + componentId + " not supported.");
  }

  public List<ComponentResource> getGlobalComponentResources() {
    final List<ComponentResource> globalResources = new ArrayList<ComponentResource>();
    globalResources.add(createStaticResource(
        "web/pe.com.unifiedgo.accounting/js/execGenerateGLButton.js", true));

    globalResources.add(createStaticResource(
        "web/pe.com.unifiedgo.accounting/js/execSelectPayment.js", true));
    globalResources.add(createStaticResource(
        "web/pe.com.unifiedgo.accounting/js/execGenerateSunatReceipts.js", true));
    globalResources.add(createStaticResource(
        "web/pe.com.unifiedgo.accounting/js/execExchangeForBOE.js", true));

    return globalResources;
  }

}