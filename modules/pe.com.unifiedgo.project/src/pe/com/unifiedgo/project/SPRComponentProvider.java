package pe.com.unifiedgo.project;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.openbravo.client.kernel.BaseComponentProvider;
import org.openbravo.client.kernel.Component;
import org.openbravo.client.kernel.ComponentProvider;

@ApplicationScoped
@ComponentProvider.Qualifier("SPR_MODULE_PROVIDER")
public class SPRComponentProvider extends BaseComponentProvider {

  public Component getComponent(String componentId, Map<String, Object> parameters) {
    throw new IllegalArgumentException("Component id " + componentId + " not supported.");
  }

  public List<ComponentResource> getGlobalComponentResources() {
    final List<ComponentResource> globalResources = new ArrayList<ComponentResource>();
    globalResources.add(createStaticResource(
        "web/pe.com.unifiedgo.project/js/execBDGLINESGenerateRequisition.js", true));
    globalResources
        .add(createStaticResource("web/pe.com.unifiedgo.project/js/ob-requisition.js", true));
    return globalResources;
  }

}