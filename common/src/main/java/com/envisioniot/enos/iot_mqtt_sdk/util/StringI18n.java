package com.envisioniot.enos.iot_mqtt_sdk.util;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * tsl string internationalization
 *
 * @author hongjie.shan
 * @date 2018/10/17
 */
public class StringI18n implements Serializable {

    private static final long serialVersionUID = -8668593236025646158L;

    private String defaultValue = null;

    private Map<String, String> i18nValue = new HashMap<>();

    public StringI18n() {
    }

    public StringI18n(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * get localized value by locale
     *
     * @param locale a string representation of LOCALE.
     *               You can use Locale.XXXXX.toString() to get string representation of the locale,
     *               such as Locale.US.toString(), Locale.SIMPLIFIED_CHINESE.toString(), etc
     * @return
     */
    public String getLocalizedValue(String locale) {
        return i18nValue.get(locale);
    }

    /**
     * put localized value
     *
     * @param locale         a string representation of LOCALE.
     *                       You can use Locale.XXXXX.toString() to get string representation of the locale,
     *                       such as Locale.US.toString(), Locale.SIMPLIFIED_CHINESE.toString(), etc
     * @param localizedValue
     * @return
     */
    public void setLocalizedValue(String locale, String localizedValue) {
        this.i18nValue.put(locale, localizedValue);
    }

    public Map<String, String> getI18nValue() {
        return i18nValue;
    }

    public void setI18nValue(Map<String, String> i18nValue) {
        this.i18nValue = i18nValue;
    }

    @Override
    public String toString() {
        return "StringI18n [defaultValue=" + defaultValue + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
        result = prime * result + ((i18nValue == null) ? 0 : i18nValue.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        StringI18n other = (StringI18n) obj;
        if (defaultValue == null) {
            if (other.defaultValue != null) {
                return false;
            }
        } else if (!defaultValue.equals(other.defaultValue)) {
            return false;
        }
        if (i18nValue == null) {
            if (other.i18nValue != null) {
                return false;
            }
        } else if (!i18nValue.equals(other.i18nValue)) {
            return false;
        }
        return true;
    }
}
