package eu.arrowhead.core.translator.services.fiware.common;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FiwareEntity {

    //=================================================================================================
    // members
    private String id;
    private String type;
    private final Map<String, Object> other = new HashMap<>();

    //=================================================================================================
    // methods
    //-------------------------------------------------------------------------------------------------
    public String getId() {
        return id;
    }

    //-------------------------------------------------------------------------------------------------
    public String getType() {
        return type;
    }

    //-------------------------------------------------------------------------------------------------
    public Object getProperty(String key) {
        return other.get(key);
    }

    //-------------------------------------------------------------------------------------------------
    @JsonAnyGetter
    public Map<String, Object> any() {
        return other;
    }

    //-------------------------------------------------------------------------------------------------
    @JsonAnySetter
    public void set(String key, Object value) {
        other.put(key, value);
    }

    //-------------------------------------------------------------------------------------------------
    @Override
    public boolean equals(Object o) {
        if (o == null || o.getClass() != FiwareEntity.class) {
            return false;
        }
        if (o == this) {
            return true;
        }
        FiwareEntity entity = (FiwareEntity) o;
        if (!id.equals(entity.id) || !type.equals(entity.type)) {
            return false;
        }
        if (other.size() != entity.other.size()) {
            return false;
        }
        if (!other.keySet().stream().noneMatch((key) -> (!other.get(key).equals(entity.other.get(key))))) {
            return false;
        }
        return true;
    }

    //-------------------------------------------------------------------------------------------------
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + Objects.hashCode(this.id);
        hash = 79 * hash + Objects.hashCode(this.type);
        hash = 79 * hash + Objects.hashCode(this.other);
        return hash;
    }
}
