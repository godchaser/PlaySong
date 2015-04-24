
package models.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    "songs",
    "fonts",
    "sizes",
    "frontpage"
})
public class JsonSongbookGenerator {

    @JsonProperty("songs")
    private List<Song> songs = new ArrayList<Song>();
    @JsonProperty("fonts")
    private Fonts fonts;
    @JsonProperty("sizes")
    private Sizes sizes;
    @JsonProperty("frontpage")
    private Frontpage frontpage;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The songs
     */
    @JsonProperty("songs")
    public List<Song> getSongs() {
        return songs;
    }

    /**
     * 
     * @param songs
     *     The songs
     */
    @JsonProperty("songs")
    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    /**
     * 
     * @return
     *     The fonts
     */
    @JsonProperty("fonts")
    public Fonts getFonts() {
        return fonts;
    }

    /**
     * 
     * @param fonts
     *     The fonts
     */
    @JsonProperty("fonts")
    public void setFonts(Fonts fonts) {
        this.fonts = fonts;
    }

    /**
     * 
     * @return
     *     The sizes
     */
    @JsonProperty("sizes")
    public Sizes getSizes() {
        return sizes;
    }

    /**
     * 
     * @param sizes
     *     The sizes
     */
    @JsonProperty("sizes")
    public void setSizes(Sizes sizes) {
        this.sizes = sizes;
    }

    /**
     * 
     * @return
     *     The frontpage
     */
    @JsonProperty("frontpage")
    public Frontpage getFrontpage() {
        return frontpage;
    }

    /**
     * 
     * @param frontpage
     *     The frontpage
     */
    @JsonProperty("frontpage")
    public void setFrontpage(Frontpage frontpage) {
        this.frontpage = frontpage;
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
