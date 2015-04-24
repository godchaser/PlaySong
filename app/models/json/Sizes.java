
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
import org.apache.commons.lang3.builder.ToStringBuilder;

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

}
