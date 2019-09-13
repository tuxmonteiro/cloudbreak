package com.sequenceiq.cloudbreak.service.image;

import static org.apache.commons.lang3.StringUtils.substringAfterLast;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.cloud.model.ClouderaManagerProduct;

@Component
public class PreWarmParcelParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(PreWarmParcelParser.class);

    public Optional<ClouderaManagerProduct> parseProductFromParcel(List<String> parcel) {
        Optional<String> parcelUrl = parcel.stream().filter(parcelPart -> parcelPart.endsWith("/parcel") || parcelPart.endsWith("/parcels"))
                .findFirst();
        Optional<String> parcelVersion = parcel.stream().filter(parcelPart -> parcelPart.endsWith(".parcel"))
                .findFirst();
        if (parcelUrl.isEmpty() || parcelVersion.isEmpty()) {
            LOGGER.warn("Parcel URL or version could not be found in the image metadata. "
                    + "Parcel url: '{}', Parcel name: '{}'.", parcelUrl.orElse("null"), parcelVersion.orElse("null"));
            return Optional.empty();
        } else {
            ClouderaManagerProduct product = new ClouderaManagerProduct();
            String name = StringUtils.substringBefore(substringAfterLast(parcelUrl.get(), ".com/"), "/");
            LOGGER.info("The parsed product name for parcel is: '{}'. Parcel URL is: '{}'", name, parcelUrl.get());
            product.setName(name);
            product.setVersion(parcelVersion.get());
            product.setParcel(parcelUrl.get());
            return Optional.of(product);
        }
    }
}
