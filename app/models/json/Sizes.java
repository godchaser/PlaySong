
package models.json;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Generated;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "titleSize",
    "lyricsSize"
})
public class Sizes {

    @JsonProperty("titleSize")
    private String titleSize;
    @JsonProperty("lyricsSize")
    private String lyricsSize;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The titleSize
     */
    @JsonProperty("titleSize")
    public String getTitleSize() {
        return titleSize;
    }

    /**
     * 
     * @param titleSize
     *     The titleSize
     */
    @JsonProperty("titleSize")
    public void setTitleSize(String titleSize) {
        this.titleSize = titleSize;
    }

    /**
     * 
     * @return
     *     The lyricsSize
     */
    @JsonProperty("lyricsSize")
    public String getLyricsSize() {
        return lyricsSize;
    }

    /**
     * 
     * @param lyricsSize
     *     The lyricsSize
     */
    @JsonProperty("lyricsSize")
    public void setLyricsSize(String lyricsSize) {
        this.lyricsSize = lyricsSize;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(titleSize).append(lyricsSize).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Sizes) == false) {
            return false;
        }
        Sizes rhs = ((Sizes) other);
        return new EqualsBuilder().append(titleSize, rhs.titleSize).append(lyricsSize, rhs.lyricsSize).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
