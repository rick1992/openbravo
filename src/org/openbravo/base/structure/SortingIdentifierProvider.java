package org.openbravo.base.structure;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.provider.OBSingleton;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.model.ad.system.Language;

/**
 * Provides the sorting identifier/title of an object using the
 * {@link Entity#getIdentifierProperties() identifierProperties} of the {@link Entity Entity}.
 * 
 * Note: the getSortingIdentifier can also be generated in the java entity but the current approach
 * makes it possible to change the sorting identifier definition at runtime.
 * 
 * 
 */

public class SortingIdentifierProvider implements OBSingleton {

  public static final String SEPARATOR = " - ";
  private static SortingIdentifierProvider instance;

  public static synchronized SortingIdentifierProvider getInstance() {
    if (instance == null) {
      instance = OBProvider.getInstance().get(SortingIdentifierProvider.class);
    }
    return instance;
  }

  public static synchronized void setInstance(SortingIdentifierProvider instance) {
    SortingIdentifierProvider.instance = instance;
  }

  private SimpleDateFormat dateFormat = null;
  private SimpleDateFormat dateTimeFormat = null;

  /**
   * Returns the sorting identifier of the object. The sorting identifier is computed using the
   * identifier properties of the Entity of the object and the em_scr_ignoreforsorting column. It is
   * translated (if applicable) to the current language
   * 
   * @param o
   *          the object for which the sorting identifier is generated
   * @return the sorting identifier
   */
  public String getSortingIdentifier(Object o) {
    final Language lang = OBContext.getOBContext() != null ? OBContext.getOBContext().getLanguage() : null;
    return getSortingIdentifier(o, true, lang);
  }

  // identifyDeep determines if refered to objects are used
  // to identify the object
  private String getSortingIdentifier(Object o, boolean identifyDeep, Language language) {
    // TODO: add support for null fields
    final StringBuilder sb = new StringBuilder();
    final DynamicEnabled dob = (DynamicEnabled) o;
    final String entityName = ((Identifiable) dob).getEntityName();
    final List<Property> identifiers = ModelProvider.getInstance().getEntity(entityName).getIdentifierProperties();

    for (final Property identifier : identifiers) {
      if (sb.length() > 0) {
        sb.append(SEPARATOR);
      }

      if (identifier.isScrIgnoreforsorting()) {
        continue;
      }

      Property property = ((BaseOBObject) dob).getEntity().getProperty(identifier.getName());
      Object value;

      if (property.hasDisplayColumn()) {
        Property displayColumnProperty = DalUtil.getPropertyFromPath(property.getReferencedProperty().getEntity(), property.getDisplayPropertyName());
        BaseOBObject referencedObject = (BaseOBObject) dob.get(property.getName());
        if (referencedObject == null) {
          continue;
        }
        if (displayColumnProperty.hasDisplayColumn()) {
          // Allowing one level deep of displayed column pointing to references with display column
          value = ((BaseOBObject) dob.get(property.getDisplayPropertyName())).get(displayColumnProperty.getDisplayPropertyName());
        } else if (!displayColumnProperty.isPrimitive()) {
          // Displaying identifier for non primitive properties

          value = ((BaseOBObject) referencedObject.get(property.getDisplayPropertyName())).getSortingIdentifier();
        } else {
          value = ((BaseOBObject) referencedObject).get(property.getDisplayPropertyName(), language);
        }

        // Assign displayColumnProperty to apply formatting if needed
        property = displayColumnProperty;
      } else if (property.isTranslatable()) {
        // Trying to get id of translatable object.
        Object id = dob.get("id");
        if (id instanceof BaseOBObject) {
          // When the object is created for a drop down list filter, it is incorrect: id is not a
          // String but a BaseOBject. This code deals with this exception.

          // TODO: once issue #23706 is fixed, this should not be needed anymore
          id = ((BaseOBObject) id).get("id");
        }

        if (id instanceof String) {
          value = ((BaseOBObject) dob).get(identifier.getName(), language, (String) id);
        } else {
          // give up, couldn't find the id
          value = ((BaseOBObject) dob).get(identifier.getName(), language);
        }

      } else if (!property.isPrimitive() && identifyDeep) {
        if (dob.get(property.getName()) != null) {
          value = ((BaseOBObject) dob.get(property.getName())).getSortingIdentifier();
        } else {
          value = "";
        }
      } else {
        value = dob.get(identifier.getName());
      }

      if (value instanceof Identifiable && identifyDeep) {
        sb.append(getSortingIdentifier(value, false, language));
      } else if (value != null) {

        // TODO: add number formatting...
        if (property.isDate() || property.isDatetime()) {
          value = formatDate(property, (Date) value);
        }

        sb.append(value);
      }
    }
    if (identifiers.size() == 0) {
      return entityName + " (" + ((Identifiable) dob).getId() + ")";
    }
    return sb.toString();
  }

  protected String getSeparator() {
    return SEPARATOR;
  }

  private synchronized String formatDate(Property property, Date date) {
    if (date == null) {
      return "";
    }
    if (dateFormat == null) {
      final String dateFormatString = OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty("dateFormat.java");
      final String dateTimeFormatString = OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty("dateTimeFormat.java");
      dateFormat = new SimpleDateFormat(dateFormatString);
      dateTimeFormat = new SimpleDateFormat(dateTimeFormatString);
    }
    if (property.isDatetime()) {
      return dateTimeFormat.format(date);//
    } else {
      return dateFormat.format(date);
    }
  }
}