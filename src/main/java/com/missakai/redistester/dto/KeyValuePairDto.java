package com.missakai.redistester.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * A key value pair that used to set and retrieve values from Redis
 * @author Missaka Iddamalgoda (@MissakaI)
 */
@ApiModel(value = "KeyValuePair", description = "A key value pair that used to set and retrieve values from Redis")
@Data
public class KeyValuePairDto {

    @ApiModelProperty("The key that is mapped in Redis")
    private String key;

    @ApiModelProperty("A valid JSON that is stored against the given key")
    private JsonNode value;
}
