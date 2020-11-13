package eu.arrowhead.core.translator.services.fiware.common;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FiwareEntity {

    //=================================================================================================
    // members
    private String id;
    private String type;
    private final Map<String, Object> other = new HashMap<>();

    @JsonCreator
    public FiwareEntity(
        @JsonProperty("id") String id,
        @JsonProperty("type") String type) {
        this.id = id; 
        this.type = type;
    }

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
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != FiwareEntity.class) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        FiwareEntity entity = (FiwareEntity) obj;
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
