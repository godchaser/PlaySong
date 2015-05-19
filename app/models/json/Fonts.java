
package models.json;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "titleFont",
    "lyricsFont"
})
public class Fonts {

    @JsonProperty("titleFont")
    private String titleFont;
    @JsonProperty("lyricsFont")
    private String lyricsFont;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The titleFont
     */
    @JsonProperty("titleFont")
    public String getTitleFont() {
        return titleFont;
    }

    /**
     * 
     * @param titleFont
     *     The titleFont
     */
    @JsonProperty("titleFont")
    public void setTitleFont(String titleFont) {
        this.titleFont = titleFont;
    }

    /**
     * 
     * @return
     *     The lyricsFont
     */
    @JsonProperty("lyricsFont")
    public String getLyricsFont() {
        return lyricsFont;
    }

    /**
     * 
     * @param lyricsFont
     *     The lyricsFont
     */
    @JsonProperty("lyricsFont")
    public void setLyricsFont(String lyricsFont) {
        this.lyricsFont = lyricsFont;
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
        return new HashCodeBuilder().append(titleFont).append(lyricsFont).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Fonts) == false) {
            return false;
        }
        Fonts rhs = ((Fonts) other);
        return new EqualsBuilder().append(titleFont, rhs.titleFont).append(lyricsFont, rhs.lyricsFont).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
